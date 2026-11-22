import Foundation

public enum T8DataStreamError: Error, Equatable, Sendable, CustomStringConvertible {
    case idleTimeout(port: UInt16, seconds: Int)

    public var description: String {
        switch self {
        case .idleTimeout(let port, let seconds): "No data on port \(port) for \(seconds) s."
        }
    }
}

/// One data port (7778 video or 7779 audio), read until the camera closes it.
///
/// The sequence is pull-based: the socket is read only when the consumer asks for the next
/// chunk, so a slow consumer (paced playback) applies TCP backpressure instead of buffering
/// the whole recording in memory. Breaking out of the loop closes the socket.
public struct T8DataStream: AsyncSequence, Sendable {
    public typealias Element = [UInt8]

    public struct Options: Sendable {
        public var connectTimeout: Duration = .seconds(5)
        /// The camera may open the port a moment after the control command; keep retrying this long.
        public var connectRetryWindow: Duration = .seconds(4)
        public var retryInterval: Duration = .milliseconds(250)
        /// Fail if a single read waits this long for data.
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

    public func makeAsyncIterator() -> AsyncIterator {
        AsyncIterator(stream: self)
    }

    public struct AsyncIterator: AsyncIteratorProtocol {
        let stream: T8DataStream
        private let box = ConnectionBox()
        private var finished = false

        init(stream: T8DataStream) {
            self.stream = stream
        }

        public mutating func next() async throws -> [UInt8]? {
            guard !finished else { return nil }
            do {
                let connection: any T8Connection
                if let existing = box.connection {
                    connection = existing
                } else {
                    connection = try await stream.connectWithRetry()
                    box.connection = connection
                }
                let bytes = try await box.receive(from: connection, maximumLength: stream.options.chunkSize, idleTimeout: stream.options.idleTimeout)
                if bytes.isEmpty {
                    finished = true
                    box.close()
                    if box.timedOut {
                        let seconds = Int(stream.options.idleTimeout.components.seconds)
                        stream.log?.warning("Port \(stream.port): idle for \(seconds) s after \(box.total) bytes")
                        throw T8DataStreamError.idleTimeout(port: stream.port, seconds: seconds)
                    }
                    stream.log?.info("Port \(stream.port): end of stream after \(box.total) bytes")
                    return nil
                }
                box.total += bytes.count
                return bytes
            } catch {
                finished = true
                box.close()
                throw error
            }
        }
    }

    func connectWithRetry() async throws -> any T8Connection {
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

/// Owns the socket of one iterator; closing happens on end of stream, on error, on task
/// cancellation, or when the iterator is dropped (the consumer broke out of its loop).
final class ConnectionBox: @unchecked Sendable {
    private let lock = NSLock()
    private var storedConnection: (any T8Connection)?
    private var didTimeOut = false
    var total = 0

    var connection: (any T8Connection)? {
        get { lock.withLock { storedConnection } }
        set { lock.withLock { storedConnection = newValue } }
    }

    var timedOut: Bool { lock.withLock { didTimeOut } }

    func receive(from connection: any T8Connection, maximumLength: Int, idleTimeout: Duration) async throws -> [UInt8] {
        let watchdog = Task { [weak self] in
            try? await Task.sleep(for: idleTimeout)
            guard !Task.isCancelled, let self else { return }
            self.lock.withLock { self.didTimeOut = true }
            connection.close()
        }
        defer { watchdog.cancel() }
        return try await withTaskCancellationHandler {
            try await connection.receive(maximumLength: maximumLength)
        } onCancel: {
            connection.close()
        }
    }

    func close() {
        connection?.close()
    }

    deinit {
        storedConnection?.close()
    }
}

/// H.264 access units read from a data stream, pulled on demand.
public struct T8AccessUnitSequence: AsyncSequence, Sendable {
    public typealias Element = H264AccessUnit

    public let chunks: T8DataStream

    public init(chunks: T8DataStream) {
        self.chunks = chunks
    }

    public func makeAsyncIterator() -> AsyncIterator {
        AsyncIterator(chunks: chunks.makeAsyncIterator(), log: chunks.log)
    }

    public struct AsyncIterator: AsyncIteratorProtocol {
        var chunks: T8DataStream.AsyncIterator
        let log: T8PacketLog?
        var parser = AnnexBParser()
        var assembler = H264AccessUnitAssembler()
        var ready: [H264AccessUnit] = []
        var ended = false
        var reportedSkip = false

        public mutating func next() async throws -> H264AccessUnit? {
            while ready.isEmpty {
                if ended { return nil }
                if let chunk = try await chunks.next() {
                    ready += assembler.append(parser.append(chunk))
                    if !reportedSkip, parser.skippedLeadingBytes > 0 {
                        reportedSkip = true
                        log?.warning("Video: skipped \(parser.skippedLeadingBytes) bytes before the first H.264 start code")
                    }
                } else {
                    ended = true
                    ready += assembler.append(parser.finish()) + assembler.finish()
                }
            }
            return ready.removeFirst()
        }
    }
}
