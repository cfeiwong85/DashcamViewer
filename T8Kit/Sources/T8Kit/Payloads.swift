import Foundation

/// Payload builders and readers for the structures in `MainProtocol.java`.
public enum T8Payload {
    /// `CNSSimpleIndex`: one little-endian i32.
    public static func index(_ value: Int32) -> [UInt8] {
        T8Bytes.le(value)
    }

    /// `IpCamStart()` payload (MainFreeView.java:911-933).
    public static let ipcamStart: [UInt8] = [1, 0, 0, 0, 0, 0, 0, 0]

    /// 8200 payload: `type u32, index u32, channel u32` (MainFileListView_blackbox2.java:691-719).
    public static func fileTransfer(folder: T8Folder, index: Int, channel: T8Channel) -> [UInt8] {
        T8Bytes.le(UInt32(folder.rawValue)) + T8Bytes.le(UInt32(truncatingIfNeeded: index)) + T8Bytes.le(UInt32(channel.rawValue))
    }
}

public enum T8Channel: Int, Sendable, CaseIterable, Hashable {
    case front = 0
    case rear = 1

    public var title: String { self == .front ? "Front" : "Rear" }
    /// LogonT appends this letter to the saved file name.
    public var fileSuffix: String { self == .front ? "A" : "B" }
    /// Front records at 30 fps, rear at 25 fps (MainFileFreeView.java:327-335, MainMP4ParSer.java:140-143).
    public var framesPerSecond: Int { self == .front ? 30 : 25 }
}

/// Reply to 8200: `{fileSize u32, duration u32}` where duration is a frame count (MainProtocol.java:225-251).
public struct T8FileStart: Hashable, Sendable {
    public var fileSize: Int32
    public var frameCount: Int32

    public init(fileSize: Int32, frameCount: Int32) {
        self.fileSize = fileSize
        self.frameCount = frameCount
    }

    public init?(payload: [UInt8]) {
        guard let size = T8Bytes.i32LE(payload, at: 0), let frames = T8Bytes.i32LE(payload, at: 4) else { return nil }
        self.init(fileSize: size, frameCount: frames)
    }

    public var payload: [UInt8] { T8Bytes.le(fileSize) + T8Bytes.le(frameCount) }

    public func duration(for channel: T8Channel) -> TimeInterval? {
        frameCount > 0 ? Double(frameCount) / Double(channel.framesPerSecond) : nil
    }
}

/// `CNSTime`: 9 little-endian i32 (sec, min, hour, mday, month 1-12, full year, wday, yday, isdst),
/// MainProtocol.java:304-369; LogonT fills only the first six (MainFreeView.java:1015-1035).
public struct T8DateTime: Hashable, Sendable {
    public var year: Int
    public var month: Int
    public var day: Int
    public var hour: Int
    public var minute: Int
    public var second: Int

    public init(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int) {
        self.year = year
        self.month = month
        self.day = day
        self.hour = hour
        self.minute = minute
        self.second = second
    }

    public init(date: Date, timeZone: TimeZone = .current) {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = timeZone
        let parts = calendar.dateComponents([.year, .month, .day, .hour, .minute, .second], from: date)
        self.init(
            year: parts.year ?? 2000, month: parts.month ?? 1, day: parts.day ?? 1,
            hour: parts.hour ?? 0, minute: parts.minute ?? 0, second: parts.second ?? 0
        )
    }

    public init?(payload: [UInt8]) {
        guard payload.count >= 24,
              let sec = T8Bytes.i32LE(payload, at: 0), let min = T8Bytes.i32LE(payload, at: 4),
              let hour = T8Bytes.i32LE(payload, at: 8), let day = T8Bytes.i32LE(payload, at: 12),
              let month = T8Bytes.i32LE(payload, at: 16), let year = T8Bytes.i32LE(payload, at: 20)
        else { return nil }
        self.init(year: Int(year), month: Int(month), day: Int(day), hour: Int(hour), minute: Int(min), second: Int(sec))
    }

    /// LogonT refuses to send anything outside these ranges (MainFreeView.java:1023).
    public var isAcceptedByLogonT: Bool {
        (2014...2037).contains(year) && (1...12).contains(month) && (1...31).contains(day)
            && (0...23).contains(hour) && (0...59).contains(minute) && (0...59).contains(second)
    }

    public var payload: [UInt8] {
        [second, minute, hour, day, month, year, 0, 0, 0].flatMap { T8Bytes.le(Int32(truncatingIfNeeded: $0)) }
    }
}

public enum T8WiFiPasswordError: Error, Equatable, Sendable {
    case wrongLength
    case containsSpace
    case notASCII
}

/// `CNSWiFiSetting`: ssid[64], oldPwd[64], newPwd[64]. LogonT fills only newPwd (ActivityWifiPassword.java:90-98).
public enum T8WiFiPassword {
    public static func validate(_ password: String) throws(T8WiFiPasswordError) {
        guard password.unicodeScalars.allSatisfy({ $0.isASCII && $0.value >= 0x21 && $0.value < 0x7F || $0 == " " }) else {
            throw .notASCII
        }
        guard !password.contains(" ") else { throw .containsSpace }
        guard password.utf8.count == 8 else { throw .wrongLength }
    }

    public static func payload(newPassword: String) throws(T8WiFiPasswordError) -> [UInt8] {
        try validate(newPassword)
        return [UInt8](repeating: 0, count: 128) + T8Bytes.fixedField(newPassword, size: 64)
    }

    /// The new password inside a 192-byte payload (used by the simulator).
    public static func newPassword(fromPayload payload: [UInt8]) -> String? {
        guard payload.count >= 192 else { return nil }
        return T8Bytes.cString(payload[128..<192])
    }
}
