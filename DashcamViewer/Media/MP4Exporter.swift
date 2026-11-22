import AVFoundation
import CoreMedia
import T8Kit

enum MP4ExportError: Error, LocalizedError {
    case noVideo
    case cannotCreateWriter(String)
    case writerFailed(String)

    var errorDescription: String? {
        switch self {
        case .noVideo: "The recording has no decodable video (no H.264 key frame found)."
        case .cannotCreateWriter(let reason): "Cannot create the MP4 file: \(reason)"
        case .writerFailed(let reason): "Writing the MP4 failed: \(reason)"
        }
    }
}

struct MP4ExportResult {
    let url: URL
    let videoFrames: Int
    let audioSeconds: Double
    /// Access units that could not be wrapped in a sample buffer (normally 0).
    let skippedFrames: Int
}

/// Muxes the camera's raw streams into an .mp4: H.264 passed through unchanged, PCM encoded
/// to AAC. This replaces LogonT's mp4parser + MediaCodec path (MainMP4ParSer.java).
enum MP4Exporter {
    static func export(h264 videoURL: URL, pcm audioURL: URL?, framesPerSecond: Int, to output: URL) async throws -> MP4ExportResult {
        guard framesPerSecond > 0 else { throw MP4ExportError.cannotCreateWriter("invalid frame rate \(framesPerSecond)") }
        try? FileManager.default.removeItem(at: output)
        var video = try AccessUnitFileReader(url: videoURL)

        // The video input needs the format description up front, so find the first key frame.
        var first: H264AccessUnit?
        while let unit = try video.next() {
            if unit.isKeyframe, unit.sps != nil, unit.pps != nil {
                first = unit
                break
            }
        }
        guard let first, let sps = first.sps?.bytes, let pps = first.pps?.bytes,
              let format = H264SampleBuffers.formatDescription(sps: sps, pps: pps) else {
            throw MP4ExportError.noVideo
        }

        let writer: AVAssetWriter
        do {
            writer = try AVAssetWriter(outputURL: output, fileType: .mp4)
        } catch {
            throw MP4ExportError.cannotCreateWriter(error.localizedDescription)
        }
        let videoInput = AVAssetWriterInput(mediaType: .video, outputSettings: nil, sourceFormatHint: format)
        videoInput.expectsMediaDataInRealTime = false
        guard writer.canAdd(videoInput) else { throw MP4ExportError.cannotCreateWriter("video input rejected") }
        writer.add(videoInput)

        var audio: PCMFileReader?
        var audioInput: AVAssetWriterInput?
        if let audioURL, let reader = try? PCMFileReader(url: audioURL), reader.byteCount >= 2 {
            let settings: [String: Any] = [
                AVFormatIDKey: kAudioFormatMPEG4AAC,
                AVSampleRateKey: T8PCM.sampleRate,
                AVNumberOfChannelsKey: 1,
            ]
            let input = AVAssetWriterInput(mediaType: .audio, outputSettings: settings)
            input.expectsMediaDataInRealTime = false
            if writer.canAdd(input) {
                writer.add(input)
                audioInput = input
                audio = reader
            }
        }

        guard writer.startWriting() else {
            try? FileManager.default.removeItem(at: output)
            throw MP4ExportError.writerFailed(writer.error?.localizedDescription ?? "startWriting")
        }
        do {
            return try await write(
                writer: writer, videoInput: videoInput, audioInput: audioInput, format: format,
                first: first, video: &video, audio: &audio, framesPerSecond: framesPerSecond, output: output
            )
        } catch {
            // Never leave a half-written file behind (it would show up in the Saved list).
            if writer.status == .writing { writer.cancelWriting() }
            try? FileManager.default.removeItem(at: output)
            throw error
        }
    }

    private static func write(
        writer: AVAssetWriter,
        videoInput: AVAssetWriterInput,
        audioInput: AVAssetWriterInput?,
        format: CMVideoFormatDescription,
        first: H264AccessUnit,
        video: inout AccessUnitFileReader,
        audio: inout PCMFileReader?,
        framesPerSecond: Int,
        output: URL
    ) async throws -> MP4ExportResult {
        writer.startSession(atSourceTime: .zero)

        let frameDuration = CMTime(value: 1, timescale: CMTimeScale(framesPerSecond))
        var frameIndex = 0
        var skippedFrames = 0
        var pendingVideo: H264AccessUnit? = first
        var audioSamples = 0
        var videoDone = false
        var audioDone = audioInput == nil

        // Interleave by timestamp so neither input stalls waiting for the other.
        while !(videoDone && audioDone) {
            if writer.status == .failed {
                throw MP4ExportError.writerFailed(writer.error?.localizedDescription ?? "unknown")
            }
            let videoTime = Double(frameIndex) / Double(framesPerSecond)
            let audioTime = Double(audioSamples) / Double(T8PCM.sampleRate)
            let writeVideo = !videoDone && (audioDone || videoTime <= audioTime)
            if writeVideo {
                guard videoInput.isReadyForMoreMediaData else {
                    try await Task.sleep(for: .milliseconds(5))
                    continue
                }
                guard let unit = pendingVideo else {
                    videoInput.markAsFinished()
                    videoDone = true
                    continue
                }
                let time = CMTime(value: CMTimeValue(frameIndex), timescale: CMTimeScale(framesPerSecond))
                if let sample = H264SampleBuffers.sampleBuffer(unit, format: format, presentationTime: time, duration: frameDuration) {
                    if !videoInput.append(sample) {
                        throw MP4ExportError.writerFailed(writer.error?.localizedDescription ?? "video append")
                    }
                    frameIndex += 1
                } else {
                    skippedFrames += 1
                }
                pendingVideo = try video.next()
            } else if let input = audioInput {
                guard input.isReadyForMoreMediaData else {
                    try await Task.sleep(for: .milliseconds(5))
                    continue
                }
                guard var reader = audio, let chunk = try reader.next(samples: 1024) else {
                    input.markAsFinished()
                    audioDone = true
                    continue
                }
                audio = reader
                if let sample = PCMSampleBuffer.make(chunk, startSample: audioSamples) {
                    if !input.append(sample) {
                        throw MP4ExportError.writerFailed(writer.error?.localizedDescription ?? "audio append")
                    }
                }
                audioSamples += chunk.count / 2
            }
        }

        await writer.finishWriting()
        guard writer.status == .completed else {
            throw MP4ExportError.writerFailed(writer.error?.localizedDescription ?? "status \(writer.status.rawValue)")
        }
        return MP4ExportResult(
            url: output, videoFrames: frameIndex,
            audioSeconds: Double(audioSamples) / Double(T8PCM.sampleRate), skippedFrames: skippedFrames
        )
    }
}

