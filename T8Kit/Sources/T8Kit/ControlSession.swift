import Foundation

public enum T8SessionError: Error, Equatable, Sendable, CustomStringConvertible {
    case notConnected
    /// 41023 result < 0 (MainFreeView.java:1408-1419).
    case anotherPhoneConnected
    /// 41023 result 2.
    case cameraIsPlayingBack
    /// 41023 result 3.
    case cameraIsShowingPhotos
    case unexpectedConnectResult(Int32)
    case timeout(T8Command)
    case rejected(T8Command, Int32)
    case malformedReply(T8Command)
    case disconnected(String)
    case cameraStopped
    case busy(String)

    public var description: String {
        switch self {
        case .notConnected: "Not connected to the camera."
        case .anotherPhoneConnected: "Another phone is already connected. Disconnect it, then try again."
        case .cameraIsPlayingBack: "The camera is replaying a recording. Try again when it has finished."
        case .cameraIsShowingPhotos: "The camera is showing photos. Try again when it has finished."
        case .unexpectedConnectResult(let value): "The camera refused the connection (result \(value))."
        case .timeout(let command): "No reply from the camera to \(command.name)."
        case .rejected(let command, let value): "The camera rejected \(command.name) (result \(value))."
        case .malformedReply(let command): "The camera sent an unreadable reply to \(command.name)."
        case .disconnected(let reason): "Disconnected: \(reason)"
        case .cameraStopped: "The camera stopped the session."
        case .busy(let what): "Busy: \(what)"
        }
    }
}

public enum T8SessionEvent: Sendable {
    case connected
    case heartbeat
    /// The camera sent 40888; the session already answered 40889.
    case cameraStopped
    case disconnected(reason: String)
    case unsolicited(T8Packet)
}

