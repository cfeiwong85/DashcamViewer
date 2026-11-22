import Foundation
import Testing
@testable import T8Kit

/// In-memory connection: bytes the session sends go to `onSend`; `push` delivers camera bytes.
final class PipeConnection: T8Connection, @unchecked Sendable {
    private let lock = NSLock()
    private var inbound: [[UInt8]] = []
    private var waiting: CheckedContinuation<[UInt8], Error>?
    private var closed = false
    var onSend: (@Sendable ([UInt8]) -> Void)?

    func send(_ bytes: [UInt8]) async throws {
        let (isClosed, callback) = lock.withLock { (closed, onSend) }
        if isClosed { throw T8TransportError.closed }
        callback?(bytes)
    }

    func receive(maximumLength: Int) async throws -> [UInt8] {
        try await withCheckedThrowingContinuation { continuation in
            lock.lock()
            if !inbound.isEmpty {
                let next = inbound.removeFirst()
                lock.unlock()
                continuation.resume(returning: next)
            } else if closed {
                lock.unlock()
                continuation.resume(returning: [])
            } else {
                waiting = continuation
                lock.unlock()
            }
        }
    }

    func push(_ bytes: [UInt8]) {
        lock.lock()
        if let waiter = waiting {
            waiting = nil
            lock.unlock()
            waiter.resume(returning: bytes)
        } else {
            inbound.append(bytes)
            lock.unlock()
        }
    }

    func close() {
        lock.lock()
        closed = true
        let waiter = waiting
        waiting = nil
        lock.unlock()
        waiter?.resume(returning: [])
    }
}

/// A scripted camera: `respond` maps each phone packet to camera packets.
final class FakeCamera: T8Connector, @unchecked Sendable {
    let connection = PipeConnection()
    private let lock = NSLock()
    private var parser = T8PacketParser(expectedHead: .phone, legacyShortFrameCommands: [.previewStop])
    private var receivedPackets: [T8Packet] = []
    let respond: @Sendable (T8Packet) -> [T8Packet]

    init(respond: @escaping @Sendable (T8Packet) -> [T8Packet]) {
        self.respond = respond
        connection.onSend = { [weak self] bytes in self?.handle(bytes) }
    }

    var received: [T8Packet] {
        lock.lock()
        defer { lock.unlock() }
        return receivedPackets
    }

    private func handle(_ bytes: [UInt8]) {
        lock.lock()
        let packets = parser.append(bytes).compactMap { event -> T8Packet? in
            if case .packet(let packet, _) = event { packet } else { nil }
        }
        receivedPackets += packets
        lock.unlock()
        for packet in packets {
            for reply in respond(packet) { connection.push(reply.encoded()) }
        }
    }

    func connect(host: String, port: UInt16, timeout: Duration) async throws -> any T8Connection {
        connection
    }

    static func standard(connectResult: Int32 = 0) -> @Sendable (T8Packet) -> [T8Packet] {
        { packet in
            switch packet.command {
            case .netConnectState where packet.int32Value == 1:
                return [.camera(.netConnectStateReply, T8Payload.index(connectResult))]
            case .getConfig:
                let config = T8Config(productName: "logon t8", serial: "1", firmwareVersion: "FH", wifiPassword: "12345678")
                return [.camera(.getConfigReply, config.bytes)]
            case .ipcamStart:
                return [.camera(.ipcamStart, [0, 0, 0, 0])]
            case .fileList:
                let folder = Int(packet.int32Value ?? 0)
                return (0..<3).map { page in
                    .camera(.fileListReply, T8FileListPage(totalPages: 3, currentPage: UInt32(page), entries: [
                        T8FileListEntry(name: "2026112\(folder)_10000\(page)_I.avi", hasRear: page == 0),
                    ]).payload)
                }
            case let command where T8Setting.allCases.contains(where: { $0.command == command }):
                return [.camera(command.plusOne, T8Payload.index(0))]
            default:
                return []
            }
        }
    }
}

@Suite("Control session")
struct SessionTests {
    func makeSession(_ camera: FakeCamera, replyTimeout: Duration = .seconds(2)) -> T8ControlSession {
        var options = T8ControlSession.Options()
        options.replyTimeout = replyTimeout
        options.fileListPageTimeout = replyTimeout
        options.previewOffDelay = .milliseconds(10)
        return T8ControlSession(connector: camera, options: options)
    }

    @Test("Handshake sends NET_CONNECT_STATE(1) and succeeds on result 0")
    func handshake() async throws {
        let camera = FakeCamera(respond: FakeCamera.standard())
        let session = makeSession(camera)
        try await session.connect()
        #expect(await session.isConnected)
        #expect(camera.received.first == .phone(.netConnectState, T8Payload.index(1)))
        await session.disconnect()
        #expect(camera.received.last == .phone(.netConnectState, T8Payload.index(0)))
        #expect(await !session.isConnected)
    }

