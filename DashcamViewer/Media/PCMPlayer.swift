import AVFoundation
import T8Kit

/// Plays the camera's 8 kHz mono 16-bit PCM as it arrives.
final class PCMPlayer {
    private let engine = AVAudioEngine()
    private let node = AVAudioPlayerNode()
    private let format: AVAudioFormat
    private var carry: UInt8?
    private(set) var isRunning = false

    init?() {
        guard let format = AVAudioFormat(commonFormat: .pcmFormatFloat32, sampleRate: Double(T8PCM.sampleRate), channels: 1, interleaved: false) else {
            return nil
        }
        self.format = format
        engine.attach(node)
        engine.connect(node, to: engine.mainMixerNode, format: format)
    }

    func start() throws {
        guard !isRunning else { return }
        let audioSession = AVAudioSession.sharedInstance()
        try audioSession.setCategory(.playback, mode: .moviePlayback)
        try audioSession.setActive(true)
        try engine.start()
        node.play()
        isRunning = true
    }

    /// Queues raw bytes; an odd byte is kept for the next chunk.
    func schedule(_ bytes: [UInt8]) {
        var data = bytes
        if let carry {
            data.insert(carry, at: 0)
            self.carry = nil
        }
        if data.count % 2 == 1 {
            carry = data.removeLast()
        }
        let samples = T8PCM.samples(data)
        guard !samples.isEmpty,
              let buffer = AVAudioPCMBuffer(pcmFormat: format, frameCapacity: AVAudioFrameCount(samples.count)),
              let channel = buffer.floatChannelData?[0] else { return }
        buffer.frameLength = AVAudioFrameCount(samples.count)
        for (i, sample) in samples.enumerated() {
            channel[i] = Float(sample) / 32768
        }
        node.scheduleBuffer(buffer)
    }

    func stop() {
        node.stop()
        engine.stop()
        isRunning = false
        carry = nil
        try? AVAudioSession.sharedInstance().setActive(false, options: .notifyOthersOnDeactivation)
    }
}