/// The control connection on port 7777. One instance per connection attempt.
public actor T8ControlSession {
    public struct Options: Sendable {
        public var address: T8Address
        /// LogonT uses 3000 ms (MainFreeView.java:687).
        public var connectTimeout: Duration = .seconds(3)
        public var replyTimeout: Duration = .seconds(5)
        public var fileListPageTimeout: Duration = .seconds(8)
        /// Pause between RECORD_ON_OFF(0) and the PreViewOff frame; LogonT uses 500 ms (MainFreeView.java:1533-1538).
        public var previewOffDelay: Duration = .milliseconds(500)
        public var answerHeartbeats = true

        public init(address: T8Address = .camera) {
            self.address = address
        }
    }

    private struct Waiter {
        let id: UInt64
        let accepts: Set<T8Command>
        let continuation: CheckedContinuation<T8Packet, Error>
        let timeoutTask: Task<Void, Never>?
    }

    public nonisolated let events: AsyncStream<T8SessionEvent>
    public nonisolated let log: T8PacketLog
    public nonisolated let options: Options

    private let eventContinuation: AsyncStream<T8SessionEvent>.Continuation
    private let connector: any T8Connector
    private var connection: (any T8Connection)?
    private var receiveTask: Task<Void, Never>?
    private var parser = T8PacketParser(expectedHead: .camera)
    private var waiters: [Waiter] = []
    private var nextWaiterID: UInt64 = 0
    private var listInProgress = false
    private var bufferedListPages: [T8Packet] = []
    public private(set) var isConnected = false
    public private(set) var lastConfig: T8Config?
    public private(set) var heartbeatCount = 0

    public init(connector: any T8Connector, options: Options = Options(), log: T8PacketLog = T8PacketLog()) {
        self.connector = connector
        self.options = options
        self.log = log
        (events, eventContinuation) = AsyncStream.makeStream(of: T8SessionEvent.self, bufferingPolicy: .bufferingNewest(100))
    }

    deinit {
        connection?.close()
        receiveTask?.cancel()
        eventContinuation.finish()
    }

    // MARK: - Connection

    /// Opens 7777 and sends NET_CONNECT_STATE(1); succeeds when the camera answers 41023 with 0.
    public func connect() async throws {
        guard connection == nil else { return }
        let address = options.address
        log.info("Connecting to \(address.host):\(address.controlPort)")
        let conn: any T8Connection
        do {
            conn = try await connector.connect(host: address.host, port: address.controlPort, timeout: options.connectTimeout)
        } catch {
            log.warning("Connect failed: \(error)")
            throw error
        }
        connection = conn
        parser = T8PacketParser(expectedHead: .camera)
        startReceiving(conn)
        do {
            let reply = try await request(.phone(.netConnectState, T8Payload.index(1)), expecting: [.netConnectStateReply])
            guard let result = reply.int32Value else { throw T8SessionError.malformedReply(.netConnectState) }
            switch result {
            case 0: break
            case ..<0: throw T8SessionError.anotherPhoneConnected
            case 2: throw T8SessionError.cameraIsPlayingBack
            case 3: throw T8SessionError.cameraIsShowingPhotos
            default: throw T8SessionError.unexpectedConnectResult(result)
            }
        } catch {
            log.warning("Handshake failed: \(error)")
            teardown(reason: "handshake failed")
            throw error
        }
        isConnected = true
        log.info("Connected")
        eventContinuation.yield(.connected)
    }

    /// Sends NET_CONNECT_STATE(0) like LogonT's exit paths, then closes the socket.
    public func disconnect() async {
        guard connection != nil else { return }
        try? await send(.phone(.netConnectState, T8Payload.index(0)))
        teardown(reason: "disconnected by app")
    }

    private func teardown(reason: String) {
        guard let conn = connection else { return }
        connection = nil
        isConnected = false
        conn.close()
        receiveTask?.cancel()
        receiveTask = nil
        let pending = waiters
        waiters.removeAll()
        for waiter in pending {
            waiter.timeoutTask?.cancel()
            waiter.continuation.resume(throwing: T8SessionError.disconnected(reason))
        }
        listInProgress = false
        bufferedListPages.removeAll()
        log.info("Connection closed: \(reason)")
        eventContinuation.yield(.disconnected(reason: reason))
    }

    private func startReceiving(_ conn: any T8Connection) {
        receiveTask = Task { [weak self] in
            while !Task.isCancelled {
                let bytes: [UInt8]
                do {
                    bytes = try await conn.receive(maximumLength: 65536)
                } catch {
                    await self?.receiveEnded(conn, reason: "\(error)")
                    return
                }
                if bytes.isEmpty {
                    await self?.receiveEnded(conn, reason: "closed by camera")
                    return
                }
                await self?.handle(bytes)
            }
        }
    }

    private func receiveEnded(_ conn: any T8Connection, reason: String) {
        // Ignore a late callback from a connection that was already replaced or closed.
        guard let current = connection, current === conn else { return }
        teardown(reason: reason)
    }

    // MARK: - Sending and receiving

    public func send(_ packet: T8Packet) async throws {
        try await sendBytes(packet.encoded(), command: packet.command)
    }

    /// Sends bytes as-is (for LogonT's hand-built frames) and logs them.
    public func sendBytes(_ bytes: [UInt8], command: T8Command?, note: String = "") async throws {
        guard let conn = connection else { throw T8SessionError.notConnected }
        if let command, command == .formatSDCard || command == .factoryReset {
            // Defence in depth: the app never offers these (PLAN.md decision).
            log.warning("Refused to send \(command)")
            throw T8SessionError.rejected(command, -1)
        }
        log.add(.sent, command: command, bytes: bytes, note: note)
        do {
            try await conn.send(bytes)
        } catch {
            log.warning("Send failed: \(error)")
            throw error
        }
    }

    /// Sends a packet and waits for the first packet whose command is in `replies`.
    public func request(_ packet: T8Packet, expecting replies: Set<T8Command>, timeout: Duration? = nil) async throws -> T8Packet {
        guard connection != nil else { throw T8SessionError.notConnected }
        let id = nextWaiterID
        nextWaiterID += 1
        let limit = timeout ?? options.replyTimeout
        let command = packet.command
        return try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<T8Packet, Error>) in
            let timeoutTask = Task { [weak self] in
                try? await Task.sleep(for: limit)
                guard !Task.isCancelled else { return }
                await self?.failWaiter(id, with: T8SessionError.timeout(command))
            }
            waiters.append(Waiter(id: id, accepts: replies, continuation: continuation, timeoutTask: timeoutTask))
            Task { [weak self] in
                do {
                    try await self?.send(packet)
                } catch {
                    await self?.failWaiter(id, with: error)
                }
            }
        }
    }

    /// Waits for a packet without sending anything.
    private func waitFor(_ replies: Set<T8Command>, timeout: Duration) async throws -> T8Packet {
        guard connection != nil else { throw T8SessionError.notConnected }
        let id = nextWaiterID
        nextWaiterID += 1
        let command = replies.first ?? T8Command(0)
        return try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<T8Packet, Error>) in
            let timeoutTask = Task { [weak self] in
                try? await Task.sleep(for: timeout)
                guard !Task.isCancelled else { return }
                await self?.failWaiter(id, with: T8SessionError.timeout(command))
            }
            waiters.append(Waiter(id: id, accepts: replies, continuation: continuation, timeoutTask: timeoutTask))
        }
    }

    private func failWaiter(_ id: UInt64, with error: Error) {
        guard let index = waiters.firstIndex(where: { $0.id == id }) else { return }
        let waiter = waiters.remove(at: index)
        waiter.timeoutTask?.cancel()
        waiter.continuation.resume(throwing: error)
    }

    private func handle(_ bytes: [UInt8]) async {
        for event in parser.append(bytes) {
            switch event {
            case .discarded(let junk):
                log.add(.warning, bytes: junk, note: "skipped \(junk.count) bytes that are not a frame")
            case .packet(let packet, let warnings):
                let note = warnings.isEmpty ? "" : warnings.map { "\($0)" }.joined(separator: ", ")
                log.add(.received, command: packet.command, bytes: packet.encoded(), note: note)
                await dispatch(packet)
            }
        }
    }

    private func dispatch(_ packet: T8Packet) async {
        switch packet.command {
        case .heartbeat:
            heartbeatCount += 1
            eventContinuation.yield(.heartbeat)
            if options.answerHeartbeats {
                try? await send(.phone(.heartbeatReply))
            }
            return
        case .cameraStop:
            try? await send(.phone(.cameraStopReply))
            eventContinuation.yield(.cameraStopped)
            return
        default:
            break
        }
        if let index = waiters.firstIndex(where: { $0.accepts.contains(packet.command) }) {
            let waiter = waiters.remove(at: index)
            waiter.timeoutTask?.cancel()
            waiter.continuation.resume(returning: packet)
            return
        }
        if packet.command == .fileListReply && listInProgress {
            bufferedListPages.append(packet)
            return
        }
        eventContinuation.yield(.unsolicited(packet))
    }

    // MARK: - Commands

    /// Sends a `CNSSimpleIndex` command and checks the `CNSResult` reply.
    @discardableResult
    public func simpleRequest(_ command: T8Command, index: Int32, reply: T8Command, timeout: Duration? = nil) async throws -> Int32 {
        let packet = try await request(.phone(command, T8Payload.index(index)), expecting: [reply], timeout: timeout)
        guard let result = packet.int32Value else { throw T8SessionError.malformedReply(command) }
        guard result >= 0 else { throw T8SessionError.rejected(command, result) }
        return result
    }

    public func readConfig() async throws -> T8Config {
        let reply = try await request(.phone(.getConfig), expecting: [.getConfigReply])
        guard let config = T8Config(payload: reply.payload) else { throw T8SessionError.malformedReply(.getConfig) }
        lastConfig = config
        return config
    }

    /// RECORD_ON_OFF: 1 when entering live view, 0 when leaving it or before listing files.
    /// LogonT does not wait for a reply.
    public func setPhoneViewMode(_ on: Bool) async throws {
        try await send(.phone(.recordOnOff, T8Payload.index(on ? 1 : 0)))
    }

    /// LogonT's live-view start after the handshake: RECORD_ON_OFF(1), GET_CONFIG,
    /// IPCAM_START, wait for its echo, then FREEVIEW_START. The caller opens port 7778 next.
    public func startPreview() async throws -> T8Config {
        try await setPhoneViewMode(true)
        let config = try await readConfig()
        _ = try await request(.phone(.ipcamStart, T8Payload.ipcamStart), expecting: [.ipcamStart])
        try await send(.phone(.freeviewStart))
        return config
    }

    /// LogonT's onPause: RECORD_ON_OFF(0), then 500 ms later the 9-byte PreViewOff frame.
    public func stopPreview() async throws {
        try await setPhoneViewMode(false)
        try await Task.sleep(for: options.previewOffDelay)
        try await sendBytes(T8LegacyFrame.previewOff, command: .previewStop, note: "LogonT PreViewOff frame (length field 4, no payload)")
    }

    /// Switches the live view between front and rear (41014 → 41015).
    public func switchCamera() async throws {
        try await simpleRequest(.changeCamera, index: 0, reply: .changeCameraReply)
    }

    /// Snapshot: LogonT's sendData quirk sends a 4-byte payload of index 0 (MainProtocol.java:144-147).
    public func takeSnapshot() async throws {
        try await simpleRequest(.snapshot, index: 0, reply: .snapshotReply)
    }

    /// Voice recording on the camera: index 0 = record, 1 = do not record.
    public func setVoiceRecording(_ enabled: Bool) async throws {
        try await simpleRequest(.setMute, index: enabled ? 0 : 1, reply: .setMuteReply)
    }

    /// SET_TIME. LogonT ignores the reply in live view, so a missing reply is not an error unless `requireReply`.
    public func setTime(_ time: T8DateTime, requireReply: Bool = false) async throws {
        if requireReply {
            _ = try await request(.phone(.setTime, time.payload), expecting: [.setTimeReply])
        } else {
            try await send(.phone(.setTime, time.payload))
        }
    }

    public func setSetting(_ setting: T8Setting, index: Int) async throws {
        guard setting.isChangeAllowed else { throw T8SessionError.rejected(setting.command, -1) }
        try await simpleRequest(setting.command, index: Int32(index), reply: setting.replyCommand)
        if var config = lastConfig {
            config[setting.configField] = Int32(index)
            lastConfig = config
        }
    }

    /// Changes the camera's Wi-Fi password. The camera drops the Wi-Fi connection afterwards.
    public func setWiFiPassword(_ password: String) async throws {
        let payload = try T8WiFiPassword.payload(newPassword: password)
        let reply = try await request(.phone(.setWiFiPassword, payload), expecting: [.setWiFiPassword, T8Command(41010)])
        guard let result = reply.int32Value else { throw T8SessionError.malformedReply(.setWiFiPassword) }
        guard result >= 0 else { throw T8SessionError.rejected(.setWiFiPassword, result) }
    }

    /// Lists one folder, collecting every 40998 page. LogonT sends RECORD_ON_OFF(0) first.
    public func listRecordings(in folder: T8Folder, sendRecordOff: Bool = true) async throws -> T8FileListAssembler {
        guard !listInProgress else { throw T8SessionError.busy("a file list is already loading") }
        listInProgress = true
        bufferedListPages.removeAll()
        defer {
            listInProgress = false
            bufferedListPages.removeAll()
        }
        if sendRecordOff {
            try await setPhoneViewMode(false)
        }
        var assembler = T8FileListAssembler(folder: folder)
        var page = try await request(
            .phone(.fileList, T8Payload.index(Int32(folder.rawValue))),
            expecting: [.fileListReply],
            timeout: options.fileListPageTimeout
        )
        while true {
            guard let parsed = T8FileListPage(payload: page.payload) else { throw T8SessionError.malformedReply(.fileList) }
            assembler.add(parsed)
            if assembler.isComplete { break }
            if !bufferedListPages.isEmpty {
                page = bufferedListPages.removeFirst()
                continue
            }
            do {
                page = try await waitFor([.fileListReply], timeout: options.fileListPageTimeout)
            } catch T8SessionError.timeout {
                log.warning("File list stopped after \(assembler.pagesReceived) page(s)")
                break
            }
        }
        log.info("\(folder.title): \(assembler.recordings.count) recording(s) in \(assembler.pagesReceived) page(s)")
        return assembler
    }

    /// FILE_TRANS_START (8200). Returns the 8201 reply if it arrives within `timeout`;
    /// LogonT does not wait for it before opening the data ports.
    public func startFileTransfer(_ recording: T8Recording, channel: T8Channel, timeout: Duration = .seconds(2)) async throws -> T8FileStart? {
        let packet = T8Packet.phone(.fileTransferStart, T8Payload.fileTransfer(folder: recording.folder, index: recording.index, channel: channel))
        do {
            let reply = try await request(packet, expecting: [.fileTransferStartReply], timeout: timeout)
            return T8FileStart(payload: reply.payload)
        } catch T8SessionError.timeout {
            log.info("No 8201 reply within \(timeout); continuing like LogonT")
            return nil
        }
    }
}
