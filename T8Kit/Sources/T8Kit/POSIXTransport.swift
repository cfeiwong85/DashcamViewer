import Foundation
#if canImport(Glibc)
import Glibc
#elseif canImport(Darwin)
import Darwin
#endif

/// Blocking BSD sockets driven from dispatch queues. Works on Linux and Apple platforms,
/// which lets `swift test` and the simulator run without Network.framework.
public struct POSIXConnector: T8Connector {
    public init() {}

    public func connect(host: String, port: UInt16, timeout: Duration) async throws -> any T8Connection {
        let fd = try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<Int32, Error>) in
            DispatchQueue.global().async {
                continuation.resume(with: Result { try POSIXSocket.connect(host: host, port: port, timeout: timeout) })
            }
        }
        return POSIXConnection(fileDescriptor: fd)
    }
}

public enum POSIXSocket {
    static func errorText(_ code: Int32 = errno) -> String {
        String(cString: strerror(code))
    }

    static func ipv4Address(host: String) throws -> in_addr {
        let name = host == "localhost" ? "127.0.0.1" : host
        var address = in_addr()
        if inet_pton(AF_INET, name, &address) == 1 { return address }
        var hints = addrinfo()
        hints.ai_family = AF_INET
        #if canImport(Glibc)
        hints.ai_socktype = Int32(SOCK_STREAM.rawValue)
        #else
        hints.ai_socktype = SOCK_STREAM
        #endif
        var result: UnsafeMutablePointer<addrinfo>?
        guard getaddrinfo(name, nil, &hints, &result) == 0, let info = result, let sockaddr = info.pointee.ai_addr else {
            throw T8TransportError.resolveFailed(host)
        }
        defer { freeaddrinfo(result) }
        return sockaddr.withMemoryRebound(to: sockaddr_in.self, capacity: 1) { $0.pointee.sin_addr }
    }

    static func makeSocket() throws -> Int32 {
        #if canImport(Glibc)
        let fd = socket(AF_INET, Int32(SOCK_STREAM.rawValue), 0)
        #else
        let fd = socket(AF_INET, SOCK_STREAM, 0)
        #endif
        guard fd >= 0 else { throw T8TransportError.io("socket(): \(errorText())") }
        #if canImport(Darwin)
        var on: Int32 = 1
        setsockopt(fd, SOL_SOCKET, SO_NOSIGPIPE, &on, socklen_t(MemoryLayout<Int32>.size))
        #endif
        return fd
    }

    static func setNoDelay(_ fd: Int32) {
        var on: Int32 = 1
        setsockopt(fd, Int32(IPPROTO_TCP), TCP_NODELAY, &on, socklen_t(MemoryLayout<Int32>.size))
    }

    static func sockaddrIn(address: in_addr, port: UInt16) -> sockaddr_in {
        var addr = sockaddr_in()
        #if canImport(Darwin)
        addr.sin_len = UInt8(MemoryLayout<sockaddr_in>.size)
        #endif
        addr.sin_family = sa_family_t(AF_INET)
        addr.sin_port = port.bigEndian
        addr.sin_addr = address
        return addr
    }

