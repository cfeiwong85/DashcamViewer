import T8Kit

/// Writes H.264 syntax elements MSB first.
struct BitWriter {
    private(set) var bytes: [UInt8] = []
    private var current: UInt8 = 0
    private var bitCount = 0

    var isByteAligned: Bool { bitCount == 0 }

    mutating func bit(_ value: UInt32) {
        current = current << 1 | UInt8(value & 1)
        bitCount += 1
        if bitCount == 8 {
            bytes.append(current)
            current = 0
            bitCount = 0
        }
    }

    mutating func u(_ value: UInt32, _ bits: Int) {
        for shift in stride(from: bits - 1, through: 0, by: -1) {
            bit(value >> UInt32(shift))
        }
    }

    mutating func ue(_ value: UInt32) {
        let x = value + 1
        let length = 32 - x.leadingZeroBitCount
        u(0, length - 1)
        u(x, length)
    }

    mutating func se(_ value: Int32) {
        ue(value > 0 ? UInt32(2 * value - 1) : UInt32(-2 * value))
    }

    mutating func alignWithZeros() {
        while bitCount != 0 { bit(0) }
    }

    mutating func byte(_ value: UInt8) {
        precondition(isByteAligned)
        bytes.append(value)
    }

    /// rbsp_trailing_bits(): a 1 then zeros to the byte boundary.
    mutating func trailingBits() {
        bit(1)
        alignWithZeros()
    }
}

/// A Baseline-profile H.264 encoder that writes every macroblock as I_PCM (raw samples).
/// It produces valid streams without any codec library, so the simulator can serve video
/// that VideoToolbox, AVFoundation and FFmpeg decode. Every picture is an IDR picture.
public struct TestPatternEncoder: Sendable {
    public let width: Int
    public let height: Int
    public let channel: T8Channel
    private var frameNumber = 0

    /// `width` and `height` must be multiples of 16.
    public init(width: Int, height: Int, channel: T8Channel) {
        precondition(width % 16 == 0 && height % 16 == 0 && width > 0 && height > 0)
        self.width = width
        self.height = height
        self.channel = channel
    }

    public var spsNAL: [UInt8] {
        var w = BitWriter()
        w.u(66, 8)            // profile_idc: Baseline
        w.u(0xC0, 8)          // constraint_set0/1 (constrained Baseline)
        w.u(31, 8)            // level_idc 3.1
        w.ue(0)               // seq_parameter_set_id
        w.ue(0)               // log2_max_frame_num_minus4
        w.ue(2)               // pic_order_cnt_type
        w.ue(1)               // max_num_ref_frames
        w.u(0, 1)             // gaps_in_frame_num_value_allowed_flag
        w.ue(UInt32(width / 16 - 1))
        w.ue(UInt32(height / 16 - 1))
        w.u(1, 1)             // frame_mbs_only_flag
        w.u(1, 1)             // direct_8x8_inference_flag
        w.u(0, 1)             // frame_cropping_flag
        w.u(0, 1)             // vui_parameters_present_flag
        w.trailingBits()
        return Self.nal(header: 0x67, rbsp: w.bytes)
    }

    public var ppsNAL: [UInt8] {
        var w = BitWriter()
        w.ue(0)               // pic_parameter_set_id
        w.ue(0)               // seq_parameter_set_id
        w.u(0, 1)             // entropy_coding_mode_flag (CAVLC)
        w.u(0, 1)             // bottom_field_pic_order_in_frame_present_flag
        w.ue(0)               // num_slice_groups_minus1
        w.ue(0)               // num_ref_idx_l0_default_active_minus1
        w.ue(0)               // num_ref_idx_l1_default_active_minus1
        w.u(0, 1)             // weighted_pred_flag
        w.u(0, 2)             // weighted_bipred_idc
        w.se(0)               // pic_init_qp_minus26
        w.se(0)               // pic_init_qs_minus26
        w.se(0)               // chroma_qp_index_offset
        w.u(1, 1)             // deblocking_filter_control_present_flag
        w.u(0, 1)             // constrained_intra_pred_flag
        w.u(0, 1)             // redundant_pic_cnt_present_flag
        w.trailingBits()
        return Self.nal(header: 0x68, rbsp: w.bytes)
    }

