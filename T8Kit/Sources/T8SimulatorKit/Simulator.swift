import Foundation
import T8Kit

/// Fault injection for testing the app's error handling.
public struct T8SimulatorFaults: Sendable, Equatable {
    /// Answer every NET_CONNECT_STATE(1) with -1 ("another phone is already connected").
    public var alreadyConnected = false
    /// Close the control connection this long after a successful handshake.
    public var dropControlAfter: Duration?
    /// Close the control connection after this many packets from the phone.
    public var dropControlAfterPackets: Int?
    /// Limit every port to this many bytes per second.
    public var bytesPerSecond: Int?
    /// Wait this long before every control reply.
    public var replyLatency: Duration?
    /// Send CAMERA_STOP (40888) this long after the handshake.
    public var cameraStopAfter: Duration?
    /// Data ports send this many bytes, then go silent without closing (like Wi-Fi dropping mid-transfer).
    public var stallDataAfterBytes: Int?

    public init() {}
}

public struct T8SimulatorOptions: Sendable {
    public var host = "127.0.0.1"
    /// Control port; video and audio use the next two. 0 picks free ports (for tests).
    public var basePort: UInt16 = T8Endpoint.controlPort
    /// nil disables heartbeats.
    public var heartbeatInterval: Duration? = .seconds(5)
    /// LogonT only answers heartbeats with a payload (MainFreeView.java:175); set [] to test empty ones.
    public var heartbeatPayload: [UInt8] = [0, 0, 0, 0]
    public var clipSeconds = 3
    public var recordingCounts: [T8Folder: Int] = [.driving: 25, .event: 3, .parking: 2, .photo: 3]
    public var entriesPerPage = 10
    public var frontSize = (width: 256, height: 144)
    public var rearSize = (width: 176, height: 144)
    public var faults = T8SimulatorFaults()

    public init() {}
}

/// What the simulator has seen; tests assert on it.
public struct T8SimulatorStats: Sendable, Equatable {
    public var commands: [T8Command: Int] = [:]
    public var controlConnections = 0
    public var rejectedConnections = 0
    public var heartbeatsSent = 0
    public var heartbeatRepliesReceived = 0
    public var destructiveCommandsReceived = 0
    public var videoConnections = 0
    public var audioConnections = 0
    public var lastTime: T8DateTime?
    public var wifiPassword: String?

    public func count(_ command: T8Command) -> Int { commands[command, default: 0] }
}

