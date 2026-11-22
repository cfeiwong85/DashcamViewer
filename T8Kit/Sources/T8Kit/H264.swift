/// H.264 Annex-B handling: splitting a byte stream into NAL units, grouping them into
/// access units (frames), and reading the resolution from the SPS.

public struct H264NALUnit: Hashable, Sendable {
    /// NAL bytes without the start code (first byte is the NAL header).
    public var bytes: [UInt8]

    public init(bytes: [UInt8]) {
        self.bytes = bytes
    }

    public var type: UInt8 { (bytes.first ?? 0) & 0x1F }
    public var isSPS: Bool { type == 7 }
    public var isPPS: Bool { type == 8 }
    public var isIDR: Bool { type == 5 }
    /// Coded slice (types 1-5).
    public var isVCL: Bool { (1...5).contains(type) }

    /// `first_mb_in_slice == 0`, i.e. the first slice of a picture. ue(v) of 0 is a single 1 bit.
    public var startsNewPicture: Bool {
        isVCL && bytes.count > 1 && (bytes[1] & 0x80) != 0
    }
}

/// Splits an Annex-B byte stream (start codes `00 00 01` or `00 00 00 01`) into NAL units.
/// Bytes before the first start code are counted in `skippedLeadingBytes` (the live stream's
/// exact start is unconfirmed on hardware; see T8_PROTOCOL.md open item 1).
public struct AnnexBParser: Sendable {
    private var buffer: [UInt8] = []
    /// Where scanning for the next start code resumes.
    private var scanFrom = 0
    /// Index in `buffer` of the current NAL's first byte, once a start code has been seen.
    private var nalStart: Int?
    public private(set) var skippedLeadingBytes = 0

    public init() {}

    public mutating func append(_ bytes: [UInt8]) -> [H264NALUnit] {
        buffer.append(contentsOf: bytes)
        var units: [H264NALUnit] = []
        var i = scanFrom
        while i + 2 < buffer.count {
            guard buffer[i] == 0, buffer[i + 1] == 0, buffer[i + 2] == 1 else {
                i += 1
                continue
            }
            if let start = nalStart {
                if let unit = Self.unit(buffer[start..<i]) { units.append(unit) }
            } else {
                // Bytes before the first start code; a leading zero_byte is part of the start code.
                var garbage = i
                while garbage > 0 && buffer[garbage - 1] == 0 { garbage -= 1 }
                skippedLeadingBytes += garbage
            }
            nalStart = i + 3
            i += 3
        }
        if let start = nalStart {
            buffer.removeFirst(start)
            i -= start
            nalStart = 0
        } else {
            // No start code yet: keep the last three bytes, which may begin a 4-byte start code.
            let drop = max(0, buffer.count - 3)
            skippedLeadingBytes += drop
            buffer.removeFirst(drop)
            i = max(0, i - drop)
        }
        scanFrom = max(0, i)
        return units
    }

    /// End of stream: returns the last NAL unit.
    public mutating func finish() -> [H264NALUnit] {
        defer {
            buffer.removeAll()
            nalStart = nil
            scanFrom = 0
        }
        guard let start = nalStart else {
            skippedLeadingBytes += buffer.count
            return []
        }
        return Self.unit(buffer[start...]).map { [$0] } ?? []
    }

    private static func unit(_ slice: ArraySlice<UInt8>) -> H264NALUnit? {
        var end = slice.endIndex
        // Trailing zeros belong to the next 4-byte start code or are padding.
        while end > slice.startIndex && slice[end - 1] == 0 { end -= 1 }
        return end > slice.startIndex ? H264NALUnit(bytes: Array(slice[slice.startIndex..<end])) : nil
    }
}

/// One coded picture plus any parameter sets / SEI that preceded it.
public struct H264AccessUnit: Hashable, Sendable {
    public var nalUnits: [H264NALUnit]
    public var index: Int

    public var isKeyframe: Bool { nalUnits.contains { $0.isIDR } }
    public var sps: H264NALUnit? { nalUnits.last { $0.isSPS } }
    public var pps: H264NALUnit? { nalUnits.last { $0.isPPS } }
    public var vclUnits: [H264NALUnit] { nalUnits.filter(\.isVCL) }

    /// Annex-B bytes (4-byte start codes), e.g. for saving a raw `.h264` file.
    public var annexB: [UInt8] {
        nalUnits.flatMap { [0, 0, 0, 1] + $0.bytes }
    }
}

/// Groups NAL units into access units (H.264 7.4.1.2.3, simplified for a single-slice-group stream).
public struct H264AccessUnitAssembler: Sendable {
    private var pending: [H264NALUnit] = []
    private var pendingHasVCL = false
    private var count = 0

    public init() {}

    public mutating func append(_ units: [H264NALUnit]) -> [H264AccessUnit] {
        var out: [H264AccessUnit] = []
        for unit in units {
            let startsNew: Bool
            if unit.isVCL {
                startsNew = pendingHasVCL && unit.startsNewPicture
            } else {
                // AUD, SPS, PPS, SEI (and types 14-18) after a picture begin the next access unit.
                startsNew = pendingHasVCL && [6, 7, 8, 9, 14, 15, 16, 17, 18].contains(unit.type)
            }
            if startsNew, let au = flush() { out.append(au) }
            pending.append(unit)
            if unit.isVCL { pendingHasVCL = true }
        }
        return out
    }

    public mutating func finish() -> [H264AccessUnit] {
        flush().map { [$0] } ?? []
    }

