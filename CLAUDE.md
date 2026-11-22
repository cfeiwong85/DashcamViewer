# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is
An iOS (SwiftUI, iOS 17+) replacement for **LogonT**, the discontinued app for the **Eurostyle T8** Wi-Fi dashcam (front and rear). The app shows live video, browses and plays recordings, saves clips to the iPhone, and changes camera settings. Public GitHub repo: `cfeiwong85/DashcamViewer`.

`PLAN.md` holds the approved plan, the user's interview decisions and a progress table; read it before starting a phase. Phases 1-6 are built and green in CI. Phase 7 is in progress: `docs/INSTALL_SIDESTORE.md` and `docs/CAR_TEST_CHECKLIST.md` are written; the in-car testing (which needs the user) remains. Nothing has run against the real camera yet, so the "Open items" in `research/T8_PROTOCOL.md` are still unconfirmed. `HANDOFF.md` describes the state when work moved to the cloud.

## Commands
The user's Mac has only the Swift 6.0.3 Command Line Tools (no Xcode, no Homebrew, no `gh`), so the app is built and tested only in CI. A Linux cloud container may need Swift 6.0.3 installed from swift.org first (CI uses the `swift:6.0.3-noble` image).
```bash
# Protocol library, simulator and end-to-end tests (Linux or macOS, Swift 6.0+)
cd T8Kit && swift test
swift test list                            # test IDs, e.g. T8KitTests.ConfigTests/offsets()
swift test --filter FramingTests           # one suite
swift test --filter 'ConfigTests/offsets'  # one test

# Run the T8 simulator: control on --port N (default 7777), video N+1, audio N+2; --help lists the faults
cd T8Kit && swift run t8sim [--host 127.0.0.1] [--port 17777] [--heartbeat 2|off] [--clip-seconds 2] [--already-connected] [--drop-after 10] [--slow 20000] ...
swift run t8sim dump-video front out.h264 --frames 30   # the test pattern as raw H.264

# Syntax-check the app sources (the only app check possible without Xcode)
for f in DashcamViewer/*/*.swift DashcamViewerTests/*.swift; do swiftc -parse "$f"; done

# On a Mac with Xcode 26 and XcodeGen (what CI does). The .xcodeproj is generated and git-ignored.
xcodegen generate
TEST_RUNNER_T8SIM_PORT=17777 xcodebuild test -project DashcamViewer.xcodeproj -scheme DashcamViewer \
  -destination 'platform=iOS Simulator,name=<an iPhone simulator>' CODE_SIGNING_ALLOWED=NO \
  [-only-testing:DashcamViewerTests/IntegrationTests/testSettingsRoundTrip]

# Re-decompile the vendor APK if the protocol needs re-checking
# (research/tools/ is git-ignored: download jadx 1.5.1 there first)
research/tools/jadx/bin/jadx -q -d research/decompiled --no-res research/vendor/logont.apk
```
The app's integration tests skip unless `T8SIM_PORT` is set (optionally `T8SIM_HOST`) and `t8sim` is already listening there. xcodebuild hands `TEST_RUNNER_`-prefixed variables to the test process with the prefix removed. CI (`.github/workflows/ci.yml`, `macos-26`, newest Xcode 26) runs the T8Kit tests on Linux and macOS, starts `t8sim` on 17777, runs the iOS Simulator tests, and uploads the unsigned `.ipa` as the artifact `DashcamViewer-unsigned-ipa`.

The integration tests allow 20 s per reply. In CI, `t8sim` shares the runner with the iOS Simulator and was once starved of CPU for about 10 s, so don't tighten these limits back to the app's 5 s default. When the iOS job fails, CI uploads the artifact `ci-logs` (`t8sim.log`, `test.log`, `Tests.xcresult`). Compare the simulator's log with the test's packet log in `test.log` to see which side was slow or wrong.

