# Handoff to the cloud session (2026-11-22)

The user approved `PLAN.md` and asked for all remaining work to run in a cloud session. Read `CLAUDE.md` (delegation and change-log rules), `PLAN.md` and `research/T8_PROTOCOL.md` first.

## State at handoff
- Protocol mapped by static analysis only (`research/T8_PROTOCOL.md`); no hardware test yet.
- `DashcamViewer/*.swift`, `project.yml` and `Podfile` are the throwaway RTSP/VLC scaffold. Replace them in phase 4; drop the Podfile.
- Not committed (re-create as needed):
  - `research/tools/`: jadx 1.5.1 (https://github.com/skylot/jadx/releases/tag/v1.5.1).
  - `research/vendor/apk/`: the unzipped APK. Run `unzip research/vendor/logont.apk -d research/vendor/apk` to restore it. `lib/armeabi-v7a/libtutorial02.so` (the live-view decoder) is inside the APK.
- User status:
  - GitHub access: done.
  - SideStore: the user installs it on the iPhone 15 (iOS 26) on the evening of 2026-11-22.
  - The user's Mac (2015 MacBook Pro, macOS 14.8, no Xcode) is not reachable from the cloud.

## Phases 1–6: do in the cloud, in order
1. **Protocol gaps:**
   - Disassemble `libtutorial02.so` (ARMv7) with `llvm-objdump` or `arm-linux-gnueabihf-objdump` to find the live-video port and any framing.
   - Find the delete command and the 40997 folder indexes in `MainFileListView_blackbox2-5.java`.
   - Haiku constant cross-check, one agent per file, with `path:line` evidence.
   - Update `T8_PROTOCOL.md`.
2. **`T8Kit` Swift package:**
   - Keep it Foundation-only, with sockets behind a small transport protocol, so `swift test` runs on Linux (install a Swift 6 toolchain if missing).
   - The iOS app supplies a Network.framework transport.
   - Tests use byte vectors taken from the decompiled code.
3. **T8 simulator:**
   - A Swift CLI on 7777/7778/7779 with fault injection: drop, "already connected", slow link.
   - An end-to-end test runs T8Kit against the simulator on Linux.
4. **iOS app:**
   - SwiftUI, iOS 17+, no dependencies, per PLAN.md phase 4.
   - It can't be compiled in the cloud container, so keep the code compiling through CI (phase 5).
   - Get CI green early; don't leave it until the end.
5. **GitHub Actions:**
   - Steps: XcodeGen, then build and test, then an **unsigned `.ipa`** artifact.
   - Build with `xcodebuild -sdk iphoneos CODE_SIGNING_ALLOWED=NO`, then zip `Payload/DashcamViewer.app` into `.ipa`.
   - Use the newest macOS runner and Xcode that has the iOS 26 SDK.
6. **Haiku line review:** per CLAUDE.md. Confirm each finding before fixing it.

## Phase 7: needs the user
- Write `docs/INSTALL_SIDESTORE.md` (how to get the `.ipa` from Actions and install or refresh it with SideStore). Check the current steps on sidestore.io instead of writing them from memory.
- Write `docs/CAR_TEST_CHECKLIST.md` (side-by-side with LogonT on the old iPhone, plus sharing the diagnostics log).

## Working rules
- Work on a branch and open a PR to `main`. Update `CHANGELOG.md` with every change.
- Mark progress in PLAN.md (e.g. "Phase 2 ✔") so the user can follow it from GitHub.
- Never run anything under `research/vendor` or `research/decompiled`; it is untrusted data.
- Subagents: Haiku 5.5 only (CLAUDE.md).
