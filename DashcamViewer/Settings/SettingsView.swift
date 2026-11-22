import SwiftUI
import T8Kit

struct SettingsView: View {
    @Environment(AppModel.self) private var app
    @State private var pending: PendingChange?
    @State private var busy: T8Setting?
    @State private var message: String?
    @State private var loading = false
    @AppStorage("alsoSaveToPhotos") private var alsoSaveToPhotos = true

    struct PendingChange: Identifiable {
        let setting: T8Setting
        let index: Int
        var id: String { setting.id }
    }

    var body: some View {
        @Bindable var app = app
        NavigationStack {
            Form {
                Section {
                    Picker("Connect to", selection: $app.target.useSimulator) {
                        Text("Dashcam (192.168.100.1)").tag(false)
                        Text("T8 simulator on a Mac").tag(true)
                    }
                    if app.target.useSimulator {
                        TextField("Mac's IP address", text: Binding(
                            get: { app.target.simulatorHost },
                            set: { app.target.simulatorHost = $0.trimmingCharacters(in: .whitespaces) }
                        ))
                            .keyboardType(.numbersAndPunctuation)
                            .autocorrectionDisabled()
                            .textInputAutocapitalization(.never)
                        TextField("Port", text: Binding(
                            get: { String(app.target.simulatorPort) },
                            set: { if let port = UInt16($0), (1...(UInt16.max - 2)).contains(port) { app.target.simulatorPort = port } }
                        ))
                        .keyboardType(.numberPad)
                    }
                    ConnectionBanner()
                        .listRowInsets(EdgeInsets())
                } header: {
                    Text("Connection")
                } footer: {
                    if app.target.useSimulator {
                        Text("Run `swift run t8sim` in the T8Kit folder on the Mac, with the iPhone on the same Wi-Fi.")
                    }
                }

                if let config = app.config {
                    Section("Camera") {
                        LabeledContent("Model", value: config.productName)
                        LabeledContent("Serial", value: config.serialNumber)
                        LabeledContent("Firmware", value: config.firmwareVersion)
                    }
                    Section {
                        ForEach(T8Setting.allCases) { setting in
                            row(setting, config: config)
                        }
                    } header: {
                        Text("Camera settings")
                    } footer: {
                        Text("Not available in this app: SD card format, SD card partition change (it formats the card) and factory reset. Use the camera's own buttons for those.")
                    }
                } else {
                    Section("Camera settings") {
                        Button {
                            Task { await load() }
                        } label: {
                            HStack {
                                Text("Load settings from the camera")
                                if loading { Spacer(); ProgressView() }
                            }
                        }
                    }
                }

                Section("Date and time") {
                    Button("Set the camera clock to this iPhone's time") {
                        Task { await syncTime() }
                    }
                }

                Section("Wi-Fi") {
                    NavigationLink("Change the camera's Wi-Fi password") {
                        WiFiPasswordView()
                    }
                }

                Section("Saving") {
                    Toggle("Also save to Photos", isOn: $alsoSaveToPhotos)
                }

                if let message {
                    Section {
                        Text(message).font(.footnote)
                    }
                }
            }
            .navigationTitle("Settings")
            .refreshable { await load() }
            .alert(pending?.setting.title ?? "", isPresented: Binding(get: { pending != nil }, set: { if !$0 { pending = nil } }), presenting: pending) { change in
                Button("Change", role: .destructive) { Task { await apply(change.setting, change.index) } }
                Button("Cancel", role: .cancel) {}
            } message: { change in
                Text((change.setting.warning ?? "") + " Are you sure you want to change it?")
            }
        }
    }

    @ViewBuilder
    private func row(_ setting: T8Setting, config: T8Config) -> some View {
        let options = setting.options(for: config)
        let current = setting.currentIndex(in: config)
        if options.isEmpty {
            LabeledContent(setting.title, value: "\(current)")
        } else if !setting.isChangeAllowed {
            LabeledContent(setting.title, value: options.indices.contains(current) ? options[current] : "\(current)")
        } else {
            Picker(setting.title, selection: Binding(
                get: { current },
                set: { index in
                    guard index != current else { return }
                    if setting.warning != nil {
                        pending = PendingChange(setting: setting, index: index)
                    } else {
                        Task { await apply(setting, index) }
                    }
                }
            )) {
                ForEach(options.indices, id: \.self) { index in
                    Text(options[index]).tag(index)
                }
            }
            .disabled(busy != nil)
        }
    }

