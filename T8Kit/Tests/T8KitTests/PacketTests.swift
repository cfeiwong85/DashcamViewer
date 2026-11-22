import Testing
@testable import T8Kit

/// Byte vectors are LogonT's hand-built frames, computed from the cited Java lines.
@Suite("Framing")
struct FramingTests {
    @Test("NET_CONNECT_STATE(1) equals WiFiConnect() (MainFreeView.java:880-909)")
    func netConnect() {
        #expect(T8Packet.phone(.netConnectState, T8Payload.index(1)).encoded() ==
            [0x02, 0xA0, 0x3E, 0x04, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x03, 0x9A])
    }

    @Test("IPCAM_START equals IpCamStart() (MainFreeView.java:911-943)")
    func ipcamStart() {
        #expect(T8Packet.phone(.ipcamStart, T8Payload.ipcamStart).encoded() ==
            [0x02, 0x20, 0x01, 0x08, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x29])
    }

    @Test("FREEVIEW_START equals freeviewcon() (MainFreeView.java:945-958)")
    func freeview() {
        #expect(T8Packet.phone(.freeviewStart).encoded() == [0x02, 0x01, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x03, 0xFF])
    }

    @Test("CHANGE_CAM equals CamChange() (MainFreeView.java:985-1003)")
    func changeCamera() {
        #expect(T8Packet.phone(.changeCamera, T8Payload.index(0)).encoded() ==
            [0x02, 0xA0, 0x36, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x93])
    }

    @Test("RECORD_ON_OFF(0) equals RecordOnOff() (MainFreeView.java:1150-1169)")
    func recordOff() {
        #expect(T8Packet.phone(.recordOnOff, T8Payload.index(0)).encoded() ==
            [0x02, 0xA0, 0x3A, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x9F])
    }

    @Test("GET_CONFIG with no payload, as MainProtocol.send(41012, null, 0) builds it")
    func getConfig() {
        #expect(T8Packet.phone(.getConfig).encoded() == [0x02, 0xA0, 0x34, 0x00, 0x00, 0x00, 0x00, 0x03, 0x95])
    }

    @Test("PreViewOff legacy frame (MainFreeView.java:1123-1138)")
    func previewOff() {
        #expect(T8LegacyFrame.previewOff == [0x02, 0x20, 0x02, 0x04, 0x00, 0x00, 0x00, 0x03, 0x27])
    }

    @Test("FILE_TRANS_START equals Filedown(5, 1) in the driving list (MainFileListView_blackbox2.java:691-719)")
    func fileDownload() {
        let recording = T8Recording(folder: .driving, index: 5, name: "20180511_052412_I.avi", hasRear: true)
        let packet = T8Packet.phone(.fileTransferStart, T8Payload.fileTransfer(folder: recording.folder, index: recording.index, channel: .rear))
        #expect(packet.encoded() == [
            0x02, 0x20, 0x08, 0x0C, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x05, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x03, 0x21,
        ])
    }

    @Test("FILE_TRANS_START equals Fileview(258, \"E\", 0) (MainFileFreeView.java:604-656)")
    func filePlayback() {
        let packet = T8Packet.phone(.fileTransferStart, T8Payload.fileTransfer(folder: .event, index: 258, channel: .front))
        #expect(packet.encoded() == [
            0x02, 0x20, 0x08, 0x0C, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00,
            0x02, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x27,
        ])
    }

    @Test("Camera frames start with 0x01 and carry the same checksum rule")
    func cameraFrame() {
        let bytes = T8Packet.camera(.netConnectStateReply, T8Payload.index(-1)).encoded()
        #expect(bytes.first == 0x01)
        #expect(bytes[bytes.count - 2] == 0x03)
        #expect(bytes.last == T8Bytes.xor(bytes.dropLast()))
    }

    @Test("Command hex values")
    func commandValues() {
        #expect(T8Command.changeCamera.rawValue == 0xA036)
        #expect(T8Command.changeCameraReply.rawValue == 0xA037)
        #expect(T8Command.fileList.rawValue == 0xA025)
        #expect(T8Command.netConnectState.rawValue == 0xA03E)
        #expect(T8Command.recordOnOff.rawValue == 0xA03A)
        #expect(T8Command.heartbeat.rawValue == 0x9FB6)
        #expect(T8Command.ipcamStart.rawValue == 0x2001)
        #expect(T8Command.freeviewStart.rawValue == 0x01FF)
    }
}

