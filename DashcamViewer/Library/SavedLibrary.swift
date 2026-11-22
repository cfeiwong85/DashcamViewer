import Foundation
import Observation
import Photos
import T8Kit

/// Recordings and photos saved on this iPhone, in the app's Documents folder
/// (visible in the Files app under On My iPhone › Dashcam Viewer).
@MainActor
@Observable
final class SavedLibrary {
    struct Item: Identifiable, Hashable {
        let url: URL
        let size: Int64
        let date: Date
        var id: URL { url }
        var name: String { url.lastPathComponent }
        var isVideo: Bool { url.pathExtension.lowercased() == "mp4" }
    }

    private(set) var items: [Item] = []
    let directory: URL

    init() {
        directory = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        reload()
    }

    func reload() {
        let keys: [URLResourceKey] = [.fileSizeKey, .contentModificationDateKey]
        let urls = (try? FileManager.default.contentsOfDirectory(at: directory, includingPropertiesForKeys: keys, options: [.skipsHiddenFiles])) ?? []
        items = urls
            .filter { ["mp4", "jpg", "jpeg"].contains($0.pathExtension.lowercased()) }
            .map { url in
                let values = try? url.resourceValues(forKeys: Set(keys))
                return Item(url: url, size: Int64(values?.fileSize ?? 0), date: values?.contentModificationDate ?? .distantPast)
            }
            .sorted { $0.date > $1.date }
    }

    func delete(_ item: Item) throws {
        try FileManager.default.removeItem(at: item.url)
        reload()
    }

    /// `name` if free, otherwise `name-2`, `name-3`, ...
    func uniqueURL(for name: String) -> URL {
        let base = (name as NSString).deletingPathExtension
        let ext = (name as NSString).pathExtension
        var candidate = directory.appendingPathComponent(name)
        var counter = 2
        while FileManager.default.fileExists(atPath: candidate.path) {
            candidate = directory.appendingPathComponent("\(base)-\(counter).\(ext)")
            counter += 1
        }
        return candidate
    }
}

enum PhotosSaver {
    enum Failure: Error, LocalizedError {
        case notAllowed

        var errorDescription: String? {
            "Photos access is off. Allow “Add Photos Only” for Dashcam Viewer in Settings › Privacy › Photos."
        }
    }

    static func save(_ url: URL, isVideo: Bool) async throws {
        let status = await PHPhotoLibrary.requestAuthorization(for: .addOnly)
        guard status == .authorized || status == .limited else { throw Failure.notAllowed }
        try await PHPhotoLibrary.shared().performChanges {
            let request = PHAssetCreationRequest.forAsset()
            request.addResource(with: isVideo ? .video : .photo, fileURL: url, options: nil)
        }
    }
}

/// Downloads a recording, converts it to .mp4 and stores it in the library (and Photos).
@MainActor
@Observable
final class TransferModel {
    var isRunning = false
    var progress: Double?
    var status = ""
    var error: String?

    func save(_ recording: T8Recording, channel: T8Channel, app: AppModel, alsoToPhotos: Bool) async {
        guard !isRunning else { return }
        guard app.activity == .idle else {
            error = "Stop the live view or playback first."
            return
        }
        isRunning = true
        error = nil
        progress = nil
        status = "Requesting \(recording.name) (\(channel.title))…"
        app.activity = .download
        let work = FileManager.default.temporaryDirectory.appendingPathComponent("download-\(UUID().uuidString)")
        defer {
            try? FileManager.default.removeItem(at: work)
            app.activity = .idle
            isRunning = false
        }
        do {
            let session = try await app.connectedSession()
            let file = try await app.media.download(recording, channel: channel, session: session, to: work) { [weak self] bytes, expected in
                Task { @MainActor in
                    guard let self else { return }
                    if let expected, expected > 0 {
                        self.progress = min(1, Double(bytes) / Double(expected))
                    }
                    self.status = "Downloading… \(ByteCountFormatter.string(fromByteCount: Int64(bytes), countStyle: .file))"
                }
            }
            guard file.videoByteCount > 0 else { throw MP4ExportError.noVideo }
            let saved: URL
            if recording.folder.isVideo {
                status = "Converting to MP4…"
                let output = app.library.uniqueURL(for: recording.fileStem(for: channel) + ".mp4")
                let result = try await MP4Exporter.export(h264: file.videoURL, pcm: file.audioURL, framesPerSecond: channel.framesPerSecond, to: output)
                app.log.info("Exported \(output.lastPathComponent): \(result.videoFrames) frames, \(String(format: "%.1f", result.audioSeconds)) s audio")
                saved = output
            } else {
                saved = app.library.uniqueURL(for: recording.name)
                try FileManager.default.moveItem(at: file.videoURL, to: saved)
            }
            app.library.reload()
            if alsoToPhotos {
                status = "Saving to Photos…"
                try await PhotosSaver.save(saved, isVideo: recording.folder.isVideo)
                status = "Saved to Photos and the Saved tab."
            } else {
                status = "Saved to the Saved tab."
            }
            progress = 1
        } catch {
            self.error = AppModel.message(for: error)
            status = ""
            app.log.warning("Save failed: \(error)")
        }
    }
}
