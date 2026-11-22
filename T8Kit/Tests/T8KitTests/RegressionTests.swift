import Foundation
import Testing
@testable import T8Kit

/// Regression tests for findings from the phase 6 line review.
@Suite("Review regressions")
struct RegressionTests {
    @Test("Byte readers return nil instead of trapping on extreme offsets")
    func extremeOffsets() {
        let bytes: [UInt8] = [1, 2, 3, 4]
        #expect(T8Bytes.u32LE(bytes, at: Int.max) == nil)
        #expect(T8Bytes.u32LE(bytes, at: -1) == nil)
        #expect(T8Bytes.u16BE(bytes, at: Int.max) == nil)
        #expect(T8Bytes.u32LE(bytes[1...], at: 0) == nil)
        #expect(T8Bytes.u16BE(bytes[1...], at: 1) == 0x0304)
    }

    @Test("Simulator address with the highest ports does not overflow")
    func highPorts() {
        let address = T8Address.simulator(basePort: UInt16.max)
        #expect(address.audioPort == UInt16.max)
        #expect(address.controlPort == UInt16.max - 2)
    }

    @Test("A short record slice and a maximal page number do not trap")
    func fileListEdges() {
        let entry = T8FileListEntry(record: [0x32, 0x30][...])
        #expect(entry.name == "20")
        #expect(!entry.hasRear)
        #expect(T8FileListPage(totalPages: 5, currentPage: UInt32.max, entries: []).isLast)
    }

    @Test("Malformed SPS data is rejected instead of crashing", arguments: [
        [UInt8]([0x67, 0x42, 0xC0, 0x1F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF]),
        [0x67, 0x64, 0x00, 0x28, 0xAC, 0x00, 0x00, 0x00, 0x01, 0xFF, 0xFF, 0xFF],
        [0x67, 0x42, 0x00, 0x1E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01],
    ])
    func malformedSPS(bytes: [UInt8]) {
        _ = H264SPSInfo(sps: H264NALUnit(bytes: bytes))
    }

    @Test("A second concurrent connect is refused, not overwriting the first")
    func concurrentConnect() async throws {
        let camera = FakeCamera(respond: FakeCamera.standard())
        let session = T8ControlSession(connector: SlowConnector(inner: camera))
        async let first: Void = session.connect()
        try await Task.sleep(for: .milliseconds(20))
        await #expect(throws: T8SessionError.busy("already connecting")) { try await session.connect() }
        try await first
        #expect(await session.isConnected)
        await session.disconnect()
    }

    @Test("Out-of-range setting indexes are refused before sending")
    func settingRange() async throws {
        let camera = FakeCamera(respond: FakeCamera.standard())
        let session = T8ControlSession(connector: camera)
        try await session.connect()
        _ = try await session.readConfig()
        await #expect(throws: T8SessionError.rejected(.setLED, -1)) { try await session.setSetting(.led, index: 2) }
        await #expect(throws: T8SessionError.rejected(.setLED, -1)) { try await session.setSetting(.led, index: -1) }
        #expect(!camera.received.contains { $0.command == .setLED })
        await session.disconnect()
    }

    @Test("Cancelling a data stream reader throws instead of reporting a complete stream")
    func cancelledStream() async throws {
        let pipe = PipeConnection()
        let stream = T8DataStream(connector: FixedConnector(connection: pipe), host: "x", port: 1)
        let reader = Task { () -> Int in
            var total = 0
            for try await chunk in stream { total += chunk.count }
            return total
        }
        pipe.push([1, 2, 3])
        try await Task.sleep(for: .milliseconds(50))
        reader.cancel()
        await #expect(throws: CancellationError.self) { try await reader.value }
    }
}

/// Delays connect so two connect() calls overlap.
struct SlowConnector: T8Connector {
    let inner: any T8Connector

    func connect(host: String, port: UInt16, timeout: Duration) async throws -> any T8Connection {
        try await Task.sleep(for: .milliseconds(100))
        return try await inner.connect(host: host, port: port, timeout: timeout)
    }
}

struct FixedConnector: T8Connector {
    let connection: any T8Connection

    func connect(host: String, port: UInt16, timeout: Duration) async throws -> any T8Connection {
        connection
    }
}
