# Changelog

## 2026-11-22 (cloud session: phases 1-6)
**Phase 1, protocol gaps (`research/T8_PROTOCOL.md` rev. 2).**
- Live video is TCP **7778**: LogonT's native decoder opens the URL `socket: -f h264` through a custom FFmpeg `socket` protocol compiled into the bundled `libavformat-56.so`, whose `url_open` connects to 192.168.100.1 port 0x1E62 (disassembled with `llvm-objdump`). No framing is added; FFmpeg probes raw H.264.
- Folder indexes for 40997: 0 Driving, 1 Event, 2 Parking, 3 Photo (the old spec said 2 for driving). They equal the 8200 type codes.
- Delete: LogonT has **no camera-side delete command**. Its swipe-to-delete removes the downloaded copy on the phone (`MainFileListView2.java:191-192`, manual p.13). Every command LogonT can send is now listed in the spec.
- Corrections: camera switch is 41014 (0xA036), not 40950; snapshot sends index 0 (a `sendData` quirk); 8201's "duration" is a frame count; the Wi-Fi password reply is 41009; SD partition change formats the card; LogonT ignores packets with an empty payload, including empty heartbeats.
- Six Haiku 5.5 checkers cross-checked 98 claims with quoted source lines: 97 confirmed, 1 mismatch in the claim's wording, re-read and resolved.

**Phase 2, `T8Kit` package (Swift 6, Foundation-only, builds on Linux).** Framing, streaming parser, payloads, config/settings/file-list models, `T8ControlSession` actor (handshake, heartbeat replies, camera stop, reply matching with timeouts), pull-based data streams with idle timeouts, H.264 Annex-B/SPS parsing, `T8MediaClient` downloads, POSIX transport, packet log. Unit tests use byte vectors from LogonT's hand-built frames.

**Phase 3, simulator.** `T8SimulatorKit` + `t8sim` CLI on 7777/7778/7779 with faults (already connected, drop after time or packets, slow link, reply latency, camera stop, stalled data port, empty heartbeats). Video comes from a dependency-free I_PCM H.264 encoder (decoded cleanly by FFmpeg 6.1 and VideoToolbox). End-to-end tests run T8Kit against it over loopback. 68 tests pass on Linux (Swift 6.0.3) and macOS (Xcode 26).

**Phase 4, iOS app.** Replaced the RTSP/VLC scaffold and dropped the Podfile. SwiftUI, iOS 17+, no dependencies: Live (AVSampleBufferDisplayLayer, front/rear, mirror, snapshot, voice toggle, clock sync), Recordings (paged folders, streamed playback with audio, save as MP4 with H.264 passthrough + AAC, to the app and Photos), Saved (swipe-to-delete with confirmation; camera-side delete is not possible, see above), Settings (camera options, warnings, Wi-Fi password behind a warning; format, factory reset and partition change are not offered), Diagnostics (checklist, packet log, shareable log). Network.framework transport pinned to Wi-Fi for the camera. Simulator connection target for testing at home.

**Phase 5, CI** (`.github/workflows/ci.yml`): T8Kit tests on Linux and macOS, XcodeGen, iOS Simulator tests (VideoToolbox decode, MP4 export, and integration tests against `t8sim`), unsigned `.ipa` artifact, on `macos-26` with the newest Xcode 26.

**Phase 6, review.** 23 Haiku line reviewers; every finding re-read before acting. The fixes and the dismissed findings with reasons are in `research/REVIEW_PHASE6.md`. CI run 1 also found a Network.framework end-of-stream bug and a duplicate static copy of T8Kit in the test bundle; both fixed.

**Other.** `CLAUDE.md` commands and architecture updated for the new layout. jadx 1.5.1 downloaded to the git-ignored `research/tools/` to decode the APK's resource strings (option labels).

## 2026-11-22 (repo made public)
- At the user's request, changed `cfeiwong85/DashcamViewer` from private to public, keeping all its contents, including the vendor APK, PDFs and decompiled sources. The user was told about the third-party material first and chose to keep it.
- Updated `CLAUDE.md` and `PLAN.md` to say the repo is public.

## 2026-11-22 (cloud handoff)
- The user approved `PLAN.md`. Added `HANDOFF.md` (state, phase-by-phase instructions for the cloud session) and `.gitignore` (excludes jadx and the unzipped APK).
- Initialised git and pushed the project to the private GitHub repo `cfeiwong85/DashcamViewer`.
- Updated `CLAUDE.md` with the repo name and the jadx download note.

## 2026-11-22 (/init)
- Rewrote `CLAUDE.md`: added the standard header, commands (syntax check, xcodegen, jadx re-decompile), the protocol-first architecture overview, and a note that the scaffold is throwaway. Kept the Haiku-only delegation policy, now also covering Workflow `agent()` calls.

## 2026-11-22 (plan)
- Added `PLAN.md`: interview decisions (SideStore for the 7-day issue, GitHub Actions cloud build, delete and Wi-Fi password controls only, simulator plus car testing) and a 7-phase build/test plan. Awaiting approval.

## 2026-11-22 (protocol research)
- Identified the camera: Eurostyle T8 (TOMO-CSE Autotrim, Singapore), old app LogonT (`com.logon_e100`).
- Downloaded the vendor LogonT APK, user manual and spec sheet into `research/vendor/`, and jadx 1.5.1 into `research/tools/`.
- Decompiled the APK into `research/decompiled/`. 10 Haiku 5.5 agents extracted the protocol line by line (`research/extraction.md`); the orchestrator verified the core.
- Added `research/T8_PROTOCOL.md`: IP 192.168.100.1, TCP 7777 control / 7778 video / 7779 audio, framing, session flow, file list, download, settings.
- Updated the `CLAUDE.md` build notes: the T8 uses a custom TCP protocol; the RTSP/VLC scaffold is to be replaced.

## 2026-11-22
- Created project `DashcamViewer` (SwiftUI, iOS 17+): Live view, Recordings browser (Novatek XML + HTML index parsing), Settings with presets, PlayerView (AVPlayer; MobileVLCKit for RTSP).
- Added `project.yml` (XcodeGen) and `Podfile` (MobileVLCKit).
- Added Haiku 5.5-only subagents `.claude/agents/haiku-line-reviewer.md` and `haiku-checker.md`.
- Added `CLAUDE.md` delegation policy: subagents must be Haiku 5.5 only; orchestrator edits and verifies.
- Not built: full Xcode is not installed on this Mac (only Command Line Tools).
