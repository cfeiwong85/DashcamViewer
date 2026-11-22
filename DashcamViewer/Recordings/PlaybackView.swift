import SwiftUI
import T8Kit
import UIKit

/// Streams a recording like LogonT's MainFileFreeView: 8200, then audio on 7779 and video
/// on 7778, shown at the recording's frame rate. The raw streams are kept in a temporary
/// folder so the recording can be saved afterwards without downloading it again.
@MainActor
@Observable
final class PlaybackModel {
    enum Phase: Equatable {
        case loading
        case playing
        case finished
        case failed(String)
    }

    let renderer = H264Renderer()
    var phase: Phase = .loading
    var currentFrame = 0
    var totalFrames: Int?
    var saveStatus: String?
    var photo: UIImage?
    private var task: Task<Void, Never>?
    private var audioTask: Task<Void, Never>?
    private var player: PCMPlayer?
    private let work = FileManager.default.temporaryDirectory.appendingPathComponent("playback-\(UUID().uuidString)")
    private var videoFile: URL { work.appendingPathComponent("video.h264") }
    private var audioFile: URL { work.appendingPathComponent("audio.pcm") }

    func start(_ recording: T8Recording, channel: T8Channel, app: AppModel) {
        guard task == nil else { return }
        guard app.activity == .idle else {
            phase = .failed("Stop the live view or download first.")
            return
        }
        app.activity = .playback
        task = Task { [weak self] in
            await self?.run(recording, channel: channel, app: app)
        }
    }

    private func run(_ recording: T8Recording, channel: T8Channel, app: AppModel) async {
        do {
            try FileManager.default.createDirectory(at: work, withIntermediateDirectories: true)
            let session = try await app.connectedSession()
            let start = try await session.startFileTransfer(recording, channel: channel)
            if let frames = start?.frameCount, frames > 0 { totalFrames = Int(frames) }
            // LogonT connects audio after 1.5 s and video after 2 s (MainFileFreeView.java:676-687).
            try await Task.sleep(for: .milliseconds(1500))
            if recording.folder.isVideo {
                startAudio(app: app)
            }
            try await Task.sleep(for: .milliseconds(500))

            if !recording.folder.isVideo {
                var bytes: [UInt8] = []
                for try await chunk in app.media.videoStream() { bytes += chunk }
                try Data(bytes).write(to: videoFile)
                photo = UIImage(data: Data(bytes))
                phase = photo == nil ? .failed("The photo could not be read (\(bytes.count) bytes).") : .finished
                return
            }

            let writer = try FileWriter(url: videoFile)
            defer { writer.close() }
            let interval = 1.0 / Double(channel.framesPerSecond)
            var clockStart: ContinuousClock.Instant?
            for try await unit in app.media.accessUnits() {
                if Task.isCancelled { break }
                try writer.write(unit.annexB)
                let index = currentFrame
                if let clockStart {
                    let due = clockStart + .milliseconds(Int64(Double(index) * interval * 1000))
                    if due > .now { try await Task.sleep(until: due, clock: .continuous) }
                } else {
                    clockStart = .now
                    try? player?.start()
                }
                if renderer.enqueue(unit) {
                    phase = .playing
                }
                currentFrame += 1
            }
            if !Task.isCancelled { phase = .finished }
        } catch is CancellationError {
        } catch {
            if !Task.isCancelled { phase = .failed(AppModel.message(for: error)) }
        }
    }

    private func startAudio(app: AppModel) {
        guard let player = PCMPlayer() else { return }
        self.player = player
        let audioFile = self.audioFile
        let stream = app.media.audioChunks()
        audioTask = Task {
            guard let writer = try? FileWriter(url: audioFile) else { return }
            defer { writer.close() }
            do {
                for try await chunk in stream {
                    try writer.write(chunk)
                    player.schedule(chunk)
                }
            } catch {
                app.log.warning("Audio stream: \(error)")
            }
        }
    }

    func stop(app: AppModel) {
        task?.cancel()
        task = nil
        audioTask?.cancel()
        audioTask = nil
        player?.stop()
        player = nil
        if app.activity == .playback { app.activity = .idle }
    }