@Suite("Parser")
struct ParserTests {
    func packets(_ events: [T8PacketParser.Event]) -> [T8Packet] {
        events.compactMap { if case .packet(let p, _) = $0 { p } else { nil } }
    }

    @Test("Byte-by-byte delivery yields the same packets")
    func byteByByte() {
        let stream = T8Packet.camera(.netConnectStateReply, T8Payload.index(0)).encoded()
            + T8Packet.camera(.heartbeat).encoded()
            + T8Packet.camera(.getConfigReply, Array(repeating: 7, count: 256)).encoded()
        var parser = T8PacketParser(expectedHead: .camera)
        var events: [T8PacketParser.Event] = []
        for byte in stream { events += parser.append([byte]) }
        let got = packets(events)
        #expect(got.map(\.command) == [.netConnectStateReply, .heartbeat, .getConfigReply])
        #expect(got[0].int32Value == 0)
        #expect(got[1].payload.isEmpty)
        #expect(got[2].payload.count == 256)
        #expect(parser.bufferedByteCount == 0)
    }

    @Test("Several packets in one chunk")
    func oneChunk() {
        var parser = T8PacketParser(expectedHead: .camera)
        let stream = (0..<5).flatMap { T8Packet.camera(.setMuteReply, T8Payload.index(Int32($0))).encoded() }
        let got = packets(parser.append(stream))
        #expect(got.compactMap(\.int32Value) == [0, 1, 2, 3, 4])
    }

    @Test("Garbage before a frame is skipped and reported")
    func resync() {
        var parser = T8PacketParser(expectedHead: .camera)
        let junk: [UInt8] = [0x55, 0x02, 0x99]
        let events = parser.append(junk + T8Packet.camera(.snapshotReply, T8Payload.index(0)).encoded())
        #expect(events.first == .discarded(junk))
        #expect(packets(events).map(\.command) == [.snapshotReply])
    }

    @Test("A head byte with an impossible length is skipped")
    func oversize() {
        var parser = T8PacketParser(expectedHead: .camera)
        let bogus: [UInt8] = [0x01, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0x7F]
        let good = T8Packet.camera(.heartbeat).encoded()
        let got = packets(parser.append(bogus + good))
        #expect(got.map(\.command) == [.heartbeat])
    }

    @Test("Bad checksum and trailer are accepted with warnings, like LogonT")
    func warnings() {
        var bytes = T8Packet.camera(.setTimeReply, T8Payload.index(0)).encoded()
        bytes[bytes.count - 1] ^= 0xFF
        bytes[bytes.count - 2] = 0x04
        var parser = T8PacketParser(expectedHead: .camera)
        let events = parser.append(bytes)
        guard case .packet(let packet, let warnings) = events.first else {
            Issue.record("no packet")
            return
        }
        #expect(packet.command == .setTimeReply)
        #expect(warnings.contains(.badTrailer(0x04)))
        #expect(warnings.contains { if case .badChecksum = $0 { true } else { false } })
    }

    @Test("Phone frames are ignored by a camera-side parser and vice versa")
    func direction() {
        var parser = T8PacketParser(expectedHead: .phone)
        let got = packets(parser.append(T8Packet.camera(.heartbeat).encoded() + T8Packet.phone(.heartbeatReply).encoded()))
        #expect(got.map(\.command) == [.heartbeatReply])
    }

    @Test("LogonT's short PreViewOff and freeviewstop frames parse when allowed")
    func legacyShortFrames() {
        var parser = T8PacketParser(expectedHead: .phone, legacyShortFrameCommands: [.previewStop])
        let freeviewStop: [UInt8] = [0x02, 0x20, 0x02, 0x04, 0x00, 0x00, 0x00, 0x00, 0x03, 0x27]
        let next = T8Packet.phone(.netConnectState, T8Payload.index(0)).encoded()
        let events = parser.append(T8LegacyFrame.previewOff + freeviewStop + next)
        let got = packets(events)
        #expect(got.map(\.command) == [.previewStop, .previewStop, .netConnectState])
        #expect(got[0].payload.isEmpty)
        #expect(got[1].payload == [0x00])
        #expect(got[2].int32Value == 0)
    }

    @Test("finish() reports an incomplete frame")
    func finish() {
        var parser = T8PacketParser(expectedHead: .camera)
        let partial = Array(T8Packet.camera(.getConfigReply, [1, 2, 3]).encoded().prefix(8))
        #expect(parser.append(partial).isEmpty)
        #expect(parser.finish() == [.discarded(partial)])
    }
}