    /// The next picture as Annex-B bytes: SPS + PPS + IDR slice (parameter sets repeat so a
    /// decoder can join a live stream at any frame).
    public mutating func nextFrame() -> [UInt8] {
        let n = frameNumber
        frameNumber += 1
        var out: [UInt8] = []
        for nal in [spsNAL, ppsNAL, slice(frame: n)] {
            out += [0, 0, 0, 1] + nal
        }
        return out
    }

    private func slice(frame: Int) -> [UInt8] {
        var w = BitWriter()
        w.ue(0)                       // first_mb_in_slice
        w.ue(7)                       // slice_type: I (all slices)
        w.ue(0)                       // pic_parameter_set_id
        w.u(0, 4)                     // frame_num (IDR)
        w.ue(UInt32(frame % 2))       // idr_pic_id: consecutive IDRs must differ
        w.u(0, 1)                     // no_output_of_prior_pics_flag
        w.u(0, 1)                     // long_term_reference_flag
        w.se(0)                       // slice_qp_delta
        w.ue(1)                       // disable_deblocking_filter_idc
        let mbsWide = width / 16
        for mb in 0..<(mbsWide * (height / 16)) {
            w.ue(25)                  // mb_type I_PCM
            w.alignWithZeros()        // pcm_alignment_zero_bit
            let mx = (mb % mbsWide) * 16
            let my = (mb / mbsWide) * 16
            for y in 0..<16 {
                for x in 0..<16 { w.byte(luma(x: mx + x, y: my + y, frame: frame)) }
            }
            let (cb, cr) = chroma(mbX: mx, mbY: my, frame: frame)
            for _ in 0..<64 { w.byte(cb) }
            for _ in 0..<64 { w.byte(cr) }
        }
        w.trailingBits()
        return Self.nal(header: 0x65, rbsp: w.bytes)
    }

    /// Sample values stay in 16...235 so PCM data never contains 00 00.
    private func luma(x: Int, y: Int, frame: Int) -> UInt8 {
        let fps = channel.framesPerSecond
        let barX = (frame * 4) % width
        let barY = (frame * 2) % height
        if channel == .front, x >= barX && x < barX + 16 { return 235 }
        if channel == .rear, y >= barY && y < barY + 16 { return 235 }
        // Second counter: a block in the top-left that blinks once per second.
        if x < 32 && y < 32 && (frame / fps) % 2 == 0 { return 200 }
        let checker = ((x / 32) + (y / 32)) % 2 == 0
        return UInt8(clamping: (checker ? 60 : 110) + (x * 60) / width)
    }

    private func chroma(mbX: Int, mbY: Int, frame: Int) -> (UInt8, UInt8) {
        switch channel {
        case .front: (UInt8(clamping: 112 + (mbX * 32) / width), 128)
        case .rear: (110, UInt8(clamping: 150 + (mbY * 40) / height))
        }
    }

    /// NAL header + RBSP with emulation-prevention bytes inserted.
    static func nal(header: UInt8, rbsp: [UInt8]) -> [UInt8] {
        var out: [UInt8] = [header]
        out.reserveCapacity(rbsp.count + rbsp.count / 64 + 1)
        var zeros = 0
        for byte in rbsp {
            if zeros >= 2 && byte <= 3 {
                out.append(3)
                zeros = 0
            }
            out.append(byte)
            zeros = byte == 0 ? zeros + 1 : 0
        }
        return out
    }
}

/// A 440 Hz tone (beeping once per second) as camera-format PCM: 8 kHz mono s16le.
public struct TonePCM: Sendable {
    private var sampleIndex = 0

    public init() {}

    public mutating func next(bytes count: Int) -> [UInt8] {
        var out: [UInt8] = []
        out.reserveCapacity(count)
        for _ in 0..<(count / 2) {
            let t = Double(sampleIndex) / Double(T8PCM.sampleRate)
            let beep = sampleIndex % T8PCM.sampleRate < T8PCM.sampleRate / 5
            let value = beep ? Int16(6000 * sin(2 * .pi * 440 * t)) : 0
            out += [UInt8(truncatingIfNeeded: value), UInt8(truncatingIfNeeded: value >> 8)]
            sampleIndex += 1
        }
        return out
    }
}

#if canImport(Glibc)
import Glibc
#elseif canImport(Darwin)
import Darwin
#endif
