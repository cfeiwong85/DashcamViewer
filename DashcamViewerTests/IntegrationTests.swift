import AVFoundation
import T8Kit
import XCTest
@testable import DashcamViewer

/// The app's Network.framework transport against a running `t8sim`.
/// CI starts the simulator and passes TEST_RUNNER_T8SIM_HOST / TEST_RUNNER_T8SIM_PORT;
/// locally the tests are skipped unless those variables are set.
final class IntegrationTests: XCTestCase {
    private var address = T8Address.camera

    override func setUpWithError() throws {
        let environment = ProcessInfo.processInfo.environment
        guard let portText = environment["T8SIM_PORT"], let port = UInt16(portText) else {
            throw XCTSkip("Set T8SIM_PORT (with t8sim running) to run the integration tests.")
        }
        address = .simulator(host: environment["T8SIM_HOST"] ?? "127.0.0.1", basePort: port)
    }

    private func makeSession() -> T8ControlSession {
        var options = T8ControlSession.Options(address: address)
        options.replyTimeout = .seconds(5)
        return T8ControlSession(connector: NWConnector(requireWiFi: false), options: options)
    }

    private var media: T8MediaClient {
        T8MediaClient(connector: NWConnector(requireWiFi: false), address: address)
    }

    func testLiveViewOverNetworkFrameworkDecodes() async throws {
        let session = makeSession()
        try await session.connect()
        addTeardownBlock { await session.disconnect() }
        let config = try await session.startPreview()
        XCTAssertFalse(config.productName.isEmpty)
        try await Task.sleep(for: .seconds(1))
        var units: [H264AccessUnit] = []
        for try await unit in media.accessUnits() {
            units.append(unit)
            if units.count == 10 { break }
        }
        try await session.stopPreview()
        let decoded = try decodeWithVideoToolbox(units)
        XCTAssertEqual(decoded.frames, 10)
    }

    func testListDownloadAndExport() async throws {
        let session = makeSession()
        try await session.connect()
        addTeardownBlock { await session.disconnect() }
        let list = try await session.listRecordings(in: .driving)
        XCTAssertTrue(list.isComplete)
        let recording = try XCTUnwrap(list.recordings.first)

        let dir = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
        defer { try? FileManager.default.removeItem(at: dir) }
        let file = try await media.download(recording, channel: .front, session: session, to: dir)
        XCTAssertGreaterThan(file.videoByteCount, 0)
        XCTAssertGreaterThan(file.audioByteCount, 0)

        let output = dir.appendingPathComponent("export.mp4")
        let result = try await MP4Exporter.export(h264: file.videoURL, pcm: file.audioURL, framesPerSecond: 30, to: output)
        XCTAssertEqual(result.videoFrames, Int(file.fileStart?.frameCount ?? -1))
        let tracks = try await AVURLAsset(url: output).loadTracks(withMediaType: .video)
        XCTAssertEqual(tracks.count, 1)
    }

    func testSettingsRoundTrip() async throws {
        let session = makeSession()
        try await session.connect()
        addTeardownBlock { await session.disconnect() }
        try await session.setSetting(.lcdBrightness, index: 6)
        let config = try await session.readConfig()
        XCTAssertEqual(config[.lcdBrightness], 6)
    }
}
