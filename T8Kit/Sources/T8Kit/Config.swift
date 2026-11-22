/// `CNSConfig`, the 256-byte reply to GET_CONFIG (41013). Offsets from CNSConfig.java:93-121.
public struct T8Config: Hashable, Sendable {
    public static let byteCount = 256
    /// The last field LogonT reads before the CRC is uAdas at offset 152.
    public static let minimumByteCount = 156

    public enum Field: Int, CaseIterable, Sendable {
        case recordQuality = 0
        case rearMirror = 4
        case drivingImpactSensitivity = 8
        case voiceVolume = 12
        case mute = 16
        case lcdBrightness = 20
        case whiteBalance = 24
        case contrast = 28
        case exposure = 32
        case screenOffTime = 36
        case timeZone = 40
        case crashAlarm = 44
        case parkingLowPower = 48
        case parkImpactSensitivity = 52
        case parkingRecordingTime = 56
        case partition = 60
        case voiceGuide = 64
        case velocity = 120
        case language = 140
        case bitrate = 144
        case led = 148
        case adas = 152
        case crc = 252
    }

    public enum QualityVariant: String, Sendable {
        case fh = "FH"
        case ff = "FF"
        case hh = "HH"
        case unknown = ""
    }

    /// Always 256 bytes (shorter replies are zero-padded).
    public private(set) var bytes: [UInt8]

    public init?(payload: [UInt8]) {
        guard payload.count >= Self.minimumByteCount else { return nil }
        var padded = Array(payload.prefix(Self.byteCount))
        padded += repeatElement(0, count: Self.byteCount - padded.count)
        bytes = padded
    }

    /// Builds a config (used by the simulator and tests).
    public init(productName: String, serial: String, firmwareVersion: String, wifiPassword: String, values: [Field: Int32] = [:]) {
        bytes = [UInt8](repeating: 0, count: Self.byteCount)
        replace(68, T8Bytes.fixedField(productName, size: 16))
        replace(84, T8Bytes.fixedField(serial, size: 16))
        replace(100, T8Bytes.fixedField(firmwareVersion, size: 20))
        replace(124, T8Bytes.fixedField(wifiPassword, size: 16))
        for (field, value) in values { self[field] = value }
    }

    private mutating func replace(_ offset: Int, _ data: [UInt8]) {
        bytes.replaceSubrange(offset..<(offset + data.count), with: data)
    }

    public subscript(field: Field) -> Int32 {
        get { T8Bytes.i32LE(bytes, at: field.rawValue) ?? 0 }
        set { replace(field.rawValue, T8Bytes.le(newValue)) }
    }

    public var productName: String { T8Bytes.cString(bytes[68..<84]) }
    public var serialNumber: String { T8Bytes.cString(bytes[84..<100]) }
    public var firmwareVersion: String { T8Bytes.cString(bytes[100..<120]) }
    public var wifiPassword: String { T8Bytes.cString(bytes[124..<140]) }

    /// `nMute != 0` means voice is not recorded (CNSConfig.java:89-91, MainFreeView.java:1363).
    public var isVoiceRecordingOn: Bool { self[.mute] == 0 }

    /// LogonT's `VER_CHECK == 1` test: product name contains lowercase "t8" (CNSConfig.java:288).
    public var isT8: Bool { productName.contains("t8") }

    /// LogonT's FHD_CHECK, from the firmware version string (CNSConfig.java:295-304).
    public var qualityVariant: QualityVariant {
        let version = firmwareVersion
        if version.contains("FH") { return .fh }
        if version.contains("FF") { return .ff }
        if version.contains("HH") { return .hh }
        return .unknown
    }
}
