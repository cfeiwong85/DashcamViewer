import Foundation
import Network
import T8Kit

/// Network.framework transport for T8Kit. `requireWiFi` pins camera traffic to the Wi-Fi
/// interface, because iOS may otherwise prefer cellular when the dashcam Wi-Fi has no internet.
struct NWConnector: T8Connector {
    var requireWiFi: Bool

    func connect(host: String, port: UInt16, timeout: Duration) async throws -> any T8Connection {
        guard let nwPort = NWEndpoint.Port(rawValue: port) else {
            throw T8TransportError.connectFailed(host: host, port: port, reason: "invalid port")
        }
        let tcp = NWProtocolTCP.Options()
        tcp.noDelay = true
        let parameters = NWParameters(tls: nil, tcp: tcp)
        if requireWiFi {
            parameters.requiredInterfaceType = .wifi
        }
        let connection = NWConnection(host: NWEndpoint.Host(host), port: nwPort, using: parameters)
        let transport = NWTransport(connection: connection, host: host, port: port)
        try await transport.start(timeout: timeout)
        return transport
    }
}

/// Resumes a continuation at most once, from any thread.
private final class ResumeOnce<T>: @unchecked Sendable {
    private let lock = NSLock()
    private var continuation: CheckedContinuation<T, Error>?

    init(_ continuation: CheckedContinuation<T, Error>) {
        self.continuation = continuation
    }

    @discardableResult
    func resume(_ result: Result<T, Error>) -> Bool {
        lock.lock()
        let pending = continuation
        continuation = nil
        lock.unlock()
        pending?.resume(with: result)
        return pending != nil
    }
}

final class NWTransport: T8Connection, @unchecked Sendable {
    private let connection: NWConnection
    private let host: String
    private let port: UInt16
    private let queue: DispatchQueue
    private let lock = NSLock()
    private var closed = false
    private var waitingReason: String?

    init(connection: NWConnection, host: String, port: UInt16) {
        self.connection = connection
        self.host = host
        self.port = port
        queue = DispatchQueue(label: "NWTransport.\(port)")
    }

    private var isClosed: Bool {
        lock.lock()
        defer { lock.unlock() }
        return closed
    }

    func start(timeout: Duration) async throws {
        let seconds = Double(timeout.components.seconds) + Double(timeout.components.attoseconds) / 1e18
        try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<Void, Error>) in
            let once = ResumeOnce(continuation)
            let host = self.host
            let port = self.port
            connection.stateUpdateHandler = { [weak self] state in
                switch state {
                case .ready:
                    once.resume(.success(()))
                case .failed(let error):
                    once.resume(.failure(T8TransportError.connectFailed(host: host, port: port, reason: "\(error)")))
                case .waiting(let error):
                    // No route yet (wrong Wi-Fi, or Local Network permission not granted). Keep the
                    // reason for the timeout message.
                    self?.lock.lock()
                    self?.waitingReason = "\(error)"
                    self?.lock.unlock()
                case .cancelled:
                    once.resume(.failure(T8TransportError.closed))
                default:
                    break
                }
            }
            connection.start(queue: queue)
            queue.asyncAfter(deadline: .now() + seconds) { [weak self] in
                guard let self else { return }
                self.lock.lock()
                let reason = self.waitingReason
                self.lock.unlock()
                let error: Error = reason.map { T8TransportError.connectFailed(host: host, port: port, reason: $0) }
                    ?? T8TransportError.connectTimedOut(host: host, port: port)
                if once.resume(.failure(error)) {
                    self.connection.cancel()
                }
            }
        }
    }

    func send(_ bytes: [UInt8]) async throws {
        guard !isClosed else { throw T8TransportError.closed }
        try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<Void, Error>) in
            connection.send(content: Data(bytes), completion: .contentProcessed { error in
                if let error {
                    continuation.resume(throwing: T8TransportError.io("\(error)"))
                } else {
                    continuation.resume()
                }
            })
        }
    }

    func receive(maximumLength: Int) async throws -> [UInt8] {
        guard !isClosed else { return [] }
        return try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<[UInt8], Error>) in
            connection.receive(minimumIncompleteLength: 1, maximumLength: maximumLength) { [weak self] data, _, isComplete, error in
                if let data, !data.isEmpty {
                    continuation.resume(returning: [UInt8](data))
                } else if isComplete || (self?.isClosed ?? true) {
                    continuation.resume(returning: [])
                } else if let error {
                    continuation.resume(throwing: T8TransportError.io("\(error)"))
                } else {
                    continuation.resume(returning: [])
                }
            }
        }
    }

    func close() {
        lock.lock()
        let wasClosed = closed
        closed = true
        lock.unlock()
        if !wasClosed {
            connection.cancel()
        }
    }
}
