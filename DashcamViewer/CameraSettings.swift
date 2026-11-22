import Foundation

struct CameraPreset: Identifiable, Hashable {
    let id: String
    let name: String
    let liveURL: String
    let fileListURL: String
    let commandURL: String   // optional: sent before live view (e.g. switch to video mode)

    static let all: [CameraPreset] = [
        .init(id: "novatek", name: "Novatek-based (most Wi-Fi dashcams)",
              liveURL: "rtsp://192.168.1.254/xxx.mov",
              fileListURL: "http://192.168.1.254/?custom=1&cmd=3015",
              commandURL: "http://192.168.1.254/?custom=1&cmd=2016&par=1"),
        .init(id: "generic", name: "Generic / custom",
              liveURL: "rtsp://192.168.0.1/live",
              fileListURL: "http://192.168.0.1/",
              commandURL: ""),
    ]
}

/// Persisted, user-editable camera addresses.
final class CameraSettings: ObservableObject {
    @Published var liveURL: String { didSet { save() } }
    @Published var fileListURL: String { didSet { save() } }
    @Published var commandURL: String { didSet { save() } }

    private let d = UserDefaults.standard

    init() {
        let p = CameraPreset.all[0]
        liveURL = d.string(forKey: "liveURL") ?? p.liveURL
        fileListURL = d.string(forKey: "fileListURL") ?? p.fileListURL
        commandURL = d.string(forKey: "commandURL") ?? p.commandURL
    }

    func apply(_ p: CameraPreset) {
        liveURL = p.liveURL; fileListURL = p.fileListURL; commandURL = p.commandURL
    }

    private func save() {
        d.set(liveURL, forKey: "liveURL")
        d.set(fileListURL, forKey: "fileListURL")
        d.set(commandURL, forKey: "commandURL")
    }
}
