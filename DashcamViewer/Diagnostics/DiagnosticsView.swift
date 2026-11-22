import SwiftUI
import T8Kit
import UIKit
import UniformTypeIdentifiers

/// The diagnostics log as a .txt file, built only when the user shares it.
struct LogExport: Transferable {
    let log: T8PacketLog
    let header: String

    static var transferRepresentation: some TransferRepresentation {
        FileRepresentation(exportedContentType: .plainText) { export in
            let stamp = Int(Date().timeIntervalSince1970)
            let unique = UUID().uuidString.prefix(8)
            let url = FileManager.default.temporaryDirectory.appendingPathComponent("DashcamViewer-log-\(stamp)-\(unique).txt")
            try export.log.exportText(header: export.header).write(to: url, atomically: true, encoding: .utf8)
            return SentTransferredFile(url)
        }
    }
}

enum NetworkInfo {
    /// IPv4 address of the Wi-Fi interface (en0), if any.
    static func wifiAddress() -> String? {
        var addresses: UnsafeMutablePointer<ifaddrs>?
        guard getifaddrs(&addresses) == 0, let first = addresses else { return nil }
        defer { freeifaddrs(addresses) }
        var pointer: UnsafeMutablePointer<ifaddrs>? = first
        while let current = pointer {
            let interface = current.pointee
            if let address = interface.ifa_addr, address.pointee.sa_family == UInt8(AF_INET),
               String(cString: interface.ifa_name) == "en0" {
                var host = [CChar](repeating: 0, count: Int(NI_MAXHOST))
                if getnameinfo(address, socklen_t(address.pointee.sa_len), &host, socklen_t(host.count), nil, 0, NI_NUMERICHOST) == 0 {
                    return String(cString: host)
                }
            }
            pointer = interface.ifa_next
        }
        return nil
    }
}

struct DiagnosticsView: View {
    @Environment(AppModel.self) private var app
    @State private var entries: [T8LogEntry] = []
    @State private var wifiAddress: String?
    @State private var portCheck: Bool?
    @State private var portDetail = ""
    @State private var checking = false

    var body: some View {
        NavigationStack {
            List {
                Section {
                    check("iPhone is on the dashcam Wi-Fi",
                          ok: app.target.useSimulator ? nil : wifiAddress?.hasPrefix("192.168.100."),
                          detail: wifiAddress.map { "Wi-Fi address \($0)" } ?? "No Wi-Fi address")
                    check("Camera answers on port \(app.target.address.controlPort)", ok: portCheck, detail: portDetail)
                    Button(checking ? "Checking…" : "Test the connection") { Task { await testPort() } }
                        .disabled(checking)
                    LabeledContent("Session", value: sessionText)
                    LabeledContent("Heartbeats", value: heartbeatText)
                } header: {
                    Text("Checklist")
                } footer: {
                    Text("If iOS says the dashcam Wi-Fi has no internet, choose to stay connected. Dashcam Viewer also needs Local Network access: Settings › Privacy & Security › Local Network.")
                }

                Section {
                    ShareLink(item: LogExport(log: app.log, header: header), preview: SharePreview("Dashcam Viewer diagnostics log")) {
                        Label("Share the diagnostics log", systemImage: "square.and.arrow.up")
                    }
                    Button("Clear the log", role: .destructive) {
                        app.log.clear()
                        entries = []
                    }
                } footer: {
                    Text("After a car test, share this log so the protocol can be fixed. It contains every packet sent and received, in hex.")
                }

                Section("Packet log (newest at the bottom)") {
                    ForEach(entries.suffix(300)) { entry in
                        Text(entry.formatted(hexLimit: 48))
                            .font(.system(.caption2, design: .monospaced))
                            .foregroundStyle(color(for: entry.kind))
                            .textSelection(.enabled)
                    }
                }
            }
            .navigationTitle("Diagnostics")
            .task {
                while !Task.isCancelled {
                    entries = app.log.snapshot
                    wifiAddress = NetworkInfo.wifiAddress()
                    try? await Task.sleep(for: .milliseconds(700))
                }
            }
        }
    }

    private var header: String {
        let info = Bundle.main.infoDictionary
        let version = info?["CFBundleShortVersionString"] as? String ?? "?"
        let build = info?["CFBundleVersion"] as? String ?? "?"
        return """
        Dashcam Viewer \(version) (\(build)) on iOS \(UIDevice.current.systemVersion)
        Target: \(app.target.title)
        Camera: \(app.config.map { "\($0.productName) / \($0.firmwareVersion)" } ?? "config not read")
        Wi-Fi address: \(NetworkInfo.wifiAddress() ?? "none")
        Exported: \(Date().formatted(date: .numeric, time: .standard))
        """
    }

    private var sessionText: String {
        switch app.state {
        case .connected: "Connected"
        case .connecting: "Connecting"
        case .disconnected: "Not connected"
        case .failed(let reason): "Failed: \(reason)"
        }
    }

    private var heartbeatText: String {
        guard let last = app.lastHeartbeat else { return "\(app.heartbeats) received" }
        return "\(app.heartbeats) received, last \(last.formatted(date: .omitted, time: .standard))"
    }

    private func check(_ title: String, ok: Bool?, detail: String) -> some View {
        HStack(alignment: .top) {
            Image(systemName: ok == nil ? "questionmark.circle" : (ok == true ? "checkmark.circle.fill" : "xmark.circle.fill"))
                .foregroundStyle(ok == nil ? Color.secondary : (ok == true ? Color.green : Color.red))
            VStack(alignment: .leading) {
                Text(title)
                if !detail.isEmpty {
                    Text(detail).font(.caption).foregroundStyle(.secondary)
                }
            }
        }
    }

    private func color(for kind: T8LogEntry.Kind) -> Color {
        switch kind {
        case .sent: .blue
        case .received: .primary
        case .info: .secondary
        case .warning: .orange
        }
    }

    /// A bare TCP connect to the control port. Skipped while a session is open, because the
    /// camera accepts one phone at a time.
    private func testPort() async {
        checking = true
        defer { checking = false }
        switch app.state {
        case .connected:
            portCheck = true
            portDetail = "A session is open, so the port works."
            return
        case .connecting:
            portDetail = "A connection attempt is running; try again in a moment."
            return
        case .disconnected, .failed:
            break
        }
        let address = app.target.address
        do {
            let connection = try await app.connector.connect(host: address.host, port: address.controlPort, timeout: .seconds(3))
            connection.close()
            portCheck = true
            portDetail = "\(address.host):\(address.controlPort) accepted a connection."
            app.log.info("Port test: \(address.host):\(address.controlPort) reachable")
        } catch {
            portCheck = false
            portDetail = AppModel.message(for: error)
            app.log.warning("Port test failed: \(error)")
        }
    }
}
