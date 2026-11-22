/// A connected TCP byte stream. T8Kit uses `POSIXConnector` on Linux and macOS;
/// the iOS app supplies a Network.framework implementation.
public protocol T8Connection: AnyObject, Sendable {
    /// Writes all bytes or throws.
    func send(_ bytes: [UInt8]) async throws
    /// Returns between 1 and `maximumLength` bytes, or an empty array when the peer closed the stream.
    func receive(maximumLength: Int) async throws -> [UInt8]
    /// Closes the connection; a pending `receive` returns an empty array or throws.
    func close()
}

public protocol T8Connector: Sendable {
    func connect(host: String, port: UInt16, timeout: Duration) async throws -> any T8Connection
}

public enum T8TransportError: Error, Equatable, Sendable, CustomStringConvertible {
    case resolveFailed(String)
    case connectFailed(host: String, port: UInt16, reason: String)
    case connectTimedOut(host: String, port: UInt16)
    case closed
    case io(String)

    public var description: String {
        switch self {
        case .resolveFailed(let host): "Cannot resolve \(host)"
        case .connectFailed(let host, let port, let reason): "Cannot connect to \(host):\(port): \(reason)"
        case .connectTimedOut(let host, let port): "Timed out connecting to \(host):\(port)"
        case .closed: "Connection closed"
        case .io(let reason): "Network error: \(reason)"
        }
    }
}

/// Well-known addresses of the camera.
public enum T8Endpoint {
    public static let cameraHost = "192.168.100.1"
    public static let controlPort: UInt16 = 7777
    public static let videoPort: UInt16 = 7778
    public static let audioPort: UInt16 = 7779
}

/// Host and ports, overridable so the app and tests can talk to the simulator.
public struct T8Address: Hashable, Sendable {
    public var host: String
    public var controlPort: UInt16
    public var videoPort: UInt16
    public var audioPort: UInt16

    public init(
        host: String = T8Endpoint.cameraHost,
        controlPort: UInt16 = T8Endpoint.controlPort,
        videoPort: UInt16 = T8Endpoint.videoPort,
        audioPort: UInt16 = T8Endpoint.audioPort
    ) {
        self.host = host
        self.controlPort = controlPort
        self.videoPort = videoPort
        self.audioPort = audioPort
    }

    public static let camera = T8Address()

    /// The simulator listens on `base`, `base + 1`, `base + 2` (base is capped at 65533).
    public static func simulator(host: String = "127.0.0.1", basePort: UInt16) -> T8Address {
        let base = min(basePort, UInt16.max - 2)
        return T8Address(host: host, controlPort: base, videoPort: base + 1, audioPort: base + 2)
    }
}
