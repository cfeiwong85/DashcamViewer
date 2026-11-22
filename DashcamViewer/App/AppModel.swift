import Foundation
import Observation
import T8Kit

/// Where the app connects: the real camera, or the T8 simulator on a Mac (for testing at home).
struct ConnectionTarget: Codable, Equatable {
    var useSimulator = false
    var simulatorHost = "192.168.1.10"
    var simulatorPort: UInt16 = T8Endpoint.controlPort

    var address: T8Address {
        useSimulator ? .simulator(host: simulatorHost, basePort: simulatorPort) : .camera
    }

    var title: String {
        useSimulator ? "Simulator \(simulatorHost):\(simulatorPort)" : "Camera \(T8Endpoint.cameraHost)"
    }
}

/// Which feature currently owns the camera's data ports. The T8 serves one stream at a time.
enum MediaActivity: Equatable {
    case idle
    case live
    case playback
    case download
}

@MainActor
@Observable
final class AppModel {
    enum ConnectionState: Equatable {
        case disconnected
        case connecting
        case connected
        case failed(String)

        var isConnected: Bool { self == .connected }
    }

    let log = T8PacketLog()
    let library = SavedLibrary()
    private(set) var state: ConnectionState = .disconnected
    private(set) var session: T8ControlSession?
    private(set) var config: T8Config?
    private(set) var lastHeartbeat: Date?
    private(set) var heartbeats = 0
    var activity: MediaActivity = .idle
    /// Set when the camera stops the session or drops the connection, so screens can show why.
    var notice: String?

    var target: ConnectionTarget {
        didSet {
            if let data = try? JSONEncoder().encode(target) {
                UserDefaults.standard.set(data, forKey: Self.targetKey)
            }
        }
    }

    private static let targetKey = "connectionTarget"
    private var eventsTask: Task<Void, Never>?
    private var connectTask: Task<T8ControlSession, Error>?

    init() {
        if let data = UserDefaults.standard.data(forKey: Self.targetKey),
           let saved = try? JSONDecoder().decode(ConnectionTarget.self, from: data) {
            target = saved
        } else {
            target = ConnectionTarget()
        }
        log.info("App started; target \(target.title)")
    }

    var connector: any T8Connector {
        NWConnector(requireWiFi: !target.useSimulator)
    }

    var media: T8MediaClient {
        T8MediaClient(connector: connector, address: target.address, log: log)
    }

    /// Returns the connected session, connecting first if needed. Concurrent callers share one attempt.
    func connectedSession() async throws -> T8ControlSession {
        if let session, state.isConnected { return session }
        if let connectTask { return try await connectTask.value }
        let task = Task { try await self.openSession() }
        connectTask = task
        defer { connectTask = nil }
        return try await task.value
    }

    private func openSession() async throws -> T8ControlSession {
        state = .connecting
        notice = nil
        var options = T8ControlSession.Options(address: target.address)
        options.connectTimeout = .seconds(3)
        let session = T8ControlSession(connector: connector, options: options, log: log)
        do {
            try await session.connect()
        } catch {
            state = .failed(Self.message(for: error))
            throw error
        }
        self.session = session
        state = .connected
        observe(session)
        do {
            config = try await session.readConfig()
        } catch {
            log.warning("Could not read the camera config: \(error)")
        }
        return session
    }

    private func observe(_ session: T8ControlSession) {
        eventsTask?.cancel()
        eventsTask = Task { [weak self] in
            for await event in session.events {
                guard let self else { return }
                switch event {
                case .connected, .unsolicited:
                    break
                case .heartbeat:
                    self.lastHeartbeat = Date()
                    self.heartbeats += 1
                case .cameraStopped:
                    self.notice = "The camera stopped the session (DVR is stopped)."
                    await self.disconnect()
                case .disconnected(let reason):
                    if self.session === session {
                        self.session = nil
                        self.state = .disconnected
                        self.activity = .idle
                        if reason != "disconnected by app" {
                            self.notice = "Connection lost: \(reason)"
                        }
                    }
                }
            }
        }
    }

    func disconnect() async {
        guard let session else { return }
        self.session = nil
        state = .disconnected
        activity = .idle
        await session.disconnect()
    }

    func refreshConfig() async throws {
        let session = try await connectedSession()
        config = try await session.readConfig()
    }

    func updateConfig(_ config: T8Config?) {
        self.config = config
    }

    /// Plain-language text for errors shown in the UI.
    static func message(for error: Error) -> String {
        switch error {
        case let error as T8SessionError:
            return error.description
        case let error as T8TransportError:
            switch error {
            case .connectFailed, .connectTimedOut:
                return "Cannot reach the camera. Check that this iPhone is on the dashcam's Wi-Fi (LogOn T8 …) and that Local Network access is allowed in Settings › Privacy › Local Network. (\(error.description))"
            default:
                return error.description
            }
        case let error as T8DataStreamError:
            return error.description
        default:
            return error.localizedDescription
        }
    }
}
