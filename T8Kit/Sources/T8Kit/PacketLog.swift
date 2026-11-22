import Foundation

/// One line of the diagnostics log the app can share after a car test.
public struct T8LogEntry: Sendable, Hashable, Identifiable {
    public enum Kind: String, Sendable {
        case sent = "TX"
        case received = "RX"
        case info = "--"
        case warning = "!!"
    }

    public let id: UInt64
    public let date: Date
    public let kind: Kind
    public let command: T8Command?
    public let bytes: [UInt8]
    public let note: String

    public init(id: UInt64, date: Date, kind: Kind, command: T8Command?, bytes: [UInt8], note: String) {
        self.id = id
        self.date = date
        self.kind = kind
        self.command = command
        self.bytes = bytes
        self.note = note
    }

    /// `12:00:01.234 TX NET_CONNECT_STATE(41022) [13] 02 A0 3E ... | note`
    public func formatted(hexLimit: Int = 64) -> String {
        let time = T8LogEntry.clockText(date)
        var line = "\(time) \(kind.rawValue)"
        if let command { line += " \(command)" }
        if !bytes.isEmpty { line += " [\(bytes.count)] \(T8Bytes.hex(bytes, limit: hexLimit))" }
        if !note.isEmpty { line += " | \(note)" }
        return line
    }

    /// Local wall-clock time as `HH:mm:ss.SSS`.
    static func clockText(_ date: Date) -> String {
        let calendar = Calendar.current
        let parts = calendar.dateComponents([.hour, .minute, .second, .nanosecond], from: date)
        let millis = (parts.nanosecond ?? 0) / 1_000_000
        return String(format: "%02d:%02d:%02d.%03d", parts.hour ?? 0, parts.minute ?? 0, parts.second ?? 0, millis)
    }
}

/// Thread-safe ring buffer of log entries. Share one instance between the session, the
/// data streams and the UI.
public final class T8PacketLog: @unchecked Sendable {
    private let lock = NSLock()
    private var entries: [T8LogEntry] = []
    private var nextID: UInt64 = 0
    private var observers: [UUID: @Sendable (T8LogEntry) -> Void] = [:]
    public let capacity: Int

    public init(capacity: Int = 5000) {
        self.capacity = capacity
    }

    public func add(_ kind: T8LogEntry.Kind, command: T8Command? = nil, bytes: [UInt8] = [], note: String = "") {
        lock.lock()
        let entry = T8LogEntry(id: nextID, date: Date(), kind: kind, command: command, bytes: bytes, note: note)
        nextID += 1
        entries.append(entry)
        if entries.count > capacity { entries.removeFirst(entries.count - capacity) }
        let callbacks = Array(observers.values)
        lock.unlock()
        for callback in callbacks { callback(entry) }
    }

    public func info(_ note: String) { add(.info, note: note) }
    public func warning(_ note: String) { add(.warning, note: note) }

    public var snapshot: [T8LogEntry] {
        lock.lock()
        defer { lock.unlock() }
        return entries
    }

    public func clear() {
        lock.lock()
        entries.removeAll()
        lock.unlock()
    }

    /// Registers a callback for new entries; returns a token for `removeObserver`.
    @discardableResult
    public func addObserver(_ callback: @escaping @Sendable (T8LogEntry) -> Void) -> UUID {
        let token = UUID()
        lock.lock()
        observers[token] = callback
        lock.unlock()
        return token
    }

    public func removeObserver(_ token: UUID) {
        lock.lock()
        observers[token] = nil
        lock.unlock()
    }

    /// Plain-text export for the Share button.
    public func exportText(header: String = "") -> String {
        let lines = snapshot.map { $0.formatted(hexLimit: 512) }
        return (header.isEmpty ? "" : header + "\n") + lines.joined(separator: "\n") + "\n"
    }
}
