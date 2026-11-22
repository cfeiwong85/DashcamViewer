# T8 Viewer: build and test plan (approved 2026-11-22; phases 1–6 run in a cloud session, see HANDOFF.md)

## Interview decisions (2026-11-22)
| Topic | Decision |
|---|---|
| Camera | Eurostyle T8, front and rear connected; old app LogonT |
| Phone | iPhone 15, iOS 26 |
| Features | Live view, play recordings, save to iPhone, camera control, file browser |
| Risky controls | Include **delete recordings** and **change Wi-Fi password**. Exclude format SD and factory reset |
| Install / 7-day issue | Free Apple ID + **SideStore** (auto-refreshes on the phone every 7 days, no Mac needed after setup) |
| Build | **Cloud build**: GitHub Actions macOS runner, private repo, unsigned `.ipa`, SideStore signs it |
| This Mac | MacBookPro12,1 (2015), macOS 14.8, Swift 6.0.3 CLI only (no Xcode). Used for protocol tests, the simulator and SideStore pairing |
| Reference device | Old iPhone/iPad with LogonT, which still works with the T8 |
| Testing | T8 simulator on this Mac first, then in the car with a shareable diagnostics log |

## Key technical finding
The T8 does **not** use RTSP/HTTP. LogonT talks a custom binary TCP protocol to `192.168.100.1` (control 7777, video 7778 = raw H.264, audio 7779 = 8 kHz PCM); see `research/T8_PROTOCOL.md`. iOS can decode raw H.264 natively (VideoToolbox), so **no third-party libraries** are needed. The earlier RTSP/VLC scaffold gets replaced.

## Phases
**0. Prerequisites (you)**
- GitHub access for me: authorize the GitHub connector via `/mcp`, or install the `gh` CLI (official .pkg from cli.github.com, since Homebrew isn't installed) and run `gh auth login`.
- SideStore: one-time install and pairing from this Mac. I'll give step-by-step instructions when we get there.

**1. Close the protocol gaps (me, plus Haiku checkers)**
- Disassemble `libtutorial02.so` (`objdump`, ARM) to confirm the live-video port and any framing before the H.264 data.
- Find the delete command and the folder-index mapping for the file list (`MainFileListView2-5`, swipe handlers).
- Haiku: one agent per file, cross-checking every constant in `T8_PROTOCOL.md` against the source, with `path:line` evidence.

**2. `T8Kit` Swift package (platform-independent, tested on this Mac with `swift test`)**
- Packet encoder/decoder with XOR CRC; streaming parser for partial reads.
- `ControlSession` actor: connect, the handshake sequence, heartbeat reply (40886→40887), timeouts, "another phone already connected" error.
- Typed commands: live start/stop, camera switch, snapshot, voice toggle, set time, file list (paged), download, delete, settings, Wi-Fi password.
- Unit tests with byte vectors copied from the decompiled code (exact packets LogonT sends).

**3. T8 simulator (Swift CLI on this Mac)**
- Listens on 7777/7778/7779, follows the protocol, serves sample H.264/PCM clips and a fake file list, and sends heartbeats.
- Fault injection: dropped connection, "already connected", slow link.

**4. iOS app (SwiftUI, iOS 17+, no dependencies)**
- **Live:** H.264 Annex-B → `AVSampleBufferDisplayLayer`; front/rear switch, snapshot, voice-record toggle.
- **Recordings / file browser:** Driving / Event / Parking / Photo folders, paging, front/rear choice, swipe-to-delete with confirmation.
- **Play and save:** stream to the player; save as `.mp4` (H.264 passthrough + PCM→AAC via `AVAssetWriter`) to Photos or Files.
- **Settings:** camera options read from the config struct, time sync, Wi-Fi password change behind a warning screen.
- **Diagnostics:** packet log (hex) with a Share button, and a connection checklist (on the T8 Wi-Fi? gateway = 192.168.100.1?).
- Info.plist: local-network permission, plus a hint to keep the dashcam Wi-Fi connected (iOS may prompt "no internet").

**5. CI (GitHub Actions)**
- On push: XcodeGen, build, unit tests, iOS-simulator integration test against the T8 simulator, then an **unsigned `.ipa`** artifact.

**6. Review (Haiku line-by-line, per `CLAUDE.md`)**
- One `haiku-line-reviewer` per Swift file with a fixed checklist; I confirm each finding and make the fixes.

**7. In-car testing (you, guided)**
- Install the `.ipa` via SideStore → run a checklist side by side with LogonT on the old iPhone → share the diagnostics log → I fix and push → repeat.
- Done when live front/rear, browsing, playing, saving, deleting and the settings changes all work on the real T8.

## Risks
- The live-video port or framing may differ from the static reading. The simulator and diagnostics log keep each car-test round short.
- SideStore needs internet to refresh (home Wi-Fi or cellular), not the dashcam Wi-Fi. If a refresh is missed, the app stops opening until you refresh in SideStore; nothing is lost.
- Free Apple ID limit: 3 sideloaded apps at once (SideStore itself counts as one).
