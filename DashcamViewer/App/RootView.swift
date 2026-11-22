import SwiftUI
import T8Kit

struct RootView: View {
    @Environment(AppModel.self) private var app
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        TabView {
            LiveView()
                .tabItem { Label("Live", systemImage: "video") }
            RecordingsView()
                .tabItem { Label("Recordings", systemImage: "film.stack") }
            SavedView()
                .tabItem { Label("Saved", systemImage: "folder") }
            SettingsView()
                .tabItem { Label("Settings", systemImage: "gearshape") }
            DiagnosticsView()
                .tabItem { Label("Diagnostics", systemImage: "stethoscope") }
        }
        .onChange(of: scenePhase) { _, phase in
            // The camera accepts one phone at a time; release it when the app goes to the background,
            // as LogonT does when it exits.
            if phase == .background {
                app.log.info("App moved to the background; disconnecting")
                Task { await app.disconnect() }
            }
        }
    }
}

/// Connection status shown at the top of the camera screens.
struct ConnectionBanner: View {
    @Environment(AppModel.self) private var app

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Circle().fill(color).frame(width: 10, height: 10)
                Text(text).font(.footnote)
                Spacer()
                switch app.state {
                case .connected:
                    Button("Disconnect") { Task { await app.disconnect() } }
                        .font(.footnote)
                case .connecting:
                    ProgressView().controlSize(.small)
                case .disconnected, .failed:
                    Button("Connect") { Task { _ = try? await app.connectedSession() } }
                        .font(.footnote)
                }
            }
            if case .failed(let reason) = app.state {
                Text(reason).font(.caption).foregroundStyle(.secondary)
            } else if let notice = app.notice {
                Text(notice).font(.caption).foregroundStyle(.secondary)
            }
        }
        .padding(.horizontal)
        .padding(.vertical, 6)
        .background(.bar)
    }

    private var color: Color {
        switch app.state {
        case .connected: .green
        case .connecting: .yellow
        case .disconnected: .gray
        case .failed: .red
        }
    }

    private var text: String {
        let product = app.config?.productName ?? ""
        switch app.state {
        case .connected: return "Connected to \(product.isEmpty ? app.target.title : product)"
        case .connecting: return "Connecting to \(app.target.title)…"
        case .disconnected: return "Not connected"
        case .failed: return "Connection failed"
        }
    }
}
