import Foundation
import Testing
@testable import T8Kit

@Suite("Payloads")
struct PayloadTests {
    @Test("CNSTime: sec, min, hour, mday, month 1-12, full year, then three zeros (MainProtocol.java:339-369)")
    func time() {
        let time = T8DateTime(year: 2026, month: 11, day: 22, hour: 10, minute: 20, second: 30)
        #expect(time.payload == [
            0x1E, 0, 0, 0, 0x14, 0, 0, 0, 0x0A, 0, 0, 0, 0x16, 0, 0, 0, 0x0B, 0, 0, 0, 0xEA, 0x07, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        ])
        #expect(T8DateTime(payload: time.payload) == time)
        #expect(time.isAcceptedByLogonT)
        #expect(!T8DateTime(year: 2040, month: 1, day: 1, hour: 0, minute: 0, second: 0).isAcceptedByLogonT)
    }

    @Test("T8DateTime from a Date uses the given time zone")
    func timeFromDate() throws {
        let utc = try #require(TimeZone(identifier: "UTC"))
        let date = Date(timeIntervalSince1970: 1_795_000_000) // 2026-11-18 11:06:40 UTC
        let time = T8DateTime(date: date, timeZone: utc)
        #expect(time == T8DateTime(year: 2026, month: 11, day: 18, hour: 11, minute: 6, second: 40))
        let singapore = try #require(TimeZone(identifier: "Asia/Singapore"))
        #expect(T8DateTime(date: date, timeZone: singapore).hour == 19)
    }

    @Test("Wi-Fi password: 192 bytes, only the new-password field is filled")
    func wifi() throws {
        let payload = try T8WiFiPassword.payload(newPassword: "abcd1234")
        #expect(payload.count == 192)
        #expect(payload[0..<128].allSatisfy { $0 == 0 })
        #expect(Array(payload[128..<136]) == Array("abcd1234".utf8))
        #expect(payload[136..<192].allSatisfy { $0 == 0 })
        #expect(T8WiFiPassword.newPassword(fromPayload: payload) == "abcd1234")
    }

    @Test("Wi-Fi password rules match ActivityWifiPassword.checkPassword")
    func wifiRules() {
        #expect(throws: T8WiFiPasswordError.wrongLength) { try T8WiFiPassword.validate("1234567") }
        #expect(throws: T8WiFiPasswordError.wrongLength) { try T8WiFiPassword.validate("123456789") }
        #expect(throws: T8WiFiPasswordError.containsSpace) { try T8WiFiPassword.validate("1234 678") }
        #expect(throws: T8WiFiPasswordError.notASCII) { try T8WiFiPassword.validate("1234567é") }
        #expect(throws: Never.self) { try T8WiFiPassword.validate("12345678") }
    }

    @Test("8201 reply: file size then frame count")
    func fileStart() {
        let reply = T8FileStart(payload: T8Bytes.le(UInt32(1_000_000)) + T8Bytes.le(UInt32(1800)))
        #expect(reply == T8FileStart(fileSize: 1_000_000, frameCount: 1800))
        #expect(reply?.duration(for: .front) == 60)
        #expect(reply?.duration(for: .rear) == 72)
        #expect(T8FileStart(payload: [1, 2, 3]) == nil)
    }
}

@Suite("Config and settings")
struct ConfigTests {
    func sample() -> T8Config {
        T8Config(
            productName: "logon t8", serial: "SN0001", firmwareVersion: "T8_FH_1.02", wifiPassword: "12345678",
            values: [.recordQuality: 1, .mute: 1, .voiceVolume: 3, .timeZone: 19, .parkingRecordingTime: 5]
        )
    }

    @Test("Offsets match CNSConfig.setBytes (CNSConfig.java:93-121)")
    func offsets() throws {
        let config = sample()
        #expect(config.bytes.count == 256)
        #expect(T8Bytes.i32LE(config.bytes, at: 0) == 1)
        #expect(T8Bytes.i32LE(config.bytes, at: 12) == 3)
        #expect(T8Bytes.i32LE(config.bytes, at: 16) == 1)
        #expect(T8Bytes.i32LE(config.bytes, at: 40) == 19)
        #expect(Array(config.bytes[68..<76]) == Array("logon t8".utf8))
        #expect(Array(config.bytes[124..<132]) == Array("12345678".utf8))
        let parsed = try #require(T8Config(payload: config.bytes))
        #expect(parsed.productName == "logon t8")
        #expect(parsed.serialNumber == "SN0001")
        #expect(parsed.firmwareVersion == "T8_FH_1.02")
        #expect(parsed.wifiPassword == "12345678")
        #expect(parsed[.timeZone] == 19)
    }