    func save(_ recording: T8Recording, channel: T8Channel, app: AppModel, alsoToPhotos: Bool) async {
        guard phase == .finished else { return }
        saveStatus = "Saving…"
        do {
            let saved: URL
            if recording.folder.isVideo {
                saved = app.library.uniqueURL(for: recording.fileStem(for: channel) + ".mp4")
                let audio = FileManager.default.fileExists(atPath: audioFile.path) ? audioFile : nil
                _ = try await MP4Exporter.export(h264: videoFile, pcm: audio, framesPerSecond: channel.framesPerSecond, to: saved)
            } else {
                saved = app.library.uniqueURL(for: recording.name)
                try FileManager.default.copyItem(at: videoFile, to: saved)
            }
            app.library.reload()
            if alsoToPhotos {
                try await PhotosSaver.save(saved, isVideo: recording.folder.isVideo)
                saveStatus = "Saved to Photos and the Saved tab."
            } else {
                saveStatus = "Saved to the Saved tab."
            }
        } catch {
            saveStatus = AppModel.message(for: error)
        }
    }

    func cleanUp() {
        try? FileManager.default.removeItem(at: work)
    }
}

/// Appends bytes to a file.
final class FileWriter {
    private let handle: FileHandle

    init(url: URL) throws {
        guard FileManager.default.createFile(atPath: url.path, contents: nil) else {
            throw CocoaError(.fileWriteUnknown)
        }
        handle = try FileHandle(forWritingTo: url)
    }

    func write(_ bytes: [UInt8]) throws {
        try handle.write(contentsOf: Data(bytes))
    }

    func close() {
        try? handle.close()
    }
}

struct PlaybackView: View {
    let recording: T8Recording
    let channel: T8Channel
    @Environment(AppModel.self) private var app
    @Environment(\.dismiss) private var dismiss
    @State private var model = PlaybackModel()
    @AppStorage("alsoSaveToPhotos") private var alsoSaveToPhotos = true
    @AppStorage("mirrorRear") private var mirrorRear = false

    var body: some View {
        NavigationStack {
            VStack(spacing: 16) {
                ZStack {
                    Color.black
                    if let photo = model.photo {
                        Image(uiImage: photo).resizable().scaledToFit()
                    } else {
                        VideoSurface(renderer: model.renderer)
                            .scaleEffect(x: channel == .rear && mirrorRear ? -1 : 1, y: 1)
                    }
                    switch model.phase {
                    case .loading:
                        ProgressView("Loading…").tint(.white).foregroundStyle(.white)
                    case .failed(let text):
                        Text(text).foregroundStyle(.white).multilineTextAlignment(.center).padding()
                    default:
                        EmptyView()
                    }
                }
                .aspectRatio(16 / 9, contentMode: .fit)

                if recording.folder.isVideo {
                    progress
                }
                if model.phase == .finished {
                    Button {
                        Task { await model.save(recording, channel: channel, app: app, alsoToPhotos: alsoSaveToPhotos) }
                    } label: {
                        Label("Save to iPhone", systemImage: "square.and.arrow.down")
                    }
                    .buttonStyle(.borderedProminent)
                }
                if let status = model.saveStatus {
                    Text(status).font(.footnote).foregroundStyle(.secondary)
                }
                Spacer()
            }
            .padding()
            .navigationTitle(RecordingsView.title(for: recording))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close") { dismiss() }
                }
                ToolbarItem(placement: .principal) {
                    Text("\(recording.folder.title) · \(channel.title)").font(.headline)
                }
            }
        }
        .onAppear { model.start(recording, channel: channel, app: app) }
        .onDisappear {
            model.stop(app: app)
            model.cleanUp()
        }
    }

    private var progress: some View {
        let fps = Double(channel.framesPerSecond)
        let elapsed = Double(model.currentFrame) / fps
        return VStack {
            if let total = model.totalFrames, total > 0 {
                ProgressView(value: min(Double(model.currentFrame), Double(total)), total: Double(total))
            }
            HStack {
                Text(Self.clock(elapsed))
                Spacer()
                if let total = model.totalFrames {
                    Text(Self.clock(Double(total) / fps))
                }
            }
            .font(.caption.monospacedDigit())
            .foregroundStyle(.secondary)
        }
    }

    static func clock(_ seconds: Double) -> String {
        let s = Int(seconds.rounded(.down))
        return String(format: "%d:%02d", s / 60, s % 60)
    }
}