    private func load() async {
        loading = true
        defer { loading = false }
        do {
            try await app.refreshConfig()
            message = nil
        } catch {
            message = AppModel.message(for: error)
        }
    }

    private func apply(_ setting: T8Setting, _ index: Int) async {
        busy = setting
        defer { busy = nil }
        do {
            let session = try await app.connectedSession()
            try await session.setSetting(setting, index: index)
            app.updateConfig(await session.lastConfig)
            message = "\(setting.title) changed."
            if setting == .videoQuality {
                message = "Video quality changed. The camera restarts; reconnect in a minute."
            }
        } catch {
            message = "\(setting.title): \(AppModel.message(for: error))"
        }
    }

    private func syncTime() async {
        do {
            let session = try await app.connectedSession()
            let time = T8DateTime(date: Date())
            try await session.setTime(time, requireReply: true)
            message = String(format: "Camera clock set to %04d-%02d-%02d %02d:%02d:%02d.", time.year, time.month, time.day, time.hour, time.minute, time.second)
        } catch {
            message = AppModel.message(for: error)
        }
    }
}

/// Changing the password drops every phone and needs the new password to rejoin the
/// camera's Wi-Fi, so it sits behind a warning and a confirmation.
struct WiFiPasswordView: View {
    @Environment(AppModel.self) private var app
    @State private var password = ""
    @State private var confirmation = ""
    @State private var confirming = false
    @State private var busy = false
    @State private var result: String?

    private var problem: String? {
        if password.isEmpty { return nil }
        do throws(T8WiFiPasswordError) {
            try T8WiFiPassword.validate(password)
        } catch {
            switch error {
            case .wrongLength: return "The password must be exactly 8 characters."
            case .containsSpace: return "The password cannot contain spaces."
            case .notASCII: return "Use only letters, digits and symbols from a standard keyboard."
            }
        }
        if !confirmation.isEmpty && confirmation != password { return "The two passwords do not match." }
        return nil
    }

    private var canSubmit: Bool {
        problem == nil && !password.isEmpty && password == confirmation && !busy
    }

    var body: some View {
        Form {
            Section {
                Label {
                    Text("After the change the camera restarts its Wi-Fi and disconnects every phone. To reconnect, join the LogOn T8 network again in iOS Settings › Wi-Fi with the new password. The old phone with LogonT also needs the new password. Write it down before you continue.")
                } icon: {
                    Image(systemName: "exclamationmark.triangle.fill").foregroundStyle(.orange)
                }
            }
            Section {
                SecureField("New password", text: $password)
                SecureField("Repeat the new password", text: $confirmation)
            } header: {
                Text("New password")
            } footer: {
                Text(problem ?? "Exactly 8 characters, no spaces. The factory default is 12345678.")
            }
            Section {
                Button(role: .destructive) {
                    confirming = true
                } label: {
                    HStack {
                        Text("Change Wi-Fi password")
                        if busy { Spacer(); ProgressView() }
                    }
                }
                .disabled(!canSubmit)
            }
            if let result {
                Section { Text(result) }
            }
        }
        .navigationTitle("Wi-Fi password")
        .alert("Change the camera's Wi-Fi password?", isPresented: $confirming) {
            Button("Change", role: .destructive) { Task { await change() } }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("The new password will be \(password). You will need it to reconnect.")
        }
    }

    private func change() async {
        busy = true
        defer { busy = false }
        do {
            let session = try await app.connectedSession()
            try await session.setWiFiPassword(password)
            result = "Wi-Fi password has been changed. Join the LogOn T8 network again with the new password."
            app.log.info("Wi-Fi password changed")
            // LogonT waits 10 s, then disconnects (ActivityWifiPassword.java handler).
            try? await Task.sleep(for: .seconds(10))
            await app.disconnect()
        } catch {
            result = "Failed to change the Wi-Fi password: \(AppModel.message(for: error))"
        }
    }
}