/// Reads access units from a raw .h264 file in 1 MB chunks.
struct AccessUnitFileReader {
    private let handle: FileHandle
    private var parser = AnnexBParser()
    private var assembler = H264AccessUnitAssembler()
    private var ready: [H264AccessUnit] = []
    private var ended = false

    init(url: URL) throws {
        handle = try FileHandle(forReadingFrom: url)
    }

    mutating func next() throws -> H264AccessUnit? {
        while ready.isEmpty {
            if ended { return nil }
            if let data = try handle.read(upToCount: 1 << 20), !data.isEmpty {
                ready += assembler.append(parser.append([UInt8](data)))
            } else {
                ended = true
                try? handle.close()
                ready += assembler.append(parser.finish()) + assembler.finish()
            }
        }
        return ready.removeFirst()
    }
}

/// Reads 16-bit PCM from a file in whole samples.
struct PCMFileReader {
    private let handle: FileHandle
    let byteCount: Int

    init(url: URL) throws {
        handle = try FileHandle(forReadingFrom: url)
        byteCount = (try? FileManager.default.attributesOfItem(atPath: url.path)[.size] as? Int) ?? 0
    }

    mutating func next(samples: Int) throws -> [UInt8]? {
        guard let data = try handle.read(upToCount: samples * 2), data.count >= 2 else {
            try? handle.close()
            return nil
        }
        var bytes = [UInt8](data)
        if bytes.count % 2 == 1 { bytes.removeLast() }
        return bytes
    }
}

enum PCMSampleBuffer {
    private static let format: CMAudioFormatDescription? = {
        var description = AudioStreamBasicDescription(
            mSampleRate: Float64(T8PCM.sampleRate),
            mFormatID: kAudioFormatLinearPCM,
            mFormatFlags: kAudioFormatFlagIsSignedInteger | kAudioFormatFlagIsPacked,
            mBytesPerPacket: 2, mFramesPerPacket: 1, mBytesPerFrame: 2,
            mChannelsPerFrame: 1, mBitsPerChannel: 16, mReserved: 0
        )
        var format: CMAudioFormatDescription?
        CMAudioFormatDescriptionCreate(
            allocator: kCFAllocatorDefault, asbd: &description, layoutSize: 0, layout: nil,
            magicCookieSize: 0, magicCookie: nil, extensions: nil, formatDescriptionOut: &format
        )
        return format
    }()

    static func make(_ bytes: [UInt8], startSample: Int) -> CMSampleBuffer? {
        guard let format, bytes.count >= 2 else { return nil }
        var block: CMBlockBuffer?
        guard CMBlockBufferCreateWithMemoryBlock(
            allocator: kCFAllocatorDefault, memoryBlock: nil, blockLength: bytes.count,
            blockAllocator: kCFAllocatorDefault, customBlockSource: nil, offsetToData: 0,
            dataLength: bytes.count, flags: kCMBlockBufferAssureMemoryNowFlag, blockBufferOut: &block
        ) == kCMBlockBufferNoErr, let block else { return nil }
        let copied = bytes.withUnsafeBytes { raw -> OSStatus in
            guard let base = raw.baseAddress else { return -1 }
            return CMBlockBufferReplaceDataBytes(with: base, blockBuffer: block, offsetIntoDestination: 0, dataLength: bytes.count)
        }
        guard copied == kCMBlockBufferNoErr else { return nil }
        var timing = CMSampleTimingInfo(
            duration: CMTime(value: 1, timescale: CMTimeScale(T8PCM.sampleRate)),
            presentationTimeStamp: CMTime(value: CMTimeValue(startSample), timescale: CMTimeScale(T8PCM.sampleRate)),
            decodeTimeStamp: .invalid
        )
        var sampleSize = 2
        var sample: CMSampleBuffer?
        guard CMSampleBufferCreateReady(
            allocator: kCFAllocatorDefault, dataBuffer: block, formatDescription: format,
            sampleCount: bytes.count / 2, sampleTimingEntryCount: 1, sampleTimingArray: &timing,
            sampleSizeEntryCount: 1, sampleSizeArray: &sampleSize, sampleBufferOut: &sample
        ) == noErr else { return nil }
        return sample
    }
}
