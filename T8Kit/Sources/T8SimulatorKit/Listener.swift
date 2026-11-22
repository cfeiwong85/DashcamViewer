import Foundation
import T8Kit
#if canImport(Glibc)
import Glibc
#elseif canImport(Darwin)
import Darwin
#endif

/// A listening TCP socket. `accept()` polls in short slices so `close()` stops it on every platform.
final class POSIXListener: @unchecked Sendable {
    private let fd: Int32
    let port: UInt16
    private let lock = NSLock()
    private var closed = false
    private let queue: DispatchQueue

    init(host: String, port: UInt16) throws {
        let address = try Self.address(host: host)
        #if canImport(Glibc)
        let fd = socket(AF_INET, Int32(SOCK_STREAM.rawValue), 0)
        #else
        let fd = socket(AF_INET, SOCK_STREAM, 0)
        #endif
        guard fd >= 0 else { throw T8TransportError.io("socket(): \(String(cString: strerror(errno)))") }
        var on: Int32 = 1
        setsockopt(fd, SOL_SOCKET, SO_REUSEADDR, &on, socklen_t(MemoryLayout<Int32>.size))
        var addr = sockaddr_in()
        #if canImport(Darwin)
        addr.sin_len = UInt8(MemoryLayout<sockaddr_in>.size)
        #endif
        addr.sin_family = sa_family_t(AF_INET)
        addr.sin_port = port.bigEndian
        addr.sin_addr = address
        let bound = withUnsafePointer(to: &addr) {
            $0.withMemoryRebound(to: sockaddr.self, capacity: 1) { bind(fd, $0, socklen_t(MemoryLayout<sockaddr_in>.size)) }
        }
        guard bound == 0, listen(fd, 8) == 0 else {
            let reason = String(cString: strerror(errno))
            _ = Self.systemClose(fd)
            throw T8TransportError.io("Cannot listen on \(host):\(port): \(reason)")
        }
        var actual = sockaddr_in()
        var length = socklen_t(MemoryLayout<sockaddr_in>.size)
        _ = withUnsafeMutablePointer(to: &actual) {
            $0.withMemoryRebound(to: sockaddr.self, capacity: 1) { getsockname(fd, $0, &length) }
        }
        self.fd = fd
        self.port = UInt16(bigEndian: actual.sin_port)
        queue = DispatchQueue(label: "t8sim.listener.\(self.port)")
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

    private static func address(host: String) throws -> in_addr {
        var address = in_addr()
        let name = host == "localhost" ? "127.0.0.1" : host
        guard inet_pton(AF_INET, name, &address) == 1 else { throw T8TransportError.resolveFailed(host) }
        return address
    }

    private var isClosed: Bool {
        lock.lock()
        defer { lock.unlock() }
        return closed
    }

    /// Waits for the next client; returns nil once the listener is closed.
    func accept() async -> POSIXConnection? {
        await withCheckedContinuation { (continuation: CheckedContinuation<POSIXConnection?, Never>) in
            queue.async {
                while !self.isClosed {
                    var pfd = pollfd(fd: self.fd, events: Int16(POLLIN), revents: 0)
                    let ready = poll(&pfd, 1, 100)
                    guard ready > 0 else { continue }
                    let client = systemAccept(self.fd)
                    if client >= 0 {
                        var on: Int32 = 1
                        setsockopt(client, Int32(IPPROTO_TCP), TCP_NODELAY, &on, socklen_t(MemoryLayout<Int32>.size))
                        #if canImport(Darwin)
                        setsockopt(client, SOL_SOCKET, SO_NOSIGPIPE, &on, socklen_t(MemoryLayout<Int32>.size))
                        #endif
                        continuation.resume(returning: POSIXConnection(fileDescriptor: client))
                        return
                    }
                }
                continuation.resume(returning: nil)
            }
        }
    }

    func close() {
        lock.lock()
        closed = true
        lock.unlock()
    }
}

private func systemAccept(_ fd: Int32) -> Int32 {
    #if canImport(Glibc)
    Glibc.accept(fd, nil, nil)
    #else
    Darwin.accept(fd, nil, nil)
    #endif
}
