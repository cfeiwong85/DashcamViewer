import Foundation
import T8Kit
import T8SimulatorKit

let usage = """
t8sim: a simulated Eurostyle T8 dashcam for testing DashcamViewer without the car.

USAGE
  t8sim [options]                         serve control 7777, video 7778, audio 7779
  t8sim dump-video front|rear FILE [--frames N]   write the test pattern as raw H.264

OPTIONS
  --host ADDRESS          address to listen on (default 0.0.0.0, so an iPhone on the same Wi-Fi can connect)
  --port N                control port; video and audio use N+1 and N+2 (default 7777)
  --heartbeat SECONDS     heartbeat interval, or "off" (default 5)
  --empty-heartbeat       send heartbeats with no payload (LogonT ignores those)
  --clip-seconds N        length of every recording (default 3)
  --quiet                 do not print the packet log

FAULTS
  --already-connected     answer every connection with "another phone is already connected"
  --drop-after SECONDS    close the control connection this long after it connects
  --drop-after-packets N  close the control connection after N packets from the phone
  --slow BYTES_PER_SEC    limit every port to this rate (e.g. 20000)
  --latency MS            wait this long before each control reply
  --camera-stop-after SECONDS   send CAMERA_STOP (40888) after this long
  --stall-data-after BYTES      data ports send this many bytes, then go silent

"""

func fail(_ message: String) -> Never {
    FileHandle.standardError.write(Data((message + "\n\n" + usage).utf8))
    exit(2)
}

var args = Array(CommandLine.arguments.dropFirst())

if args.first == "dump-video" {
    guard args.count >= 3 else { fail("dump-video needs a camera and a file") }
    let channel: T8Channel = args[1] == "rear" ? .rear : .front
    var frames = 60
    if let i = args.firstIndex(of: "--frames"), i + 1 < args.count, let n = Int(args[i + 1]) { frames = n }
    let defaults = T8SimulatorOptions()
    let size = channel == .front ? defaults.frontSize : defaults.rearSize
    var encoder = TestPatternEncoder(width: size.width, height: size.height, channel: channel)
    var data = Data()
    for _ in 0..<frames { data.append(contentsOf: encoder.nextFrame()) }
    do {
        try data.write(to: URL(fileURLWithPath: args[2]))
    } catch {
        fail("Cannot write \(args[2]): \(error)")
    }
    print("Wrote \(frames) frames (\(size.width)x\(size.height), \(data.count) bytes) to \(args[2])")
    exit(0)
}

var options = T8SimulatorOptions()
options.host = "0.0.0.0"
var quiet = false

@MainActor
func value(_ name: String) -> String {
    guard !args.isEmpty else { fail("\(name) needs a value") }
    return args.removeFirst()
}

@MainActor
func seconds(_ name: String) -> Duration {
    guard let s = Double(value(name)), s >= 0 else { fail("\(name) needs a number of seconds") }
    return .milliseconds(Int64(s * 1000))
}

while !args.isEmpty {
    let arg = args.removeFirst()
    switch arg {
    case "--host": options.host = value(arg)
    case "--port":
        guard let port = UInt16(value(arg)) else { fail("--port needs a number") }
        options.basePort = port
    case "--heartbeat":
        let text = value(arg)
        if text == "off" {
            options.heartbeatInterval = nil
        } else if let s = Double(text), s > 0 {
            options.heartbeatInterval = .milliseconds(Int64(s * 1000))
        } else {
            fail("--heartbeat needs seconds or off")
        }
    case "--empty-heartbeat": options.heartbeatPayload = []
    case "--clip-seconds":
        guard let n = Int(value(arg)), n > 0 else { fail("--clip-seconds needs a positive number") }
        options.clipSeconds = n
    case "--quiet": quiet = true
    case "--already-connected": options.faults.alreadyConnected = true
    case "--drop-after": options.faults.dropControlAfter = seconds(arg)
    case "--drop-after-packets":
        guard let n = Int(value(arg)), n > 0 else { fail("--drop-after-packets needs a positive number") }
        options.faults.dropControlAfterPackets = n
    case "--slow":
        guard let n = Int(value(arg)), n > 0 else { fail("--slow needs bytes per second") }
        options.faults.bytesPerSecond = n
    case "--latency":
        guard let ms = Int64(value(arg)), ms >= 0 else { fail("--latency needs milliseconds") }
        options.faults.replyLatency = .milliseconds(ms)
    case "--camera-stop-after": options.faults.cameraStopAfter = seconds(arg)
    case "--stall-data-after":
        guard let n = Int(value(arg)), n >= 0 else { fail("--stall-data-after needs a byte count") }
        options.faults.stallDataAfterBytes = n
    case "-h", "--help":
        print(usage)
        exit(0)
    default:
        fail("Unknown option \(arg)")
    }
}

let log = T8PacketLog()
if !quiet {
    // Unbuffered, so the log appears immediately even when redirected to a file.
    log.addObserver { entry in FileHandle.standardOutput.write(Data((entry.formatted(hexLimit: 32) + "\n").utf8)) }
}
let simulator = T8Simulator(options: options, log: log)
do {
    try await simulator.start()
} catch {
    fail("\(error)")
}
let address = await simulator.address
FileHandle.standardOutput.write(Data("T8 simulator ready: control \(address.controlPort), video \(address.videoPort), audio \(address.audioPort). Press Ctrl-C to stop.\n".utf8))
while true {
    try? await Task.sleep(for: .seconds(3600))
}
