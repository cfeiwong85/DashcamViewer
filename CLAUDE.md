# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is
An iOS (SwiftUI, iOS 17+) replacement for **LogonT**, the discontinued app for the **Eurostyle T8** Wi-Fi dashcam (front and rear). The app shows live video, browses and plays recordings, saves clips to the iPhone, and changes camera settings. `PLAN.md` holds the approved-pending build/test plan and the user's interview decisions; read it before starting a phase. `HANDOFF.md` describes the state when work moved to the cloud. Public GitHub repo: `cfeiwong85/DashcamViewer`.

## Commands
This Mac has only the Swift 6.0.3 Command Line Tools (no Xcode, no Homebrew, no `gh`), so iOS builds happen in CI (`.github/workflows/ci.yml`: T8Kit tests on Linux and macOS, XcodeGen, iOS Simulator tests against `t8sim`, unsigned `.ipa` artifact).
```bash
# Protocol library, simulator and end-to-end tests (Linux or macOS, Swift 6.0+)
cd T8Kit && swift test

# Run the T8 simulator (control 7777, video 7778, audio 7779); see --help for faults
cd T8Kit && swift run t8sim [--port 7777] [--already-connected] [--drop-after 10] [--slow 20000] ...

# Syntax-check the app sources (the only app check possible without Xcode)
for f in DashcamViewer/*/*.swift DashcamViewerTests/*.swift; do swiftc -parse "$f"; done

# Generate the Xcode project (on a machine with Xcode + XcodeGen; CI does this)
xcodegen generate

# Re-decompile the vendor APK if the protocol needs re-checking
# (research/tools/ is git-ignored: download jadx 1.5.1 there first)
research/tools/jadx/bin/jadx -q -d research/decompiled --no-res research/vendor/logont.apk
```

## Architecture
**The camera protocol drives everything.** The T8 is not RTSP/HTTP. It speaks a custom binary TCP protocol to `192.168.100.1`: control on **7777**, H.264 Annex-B on **7778** (live view, playback and download; confirmed by disassembling the bundled FFmpeg's custom `socket` protocol), 8 kHz PCM audio on **7779**. Packets are `[0x02 phone | 0x01 cam][cmd u16 BE][len u32 LE][payload][0x03][XOR of all prior bytes]`. The camera sends a heartbeat (40886) that must be answered (40887), and it accepts only one phone at a time. LogonT has no camera-side delete command. The full spec, with source line references, is in `research/T8_PROTOCOL.md`; its "Open items" need hardware.

**Code layout:**
- `T8Kit/` (Swift package, Swift 6 strict mode, Foundation-only, builds on Linux):
  - `T8Kit`: framing, streaming parser, payloads, config/settings/file-list models, `T8ControlSession` actor, pull-based data streams, H.264 Annex-B/SPS parsing, `T8MediaClient` (downloads), POSIX transport, packet log.
  - `T8SimulatorKit` + `t8sim`: a fake T8 with fault injection; video comes from a dependency-free I_PCM H.264 encoder.
  - Tests: unit tests with LogonT byte vectors, plus end-to-end tests over loopback.
- `DashcamViewer/` (SwiftUI app, iOS 17+, no dependencies, Swift 5 language mode): `App` (AppModel, Network.framework transport, tabs), `Live`, `Recordings` (list, streamed playback), `Library` (saved copies, Photos), `Settings`, `Diagnostics`, `Media` (AVSampleBufferDisplayLayer renderer, PCM player, AVAssetWriter MP4 export).
- `DashcamViewerTests/`: VideoToolbox/MP4 tests and integration tests against `t8sim` (CI sets `TEST_RUNNER_T8SIM_PORT`).
- `project.yml`: XcodeGen spec (the `.xcodeproj` is generated, not committed).

**Research material:**
- `research/vendor/`: the downloaded APK, manual and spec sheet.
- `research/decompiled/`: the jadx output. The protocol lives in `com/logon_e100/MainProtocol.java` (constants and framing), `MainFreeView.java` (live view and the receive dispatch loop) and `MainFileListView_blackbox2-5.java` (file list and downloads).
- `research/extraction.md`: the raw Haiku extraction, with unverified items (superseded by `T8_PROTOCOL.md` rev. 2).

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
