/// Camera settings LogonT exposes. Each sends the 0-based option index as a `CNSSimpleIndex`
/// and expects `command + 1` with a `CNSResult` (ActivitySelect.java:93-310).
/// Option labels come from LogonT's `res/values/arrays.xml`.
public enum T8Setting: String, CaseIterable, Sendable, Identifiable {
    case videoQuality
    case drivingImpactSensitivity
    case voiceVolume
    case voiceRecording
    case lcdBrightness
    case whiteBalance
    case contrast
    case exposure
    case screenOffTime
    case collisionSound
    case blockLowVoltage
    case parkingImpactSensitivity
    case parkingRecordingTime
    case timeZone
    case voiceGuide
    case led
    case sdPartition

    public var id: String { rawValue }

    public var command: T8Command {
        switch self {
        case .videoQuality: .setRecordQuality
        case .drivingImpactSensitivity: .setDrivingImpactSensitivity
        case .voiceVolume: .setVoiceVolume
        case .voiceRecording: .setMute
        case .lcdBrightness: .setLCDBrightness
        case .whiteBalance: .setWhiteBalance
        case .contrast: .setContrast
        case .exposure: .setExposure
        case .screenOffTime: .setScreenOffTime
        case .collisionSound: .setCrashAlarm
        case .blockLowVoltage: .setParkingLowPower
        case .parkingImpactSensitivity: .setParkImpactSensitivity
        case .parkingRecordingTime: .setParkingRecordingTime
        case .timeZone: .setTimeZone
        case .voiceGuide: .setVoiceGuide
        case .led: .setLED
        case .sdPartition: .setPartition
        }
    }

    public var replyCommand: T8Command { command.plusOne }

    public var configField: T8Config.Field {
        switch self {
        case .videoQuality: .recordQuality
        case .drivingImpactSensitivity: .drivingImpactSensitivity
        case .voiceVolume: .voiceVolume
        case .voiceRecording: .mute
        case .lcdBrightness: .lcdBrightness
        case .whiteBalance: .whiteBalance
        case .contrast: .contrast
        case .exposure: .exposure
        case .screenOffTime: .screenOffTime
        case .collisionSound: .crashAlarm
        case .blockLowVoltage: .parkingLowPower
        case .parkingImpactSensitivity: .parkImpactSensitivity
        case .parkingRecordingTime: .parkingRecordingTime
        case .timeZone: .timeZone
        case .voiceGuide: .voiceGuide
        case .led: .led
        case .sdPartition: .partition
        }
    }

    /// Names from LogonT's `setting_list` array.
    public var title: String {
        switch self {
        case .videoQuality: "Video Quality"
        case .drivingImpactSensitivity: "Driving mode impact sensitivity"
        case .voiceVolume: "Volume"
        case .voiceRecording: "Voice Record"
        case .lcdBrightness: "LCD Brightness"
        case .whiteBalance: "White Balance"
        case .contrast: "Contrast"
        case .exposure: "Exposure"
        case .screenOffTime: "Screen Off Time"
        case .collisionSound: "Collision Detection Sound"
        case .blockLowVoltage: "Block Low Voltage"
        case .parkingImpactSensitivity: "Parking mode event sensor"
        case .parkingRecordingTime: "Parking mode Recording Time"
        case .timeZone: "World Standard Time"
        case .voiceGuide: "Voice Guide"
        case .led: "LED"
        case .sdPartition: "SD Card Partition Change"
        }
    }

    /// Shown before sending; the camera's own warnings from LogonT's strings.xml.
    public var warning: String? {
        switch self {
        case .videoQuality: "If you change video resolution then DVR will restart."
        case .sdPartition: "If you resize SD card partitions then it will format SD card."
        default: nil
        }
    }

    /// The user decided not to include commands that format the SD card (PLAN.md), and a
    /// partition change formats it. The app shows this setting read-only.
    public var isChangeAllowed: Bool { self != .sdPartition }

    public func options(for config: T8Config?) -> [String] {
        let sensitivity = ["1 (High)", "2", "3", "4", "5", "6", "7", "8", "9", "10 (Low)"]
        switch self {
        case .videoQuality:
            switch config?.qualityVariant ?? .unknown {
            case .fh: return ["FHD+HD", "HD+HD"]
            case .ff: return ["FHD+FHD", "HD+HD"]
            case .hh: return ["HD+HD"]
            case .unknown: return []
            }
        case .drivingImpactSensitivity, .parkingImpactSensitivity: return sensitivity
        case .voiceVolume: return ["1", "2", "3", "4", "5"]
        case .voiceRecording: return ["Record", "Do not Record"]
        case .lcdBrightness: return ["1", "2", "3", "4", "5", "6", "7"]
        case .whiteBalance: return ["Automatic", "Sunny", "Blur", "Incandescent", "Fluorescent Lamp"]
        case .contrast: return ["0", "1", "2", "3", "4", "5", "6"]
        case .exposure: return ["-3", "-2", "-1", "0", "+1", "+2", "+3"]
        case .screenOffTime: return ["10Seconds", "20Seconds", "30Seconds", "None"]
        case .collisionSound, .led: return ["ON", "OFF"]
        case .blockLowVoltage: return ["11.5V(23.5V)", "11.7V(23.7V)", "11.9V(24.1V)", "12.1V(24.1V)", "12.3V(24.3V)"]
        case .parkingRecordingTime:
            let base = ["6Hours", "12Hours", "24Hours", "36Hours", "48Hours"]
            return (config?.isT8 ?? false) ? base + ["OFF"] : base
        case .timeZone: return (-11...12).map { $0 > 0 ? "+\($0)" : "\($0)" }
        case .voiceGuide: return ["Voice Guidance", "Not Guide"]
        case .sdPartition: return ["A-Type", "B-Type", "C-Type"]
        }
    }

    public func currentIndex(in config: T8Config) -> Int {
        Int(config[configField])
    }
}