    /// Non-blocking connect with a timeout, then switches the socket back to blocking mode.
    static func connect(host: String, port: UInt16, timeout: Duration) throws -> Int32 {
        let address = try ipv4Address(host: host)
        let fd = try makeSocket()
        var success = false
        defer { if !success { _ = close(fd) } }

        let flags = fcntl(fd, F_GETFL, 0)
        guard flags >= 0, fcntl(fd, F_SETFL, flags | O_NONBLOCK) == 0 else {
            throw T8TransportError.io("fcntl(): \(errorText())")
        }
        var addr = sockaddrIn(address: address, port: port)
        let result = withUnsafePointer(to: &addr) {
            $0.withMemoryRebound(to: sockaddr.self, capacity: 1) {
                #if canImport(Glibc)
                Glibc.connect(fd, $0, socklen_t(MemoryLayout<sockaddr_in>.size))
                #else
                Darwin.connect(fd, $0, socklen_t(MemoryLayout<sockaddr_in>.size))
                #endif
            }
        }
        if result != 0 {
            let code = errno
            guard code == EINPROGRESS else {
                throw T8TransportError.connectFailed(host: host, port: port, reason: errorText(code))
            }
            var pfd = pollfd(fd: fd, events: Int16(POLLOUT), revents: 0)
            let millis = Int32(clamping: timeout.components.seconds * 1000 + timeout.components.attoseconds / 1_000_000_000_000_000)
            let deadline = ContinuousClock.now + timeout
            var ready: Int32
            repeat {
                let remaining = deadline - ContinuousClock.now
                let wait = Int32(clamping: max(1, remaining.components.seconds * 1000 + remaining.components.attoseconds / 1_000_000_000_000_000))
                ready = poll(&pfd, 1, min(wait, max(millis, 1)))
            } while ready < 0 && errno == EINTR && ContinuousClock.now < deadline
            if ready == 0 || (ready < 0 && errno == EINTR) { throw T8TransportError.connectTimedOut(host: host, port: port) }
            if ready < 0 { throw T8TransportError.connectFailed(host: host, port: port, reason: errorText()) }
            var soError: Int32 = 0
            var length = socklen_t(MemoryLayout<Int32>.size)
            guard getsockopt(fd, SOL_SOCKET, SO_ERROR, &soError, &length) == 0 else {
                throw T8TransportError.connectFailed(host: host, port: port, reason: "getsockopt(): \(errorText())")
            }
            if soError != 0 {
                throw T8TransportError.connectFailed(host: host, port: port, reason: errorText(soError))
            }
        }
        guard fcntl(fd, F_SETFL, flags & ~O_NONBLOCK) == 0 else {
            throw T8TransportError.io("fcntl(): \(errorText())")
        }
        setNoDelay(fd)
        success = true
        return fd
    }
}

/// A connected socket. `close()` shuts the socket down (which wakes a blocked `recv`);
/// the descriptor itself is released in `deinit`, after any blocked call has returned,
/// so a reused descriptor number can never be read by a stale call.
public final class POSIXConnection: T8Connection, @unchecked Sendable {
    private let fd: Int32
    private let lock = NSLock()
    private var isClosed = false
    private let readQueue = DispatchQueue(label: "T8Kit.POSIXConnection.read")
    private let writeQueue = DispatchQueue(label: "T8Kit.POSIXConnection.write")

    public init(fileDescriptor: Int32) {
        fd = fileDescriptor
    }

    deinit {
        _ = Self.systemClose(fd)
    }

    private static func systemClose(_ fd: Int32) -> Int32 {
        #if canImport(Glibc)
        Glibc.close(fd)
        #else
        Darwin.close(fd)
        #endif
    }

    private var closed: Bool {
        lock.lock()
        defer { lock.unlock() }
        return isClosed
    }

    public func send(_ bytes: [UInt8]) async throws {
        try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<Void, Error>) in
            writeQueue.async {
                continuation.resume(with: Result { try self.sendAll(bytes) })
            }
        }
    }

    private func sendAll(_ bytes: [UInt8]) throws {
        guard !closed else { throw T8TransportError.closed }
        var offset = 0
        while offset < bytes.count {
            let written = bytes[offset...].withUnsafeBytes { buffer -> Int in
                #if canImport(Glibc)
                Glibc.send(fd, buffer.baseAddress, buffer.count, Int32(MSG_NOSIGNAL))
                #else
                Darwin.send(fd, buffer.baseAddress, buffer.count, 0)
                #endif
            }
            if written < 0 {
                if errno == EINTR { continue }
                throw closed ? T8TransportError.closed : T8TransportError.io("send(): \(POSIXSocket.errorText())")
            }
            offset += written
        }
    }

    public func receive(maximumLength: Int) async throws -> [UInt8] {
        try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<[UInt8], Error>) in
            readQueue.async {
                continuation.resume(with: Result { try self.receiveNow(maximumLength: maximumLength) })
            }
        }
    }

    private func receiveNow(maximumLength: Int) throws -> [UInt8] {
        guard !closed else { return [] }
        var buffer = [UInt8](repeating: 0, count: max(1, maximumLength))
        while true {
            let count = buffer.withUnsafeMutableBytes { recv(fd, $0.baseAddress, $0.count, 0) }
            if count >= 0 { return Array(buffer.prefix(count)) }
            if errno == EINTR { continue }
            if closed { return [] }
            throw T8TransportError.io("recv(): \(POSIXSocket.errorText())")
        }
    }

    public func close() {
        lock.lock()
        let wasClosed = isClosed
        isClosed = true
        lock.unlock()
        if !wasClosed {
            _ = shutdown(fd, Int32(SHUT_RDWR))
        }
    }
}