    @Test("Handshake results map to LogonT's messages", arguments: [
        (Int32(-1), T8SessionError.anotherPhoneConnected),
        (2, .cameraIsPlayingBack),
        (3, .cameraIsShowingPhotos),
        (7, .unexpectedConnectResult(7)),
    ])
    func handshakeErrors(result: Int32, expected: T8SessionError) async {
        let camera = FakeCamera(respond: FakeCamera.standard(connectResult: result))
        let session = makeSession(camera)
        await #expect(throws: expected) { try await session.connect() }
        #expect(await !session.isConnected)
    }

    @Test("No reply times out")
    func timeout() async {
        let camera = FakeCamera(respond: { _ in [] })
        let session = makeSession(camera, replyTimeout: .milliseconds(100))
        await #expect(throws: T8SessionError.timeout(.netConnectState)) { try await session.connect() }
    }

    @Test("Heartbeats are answered with an empty 40887, including zero-length ones")
    func heartbeat() async throws {
        let camera = FakeCamera(respond: FakeCamera.standard())
        let session = makeSession(camera)
        try await session.connect()
        camera.connection.push(T8Packet.camera(.heartbeat).encoded())
        camera.connection.push(T8Packet.camera(.heartbeat, [0, 0, 0, 0]).encoded())
        for _ in 0..<100 where camera.received.filter({ $0.command == .heartbeatReply }).count < 2 {
            try await Task.sleep(for: .milliseconds(10))
        }
        let replies = camera.received.filter { $0.command == .heartbeatReply }
        #expect(replies == [.phone(.heartbeatReply), .phone(.heartbeatReply)])
        #expect(await session.heartbeatCount == 2)
    }

    @Test("Camera stop is answered with 40889 and reported")
    func cameraStop() async throws {
        let camera = FakeCamera(respond: FakeCamera.standard())
        let session = makeSession(camera)
        try await session.connect()
        let events = session.events
        camera.connection.push(T8Packet.camera(.cameraStop, [0, 0, 0, 0]).encoded())
        var sawStop = false
        for await event in events {
            if case .cameraStopped = event {
                sawStop = true
                break
            }
        }
        #expect(sawStop)
        #expect(camera.received.contains(.phone(.cameraStopReply)))
    }

    @Test("Live view start follows LogonT's order and returns the config")
    func preview() async throws {
        let camera = FakeCamera(respond: FakeCamera.standard())
        let session = makeSession(camera)
        try await session.connect()
        let config = try await session.startPreview()
        #expect(config.isT8)
        try await session.stopPreview()
        let commands = camera.received.map(\.command)
        #expect(commands == [.netConnectState, .recordOnOff, .getConfig, .ipcamStart, .freeviewStart, .recordOnOff, .previewStop])
        #expect(camera.received[1].int32Value == 1)
        #expect(camera.received[5].int32Value == 0)
        #expect(camera.received[6].payload.isEmpty)
    }

    @Test("A three-page list arriving in one burst is assembled in order")
    func fileList() async throws {
        let camera = FakeCamera(respond: FakeCamera.standard())
        let session = makeSession(camera)
        try await session.connect()
        let list = try await session.listRecordings(in: .parking)
        #expect(list.isComplete)
        #expect(list.recordings.map(\.index) == [0, 1, 2])
        #expect(list.recordings.map(\.name) == ["20261122_100000_I.avi", "20261122_100001_I.avi", "20261122_100002_I.avi"])
        #expect(list.recordings.map(\.hasRear) == [true, false, false])
        let sent = camera.received.map(\.command)
        #expect(Array(sent.suffix(2)) == [.recordOnOff, .fileList])
        #expect(camera.received.last?.int32Value == 2)
    }

    @Test("Settings send the option index and accept command + 1")
    func settings() async throws {
        let camera = FakeCamera(respond: FakeCamera.standard())
        let session = makeSession(camera)
        try await session.connect()
        _ = try await session.readConfig()
        try await session.setSetting(.whiteBalance, index: 3)
        #expect(camera.received.last == .phone(.setWhiteBalance, T8Payload.index(3)))
        #expect(await session.lastConfig?[.whiteBalance] == 3)
        await #expect(throws: T8SessionError.rejected(.setPartition, -1)) {
            try await session.setSetting(.sdPartition, index: 0)
        }
    }

    @Test("Format and factory reset are refused before they reach the socket")
    func destructive() async throws {
        let camera = FakeCamera(respond: FakeCamera.standard())
        let session = makeSession(camera)
        try await session.connect()
        await #expect(throws: T8SessionError.rejected(.formatSDCard, -1)) {
            try await session.send(.phone(.formatSDCard, T8Payload.index(0)))
        }
        #expect(!camera.received.contains { $0.command == .formatSDCard })
    }

    @Test("Losing the connection fails pending requests")
    func disconnect() async throws {
        let camera = FakeCamera(respond: FakeCamera.standard())
        let session = makeSession(camera, replyTimeout: .seconds(5))
        try await session.connect()
        let pending = Task { try await session.takeSnapshot() }
        try await Task.sleep(for: .milliseconds(50))
        camera.connection.close()
        await #expect(throws: T8SessionError.self) { try await pending.value }
        #expect(await !session.isConnected)
    }
}
