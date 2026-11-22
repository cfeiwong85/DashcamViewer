import SwiftUI

struct SettingsView: View {
    @EnvironmentObject var settings: CameraSettings

    var body: some View {
        NavigationStack {
            Form {
                Section("Preset") {
                    ForEach(CameraPreset.all) { p in
                        Button(p.name) { settings.apply(p) }
                    }
                }
                Section("Addresses") {
                    field("Live stream URL", $settings.liveURL)
                    field("Recordings list URL", $settings.fileListURL)
                    field("Pre-live command URL (optional)", $settings.commandURL)
                }
                Section("Help") {
                    Text("1. Join the dashcam's Wi-Fi in iOS Settings.\n2. Pick a preset or enter your camera's URLs (check the old app's manual or the camera's label for its IP).\n3. Open Live or Recordings.")
                        .font(.footnote)
                }
            }
            .navigationTitle("Settings")
        }
    }

    private func field(_ title: String, _ binding: Binding<String>) -> some View {
        VStack(alignment: .leading) {
            Text(title).font(.caption).foregroundStyle(.secondary)
            TextField(title, text: binding)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
                .keyboardType(.URL)
        }
    }
}
