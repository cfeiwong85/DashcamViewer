import AVFoundation
import CoreMedia
import SwiftUI
import T8Kit
import UIKit

/// Builds CoreMedia objects from T8Kit access units.
enum H264SampleBuffers {
    static func formatDescription(sps: [UInt8], pps: [UInt8]) -> CMVideoFormatDescription? {
        guard !sps.isEmpty, !pps.isEmpty else { return nil }
        var description: CMFormatDescription?
        let status: OSStatus = sps.withUnsafeBufferPointer { spsBuffer in
            pps.withUnsafeBufferPointer { ppsBuffer in
                guard let spsBase = spsBuffer.baseAddress, let ppsBase = ppsBuffer.baseAddress else { return -1 }
                let pointers: [UnsafePointer<UInt8>] = [spsBase, ppsBase]
                let sizes: [Int] = [sps.count, pps.count]
                return CMVideoFormatDescriptionCreateFromH264ParameterSets(
                    allocator: kCFAllocatorDefault,
                    parameterSetCount: 2,
                    parameterSetPointers: pointers,
                    parameterSetSizes: sizes,
                    nalUnitHeaderLength: 4,
                    formatDescriptionOut: &description
                )
            }
        }
        return status == noErr ? description : nil
    }

    /// AVCC sample (4-byte big-endian lengths) holding the picture's slices and SEI;
    /// parameter sets travel in the format description instead.
    static func sampleBuffer(
        _ unit: H264AccessUnit,
        format: CMVideoFormatDescription,
        presentationTime: CMTime = .invalid,
        duration: CMTime = .invalid,
        displayImmediately: Bool = false
    ) -> CMSampleBuffer? {
        var payload = Data()
        for nal in unit.nalUnits where !nal.isSPS && !nal.isPPS && nal.type != 9 {
            let length = UInt32(nal.bytes.count).bigEndian
            withUnsafeBytes(of: length) { payload.append(contentsOf: $0) }
            payload.append(contentsOf: nal.bytes)
        }
        guard !payload.isEmpty else { return nil }

        var block: CMBlockBuffer?
        guard CMBlockBufferCreateWithMemoryBlock(
            allocator: kCFAllocatorDefault, memoryBlock: nil, blockLength: payload.count,
            blockAllocator: kCFAllocatorDefault, customBlockSource: nil, offsetToData: 0,
            dataLength: payload.count, flags: kCMBlockBufferAssureMemoryNowFlag, blockBufferOut: &block
        ) == kCMBlockBufferNoErr, let block else { return nil }
        let copied = payload.withUnsafeBytes { raw -> OSStatus in
            guard let base = raw.baseAddress else { return -1 }
            return CMBlockBufferReplaceDataBytes(with: base, blockBuffer: block, offsetIntoDestination: 0, dataLength: payload.count)
        }
        guard copied == kCMBlockBufferNoErr else { return nil }

        var timing = CMSampleTimingInfo(duration: duration, presentationTimeStamp: presentationTime, decodeTimeStamp: .invalid)
        var size = payload.count
        var sample: CMSampleBuffer?
        guard CMSampleBufferCreateReady(
            allocator: kCFAllocatorDefault, dataBuffer: block, formatDescription: format,
            sampleCount: 1, sampleTimingEntryCount: 1, sampleTimingArray: &timing,
            sampleSizeEntryCount: 1, sampleSizeArray: &size, sampleBufferOut: &sample
        ) == noErr, let sample else { return nil }

        if let attachments = CMSampleBufferGetSampleAttachmentsArray(sample, createIfNecessary: true),
           CFArrayGetCount(attachments) > 0 {
            let dictionary = unsafeBitCast(CFArrayGetValueAtIndex(attachments, 0), to: CFMutableDictionary.self)
            if displayImmediately {
                CFDictionarySetValue(dictionary,
                                     Unmanaged.passUnretained(kCMSampleAttachmentKey_DisplayImmediately).toOpaque(),
                                     Unmanaged.passUnretained(kCFBooleanTrue).toOpaque())
            }
            if !unit.isKeyframe {
                CFDictionarySetValue(dictionary,
                                     Unmanaged.passUnretained(kCMSampleAttachmentKey_NotSync).toOpaque(),
                                     Unmanaged.passUnretained(kCFBooleanTrue).toOpaque())
            }
        }
        return sample
    }
}

/// A UIView whose backing layer is an AVSampleBufferDisplayLayer.
final class VideoDisplayView: UIView {
    override class var layerClass: AnyClass { AVSampleBufferDisplayLayer.self }

    var displayLayer: AVSampleBufferDisplayLayer {
        // layerClass guarantees the type.
        layer as! AVSampleBufferDisplayLayer // swiftlint:disable:this force_cast
    }
}

/// Shows H.264 access units with the system decoder. Handles parameter-set changes, which
/// happen when the camera switches between the front (1080p) and rear (720p) streams.
@MainActor
final class H264Renderer {
    let view = VideoDisplayView()
    private var format: CMVideoFormatDescription?
    private var sps: [UInt8] = []
    private var pps: [UInt8] = []
    private(set) var dimensions: CMVideoDimensions?
    private(set) var framesShown = 0
    private(set) var framesDropped = 0

    init() {
        view.backgroundColor = .black
        view.displayLayer.videoGravity = .resizeAspect
    }

    var displayLayer: AVSampleBufferDisplayLayer { view.displayLayer }

    /// Returns false while waiting for the first SPS/PPS, or if the frame could not be wrapped.
    @discardableResult
    func enqueue(_ unit: H264AccessUnit) -> Bool {
        if let newSPS = unit.sps?.bytes, let newPPS = unit.pps?.bytes, newSPS != sps || newPPS != pps {
            if let description = H264SampleBuffers.formatDescription(sps: newSPS, pps: newPPS) {
                format = description
                sps = newSPS
                pps = newPPS
                dimensions = CMVideoFormatDescriptionGetDimensions(description)
                displayLayer.flush()
            }
        }
        guard let format, let sample = H264SampleBuffers.sampleBuffer(unit, format: format, displayImmediately: true) else {
            framesDropped += 1
            return false
        }
        if displayLayer.status == .failed {
            displayLayer.flush()
        }
        displayLayer.enqueue(sample)
        framesShown += 1
        return true
    }

    func clear() {
        displayLayer.flushAndRemoveImage()
        format = nil
        sps = []
        pps = []
        dimensions = nil
        framesShown = 0
        framesDropped = 0
    }

    var resolutionText: String {
        guard let dimensions else { return "" }
        return "\(dimensions.width)×\(dimensions.height)"
    }
}

/// SwiftUI host for an H264Renderer's view.
struct VideoSurface: UIViewRepresentable {
    let renderer: H264Renderer

    func makeUIView(context: Context) -> VideoDisplayView {
        renderer.view
    }

    func updateUIView(_ uiView: VideoDisplayView, context: Context) {}
}
