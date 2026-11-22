import Foundation
import Testing
import T8Kit
@testable import T8SimulatorKit

/// T8Kit against the simulator over real loopback TCP sockets.
@Suite("End to end", .serialized)
struct EndToEndTests {
    static func options(_ configure: (inout T8SimulatorOptions) -> Void = { _ in }) -> T8SimulatorOptions {
        var options = T8SimulatorOptions()
        options.host = "127.0.0.1"
        options.basePort = 0
        options.heartbeatInterval = nil
        options.clipSeconds = 1
        options.frontSize = (width: 64, height: 48)
        options.rearSize = (width: 48, height: 48)
        configure(&options)
        return options
    }

    static func withSimulator<T>(
        _ options: T8SimulatorOptions = EndToEndTests.options(),
        _ body: (T8Simulator, T8Address) async throws -> T
    ) async throws -> T {
        let simulator = T8Simulator(options: options)
        try await simulator.start()
        let address = await simulator.address
        do {
            let result = try await body(simulator, address)
            await simulator.stop()
            return result
        } catch {
            await simulator.stop()
            throw error
        }
    }

    static func session(_ address: T8Address, replyTimeout: Duration = .seconds(3)) -> T8ControlSession {
        var options = T8ControlSession.Options(address: address)
        options.replyTimeout = replyTimeout
        options.fileListPageTimeout = replyTimeout
        options.previewOffDelay = .milliseconds(20)
        return T8ControlSession(connector: POSIXConnector(), options: options)
    }

    static func media(_ address: T8Address) -> T8MediaClient {
        var client = T8MediaClient(connector: POSIXConnector(), address: address)
        client.transferConnectDelay = .milliseconds(50)
        client.dataOptions.idleTimeout = .seconds(5)
        return client
    }

    /// Waits for the first event that matches, or fails after `timeout`.
    static func waitForEvent(
        _ session: T8ControlSession,
        timeout: Duration = .seconds(5),
        where match: @escaping @Sendable (T8SessionEvent) -> Bool
    ) async -> Bool {
        let events = session.events
        return await withTaskGroup(of: Bool.self) { group in
            group.addTask {
                for await event in events where match(event) { return true }
                return false
            }
            group.addTask {
                try? await Task.sleep(for: timeout)
                return false
            }
            let first = await group.next() ?? false
            group.cancelAll()
            return first
        }
    }

    static func waitUntil(timeout: Duration = .seconds(5), _ condition: () async -> Bool) async -> Bool {
        let deadline = ContinuousClock.now + timeout
        while ContinuousClock.now < deadline {
            if await condition() { return true }
            try? await Task.sleep(for: .milliseconds(20))
        }
        return await condition()
    }