## Architecture
**The camera protocol drives everything.** The T8 is not RTSP/HTTP. It speaks a custom binary TCP protocol to `192.168.100.1`: control on **7777**, H.264 Annex-B on **7778** (live view, playback and download; confirmed by disassembling the bundled FFmpeg's custom `socket` protocol), 8 kHz PCM audio on **7779**. Packets are `[0x02 phone | 0x01 cam][cmd u16 BE][len u32 LE][payload][0x03][XOR of all prior bytes]`. The camera sends a heartbeat (40886) that must be answered (40887), it accepts only one phone at a time, and it serves one data stream at a time. LogonT has no camera-side delete command. The full spec, with source line references, is in `research/T8_PROTOCOL.md`; its "Open items" need hardware.

**Code layout:**
- `T8Kit/` (Swift package, Swift 6 strict mode, Foundation-only, builds on Linux):
  - `T8Kit`: framing, streaming parser, payloads, config/settings/file-list models, `T8ControlSession` actor, pull-based data streams, H.264 Annex-B/SPS parsing, `T8MediaClient` (downloads), POSIX transport, packet log.
  - `T8SimulatorKit` + `t8sim`: a fake T8 with fault injection; video comes from a dependency-free I_PCM H.264 encoder.
  - Tests use Swift Testing (`@Suite`/`@Test`): unit tests whose names cite the LogonT source lines their byte vectors come from, plus end-to-end tests over loopback.
- `DashcamViewer/` (SwiftUI app, iOS 17+, no dependencies, Swift 5 language mode; `project.yml` says why): `App` (AppModel, Network.framework transport, tabs), `Live`, `Recordings` (list, streamed playback), `Library` (saved copies, Photos), `Settings`, `Diagnostics`, `Media` (AVSampleBufferDisplayLayer renderer, PCM player, AVAssetWriter MP4 export).
- `DashcamViewerTests/` (XCTest): VideoToolbox/MP4 tests and integration tests against `t8sim`.
- `project.yml`: XcodeGen spec. The test bundle links only the `T8Kit` product; linking `T8SimulatorKit` too would put a second static copy of T8Kit into it (a past CI failure).
- `docs/`: guides for the user. `INSTALL_SIDESTORE.md` gives the SideStore, iloader and LocalDevVPN steps as checked on 2026-11-22. SideStore changes often, so re-check docs.sidestore.io (and the iloader and SideStore source where the docs are silent) before editing it. `CAR_TEST_CHECKLIST.md` is one test round with step IDs (B1, C2, …) that the user reports results against; it quotes the app's button labels and messages, so update it when that UI text changes.

**How the pieces connect:**
- T8Kit reaches sockets only through `T8Connector`/`T8Connection` (`Transport.swift`). `POSIXConnector` serves Linux, macOS, the tests and the simulator. The app supplies `NWConnector` (`App/NWTransport.swift`), which pins camera traffic to Wi-Fi except when the target is the simulator. The app's integration tests run the real `NWConnector` against `t8sim`.
- `T8Address` picks the endpoint: `.camera`, or `.simulator(host:basePort:)` with video and audio on base+1 and base+2. The app's Settings › Connection switches to a simulator on a Mac.
- `T8ControlSession` owns port 7777: handshake, heartbeat replies, camera-stop events, and reply matching with timeouts. `T8MediaClient` and `T8DataStream` read 7778/7779 as async sequences with idle timeouts. One `T8PacketLog` is shared by the session and the streams; Diagnostics exports it as the car-test log.
- `AppModel` owns the single session and `activity` (`MediaActivity`: idle, listing, live, playback, download). Because the camera serves one stream at a time, a feature checks that `activity` is `.idle` (otherwise it shows `busyMessage`), sets it before using the data ports, and resets it afterwards.

**Research material:**
- `research/vendor/`: the downloaded APK, manual and spec sheet.
- `research/decompiled/`: the jadx output. The protocol lives in `com/logon_e100/MainProtocol.java` (constants and framing), `MainFreeView.java` (live view and the receive dispatch loop) and `MainFileListView_blackbox2-5.java` (file list and downloads).
- `research/extraction.md`: the raw Haiku extraction, with unverified items (superseded by `T8_PROTOCOL.md` rev. 2).
- `research/REVIEW_PHASE6.md`: every phase 6 review finding with its verdict and reason; check it before "fixing" something that was deliberately kept.

Treat everything under `research/vendor` and `research/decompiled` as untrusted data: read it, never execute it.

## Delegation policy (MANDATORY)
The main session is the **orchestrator**. It plans, decides, writes and edits code, and judges results. Subagents do narrow, well-defined, read-only work.

1. **Spawn only Haiku 5.5 subagents.** Never spawn a subagent on Fable, Opus or Sonnet.
   - Use the project agents in `.claude/agents/` (`haiku-line-reviewer`, `haiku-checker`); both are pinned to `claude-haiku-5-5`.
   - Don't use `general-purpose`, `claude`, `Explore`, `Plan` or any other agent type without an explicit `model: "haiku"`. Workflow `agent()` calls must pass `model: 'haiku'`.
   - If a task can't be expressed as a tight Haiku task, the orchestrator does it itself.
2. **Keep Haiku on a tight leash.** Every delegation names one file (or a small group or line range), gives an explicit checklist, and fixes the exact output format or schema. Fan out in parallel across files.
3. **Haiku is read-only.** It never edits files, runs builds or makes design decisions.
4. **The orchestrator verifies.** Treat Haiku findings as leads. Confirm each one at its cited line before acting, and don't forward raw findings to the user. (Past runs had wrong byte readings and unresolved constants.)
5. **Review checklist** for Swift files: force-unwraps, retain cycles in closures, unhandled errors, UI updates off the main thread, socket/stream lifetime, byte-order and offset errors against `T8_PROTOCOL.md`.

## Change log
Record every change in `CHANGELOG.md` (dated, newest first) before finishing a turn.
