import Testing
@testable import T8Kit

/// The diagnostics log is shared after car tests, so it must not hold the camera's Wi-Fi password.
@Suite("Packet log")
struct PacketLogTests {
    static func hex(_ text: String) -> String { T8Bytes.hex(Array(text.utf8)) }

    @Test("GET_CONFIG_RESP: the password field (CNSConfig offset 124, 16 bytes) is stored as zeros and shown as **")
    func configReply() throws {
        let config = T8Config(productName: "logon t8", serial: "SN42", firmwareVersion: "FH", wifiPassword: "12345678")
        let frame = T8Packet.camera(.getConfigReply, config.bytes).encoded()
        let log = T8PacketLog()
        log.add(.received, command: .getConfigReply, bytes: frame)

        let entry = try #require(log.snapshot.first)
        #expect(entry.hidden == 131..<147)
        #expect(entry.bytes[131..<147].allSatisfy { $0 == 0 })
        #expect(entry.bytes.count == frame.count)
        #expect(Array(entry.bytes[..<131]) == Array(frame[..<131]))
        #expect(Array(entry.bytes[147...]) == Array(frame[147...]))
        #expect(entry.note == "Wi-Fi password hidden")

        let text = log.exportText()
        #expect(!text.contains(Self.hex("12345678")))
        #expect(text.contains(Array(repeating: "**", count: 16).joined(separator: " ")))
        #expect(text.contains(Self.hex("SN42")))
    }

    @Test("SET_WIFI_PWD: oldPwd and newPwd (CNSWiFiSetting offsets 64-191) are hidden; an existing note is kept")
    func passwordChange() throws {
        let frame = T8Packet.phone(.setWiFiPassword, try T8WiFiPassword.payload(newPassword: "newpass9")).encoded()
        let log = T8PacketLog()
        log.add(.sent, command: .setWiFiPassword, bytes: frame, note: "test")

        let entry = try #require(log.snapshot.first)
        #expect(entry.hidden == 71..<199)
        #expect(entry.bytes[71..<199].allSatisfy { $0 == 0 })
        #expect(entry.note == "test; Wi-Fi password hidden")
        #expect(!log.exportText().contains(Self.hex("newpass9")))
    }

    @Test("A 4-byte SET_WIFI_PWD result and other commands are logged unchanged")
    func untouched() throws {
        let log = T8PacketLog()
        let result = T8Packet.camera(.setWiFiPassword, T8Payload.index(0)).encoded()
        log.add(.received, command: .setWiFiPassword, bytes: result)
        let other = T8Packet.camera(.fileListReply, Array("12345678".utf8) + [UInt8](repeating: 0, count: 200)).encoded()
        log.add(.received, command: .fileListReply, bytes: other)
        log.add(.warning, bytes: other, note: "junk")

        let entries = log.snapshot
        #expect(entries.map(\.bytes) == [result, other, other])
        #expect(entries.allSatisfy { $0.hidden == nil })
        #expect(entries.map(\.note) == ["", "", "junk"])
    }

    @Test("A config reply cut short inside the password field hides only payload bytes")
    func shortConfigReply() throws {
        let frame = T8Packet.camera(.getConfigReply, Array(T8Config(productName: "p", serial: "s", firmwareVersion: "f", wifiPassword: "12345678").bytes[..<130])).encoded()
        let log = T8PacketLog()
        log.add(.received, command: .getConfigReply, bytes: frame)

        let entry = try #require(log.snapshot.first)
        #expect(entry.hidden == 131..<137)
        #expect(Array(entry.bytes.suffix(2)) == Array(frame.suffix(2)))
    }

    @Test("Hidden bytes beyond the on-screen limit leave the truncated line unchanged")
    func screenLimit() throws {
        let config = T8Config(productName: "p", serial: "s", firmwareVersion: "f", wifiPassword: "12345678")
        let frame = T8Packet.camera(.getConfigReply, config.bytes).encoded()
        let log = T8PacketLog()
        log.add(.received, command: .getConfigReply, bytes: frame)

        let line = try #require(log.snapshot.first).formatted(hexLimit: 48)
        #expect(line.contains("[\(frame.count)] \(T8Bytes.hex(frame, limit: 48))"))
        #expect(!line.contains("**"))
    }
}