    @Test("Derived flags")
    func flags() {
        let config = sample()
        #expect(!config.isVoiceRecordingOn)
        #expect(config.isT8)
        #expect(config.qualityVariant == .fh)
        #expect(T8Setting.videoQuality.options(for: config) == ["FHD+HD", "HD+HD"])
        #expect(T8Setting.parkingRecordingTime.options(for: config).last == "OFF")
        #expect(T8Setting.timeZone.options(for: config)[19] == "+8")
        #expect(T8Setting.timeZone.options(for: nil).count == 24)
    }

    @Test("Short config replies are rejected, long ones truncated")
    func lengths() {
        #expect(T8Config(payload: Array(repeating: 0, count: 100)) == nil)
        #expect(T8Config(payload: Array(repeating: 0, count: 156))?.bytes.count == 256)
        #expect(T8Config(payload: Array(repeating: 0, count: 300))?.bytes.count == 256)
    }

    @Test("Each setting replies on command + 1, as ActivitySelect.onReceive expects")
    func replies() {
        for setting in T8Setting.allCases {
            #expect(setting.replyCommand.rawValue == setting.command.rawValue + 1)
        }
        #expect(T8Setting.voiceRecording.command == .setMute)
        #expect(!T8Setting.sdPartition.isChangeAllowed)
        #expect(T8Setting.videoQuality.warning != nil)
    }
}

@Suite("File lists")
struct FileListTests {
    @Test("A 40998 page round-trips and follows MainFreeView.onReceive's layout")
    func page() throws {
        let page = T8FileListPage(totalPages: 2, currentPage: 0, entries: [
            T8FileListEntry(name: "20261122_101500_I.avi", hasRear: true),
            T8FileListEntry(name: "20261122_101600_I.avi", hasRear: false),
        ])
        let payload = page.payload
        #expect(payload.count == 8 + 64)
        #expect(Array(payload[8..<29]) == Array("20261122_101500_I.avi".utf8))
        #expect(payload[8 + 31] == 1)
        #expect(payload[8 + 32 + 31] == 0)
        // The state letter LogonT reads at payload offset 24 (MainFreeView.java:1320).
        #expect(payload[24] == UInt8(ascii: "I"))
        let parsed = try #require(T8FileListPage(payload: payload))
        #expect(parsed == page)
        #expect(!parsed.isLast)
    }

    @Test("Indexes count only names starting with 2, across pages")
    func assembler() {
        var assembler = T8FileListAssembler(folder: .event)
        assembler.add(T8FileListPage(totalPages: 2, currentPage: 0, entries: [
            T8FileListEntry(name: "20261122_101500_E.avi", hasRear: true),
            T8FileListEntry(name: "", hasRear: false),
        ]))
        #expect(!assembler.isComplete)
        assembler.add(T8FileListPage(totalPages: 2, currentPage: 1, entries: [
            T8FileListEntry(name: "20261122_101700_E.avi", hasRear: false),
        ]))
        #expect(assembler.isComplete)
        #expect(assembler.recordings.map(\.index) == [0, 1])
        #expect(assembler.recordings[1].name == "20261122_101700_E.avi")
        #expect(assembler.recordings[1].channels == [.front])
        #expect(assembler.skippedNames == [""])
    }

    @Test("Download names use the first 17 characters plus A or B")
    func names() throws {
        let recording = T8Recording(folder: .driving, index: 0, name: "20180511_052412_I.avi", hasRear: true)
        #expect(recording.fileStem(for: .front) == "20180511_052412_IA")
        #expect(recording.fileStem(for: .rear) == "20180511_052412_IB")
        let utc = try #require(TimeZone(identifier: "UTC"))
        #expect(recording.date(in: utc) == Date(timeIntervalSince1970: 1_526_016_252))
    }
}