/// A fake Eurostyle T8 that speaks the protocol in research/T8_PROTOCOL.md on three TCP ports.
public actor T8Simulator {
    private struct Recording {
        let entry: T8FileListEntry
        let folder: T8Folder
    }

    private enum DataJob {
        case video(folder: T8Folder, index: Int, channel: T8Channel)
        case audio(index: Int, channel: T8Channel)
    }

    public nonisolated let options: T8SimulatorOptions
    public nonisolated let log: T8PacketLog
    public private(set) var address = T8Address()
    public private(set) var stats = T8SimulatorStats()
    public private(set) var config: T8Config

    private var listeners: [POSIXListener] = []
    private var tasks: [Task<Void, Never>] = []
    private var clients: [Int: POSIXConnection] = [:]
    private var nextClientID = 0
    private var activeClient: Int?
    private var clientTasks: [Int: [Task<Void, Never>]] = [:]
    private var dataConnections: [ObjectIdentifier: POSIXConnection] = [:]
    private var recordings: [T8Folder: [Recording]] = [:]
    private var liveArmed = false
    private var liveChannel: T8Channel = .front
    private var pendingVideo: DataJob?
    private var pendingAudio: DataJob?
    private var phoneViewMode = false

    public init(options: T8SimulatorOptions = T8SimulatorOptions(), log: T8PacketLog = T8PacketLog()) {
        self.options = options
        self.log = log
        config = T8Config(
            productName: "logon t8", serial: "SIM00000001", firmwareVersion: "T8_FH_SIM_1.0", wifiPassword: "12345678",
            values: [.mute: 0, .voiceVolume: 2, .lcdBrightness: 4, .exposure: 3, .timeZone: 19, .partition: 1, .parkingRecordingTime: 2]
        )
        var all: [T8Folder: [Recording]] = [:]
        for folder in T8Folder.allCases {
            let count = options.recordingCounts[folder, default: 0]
            all[folder] = (0..<count).map { i in
                let minute = i % 60
                let hour = 8 + i / 60
                let ext = folder == .photo ? ".jpg" : ".avi"
                let name = String(format: "20261122_%02d%02d%02d_", hour, minute, folder.rawValue * 10) + "\(folder.typeLetter)\(ext)"
                return Recording(entry: T8FileListEntry(name: name, hasRear: folder != .photo && i % 4 != 3), folder: folder)
            }
        }
        recordings = all
    }

    // MARK: - Lifecycle

    public func start() throws {
        let base = options.basePort
        let ports: [UInt16] = base == 0 ? [0, 0, 0] : [base, base + 1, base + 2]
        let control = try POSIXListener(host: options.host, port: ports[0])
        let video = try POSIXListener(host: options.host, port: ports[1])
        let audio = try POSIXListener(host: options.host, port: ports[2])
        listeners = [control, video, audio]
        address = T8Address(host: options.host == "0.0.0.0" ? "127.0.0.1" : options.host,
                            controlPort: control.port, videoPort: video.port, audioPort: audio.port)
        log.info("Simulator listening on \(options.host): control \(control.port), video \(video.port), audio \(audio.port)")
        tasks.append(Task { [weak self] in await self?.acceptLoop(control) { sim, conn in await sim.controlConnected(conn) } })
        tasks.append(Task { [weak self] in await self?.acceptLoop(video) { sim, conn in await sim.videoConnected(conn) } })
        tasks.append(Task { [weak self] in await self?.acceptLoop(audio) { sim, conn in await sim.audioConnected(conn) } })
    }

    public func stop() {
        for listener in listeners { listener.close() }
        listeners.removeAll()
        for task in tasks { task.cancel() }
        tasks.removeAll()
        for (id, _) in clients { closeClient(id, reason: "simulator stopped") }
        for (_, conn) in dataConnections { conn.close() }
        dataConnections.removeAll()
    }

    private nonisolated func acceptLoop(_ listener: POSIXListener, _ handler: @escaping @Sendable (T8Simulator, POSIXConnection) async -> Void) async {
        while let conn = await listener.accept() {
            Task { await handler(self, conn) }
        }
    }

    // MARK: - Control port

    private func controlConnected(_ conn: POSIXConnection) {
        let id = nextClientID
        nextClientID += 1
        clients[id] = conn
        stats.controlConnections += 1
        log.info("Control client \(id) connected")
        let reader = Task { [weak self] in
            var parser = T8PacketParser(expectedHead: .phone, legacyShortFrameCommands: [.previewStop])
            while !Task.isCancelled {
                guard let bytes = try? await conn.receive(maximumLength: 4096), !bytes.isEmpty else { break }
                for event in parser.append(bytes) {
                    switch event {
                    case .packet(let packet, let warnings):
                        await self?.handle(packet, warnings: warnings, from: id)
                    case .discarded(let junk):
                        self?.log.add(.warning, bytes: junk, note: "client \(id): skipped bytes")
                    }
                }
            }
            await self?.closeClient(id, reason: "phone closed the connection")
        }
        clientTasks[id, default: []].append(reader)
    }

    private func closeClient(_ id: Int, reason: String) {
        guard let conn = clients.removeValue(forKey: id) else { return }
        conn.close()
        for task in clientTasks.removeValue(forKey: id) ?? [] { task.cancel() }
        if activeClient == id {
            activeClient = nil
            liveArmed = false
            pendingVideo = nil
            pendingAudio = nil
            for (_, data) in dataConnections { data.close() }
            dataConnections.removeAll()
        }
        log.info("Control client \(id) closed: \(reason)")
    }

    private func reply(_ packet: T8Packet, to id: Int) async {
        guard let conn = clients[id] else { return }
        if let latency = options.faults.replyLatency { try? await Task.sleep(for: latency) }
        log.add(.sent, command: packet.command, bytes: packet.encoded(), note: "to client \(id)")
        do {
            try await write(packet.encoded(), to: conn)
        } catch {
            closeClient(id, reason: "send failed")
        }
    }

    private func result(_ command: T8Command, _ value: Int32, to id: Int) async {
        await reply(.camera(command, T8Payload.index(value)), to: id)
    }

    private func handle(_ packet: T8Packet, warnings: [T8PacketParser.Warning], from id: Int) async {
        let note = warnings.isEmpty ? "client \(id)" : "client \(id) \(warnings)"
        log.add(.received, command: packet.command, bytes: packet.encoded(), note: note)
        stats.commands[packet.command, default: 0] += 1
        let received = stats.commands.values.reduce(0, +)
        if let limit = options.faults.dropControlAfterPackets, received >= limit, activeClient == id {
            log.warning("Fault: dropping control connection after \(received) packets")
            closeClient(id, reason: "fault: drop after packets")
            return
        }
        let index = packet.int32Value ?? 0
        let isActive = activeClient == id

        switch packet.command {
        case .netConnectState:
            if index == 1 {
                if options.faults.alreadyConnected || (activeClient != nil && !isActive) {
                    stats.rejectedConnections += 1
                    await result(.netConnectStateReply, -1, to: id)
                } else {
                    activeClient = id
                    await result(.netConnectStateReply, 0, to: id)
                    startSessionTimers(for: id)
                }
            } else if isActive {
                // LogonT closes the socket right after sending index 0.
                activeClient = nil
                liveArmed = false
            }
        case _ where !isActive:
            log.warning("Ignoring \(packet.command) from client \(id) without a session")
        case .heartbeatReply:
            stats.heartbeatRepliesReceived += 1
        case .cameraStopReply:
            break
        case .recordOnOff:
            phoneViewMode = index == 1
        case .getConfig:
            await reply(.camera(.getConfigReply, config.bytes), to: id)
        case .ipcamStart:
            await reply(.camera(.ipcamStart, [0, 0, 0, 0]), to: id)
        case .freeviewStart:
            liveArmed = true
            pendingVideo = nil
            pendingAudio = nil
        case .previewStop:
            liveArmed = false
        case .changeCamera:
            liveChannel = liveChannel == .front ? .rear : .front
            await result(.changeCameraReply, 0, to: id)
        case .snapshot:
            let photos = recordings[.photo, default: []]
            let name = String(format: "20261122_23%02d00_J.jpg", photos.count % 60)
            recordings[.photo, default: []].append(Recording(entry: T8FileListEntry(name: name, hasRear: false), folder: .photo))
            await result(.snapshotReply, 0, to: id)
        case .setTime:
            stats.lastTime = T8DateTime(payload: packet.payload)
            await result(.setTimeReply, 0, to: id)
        case .fileList:
            await sendFileList(folder: T8Folder(rawValue: Int(index)), to: id)
        case .fileTransferStart:
            await startTransfer(packet.payload, to: id)
        case .setWiFiPassword:
            if let password = T8WiFiPassword.newPassword(fromPayload: packet.payload), (try? T8WiFiPassword.validate(password)) != nil {
                stats.wifiPassword = password
                config = T8Config(productName: config.productName, serial: config.serialNumber, firmwareVersion: config.firmwareVersion,
                                  wifiPassword: password, values: Dictionary(uniqueKeysWithValues: T8Config.Field.allCases.map { ($0, config[$0]) }))
                await result(.setWiFiPassword, 0, to: id)
                log.info("Wi-Fi password changed; dropping the phone like a Wi-Fi restart")
                Task { [weak self] in
                    try? await Task.sleep(for: .seconds(1))
                    await self?.closeClient(id, reason: "Wi-Fi restarted after password change")
                }
            } else {
                await result(.setWiFiPassword, -1, to: id)
            }
        case .formatSDCard, .factoryReset:
            stats.destructiveCommandsReceived += 1
            log.warning("DESTRUCTIVE COMMAND RECEIVED: \(packet.command) (refused)")
            await result(packet.command.plusOne, -1, to: id)
        default:
            if let setting = T8Setting.allCases.first(where: { $0.command == packet.command }) {
                config[setting.configField] = index
                await result(setting.replyCommand, 0, to: id)
            } else {
                log.warning("Unknown command \(packet.command)")
            }
        }
    }

    private func startSessionTimers(for id: Int) {
        if let interval = options.heartbeatInterval {
            let payload = options.heartbeatPayload
            clientTasks[id, default: []].append(Task { [weak self] in
                while !Task.isCancelled {
                    try? await Task.sleep(for: interval)
                    guard !Task.isCancelled, let self else { return }
                    await self.sendHeartbeat(payload, to: id)
                }
            })
        }
        if let after = options.faults.dropControlAfter {
            clientTasks[id, default: []].append(Task { [weak self] in
                try? await Task.sleep(for: after)
                guard !Task.isCancelled else { return }
                await self?.closeClient(id, reason: "fault: drop after \(after)")
            })
        }
        if let after = options.faults.cameraStopAfter {
            clientTasks[id, default: []].append(Task { [weak self] in
                try? await Task.sleep(for: after)
                guard !Task.isCancelled else { return }
                await self?.reply(.camera(.cameraStop, [0, 0, 0, 0]), to: id)
            })
        }
    }

    private func sendHeartbeat(_ payload: [UInt8], to id: Int) async {
        guard clients[id] != nil else { return }
        stats.heartbeatsSent += 1
        await reply(.camera(.heartbeat, payload), to: id)
    }

    private func sendFileList(folder: T8Folder?, to id: Int) async {
        guard let folder else {
            await reply(.camera(.fileListReply, T8FileListPage(totalPages: 1, currentPage: 0, entries: []).payload), to: id)
            return
        }
        let entries = recordings[folder, default: []].map(\.entry)
        let perPage = max(1, options.entriesPerPage)
        let pages = max(1, (entries.count + perPage - 1) / perPage)
        for page in 0..<pages {
            let slice = Array(entries.dropFirst(page * perPage).prefix(perPage))
            let payload = T8FileListPage(totalPages: UInt32(pages), currentPage: UInt32(page), entries: slice).payload
            await reply(.camera(.fileListReply, payload), to: id)
        }
    }

    private func startTransfer(_ payload: [UInt8], to id: Int) async {
        guard let type = T8Bytes.u32LE(payload, at: 0), let folder = T8Folder(rawValue: Int(type)),
              let index = T8Bytes.u32LE(payload, at: 4), let channelRaw = T8Bytes.u32LE(payload, at: 8),
              let channel = T8Channel(rawValue: Int(channelRaw)),
              Int(index) < recordings[folder, default: []].count
        else {
            await reply(.camera(.fileTransferStartReply, T8FileStart(fileSize: -1, frameCount: -1).payload), to: id)
            return
        }
        liveArmed = false
        let frames = options.clipSeconds * channel.framesPerSecond
        let size: Int
        if folder == .photo {
            size = SampleJPEG.bytes.count
            pendingAudio = nil
        } else {
            var encoder = makeEncoder(channel)
            size = encoder.nextFrame().count * frames
            pendingAudio = .audio(index: Int(index), channel: channel)
        }
        pendingVideo = .video(folder: folder, index: Int(index), channel: channel)
        await reply(.camera(.fileTransferStartReply, T8FileStart(fileSize: Int32(size), frameCount: folder == .photo ? 0 : Int32(frames)).payload), to: id)
    }

    private func makeEncoder(_ channel: T8Channel) -> TestPatternEncoder {
        let size = channel == .front ? options.frontSize : options.rearSize
        return TestPatternEncoder(width: size.width, height: size.height, channel: channel)
    }

    // MARK: - Data ports

    private func videoConnected(_ conn: POSIXConnection) async {
        stats.videoConnections += 1
        let job = pendingVideo
        pendingVideo = nil
        dataConnections[ObjectIdentifier(conn)] = conn
        defer {
            conn.close()
            dataConnections[ObjectIdentifier(conn)] = nil
        }
        switch job {
        case .video(let folder, let index, let channel):
            log.info("Video port: sending \(folder.title) #\(index) \(channel.title)")
            if folder == .photo {
                await writeData(SampleJPEG.bytes, to: conn)
                return
            }
            var encoder = makeEncoder(channel)
            var stream: [UInt8] = []
            for _ in 0..<(options.clipSeconds * channel.framesPerSecond) { stream += encoder.nextFrame() }
            await writeData(stream, to: conn)
        case .audio, nil:
            if liveArmed {
                await serveLive(conn)
            } else {
                log.warning("Video port: nothing requested; closing")
            }
        }
    }

    /// Paced live stream that follows the front/rear switch until live view stops.
    private func serveLive(_ conn: POSIXConnection) async {
        log.info("Video port: live view started")
        var encoders: [T8Channel: TestPatternEncoder] = [.front: makeEncoder(.front), .rear: makeEncoder(.rear)]
        while liveArmed && activeClient != nil {
            let channel = liveChannel
            guard var encoder = encoders[channel] else { return }
            let frame = encoder.nextFrame()
            encoders[channel] = encoder
            guard (try? await write(frame, to: conn)) != nil else { break }
            try? await Task.sleep(for: .milliseconds(1000 / channel.framesPerSecond))
        }
        log.info("Video port: live view ended")
    }

    private func audioConnected(_ conn: POSIXConnection) async {
        stats.audioConnections += 1
        let job = pendingAudio
        pendingAudio = nil
        dataConnections[ObjectIdentifier(conn)] = conn
        defer {
            conn.close()
            dataConnections[ObjectIdentifier(conn)] = nil
        }
        guard case .audio(let index, let channel) = job else {
            log.warning("Audio port: nothing requested; closing")
            return
        }
        log.info("Audio port: sending #\(index) \(channel.title)")
        var tone = TonePCM()
        await writeData(tone.next(bytes: options.clipSeconds * T8PCM.sampleRate * T8PCM.bytesPerSample), to: conn)
    }

    /// A whole file on a data port, honouring the stall fault.
    private func writeData(_ bytes: [UInt8], to conn: POSIXConnection) async {
        if let limit = options.faults.stallDataAfterBytes, limit < bytes.count {
            guard (try? await write(Array(bytes.prefix(limit)), to: conn)) != nil else { return }
            log.warning("Fault: data port stalled after \(limit) bytes")
            // Keep the socket open and silent until the phone gives up or the session ends.
            while activeClient != nil, dataConnections[ObjectIdentifier(conn)] != nil {
                try? await Task.sleep(for: .milliseconds(100))
            }
            return
        }
        try? await write(bytes, to: conn)
    }

    /// Sends bytes, throttled when the slow-link fault is on.
    private nonisolated func write(_ bytes: [UInt8], to conn: POSIXConnection) async throws {
        guard let rate = options.faults.bytesPerSecond, rate > 0 else {
            try await conn.send(bytes)
            return
        }
        let chunk = max(64, rate / 20)
        var offset = 0
        while offset < bytes.count {
            let end = min(offset + chunk, bytes.count)
            try await conn.send(Array(bytes[offset..<end]))
            try await Task.sleep(for: .milliseconds(Int64((end - offset) * 1000 / rate)))
            offset = end
        }
    }
}
