# Changelog

## 2026-11-22 (phase 7: car-test checklist)
- PR #1 (phases 1-6, the install guide and the CLAUDE.md refresh) was merged into `main` as 90db1d0 at the user's request.
- New `docs/CAR_TEST_CHECKLIST.md`: one in-car test round with step IDs for reporting. It covers a baseline with LogonT on the old phone (lights, file lists, settings screenshots), then connection and live view, recordings and playback, saving, deleting saved copies, settings (read, compare with LogonT, change and change back), the one-phone rule, whether the camera records again after the app disconnects (open item 6 in `research/T8_PROTOCOL.md`), optional Video Quality and Wi-Fi password changes, and how to send the log. A closing table maps the protocol's open items to steps. Button labels and messages were checked against the app source. The camera facts (LED names and meanings, LogonT's screens, the factory Wi-Fi password) come from the scanned manual in `research/vendor/manual.pdf`.
- The checklist warns that the diagnostics log contains the camera's Wi-Fi password and serial number, because the GET_CONFIG reply is logged in full hex (the password is at payload offset 124), so the log should stay out of public places.
- `docs/INSTALL_SIDESTORE.md`: the download link now points only at `main` (the branch link was for before the merge); links to the checklist.
- `PLAN.md` and `CLAUDE.md`: phase 7 status; `CLAUDE.md` says to keep the checklist's quoted UI text in sync with the app.

## 2026-11-22 (CLAUDE.md refresh)
- `CLAUDE.md`: phase 7 status (install guide written; car-test checklist and in-car testing remain); a `docs/` entry saying to re-check the SideStore steps against docs.sidestore.io before editing the guide; why the integration tests allow 20 s per reply; the `ci-logs` artifact (`t8sim.log`, `test.log`, `Tests.xcresult`) that CI uploads when the iOS job fails.

## 2026-11-22 (CI: integration-test time limits)
- CI run 44995587731 (commit ea9b64e, documentation only) failed `IntegrationTests.testListDownloadAndExport` with "No reply from the camera to NET_CONNECT_STATE." The logs show the cause: the test's request was in `t8sim`'s socket at 06:57:31.5, but `t8sim` accepted the connection 2.4 s later and read the request 9.9 s later, after the test's 5 s limit. During the same 20 seconds it took 1 s to answer an ordinary request that the next test got answered in 1 ms. The iOS-side test process stayed fast throughout. `t8sim` does no blocking or heavy work on Swift's task threads, so it was starved for CPU on the shared runner while the iOS Simulator was busy; the code behaved correctly but slowly.
- `DashcamViewerTests/IntegrationTests.swift`: the session's reply and file-list page limits are now 20 s in these tests (they were 5 s and the 8 s default). The app's own limits for the real camera are unchanged. A missing reply still fails the test, only later.

## 2026-11-22 (phase 7: SideStore install guide)
- Added `docs/INSTALL_SIDESTORE.md`: one-time setup (LocalDevVPN on the iPhone, iloader on the Mac, SideStore install, trusting the developer app, Developer Mode, first refresh), downloading the `.ipa` artifact from Actions, installing it from SideStore's My Apps, first launch (Local Network permission), the 7-day refresh, installing newer builds without losing saved clips, the free-account limits, and troubleshooting (connection errors, pairing file replacement, sign-in).
- Checked against the official SideStore docs (prerequisites, install, pairing file, FAQ, troubleshooting), iloader 2.4.2's source ("SideStore (Stable)" downloads SideStore's latest GitHub release) and SideStore's source at tag 0.7.0-alpha (the **+** button in My Apps opens a Files picker for `.ipa` files). SideStore now installs with iloader and LocalDevVPN, not AltServer.
- `PLAN.md`: phase 7 status; the refresh risk now says Wi-Fi with internet and LocalDevVPN are needed (SideStore's docs say mobile data is not suitable), which corrects "home Wi-Fi or cellular".

## 2026-11-22 (/init after phases 1-6)
- Updated `CLAUDE.md` for the code as it now stands:
  - Status: phases 1-6 done, phase 7 next.
  - Commands: running one test suite or one test (`swift test list`, `--filter`); the actual `t8sim` options, including `--host`, `--heartbeat`, `--clip-seconds` and `dump-video`; running the iOS Simulator tests with `TEST_RUNNER_T8SIM_PORT` and `-only-testing`; installing Swift 6.0.3 in a Linux container.
  - A new "How the pieces connect" section: the transport protocols (POSIX vs Network.framework), `T8Address`, the session, streams and shared packet log, and the `MediaActivity` gate that keeps the camera to one stream at a time.
  - The test frameworks; why the test bundle links only `T8Kit`; and a pointer to `research/REVIEW_PHASE6.md`.

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

**Result.** CI run 4 (commit 713d908) is green: 68 T8Kit tests on Linux and macOS, 7 of 7 iOS Simulator tests (3 against `t8sim`), and the `DashcamViewer-unsigned-ipa` artifact. Opened PR #1 to `main` with what was built, what is still unconfirmed and the next steps (install with SideStore, then test in the car).

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
