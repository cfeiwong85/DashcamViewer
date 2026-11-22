import SwiftUI

struct RecordedFile: Identifiable, Hashable {
    let id = UUID()
    let name: String
    let url: URL
}

enum FileListParser {
    static let exts = ["mp4", "mov", "avi", "ts", "mkv", "jpg", "jpeg"]

    /// Supports Novatek XML (<NAME>/<FPATH>) and plain HTML directory indexes (<a href>).
    static func parse(_ text: String, base: URL) -> [RecordedFile] {
        var out: [RecordedFile] = []
        if text.contains("<FPATH>") {
            for m in matches(#"<NAME>([^<]+)</NAME>\s*<FPATH>([^<]+)</FPATH>"#, in: text) {
                let path = m[1].replacingOccurrences(of: "\\", with: "/")
                    .replacingOccurrences(of: "A:", with: "")
                if let u = URL(string: path, relativeTo: hostRoot(base))?.absoluteURL {
                    out.append(.init(name: m[0], url: u))
                }
            }
        } else {
            for m in matches(#"href=["']([^"']+)["']"#, in: text) {
                let href = m[0]
                guard exts.contains((href as NSString).pathExtension.lowercased()),
                      let u = URL(string: href, relativeTo: base)?.absoluteURL else { continue }
                out.append(.init(name: (href as NSString).lastPathComponent, url: u))
            }
        }
        return out.sorted { $0.name > $1.name }
    }

    private static func hostRoot(_ u: URL) -> URL {
        URL(string: "/", relativeTo: u)?.absoluteURL ?? u
    }

    private static func matches(_ pattern: String, in text: String) -> [[String]] {
        guard let re = try? NSRegularExpression(pattern: pattern, options: [.caseInsensitive]) else { return [] }
        let ns = text as NSString
        return re.matches(in: text, range: NSRange(location: 0, length: ns.length)).map { m in
            (1..<m.numberOfRanges).map { ns.substring(with: m.range(at: $0)) }
        }
    }
}

struct FilesView: View {
    @EnvironmentObject var settings: CameraSettings
    @State private var files: [RecordedFile] = []
    @State private var loading = false
    @State private var error: String?

    var body: some View {
        NavigationStack {
            List(files) { f in
                NavigationLink(f.name) {
                    if ["jpg", "jpeg"].contains(f.url.pathExtension.lowercased()) {
                        AsyncImage(url: f.url) { $0.resizable().scaledToFit() } placeholder: { ProgressView() }
                    } else {
                        PlayerView(url: f.url).ignoresSafeArea(edges: .bottom)
                    }
                }
            }
            .overlay {
                if loading { ProgressView() }
                else if let error { ContentUnavailableView("Couldn't load", systemImage: "exclamationmark.triangle", description: Text(error)) }
                else if files.isEmpty { ContentUnavailableView("No recordings", systemImage: "film.stack", description: Text("Pull down to refresh while on the dashcam Wi-Fi.")) }
            }
            .refreshable { await load() }
            .task { await load() }
            .navigationTitle("Recordings")
        }
    }

    private func load() async {
        guard let url = URL(string: settings.fileListURL) else { error = "Invalid list URL"; return }
        loading = true; error = nil
        defer { loading = false }
        do {
            var req = URLRequest(url: url); req.timeoutInterval = 8
            let (data, _) = try await URLSession.shared.data(for: req)
            let text = String(decoding: data, as: UTF8.self)
            files = FileListParser.parse(text, base: url)
        } catch {
            self.error = error.localizedDescription
        }
    }
}
