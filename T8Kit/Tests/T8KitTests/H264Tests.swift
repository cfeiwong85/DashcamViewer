import Testing
@testable import T8Kit

@Suite("H.264 Annex-B")
struct H264Tests {
    // A tiny synthetic stream: SPS, PPS, IDR slice, non-IDR slice, AUD + non-IDR slice.
    let sps: [UInt8] = [0x67, 0x42, 0xC0, 0x1E, 0x8C, 0x8D, 0x40]
    let pps: [UInt8] = [0x68, 0xCE, 0x3C, 0x80]
    let idr: [UInt8] = [0x65, 0x88, 0x84, 0x00, 0x33]
    let p1: [UInt8] = [0x41, 0x9A, 0x02, 0x03]
    let aud: [UInt8] = [0x09, 0xF0]
    let p2: [UInt8] = [0x41, 0x9A, 0x04]

    var stream: [UInt8] {
        let long: [UInt8] = [0, 0, 0, 1]
        let short: [UInt8] = [0, 0, 1]
        var out: [UInt8] = long + sps
        out += long + pps
        out += short + idr
        out += long + p1
        out += long + aud
        out += short + p2
        return out
    }

    func split(_ bytes: [UInt8], every n: Int) -> [[UInt8]] {
        stride(from: 0, to: bytes.count, by: n).map { Array(bytes[$0..<min($0 + n, bytes.count)]) }
    }

    @Test("NAL units are found regardless of how the stream is split", arguments: [1, 2, 3, 5, 7, 1000])
    func nalUnits(chunk: Int) {
        var parser = AnnexBParser()
        var units: [H264NALUnit] = []
        for part in split(stream, every: chunk) { units += parser.append(part) }
        units += parser.finish()
        #expect(units.map(\.bytes) == [sps, pps, idr, p1, aud, p2])
        #expect(parser.skippedLeadingBytes == 0)
    }

    @Test("Bytes before the first start code are skipped and counted")
    func leadingGarbage() {
        var parser = AnnexBParser()
        let garbage: [UInt8] = [0xAA, 0xBB, 0xCC, 0x12, 0x34]
        var units = parser.append(garbage + stream)
        units += parser.finish()
        #expect(units.first?.bytes == sps)
        #expect(parser.skippedLeadingBytes == garbage.count)
    }

    @Test("Access units group parameter sets with the following picture")
    func accessUnits() {
        var parser = AnnexBParser()
        var assembler = H264AccessUnitAssembler()
        var aus = assembler.append(parser.append(stream))
        aus += assembler.append(parser.finish())
        aus += assembler.finish()
        #expect(aus.count == 3)
        #expect(aus[0].isKeyframe)
        #expect(aus[0].sps?.bytes == sps)
        #expect(aus[0].pps?.bytes == pps)
        #expect(aus[1].nalUnits.map(\.bytes) == [p1])
        #expect(aus[2].nalUnits.map(\.bytes) == [aud, p2])
        #expect(aus.map(\.index) == [0, 1, 2])
        #expect(aus[1].annexB == [0, 0, 0, 1] + p1)
    }

    @Test("Emulation-prevention bytes are removed")
    func emulation() {
        #expect(BitReader.removeEmulationPrevention([0x00, 0x00, 0x03, 0x01, 0x00, 0x00, 0x03, 0x00]) == [0, 0, 1, 0, 0, 0])
    }

    @Test("Exp-Golomb reader")
    func expGolomb() {
        // bits: 1 | 010 | 011 | 00100 | 00101 -> ue 0, 1, 2, 3; se(00101 = 4) = -2
        var reader = BitReader(rbsp: [0b1010_0110, 0b0100_0010, 0b1000_0000])
        #expect(reader.ue() == 0)
        #expect(reader.ue() == 1)
        #expect(reader.ue() == 2)
        #expect(reader.ue() == 3)
        #expect(reader.se() == -2)
    }
}
