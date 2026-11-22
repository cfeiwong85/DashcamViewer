import Foundation

/// Little-endian and big-endian helpers used by the T8 wire format.
/// All reads are bounds-checked and return nil instead of trapping.
public enum T8Bytes {
    public static func u32LE<C: RandomAccessCollection>(_ bytes: C, at offset: Int) -> UInt32?
    where C.Element == UInt8, C.Index == Int {
        let start = bytes.startIndex + offset
        guard offset >= 0, start + 4 <= bytes.endIndex else { return nil }
        return UInt32(bytes[start])
            | UInt32(bytes[start + 1]) << 8
            | UInt32(bytes[start + 2]) << 16
            | UInt32(bytes[start + 3]) << 24
    }

    public static func i32LE<C: RandomAccessCollection>(_ bytes: C, at offset: Int) -> Int32?
    where C.Element == UInt8, C.Index == Int {
        u32LE(bytes, at: offset).map { Int32(bitPattern: $0) }
    }

    public static func u16BE<C: RandomAccessCollection>(_ bytes: C, at offset: Int) -> UInt16?
    where C.Element == UInt8, C.Index == Int {
        let start = bytes.startIndex + offset
        guard offset >= 0, start + 2 <= bytes.endIndex else { return nil }
        return UInt16(bytes[start]) << 8 | UInt16(bytes[start + 1])
    }

    public static func le(_ value: UInt32) -> [UInt8] {
        [UInt8(value & 0xFF), UInt8((value >> 8) & 0xFF), UInt8((value >> 16) & 0xFF), UInt8((value >> 24) & 0xFF)]
    }

    public static func le(_ value: Int32) -> [UInt8] {
        le(UInt32(bitPattern: value))
    }

    /// XOR of every byte, the checksum LogonT appends after the 0x03 trailer.
    public static func xor<S: Sequence>(_ bytes: S) -> UInt8 where S.Element == UInt8 {
        bytes.reduce(0, ^)
    }

    /// Hex dump such as `02 A0 3E 04`.
    public static func hex<S: Sequence>(_ bytes: S, limit: Int = .max) -> String where S.Element == UInt8 {
        var parts: [String] = []
        var count = 0
        var truncated = false
        for byte in bytes {
            if count == limit {
                truncated = true
                break
            }
            parts.append(String(format: "%02X", byte))
            count += 1
        }
        return parts.joined(separator: " ") + (truncated ? " …" : "")
    }

    /// ASCII text from a fixed-size C string field: stops at the first NUL and drops trailing spaces.
    public static func cString<C: Collection>(_ bytes: C) -> String where C.Element == UInt8 {
        let prefix = bytes.prefix { $0 != 0 }
        let scalars = prefix.map { $0 >= 0x20 && $0 < 0x7F ? Character(UnicodeScalar($0)) : "?" }
        var text = String(scalars)
        while text.last == " " { text.removeLast() }
        return text
    }

    /// Fixed-size zero-padded ASCII field.
    public static func fixedField(_ text: String, size: Int) -> [UInt8] {
        var out = Array(text.utf8.prefix(size))
        out.append(contentsOf: repeatElement(0, count: size - out.count))
        return out
    }
}
