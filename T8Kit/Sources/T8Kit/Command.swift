/// A T8 control-port command code (2 bytes, big-endian on the wire).
/// Values and names come from `MainProtocol.java`; see `research/T8_PROTOCOL.md`.
public struct T8Command: RawRepresentable, Hashable, Sendable, CustomStringConvertible {
    public let rawValue: UInt16

    public init(rawValue: UInt16) {
        self.rawValue = rawValue
    }

    public init(_ rawValue: UInt16) {
        self.rawValue = rawValue
    }

    /// The command LogonT expects as the reply to most `SET_*` commands.
    public var plusOne: T8Command { T8Command(rawValue &+ 1) }

    // Live view and session
    public static let freeviewStart = T8Command(511)          // 0x01FF
    public static let ipcamStart = T8Command(8193)            // 0x2001, reply is 8193 too
    public static let previewStop = T8Command(8194)           // 0x2002
    public static let fileTransferStart = T8Command(8200)     // 0x2008
    public static let fileTransferStartReply = T8Command(8201)
    public static let heartbeat = T8Command(40886)            // camera -> phone
    public static let heartbeatReply = T8Command(40887)
    public static let cameraStop = T8Command(40888)           // camera -> phone
    public static let cameraStopReply = T8Command(40889)
    public static let snapshot = T8Command(40904)
    public static let snapshotReply = T8Command(40905)
    public static let setMute = T8Command(40973)
    public static let setMuteReply = T8Command(40974)
    public static let setTime = T8Command(40977)
    public static let setTimeReply = T8Command(40978)
    public static let fileList = T8Command(40997)             // 0xA025
    public static let fileListReply = T8Command(40998)
    public static let setWiFiPassword = T8Command(41009)      // reply is 41009 in LogonT
    public static let getConfig = T8Command(41012)
    public static let getConfigReply = T8Command(41013)
    public static let changeCamera = T8Command(41014)         // 0xA036
    public static let changeCameraReply = T8Command(41015)
    public static let recordOnOff = T8Command(41018)          // 0xA03A
    public static let netConnectState = T8Command(41022)      // 0xA03E
    public static let netConnectStateReply = T8Command(41023)

    // Settings (reply = command + 1)
    public static let setDrivingImpactSensitivity = T8Command(40864)
    public static let setVoiceVolume = T8Command(40866)
    public static let setLCDBrightness = T8Command(40868)
    public static let setContrast = T8Command(40870)
    public static let setScreenOffTime = T8Command(40872)
    public static let setParkingLowPower = T8Command(40874)
    public static let setParkingRecordingTime = T8Command(40876)
    public static let setTimeZone = T8Command(40878)
    public static let setVoiceGuide = T8Command(40880)
    public static let setPartition = T8Command(40882)
    public static let setCrashAlarm = T8Command(40884)
    public static let setLED = T8Command(40892)
    public static let setRecordQuality = T8Command(40961)
    public static let setWhiteBalance = T8Command(40967)
    public static let setExposure = T8Command(40969)
    public static let setParkImpactSensitivity = T8Command(40985)

    // Destructive commands. T8Kit defines them for logging only and never sends them.
    public static let formatSDCard = T8Command(40991)
    public static let factoryReset = T8Command(41044)

    private static let names: [UInt16: String] = [
        511: "FREEVIEW_START", 8193: "IPCAM_START", 8194: "PREVIEW_STOP",
        8200: "FILE_TRANS_START", 8201: "FILE_TRANS_START_RESP",
        40864: "SET_DRIVING_IMPACT", 40865: "SET_DRIVING_IMPACT_RESP",
        40866: "SET_VOICE_VOLUME", 40867: "SET_VOICE_VOLUME_RESP",
        40868: "SET_LCD_BRIGHTNESS", 40869: "SET_LCD_BRIGHTNESS_RESP",
        40870: "SET_CONTRAST", 40871: "SET_CONTRAST_RESP",
        40872: "SET_SCREEN_OFF", 40873: "SET_SCREEN_OFF_RESP",
        40874: "SET_PARKING_LOW_POWER", 40875: "SET_PARKING_LOW_POWER_RESP",
        40876: "SET_PARKING_TIME", 40877: "SET_PARKING_TIME_RESP",
        40878: "SET_TIMEZONE", 40879: "SET_TIMEZONE_RESP",
        40880: "SET_VOICE_GUIDE", 40881: "SET_VOICE_GUIDE_RESP",
        40882: "SET_PARTITION", 40883: "SET_PARTITION_RESP",
        40884: "SET_CRASH_ALARM", 40885: "SET_CRASH_ALARM_RESP",
        40886: "HEARTBEAT", 40887: "HEARTBEAT_RESP",
        40888: "CAMERA_STOP", 40889: "CAMERA_STOP_RESP",
        40892: "SET_LED", 40893: "SET_LED_RESP",
        40904: "SNAPSHOT", 40905: "SNAPSHOT_RESP",
        40961: "SET_QUALITY", 40962: "SET_QUALITY_RESP",
        40967: "SET_WHITE_BALANCE", 40968: "SET_WHITE_BALANCE_RESP",
        40969: "SET_EXPOSURE", 40970: "SET_EXPOSURE_RESP",
        40973: "SET_MUTE", 40974: "SET_MUTE_RESP",
        40977: "SET_TIME", 40978: "SET_TIME_RESP",
        40985: "SET_PARK_IMPACT", 40986: "SET_PARK_IMPACT_RESP",
        40991: "FORMAT_SD", 40992: "FORMAT_SD_RESP",
        40997: "FILE_LIST", 40998: "FILE_LIST_RESP",
        41009: "SET_WIFI_PWD", 41010: "SET_WIFI_PWD_RESP?",
        41012: "GET_CONFIG", 41013: "GET_CONFIG_RESP",
        41014: "CHANGE_CAM", 41015: "CHANGE_CAM_RESP",
        41018: "RECORD_ON_OFF",
        41022: "NET_CONNECT_STATE", 41023: "NET_CONNECT_STATE_RESP",
        41044: "FACTORY_RESET", 41045: "FACTORY_RESET_RESP",
    ]

    public var name: String {
        Self.names[rawValue] ?? "CMD_\(rawValue)"
    }

    public var description: String {
        "\(name)(\(rawValue))"
    }
}