    @Test("Live view: frames decode as H.264, the camera switch changes the stream, stop and disconnect are clean")
    func liveView() async throws {
        try await Self.withSimulator { simulator, address in
            let session = Self.session(address)
            try await session.connect()
            let config = try await session.startPreview()
            #expect(config.productName == "logon t8")
            #expect(config.qualityVariant == .fh)
            try await Task.sleep(for: .milliseconds(100))

            var sizes: [String] = []
            var keyframes = 0
            var count = 0
            for try await au in Self.media(address).accessUnits() {
                count += 1
                if au.isKeyframe { keyframes += 1 }
                if let sps = au.sps, let info = H264SPSInfo(sps: sps) {
                    let size = "\(info.width)x\(info.height)"
                    if sizes.last != size { sizes.append(size) }
                }
                if count == 10 { try await session.switchCamera() }
                if count == 40 { break }
            }
            #expect(count == 40)
            #expect(keyframes == 40)
            #expect(sizes.first == "64x48")
            #expect(sizes.contains("48x48"))

            try await session.stopPreview()
            await session.disconnect()
            // The last frames may still be in flight when disconnect() returns.
            #expect(await Self.waitUntil {
                let stats = await simulator.stats
                return stats.count(.previewStop) == 1 && stats.count(.netConnectState) == 2
            })
            let stats = await simulator.stats
            #expect(stats.count(.recordOnOff) == 2)
            #expect(stats.count(.ipcamStart) == 1)
            #expect(stats.count(.freeviewStart) == 1)
            #expect(stats.count(.previewStop) == 1)
            #expect(stats.count(.changeCamera) == 1)
            #expect(stats.destructiveCommandsReceived == 0)
        }
    }

    @Test("File lists page through all recordings with LogonT's numbering")
    func fileLists() async throws {
        try await Self.withSimulator { _, address in
            let session = Self.session(address)
            try await session.connect()
            let driving = try await session.listRecordings(in: .driving)
            #expect(driving.isComplete)
            #expect(driving.pagesReceived == 3)
            #expect(driving.recordings.count == 25)
            #expect(driving.recordings.map(\.index) == Array(0..<25))
            #expect(driving.recordings.allSatisfy { $0.name.count == 21 && $0.name.dropFirst(16).first == "I" })
            let photos = try await session.listRecordings(in: .photo)
            #expect(photos.recordings.count == 3)
            #expect(photos.recordings.allSatisfy { !$0.hasRear })
            await session.disconnect()
        }
    }

    @Test("Downloading front and rear writes H.264 and PCM of the expected length")
    func download() async throws {
        try await Self.withSimulator { _, address in
            let session = Self.session(address)
            try await session.connect()
            let list = try await session.listRecordings(in: .event)
            let recording = try #require(list.recordings.first { $0.hasRear })
            let directory = FileManager.default.temporaryDirectory.appendingPathComponent("t8e2e-\(UUID().uuidString)")
            defer { try? FileManager.default.removeItem(at: directory) }
            for channel in T8Channel.allCases {
                let file = try await Self.media(address).download(recording, channel: channel, session: session, to: directory)
                #expect(file.fileStart?.frameCount == Int32(channel.framesPerSecond))
                #expect(file.videoByteCount == Int(file.fileStart?.fileSize ?? -1))
                #expect(file.audioByteCount == T8PCM.sampleRate * T8PCM.bytesPerSample)
                #expect(file.videoURL.lastPathComponent == recording.fileStem(for: channel) + ".h264")

                let bytes = try [UInt8](Data(contentsOf: file.videoURL))
                var parser = AnnexBParser()
                var assembler = H264AccessUnitAssembler()
                var aus = assembler.append(parser.append(bytes))
                aus += assembler.append(parser.finish()) + assembler.finish()
                #expect(aus.count == channel.framesPerSecond)
                let info = try #require(aus.first?.sps.flatMap(H264SPSInfo.init(sps:)))
                #expect(info.width == (channel == .front ? 64 : 48))
                #expect(info.profile == 66)
            }
            await session.disconnect()
        }
    }

    @Test("Photo download returns the JPEG and opens no audio port")
    func photo() async throws {
        try await Self.withSimulator { simulator, address in
            let session = Self.session(address)
            try await session.connect()
            try await session.takeSnapshot()
            let photos = try await session.listRecordings(in: .photo)
            #expect(photos.recordings.count == 4)
            let directory = FileManager.default.temporaryDirectory.appendingPathComponent("t8e2e-\(UUID().uuidString)")
            defer { try? FileManager.default.removeItem(at: directory) }
            let file = try await Self.media(address).download(photos.recordings[0], channel: .front, session: session, to: directory)
            let bytes = try [UInt8](Data(contentsOf: file.videoURL))
            #expect(bytes.prefix(2) == [0xFF, 0xD8])
            #expect(file.audioURL == nil)
            #expect(await simulator.stats.audioConnections == 0)
            await session.disconnect()
        }
    }

    @Test("Settings, voice recording and time reach the camera's config")
    func settings() async throws {
        try await Self.withSimulator { simulator, address in
            let session = Self.session(address)
            try await session.connect()
            try await session.setSetting(.whiteBalance, index: 2)
            try await session.setVoiceRecording(false)
            try await session.setTime(T8DateTime(year: 2026, month: 11, day: 22, hour: 9, minute: 8, second: 7), requireReply: true)
            let config = try await session.readConfig()
            #expect(config[.whiteBalance] == 2)
            #expect(!config.isVoiceRecordingOn)
            #expect(await simulator.stats.lastTime == T8DateTime(year: 2026, month: 11, day: 22, hour: 9, minute: 8, second: 7))
            await session.disconnect()
        }
    }

    @Test("A second phone is told another phone is connected")
    func secondPhone() async throws {
        try await Self.withSimulator { simulator, address in
            let first = Self.session(address)
            try await first.connect()
            let second = Self.session(address)
            await #expect(throws: T8SessionError.anotherPhoneConnected) { try await second.connect() }
            #expect(await simulator.stats.rejectedConnections == 1)
            await first.disconnect()
            let third = Self.session(address)
            #expect(await Self.waitUntil {
                (try? await third.connect()) != nil
            })
            await third.disconnect()
        }
    }

    @Test("Fault: already connected")
    func faultAlreadyConnected() async throws {
        try await Self.withSimulator(Self.options { $0.faults.alreadyConnected = true }) { _, address in
            await #expect(throws: T8SessionError.anotherPhoneConnected) { try await Self.session(address).connect() }
        }
    }

    @Test("Fault: dropped connection fails pending requests and reports the disconnect")
    func faultDrop() async throws {
        try await Self.withSimulator(Self.options { $0.faults.dropControlAfter = .milliseconds(300) }) { _, address in
            let session = Self.session(address, replyTimeout: .seconds(10))
            try await session.connect()
            let gone = Task { await Self.waitForEvent(session) { if case .disconnected = $0 { true } else { false } } }
            #expect(await gone.value)
            #expect(await !session.isConnected)
            await #expect(throws: T8SessionError.notConnected) { try await session.readConfig() }
        }
    }

    @Test("Fault: drop after N packets")
    func faultDropPackets() async throws {
        try await Self.withSimulator(Self.options { $0.faults.dropControlAfterPackets = 3 }) { _, address in
            let session = Self.session(address, replyTimeout: .seconds(2))
            try await session.connect()
            _ = try await session.readConfig()
            await #expect(throws: T8SessionError.self) { try await session.readConfig() }
        }
    }

    @Test("Fault: slow link still delivers a complete download")
    func faultSlow() async throws {
        try await Self.withSimulator(Self.options {
            $0.faults.bytesPerSecond = 40_000
            $0.faults.replyLatency = .milliseconds(100)
        }) { _, address in
            let session = Self.session(address)
            try await session.connect()
            let recording = try #require(try await session.listRecordings(in: .parking).recordings.first)
            let directory = FileManager.default.temporaryDirectory.appendingPathComponent("t8e2e-\(UUID().uuidString)")
            defer { try? FileManager.default.removeItem(at: directory) }
            let start = ContinuousClock.now
            let file = try await Self.media(address).download(recording, channel: .rear, session: session, to: directory)
            let elapsed = ContinuousClock.now - start
            #expect(file.videoByteCount == Int(file.fileStart?.fileSize ?? -1))
            #expect(file.audioByteCount == 16_000)
            #expect(elapsed > .milliseconds(Int64(file.videoByteCount * 1000 / 40_000) / 2))
            await session.disconnect()
        }
    }

    @Test("A data port that stalls fails with idleTimeout instead of hanging")
    func idleTimeout() async throws {
        try await Self.withSimulator(Self.options { $0.faults.stallDataAfterBytes = 1000 }) { _, address in
            let session = Self.session(address)
            try await session.connect()
            let recording = try #require(try await session.listRecordings(in: .event).recordings.first)
            _ = try await session.startFileTransfer(recording, channel: .front)
            var media = Self.media(address)
            media.dataOptions.idleTimeout = .milliseconds(300)
            var received = 0
            await #expect(throws: T8DataStreamError.idleTimeout(port: address.videoPort, seconds: 0)) {
                for try await chunk in media.videoStream() { received += chunk.count }
            }
            #expect(received == 1000)
            await session.disconnect()
        }
    }

    @Test("Heartbeats are answered, with and without a payload", arguments: [[UInt8]([0, 0, 0, 0]), []])
    func heartbeats(payload: [UInt8]) async throws {
        try await Self.withSimulator(Self.options {
            $0.heartbeatInterval = .milliseconds(100)
            $0.heartbeatPayload = payload
        }) { simulator, address in
            let session = Self.session(address)
            try await session.connect()
            #expect(await Self.waitUntil { await simulator.stats.heartbeatRepliesReceived >= 3 })
            #expect(await session.heartbeatCount >= 3)
            await session.disconnect()
        }
    }

    @Test("Fault: camera stop is answered and reported")
    func faultCameraStop() async throws {
        try await Self.withSimulator(Self.options { $0.faults.cameraStopAfter = .milliseconds(200) }) { simulator, address in
            let session = Self.session(address)
            try await session.connect()
            let stopped = Task { await Self.waitForEvent(session) { if case .cameraStopped = $0 { true } else { false } } }
            #expect(await stopped.value)
            #expect(await Self.waitUntil { await simulator.stats.count(.cameraStopReply) == 1 })
            await session.disconnect()
        }
    }

    @Test("Wi-Fi password change is acknowledged, then the camera drops the phone")
    func wifiPassword() async throws {
        try await Self.withSimulator { simulator, address in
            let session = Self.session(address)
            try await session.connect()
            #expect(try await session.readConfig().wifiPassword == "12345678")
            let gone = Task { await Self.waitForEvent(session) { if case .disconnected = $0 { true } else { false } } }
            try await session.setWiFiPassword("newpass9")
            #expect(await simulator.stats.wifiPassword == "newpass9")
            #expect(await simulator.config.wifiPassword == "newpass9")
            #expect(await gone.value)
            // The shared log hides both passwords; parsing above still saw the real one.
            let log = session.log.exportText()
            #expect(!log.contains(T8Bytes.hex(Array("12345678".utf8))))
            #expect(!log.contains(T8Bytes.hex(Array("newpass9".utf8))))
            #expect(log.components(separatedBy: "Wi-Fi password hidden").count - 1 >= 2)
        }
    }

    @Test("The simulator's test pattern parses with T8Kit's SPS reader")
    func encoderSPS() throws {
        var encoder = TestPatternEncoder(width: 256, height: 144, channel: .front)
        var parser = AnnexBParser()
        let units = parser.append(encoder.nextFrame()) + parser.finish()
        let sps = try #require(units.first)
        let info = try #require(H264SPSInfo(sps: sps))
        #expect(info == H264SPSInfo.testValue(width: 256, height: 144))
        #expect(units.map(\.type) == [7, 8, 5])
    }
}

extension H264SPSInfo {
    static func testValue(width: Int, height: Int) -> H264SPSInfo? {
        var encoder = TestPatternEncoder(width: width, height: height, channel: .front)
        var parser = AnnexBParser()
        guard let sps = (parser.append(encoder.nextFrame()) + parser.finish()).first,
              let info = H264SPSInfo(sps: sps), info.width == width, info.height == height, info.profile == 66, info.level == 31
        else { return nil }
        return info
    }
}
