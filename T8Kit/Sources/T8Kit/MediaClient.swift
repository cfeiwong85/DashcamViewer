import Foundation

/// Opens the camera's data ports and turns them into frames or files.
public struct T8MediaClient: Sendable {
    public let connector: any T8Connector
    public let address: T8Address
    public let log: T8PacketLog?
    public var dataOptions: T8DataStream.Options
    /// LogonT opens the data ports 1500 ms after sending 8200 (MainFileListView_blackbox2.java:624-637).
    public var transferConnectDelay: Duration = .milliseconds(1500)

    public init(connector: any T8Connector, address: T8Address = .camera, log: T8PacketLog? = nil, dataOptions: T8DataStream.Options = .init()) {
        self.connector = connector
        self.address = address
        self.log = log
        self.dataOptions = dataOptions
    }

    public func videoStream() -> T8DataStream {
        T8DataStream(connector: connector, host: address.host, port: address.videoPort, options: dataOptions, log: log)
    }

    public func audioStream() -> T8DataStream {
        T8DataStream(connector: connector, host: address.host, port: address.audioPort, options: dataOptions, log: log)
    }

    /// H.264 access units from port 7778 (live view after `startPreview`, or playback after 8200).
    public func accessUnits() -> T8AccessUnitSequence {
        T8AccessUnitSequence(chunks: videoStream())
    }

    /// 8 kHz mono 16-bit PCM chunks from port 7779.
    public func audioChunks() -> T8DataStream {
        audioStream()
    }

    /// Requests a recording with 8200 and writes the raw streams to `directory`:
    /// `<stem>.h264` and `<stem>.pcm` for videos, `<name>` for photos.
    public func download(
        _ recording: T8Recording,
        channel: T8Channel,
        session: T8ControlSession,
        to directory: URL,
        progress: (@Sendable (_ videoBytes: Int, _ expectedBytes: Int?) -> Void)? = nil
    ) async throws -> T8DownloadedFile {
        try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
        let sent = ContinuousClock.now
        let fileStart = try await session.startFileTransfer(recording, channel: channel)
        let waited = ContinuousClock.now - sent
        if waited < transferConnectDelay {
            try await Task.sleep(for: transferConnectDelay - waited)
        }
        let expected = fileStart.map { Int($0.fileSize) }.flatMap { $0 > 0 ? $0 : nil }
        let isVideo = recording.folder.isVideo
        let videoURL = directory.appendingPathComponent(isVideo ? recording.fileStem(for: channel) + ".h264" : recording.name)
        let audioURL = isVideo ? directory.appendingPathComponent(recording.fileStem(for: channel) + ".pcm") : nil

        let videoChunks = videoStream()
        let audioChunks = isVideo ? audioStream() : nil
        async let videoBytes = Self.write(videoChunks, to: videoURL) { total in progress?(total, expected) }
        async let audioBytes = Self.write(audioChunks, to: audioURL, onProgress: nil)
        let (video, audio) = try await (videoBytes, audioBytes)
        log?.info("Downloaded \(recording.name) \(channel.title): video \(video) B, audio \(audio) B")
        return T8DownloadedFile(
            recording: recording, channel: channel, fileStart: fileStart,
            videoURL: videoURL, audioURL: audioURL, videoByteCount: video, audioByteCount: audio
        )
    }

    private static func write(
        _ chunks: T8DataStream?,
        to url: URL?,
        onProgress: ((Int) -> Void)?
    ) async throws -> Int {
        guard let chunks, let url else { return 0 }
        guard FileManager.default.createFile(atPath: url.path, contents: nil) else {
            throw T8TransportError.io("Cannot create \(url.lastPathComponent)")
        }
        let handle = try FileHandle(forWritingTo: url)
        defer { try? handle.close() }
        var total = 0
        for try await chunk in chunks {
            try handle.write(contentsOf: Data(chunk))
            total += chunk.count
            onProgress?(total)
        }
        return total
    }
}

public struct T8DownloadedFile: Sendable, Hashable {
    public let recording: T8Recording
    public let channel: T8Channel
    public let fileStart: T8FileStart?
    public let videoURL: URL
    public let audioURL: URL?
    public let videoByteCount: Int
    public let audioByteCount: Int
}

/// Helpers for the camera's raw audio: 8000 Hz, mono, signed 16-bit little-endian.
public enum T8PCM {
    public static let sampleRate = 8000
    public static let bytesPerSample = 2

    public static func samples(_ bytes: [UInt8]) -> [Int16] {
        stride(from: 0, to: bytes.count - 1, by: 2).map { Int16(bitPattern: UInt16(bytes[$0]) | UInt16(bytes[$0 + 1]) << 8) }
    }

    public static func duration(byteCount: Int) -> TimeInterval {
        Double(byteCount / bytesPerSample) / Double(sampleRate)
    }

    /// A 44-byte WAV header for `dataByteCount` bytes of camera PCM.
    public static func wavHeader(dataByteCount: Int) -> [UInt8] {
        let byteRate = UInt32(sampleRate * bytesPerSample)
        var header: [UInt8] = Array("RIFF".utf8) + T8Bytes.le(UInt32(36 + dataByteCount)) + Array("WAVE".utf8)
        header += Array("fmt ".utf8) + T8Bytes.le(UInt32(16)) + [1, 0, 1, 0]
        header += T8Bytes.le(UInt32(sampleRate)) + T8Bytes.le(byteRate) + [2, 0, 16, 0]
        header += Array("data".utf8) + T8Bytes.le(UInt32(dataByteCount))
        return header
    }
}
