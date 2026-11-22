/// One control-port frame:
/// `[head][cmd_hi][cmd_lo][len u32 LE][payload][0x03][xor of all previous bytes]`.
public struct T8Packet: Hashable, Sendable, CustomStringConvertible {
    public enum Head: UInt8, Sendable {
        /// Frames sent by the camera start with 0x01.
        case camera = 0x01
        /// Frames sent by the phone start with 0x02.
        case phone = 0x02
    }

    public static let trailer: UInt8 = 0x03
    public static let headerLength = 7
    /// LogonT drops any frame that declares a longer payload (MainFreeView.java:174).
    public static let maxPayloadLength = 100_000

    public var head: Head
    public var command: T8Command
    public var payload: [UInt8]

    public init(head: Head, command: T8Command, payload: [UInt8] = []) {
        self.head = head
        self.command = command
        self.payload = payload
    }

    public static func phone(_ command: T8Command, _ payload: [UInt8] = []) -> T8Packet {
        T8Packet(head: .phone, command: command, payload: payload)
    }

    public static func camera(_ command: T8Command, _ payload: [UInt8] = []) -> T8Packet {
        T8Packet(head: .camera, command: command, payload: payload)
    }

    /// Wire bytes, built exactly like `MainProtocol.send` (MainProtocol.java:169-206).
    public func encoded() -> [UInt8] {
        var out: [UInt8] = [head.rawValue, UInt8(command.rawValue >> 8), UInt8(command.rawValue & 0xFF)]
        out.reserveCapacity(payload.count + 9)
        out += T8Bytes.le(UInt32(payload.count))
        out += payload
        out.append(Self.trailer)
        out.append(T8Bytes.xor(out))
        return out
    }

    /// The payload read as a single little-endian `i32` (LogonT's `CNSResult` / `CNSSimpleIndex`).
    public var int32Value: Int32? {
        T8Bytes.i32LE(payload, at: 0)
    }

    public var description: String {
        "\(head == .camera ? "cam" : "phone") \(command) len=\(payload.count)"
    }
}

/// Frames that LogonT writes by hand and that do not follow the normal layout.
/// T8Kit sends these byte for byte because they are what the firmware is known to accept.
public enum T8LegacyFrame {
    /// `PreViewOff()` (MainFreeView.java:1123-1138): command 0x2002 with a length field of 4 but no payload.
    public static let previewOff: [UInt8] = {
        var bytes: [UInt8] = [0x02, 0x20, 0x02, 0x04, 0x00, 0x00, 0x00, 0x03]
        bytes.append(T8Bytes.xor(bytes))
        return bytes
    }()
}