    private mutating func flush() -> H264AccessUnit? {
        guard pendingHasVCL else { return nil }
        let au = H264AccessUnit(nalUnits: pending, index: count)
        count += 1
        pending.removeAll()
        pendingHasVCL = false
        return au
    }
}

/// Reads bits from an RBSP (emulation-prevention bytes removed).
struct BitReader {
    private let bytes: [UInt8]
    private var bit = 0

    init(rbsp: [UInt8]) {
        bytes = rbsp
    }

    static func removeEmulationPrevention(_ data: [UInt8]) -> [UInt8] {
        var out: [UInt8] = []
        out.reserveCapacity(data.count)
        var zeros = 0
        for byte in data {
            if zeros >= 2 && byte == 3 {
                zeros = 0
                continue
            }
            out.append(byte)
            zeros = byte == 0 ? zeros + 1 : 0
        }
        return out
    }

    mutating func u(_ n: Int) -> UInt32? {
        var value: UInt32 = 0
        for _ in 0..<n {
            guard bit / 8 < bytes.count else { return nil }
            let b = (bytes[bit / 8] >> (7 - UInt8(bit % 8))) & 1
            value = value << 1 | UInt32(b)
            bit += 1
        }
        return value
    }

    mutating func ue() -> UInt32? {
        var zeros = 0
        while true {
            guard let b = u(1) else { return nil }
            if b == 1 { break }
            zeros += 1
            if zeros > 31 { return nil }
        }
        guard zeros > 0 else { return 0 }
        guard let rest = u(zeros) else { return nil }
        return (1 << UInt32(zeros)) - 1 + rest
    }

    mutating func se() -> Int32? {
        guard let k = ue() else { return nil }
        return k % 2 == 1 ? Int32((k + 1) / 2) : -Int32(k / 2)
    }
}

/// The few SPS fields T8Kit needs: profile, level and the cropped picture size.
public struct H264SPSInfo: Hashable, Sendable {
    public var profile: UInt8
    public var level: UInt8
    public var width: Int
    public var height: Int

    public init?(sps: H264NALUnit) {
        guard sps.isSPS, sps.bytes.count >= 4 else { return nil }
        var r = BitReader(rbsp: BitReader.removeEmulationPrevention(Array(sps.bytes.dropFirst())))
        guard let profileIDC = r.u(8), r.u(8) != nil, let levelIDC = r.u(8), r.ue() != nil else { return nil }
        var chromaFormat: UInt32 = 1
        if [100, 110, 122, 244, 44, 83, 86, 118, 128, 138, 139, 134, 135].contains(profileIDC) {
            guard let chroma = r.ue() else { return nil }
            chromaFormat = chroma
            if chroma == 3 { _ = r.u(1) }
            guard r.ue() != nil, r.ue() != nil, r.u(1) != nil, let scalingPresent = r.u(1) else { return nil }
            if scalingPresent == 1 {
                for i in 0..<(chroma == 3 ? 12 : 8) {
                    guard let present = r.u(1) else { return nil }
                    if present == 1 {
                        let size = i < 6 ? 16 : 64
                        var last: Int32 = 8
                        var next: Int32 = 8
                        for _ in 0..<size where next != 0 {
                            guard let delta = r.se() else { return nil }
                            next = (last &+ delta &+ 256) % 256
                            if next != 0 { last = next }
                        }
                    }
                }
            }
        }
        guard r.ue() != nil, let pocType = r.ue() else { return nil }
        if pocType == 0 {
            guard r.ue() != nil else { return nil }
        } else if pocType == 1 {
            guard r.u(1) != nil, r.se() != nil, r.se() != nil, let cycle = r.ue() else { return nil }
            for _ in 0..<cycle { guard r.se() != nil else { return nil } }
        }
        guard r.ue() != nil, r.u(1) != nil, let widthMbs = r.ue(), let heightMapUnits = r.ue(), let frameMbsOnly = r.u(1) else {
            return nil
        }
        if frameMbsOnly == 0 { _ = r.u(1) }
        guard r.u(1) != nil, let cropping = r.u(1) else { return nil }
        var cropLeft: UInt32 = 0, cropRight: UInt32 = 0, cropTop: UInt32 = 0, cropBottom: UInt32 = 0
        if cropping == 1 {
            guard let l = r.ue(), let rr = r.ue(), let t = r.ue(), let b = r.ue() else { return nil }
            (cropLeft, cropRight, cropTop, cropBottom) = (l, rr, t, b)
        }
        let cropUnitX: UInt32 = chromaFormat == 0 || chromaFormat == 3 ? 1 : 2
        let cropUnitY: UInt32 = (chromaFormat == 1 ? 2 : 1) * (2 - frameMbsOnly)
        // 64-bit arithmetic so a malformed SPS cannot overflow; reject impossible sizes.
        let w = (Int64(widthMbs) + 1) * 16 - Int64(cropUnitX) * (Int64(cropLeft) + Int64(cropRight))
        let h = Int64(2 - frameMbsOnly) * (Int64(heightMapUnits) + 1) * 16 - Int64(cropUnitY) * (Int64(cropTop) + Int64(cropBottom))
        guard w > 0, h > 0, w <= 16384, h <= 16384 else { return nil }
        profile = UInt8(truncatingIfNeeded: profileIDC)
        level = UInt8(truncatingIfNeeded: levelIDC)
        width = Int(w)
        height = Int(h)
    }
}
