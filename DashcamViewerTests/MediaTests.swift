import AVFoundation
import T8Kit
import T8SimulatorKit
import VideoToolbox
import XCTest
@testable import DashcamViewer

/// Decodes access units with VideoToolbox and returns how many frames came out.
func decodeWithVideoToolbox(_ units: [H264AccessUnit]) throws -> (frames: Int, size: CMVideoDimensions?) {
    guard let first = units.first(where: { $0.sps != nil && $0.pps != nil }),
          let sps = first.sps?.bytes, let pps = first.pps?.bytes,
          let format = H264SampleBuffers.formatDescription(sps: sps, pps: pps) else {
        XCTFail("no SPS/PPS")
        return (0, nil)
    }
    var session: VTDecompressionSession?
    let status = VTDecompressionSessionCreate(
        allocator: kCFAllocatorDefault, formatDescription: format, decoderSpecification: nil,
        imageBufferAttributes: nil, outputCallback: nil, decompressionSessionOut: &session
    )
    guard status == noErr, let session else {
        XCTFail("VTDecompressionSessionCreate failed: \(status)")
        return (0, nil)
    }
    defer { VTDecompressionSessionInvalidate(session) }
    final class Counter: @unchecked Sendable {
        let lock = NSLock()
        var frames = 0
        func add() { lock.lock(); frames += 1; lock.unlock() }
    }
    let counter = Counter()
    for unit in units {
        guard let sample = H264SampleBuffers.sampleBuffer(unit, format: format) else { continue }
        VTDecompressionSessionDecodeFrame(session, sampleBuffer: sample, flags: [], infoFlagsOut: nil) { status, _, image, _, _ in
            if status == noErr, image != nil { counter.add() }
        }
    }
    VTDecompressionSessionWaitForAsynchronousFrames(session)
    return (counter.frames, CMVideoFormatDescriptionGetDimensions(format))
}

func testPattern(_ channel: T8Channel, width: Int, height: Int, frames: Int) -> [UInt8] {
    var encoder = TestPatternEncoder(width: width, height: height, channel: channel)
    var bytes: [UInt8] = []
    for _ in 0..<frames { bytes += encoder.nextFrame() }
    return bytes
}

func accessUnits(_ bytes: [UInt8]) -> [H264AccessUnit] {
    var parser = AnnexBParser()
    var assembler = H264AccessUnitAssembler()
    var units = assembler.append(parser.append(bytes))
    units += assembler.append(parser.finish())
    units += assembler.finish()
    return units
}

final class MediaTests: XCTestCase {
    func testVideoToolboxDecodesTheSimulatorStream() throws {
        let units = accessUnits(testPattern(.front, width: 256, height: 144, frames: 10))
        XCTAssertEqual(units.count, 10)
        let result = try decodeWithVideoToolbox(units)
        XCTAssertEqual(result.frames, 10)
        XCTAssertEqual(result.size?.width, 256)
        XCTAssertEqual(result.size?.height, 144)
    }

    @MainActor
    func testRendererFollowsAResolutionChange() {
        let renderer = H264Renderer()
        for unit in accessUnits(testPattern(.front, width: 256, height: 144, frames: 3)) {
            XCTAssertTrue(renderer.enqueue(unit))
        }
        XCTAssertEqual(renderer.resolutionText, "256×144")
        for unit in accessUnits(testPattern(.rear, width: 176, height: 144, frames: 3)) {
            XCTAssertTrue(renderer.enqueue(unit))
        }
        XCTAssertEqual(renderer.resolutionText, "176×144")
        XCTAssertEqual(renderer.framesShown, 6)
        XCTAssertNotEqual(renderer.displayLayer.status, .failed)
    }

    func testMP4ExportHasVideoAndAACAudio() async throws {
        let dir = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
        try FileManager.default.createDirectory(at: dir, withIntermediateDirectories: true)
        defer { try? FileManager.default.removeItem(at: dir) }
        let video = dir.appendingPathComponent("clip.h264")
        let audio = dir.appendingPathComponent("clip.pcm")
        try Data(testPattern(.rear, width: 176, height: 144, frames: 50)).write(to: video)
        var tone = TonePCM()
        try Data(tone.next(bytes: 2 * T8PCM.sampleRate * 2)).write(to: audio)

        let output = dir.appendingPathComponent("clip.mp4")
        let result = try await MP4Exporter.export(h264: video, pcm: audio, framesPerSecond: 25, to: output)
        XCTAssertEqual(result.videoFrames, 50)
        XCTAssertEqual(result.audioSeconds, 2, accuracy: 0.01)

        let asset = AVURLAsset(url: output)
        let videoTracks = try await asset.loadTracks(withMediaType: .video)
        let audioTracks = try await asset.loadTracks(withMediaType: .audio)
        XCTAssertEqual(videoTracks.count, 1)
        XCTAssertEqual(audioTracks.count, 1)
        let size = try await videoTracks[0].load(.naturalSize)
        XCTAssertEqual(size.width, 176)
        let duration = try await asset.load(.duration).seconds
        XCTAssertEqual(duration, 2, accuracy: 0.15)
        let audioFormats = try await audioTracks[0].load(.formatDescriptions)
        let subtype = audioFormats.first.map { CMFormatDescriptionGetMediaSubType($0) }
        XCTAssertEqual(subtype, kAudioFormatMPEG4AAC)
    }

    func testMP4ExportWithoutAudio() async throws {
        let dir = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
        try FileManager.default.createDirectory(at: dir, withIntermediateDirectories: true)
        defer { try? FileManager.default.removeItem(at: dir) }
        let video = dir.appendingPathComponent("clip.h264")
        try Data(testPattern(.front, width: 64, height: 48, frames: 30)).write(to: video)
        let output = dir.appendingPathComponent("clip.mp4")
        let result = try await MP4Exporter.export(h264: video, pcm: nil, framesPerSecond: 30, to: output)
        XCTAssertEqual(result.videoFrames, 30)
        let tracks = try await AVURLAsset(url: output).loadTracks(withMediaType: .audio)
        XCTAssertTrue(tracks.isEmpty)
    }
}
