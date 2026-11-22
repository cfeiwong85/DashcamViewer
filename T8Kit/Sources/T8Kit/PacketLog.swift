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
    /// Bytes of `bytes` that held the Wi-Fi password. They are stored as zeros and shown as `**`.
    public let hidden: Range<Int>?

    public init(id: UInt64, date: Date, kind: Kind, command: T8Command?, bytes: [UInt8], note: String, hidden: Range<Int>? = nil) {
        self.id = id
        self.date = date
        self.kind = kind
        self.command = command
        self.bytes = bytes
        self.note = note
        self.hidden = hidden
    }

    /// `12:00:01.234 TX NET_CONNECT_STATE(41022) [13] 02 A0 3E ... | note`
    public func formatted(hexLimit: Int = 64) -> String {
        let time = T8LogEntry.clockText(date)
        var line = "\(time) \(kind.rawValue)"
        if let command { line += " \(command)" }
        if !bytes.isEmpty { line += " [\(bytes.count)] \(hexText(limit: hexLimit))" }
        if !note.isEmpty { line += " | \(note)" }
        return line
    }

    private func hexText(limit: Int) -> String {
        guard let hidden else { return T8Bytes.hex(bytes, limit: limit) }
        let parts = bytes.prefix(limit).enumerated().map { offset, byte in
            hidden.contains(offset) ? "**" : String(format: "%02X", byte)
        }
        return parts.joined(separator: " ") + (bytes.count > limit ? " …" : "")
    }

    /// Local wall-clock time as `HH:mm:ss.SSS`.
    static func clockText(_ date: Date) -> String {
        let calendar = Calendar.current
        let parts = calendar.dateComponents([.hour, .minute, .second, .nanosecond], from: date)
        let millis = (parts.nanosecond ?? 0) / 1_000_000
        return String(format: "%02d:%02d:%02d.%03d", parts.hour ?? 0, parts.minute ?? 0, parts.second ?? 0, millis)
    }
}

/// Keeps the camera's Wi-Fi password out of the log, which is shared after car tests.
public enum T8LogRedaction {
    /// Payload bytes that hold a password: `CNSConfig`'s password field in GET_CONFIG_RESP
    /// (offset 124, 16 bytes), and `CNSWiFiSetting`'s `oldPwd` and `newPwd` in SET_WIFI_PWD
    /// (offsets 64-191). 41010 is the other reply T8Kit accepts for SET_WIFI_PWD
    /// (`research/T8_PROTOCOL.md`), in case it carries the same struct.
    static let payloadRanges: [T8Command: Range<Int>] = [
        .getConfigReply: 124..<140,
        .setWiFiPassword: 64..<192,
        T8Command(41010): 64..<192,
    ]

    /// The bytes of `frame` (a whole frame, header first) to hide, or nil if none.
    /// Clamped to the payload, so a short frame's trailer and checksum stay visible.
    public static func hiddenRange(command: T8Command?, frame: [UInt8]) -> Range<Int>? {
        guard let command, let range = payloadRanges[command] else { return nil }
        let payloadEnd = frame.count - 2
        let start = T8Packet.headerLength + range.lowerBound
        let end = min(T8Packet.headerLength + range.upperBound, payloadEnd)
        return start < end ? start..<end : nil
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

    /// Adds an entry. The Wi-Fi password in a frame (see `T8LogRedaction`) is replaced by
    /// zeros before it is stored, so neither the screen nor the export can show it.
    public func add(_ kind: T8LogEntry.Kind, command: T8Command? = nil, bytes: [UInt8] = [], note: String = "") {
        var bytes = bytes
        var note = note
        let hidden = T8LogRedaction.hiddenRange(command: command, frame: bytes)
        if let hidden {
            bytes.replaceSubrange(hidden, with: repeatElement(0, count: hidden.count))
            note = note.isEmpty ? "Wi-Fi password hidden" : note + "; Wi-Fi password hidden"
        }
        lock.lock()
        let entry = T8LogEntry(id: nextID, date: Date(), kind: kind, command: command, bytes: bytes, note: note, hidden: hidden)
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
