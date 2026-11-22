import SwiftUI

struct LiveView: View {
    @EnvironmentObject var settings: CameraSettings
    @State private var started = false
    @State private var status = ""

    var body: some View {
        NavigationStack {
            VStack {
                if started, let url = URL(string: settings.liveURL) {
                    PlayerView(url: url)
                        .aspectRatio(16.0 / 9.0, contentMode: .fit)
                        .id(settings.liveURL)
                } else {
                    ContentUnavailableView("Not connected",
                        systemImage: "wifi.slash",
                        description: Text("Join the dashcam Wi-Fi, then tap Start."))
                }
                if !status.isEmpty { Text(status).font(.footnote).foregroundStyle(.secondary) }
                Button(started ? "Stop" : "Start live view") { toggle() }
                    .buttonStyle(.borderedProminent)
                    .padding()
                Spacer()
            }
            .navigationTitle("Live")
        }
    }

    private func toggle() {
        if started { started = false; return }
        Task {
            // Many cameras need a mode-switch command before streaming.
            if let cmd = URL(string: settings.commandURL), !settings.commandURL.isEmpty {
                var req = URLRequest(url: cmd); req.timeoutInterval = 4
                do { _ = try await URLSession.shared.data(for: req); status = "" }
                catch { status = "Camera command failed: \(error.localizedDescription)" }
            }
            started = true
        }
    }
}
