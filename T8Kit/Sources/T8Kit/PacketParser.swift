/// Streaming parser for control-port frames. Feed it bytes as they arrive (any split);
/// it returns complete frames and reports anything it had to skip.
///
/// Receive rules follow LogonT (MainFreeView.java:163-189): the head byte must match,
/// the length is little-endian and at most 100000, and the trailer and checksum are
/// read but not enforced. Differences from LogonT, both deliberate:
/// - On a bad head byte or oversize length LogonT throws away 7 bytes and loses sync;
///   this parser skips one byte at a time until it finds the next plausible frame.
/// - Bad trailers and checksums are reported as warnings so the diagnostics log shows them.
public struct T8PacketParser: Sendable {
    public enum Warning: Hashable, Sendable {
        case badTrailer(UInt8)
        case badChecksum(expected: UInt8, received: UInt8)
        /// A LogonT hand-built frame whose length field is larger than its real payload.
        case legacyShortFrame(declaredLength: Int)
    }

    public enum Event: Hashable, Sendable {
        case packet(T8Packet, warnings: [Warning])
        /// Bytes that could not be part of a frame (wrong head byte or impossible length).
        case discarded([UInt8])
    }

    public let expectedHead: T8Packet.Head
    /// Commands for which a short LogonT hand-built frame is accepted (see `T8LegacyFrame`).
    /// The simulator enables this for 0x2002; the phone side never needs it.
    public var legacyShortFrameCommands: Set<T8Command>

    private var buffer: [UInt8] = []
    private var start = 0
    private var discardRun: [UInt8] = []

    public init(expectedHead: T8Packet.Head, legacyShortFrameCommands: Set<T8Command> = []) {
        self.expectedHead = expectedHead
        self.legacyShortFrameCommands = legacyShortFrameCommands
    }

    /// Number of bytes held while waiting for the rest of a frame.
    public var bufferedByteCount: Int { buffer.count - start }

    public mutating func append<S: Sequence>(_ bytes: S) -> [Event] where S.Element == UInt8 {
        buffer.append(contentsOf: bytes)
        var events: [Event] = []
        while let event = nextFrame() {
            if !discardRun.isEmpty {
                events.append(.discarded(discardRun))
                discardRun.removeAll()
            }
            events.append(event)
        }
        // Compact the buffer once enough consumed bytes accumulate.
        if start > 0 && (start >= 4096 || start == buffer.count) {
            buffer.removeFirst(start)
            start = 0
        }
        return events
    }

    /// Call at end of stream: reports skipped bytes and any incomplete frame as discarded.
    public mutating func finish() -> [Event] {
        var leftover = discardRun
        leftover += buffer[start...]
        buffer.removeAll()
        start = 0
        discardRun.removeAll()
        return leftover.isEmpty ? [] : [.discarded(leftover)]
    }

    private mutating func skipByte() {
        discardRun.append(buffer[start])
        start += 1
    }

    private mutating func nextFrame() -> Event? {
        while start < buffer.count {
            let available = buffer.count - start
            guard buffer[start] == expectedHead.rawValue else {
                skipByte()
                continue
            }
            guard available >= T8Packet.headerLength else { return nil }
            guard let commandRaw = T8Bytes.u16BE(buffer, at: start + 1),
                  let declared = T8Bytes.u32LE(buffer, at: start + 3) else { return nil }
            guard declared <= UInt32(T8Packet.maxPayloadLength) else {
                skipByte()
                continue
            }
            let command = T8Command(commandRaw)
            let length = Int(declared)

            if legacyShortFrameCommands.contains(command), length > 0, !hasValidFullFrame(length: length),
               let short = shortFrame(command: command, declaredLength: length) {
                return short
            }

            let total = T8Packet.headerLength + length + 2
            guard available >= total else { return nil }
            let payloadStart = start + T8Packet.headerLength
            let payload = Array(buffer[payloadStart..<(payloadStart + length)])
            let trailer = buffer[payloadStart + length]
            let received = buffer[payloadStart + length + 1]
            let expected = T8Bytes.xor(buffer[start...(payloadStart + length)])
            var warnings: [Warning] = []
            if trailer != T8Packet.trailer { warnings.append(.badTrailer(trailer)) }
            if received != expected { warnings.append(.badChecksum(expected: expected, received: received)) }
            start += total
            let head = T8Packet.Head(rawValue: buffer[start - total]) ?? expectedHead
            return .packet(T8Packet(head: head, command: command, payload: payload), warnings: warnings)
        }
        return nil
    }

    /// True when the whole declared frame is buffered and its trailer and checksum are right.
    private func hasValidFullFrame(length: Int) -> Bool {
        let total = T8Packet.headerLength + length + 2
        guard buffer.count - start >= total else { return false }
        let trailerIndex = start + T8Packet.headerLength + length
        return buffer[trailerIndex] == T8Packet.trailer && buffer[trailerIndex + 1] == T8Bytes.xor(buffer[start...trailerIndex])
    }

    /// Accepts `[header][k payload bytes][0x03][xor]` for some k shorter than the declared length,
    /// which is how LogonT's `PreViewOff` (k = 0) and `freeviewstop` (k = 1) frames look.
    private mutating func shortFrame(command: T8Command, declaredLength: Int) -> Event? {
        let payloadStart = start + T8Packet.headerLength
        for k in 0..<declaredLength {
            let trailerIndex = payloadStart + k
            guard trailerIndex + 1 < buffer.count else { return nil }
            if buffer[trailerIndex] == T8Packet.trailer,
               buffer[trailerIndex + 1] == T8Bytes.xor(buffer[start...trailerIndex]) {
                let payload = Array(buffer[payloadStart..<trailerIndex])
                let head = T8Packet.Head(rawValue: buffer[start]) ?? expectedHead
                start = trailerIndex + 2
                return .packet(
                    T8Packet(head: head, command: command, payload: payload),
                    warnings: [.legacyShortFrame(declaredLength: declaredLength)]
                )
            }
        }
        return nil
    }
}
