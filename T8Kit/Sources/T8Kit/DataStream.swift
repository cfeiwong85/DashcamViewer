import Foundation

public enum T8DataStreamError: Error, Equatable, Sendable, CustomStringConvertible {
    case idleTimeout(port: UInt16, seconds: Int)

    public var description: String {
        switch self {
        case .idleTimeout(let port, let seconds): "No data on port \(port) for \(seconds) s."
        }
    }
}

/// Reads one data port (7778 video or 7779 audio) until the camera closes it.
public struct T8DataStream: Sendable {
    public struct Options: Sendable {
        public var connectTimeout: Duration = .seconds(5)
        /// The camera may open the port a moment after the control command; keep retrying this long.
        public var connectRetryWindow: Duration = .seconds(4)
        public var retryInterval: Duration = .milliseconds(250)
        /// Abort if no bytes arrive for this long.
        public var idleTimeout: Duration = .seconds(15)
        public var chunkSize = 64 * 1024

        public init() {}
    }

    public let connector: any T8Connector
    public let host: String
    public let port: UInt16
    public let options: Options
    public let log: T8PacketLog?

    public init(connector: any T8Connector, host: String, port: UInt16, options: Options = Options(), log: T8PacketLog? = nil) {
        self.connector = connector
        self.host = host
        self.port = port
        self.options = options
        self.log = log
    }

    /// Byte chunks until EOF. Cancelling the consumer closes the socket.
    public func chunks() -> AsyncThrowingStream<[UInt8], Error> {
        let (stream, continuation) = AsyncThrowingStream.makeStream(of: [UInt8].self)
        let task = Task {
            do {
                let connection = try await connectWithRetry()
                let watchdog = IdleWatchdog()
                let watchdogTask = Task {
                    while !Task.isCancelled {
                        try? await Task.sleep(for: .milliseconds(250))
                        if watchdog.idleTime > options.idleTimeout {
                            watchdog.markTimedOut()
                            connection.close()
                            return
                        }
                    }
                }
                defer {
                    watchdogTask.cancel()
                    connection.close()
                }
                var total = 0
                while !Task.isCancelled {
                    let bytes = try await connection.receive(maximumLength: options.chunkSize)
                    if bytes.isEmpty { break }
                    watchdog.touch()
                    total += bytes.count
                    continuation.yield(bytes)
                }
                if watchdog.timedOut {
                    let seconds = Int(options.idleTimeout.components.seconds)
                    log?.warning("Port \(port): idle for \(seconds) s after \(total) bytes")
                    throw T8DataStreamError.idleTimeout(port: port, seconds: seconds)
                }
                log?.info("Port \(port): end of stream after \(total) bytes")
                continuation.finish()
            } catch {
                continuation.finish(throwing: error)
            }
        }
        continuation.onTermination = { _ in task.cancel() }
        return stream
    }

    private func connectWithRetry() async throws -> any T8Connection {
        let clock = ContinuousClock()
        let deadline = clock.now + options.connectRetryWindow
        var attempt = 0
        while true {
            attempt += 1
            do {
                let connection = try await connector.connect(host: host, port: port, timeout: options.connectTimeout)
                log?.info("Port \(port): connected (attempt \(attempt))")
                return connection
            } catch {
                if clock.now >= deadline || Task.isCancelled {
                    log?.warning("Port \(port): \(error)")
                    throw error
                }
                try await Task.sleep(for: options.retryInterval)
            }
        }
    }
}

/// Last-activity clock shared between the reader and its watchdog.
final class IdleWatchdog: @unchecked Sendable {
    private let lock = NSLock()
    private var last = ContinuousClock.now
    private var didTimeOut = false

    func touch() {
        lock.lock()
        last = ContinuousClock.now
        lock.unlock()
    }

    var idleTime: Duration {
        lock.lock()
        defer { lock.unlock() }
        return ContinuousClock.now - last
    }

    func markTimedOut() {
        lock.lock()
        didTimeOut = true
        lock.unlock()
    }

    var timedOut: Bool {
        lock.lock()
        defer { lock.unlock() }
        return didTimeOut
    }
}
