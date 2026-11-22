# Phase 6 line review (2026-11-22)

23 `haiku-line-reviewer` agents (Claude Haiku 5.5), one per Swift file or small group (the two 430-plus-line files in two halves), with the CLAUDE.md checklist and the relevant protocol facts inline. The orchestrator re-read every finding at its cited line before deciding. "Fixed" items have a code change; regression tests are in `T8Kit/Tests/T8KitTests/RegressionTests.swift` where a unit test can exercise them.

Verdicts: **Fixed** (real problem), **Not a bug** (with the reason), **Kept** (real but deliberately left, with the reason).

## T8Kit
| File:line | Finding | Verdict |
|---|---|---|
| ControlSession:113 | Concurrent `connect()` overwrites the first connection | Fixed: a second call throws `busy` (test) |
| ControlSession:131 | Handshake completes on a connection that dropped meanwhile | Fixed: identity check after the reply |
| ControlSession:140 | `disconnect()` can close a newer connection | Fixed: closes only the connection it started with |
| ControlSession:336 | A pending `stopPreview` sends PreViewOff after a new `startPreview` | Fixed: preview generation counter |
| ControlSession:336 | Cancellation during the 500 ms wait skips PreViewOff | Fixed: the wait is not cancellable, as in LogonT |
| ControlSession:366/368 | `Int32(index)` can trap | Fixed: index range-checked against the option list (test) |
| ControlSession:424 | Malformed 8201 looks like a timeout | Fixed: logged as malformed |
| DataStream:71 | Cancellation reported as a complete stream | Fixed: throws `CancellationError` (test) |
| DataStream:132 | Watchdog can close a read that just returned | Fixed: per-read generation checked under the lock |
| DataStream:119 | `total` not under the lock | Not a bug: only touched from the iterator's serial `next()` |
| POSIXTransport:85, 108 | `fcntl` / `getsockopt` results ignored | Fixed |
| POSIXTransport:103 | `poll` EINTR reported as failure | Fixed: retried until the deadline |
| POSIXTransport:164 | `ArraySlice.withUnsafeBytes` may not exist | Not a bug: it exists; builds on Linux and macOS |
| Transport:65 | `basePort + 2` traps above 65533 | Fixed: capped (test) |
| Bytes:8, 23 | Offset added before the bounds check | Fixed (test) |
| Recordings:51 | Short record slice traps | Fixed (test) |
| Recordings:92 | `isLast` wraps at `UInt32.max` | Fixed: 64-bit compare (test) |
| H264:230, 258, 259 | Malformed SPS overflows | Fixed: wrapping / 64-bit arithmetic, impossible sizes rejected (test) |
| H264:124 | Arbitrary slice order not detected | Kept: ASO is rare in camera encoders, but not verified for the T8; the car test will show it (frames would merge) |
| PacketParser:120 | Legacy short-frame check can split a valid 0x2002 frame | Fixed: a complete, valid declared frame wins |
| MediaClient:82 | Partial files left on error | Not a bug: `download` throws and callers delete the work folder |
| MediaClient:62 | Sibling `async let` keeps writing | Not a bug: cancellation closes its socket at once |
| MediaClient:117, 120 | WAV header `UInt32` conversion can trap | Fixed: clamping |
| Command:35 | 41009 reply comment vs 41010 name | Not a bug: LogonT dispatches 41009 (MainFreeView.java:264); 41010 is accepted defensively |
| Settings, Config, Payloads, Packet, PacketLog, TestPattern | — | No findings |

## Simulator and CLI
| File:line | Finding | Verdict |
|---|---|---|
| Simulator:115 | Port arithmetic overflow | Fixed: capped |
| Simulator:140 | Connection accepted just before `stop()` stays open | Fixed: closed when the simulator is stopped |
| Simulator:155 | Receive errors logged as a normal close | Fixed |
| Simulator:205 | Drop-after-packets counts every client | Fixed: per client |
| Simulator:226 | Session release keeps pending transfers and data sockets | Fixed: shared `releaseSession()` |
| Simulator:215 | Repeated NET_CONNECT_STATE(1) starts duplicate heartbeat timers | Fixed |
| Simulator:433, 437 | Negative stall value traps; stall loop outlives the data socket | Fixed: clamped; loop bounded to 2 minutes and stops with the simulator |
| Listener:101 | Port stays in LISTEN up to 100 ms after `close()` | Kept: harmless for tests (ephemeral ports) and the CLI |
| t8sim main:40-91 | Negative/infinite/huge values, typos and missing values accepted | Fixed: validated with clear messages |

## iOS app
| File:line | Finding | Verdict |
|---|---|---|
| NWTransport:127 | Error with `isComplete` reported as EOF | Fixed; also fixed the related CI failure (a read after the final read) |
| NWTransport:84 | Lock taken through a weak optional chain | Fixed |
| AppModel:143 | `disconnect()` during a connect leaves the new session connected | Fixed: generation counter |
| AppModel:111 | A session that dropped while reading the config is returned | Fixed |
| AppModel:107, 109 | Stale config; silent config-read failure | Fixed: cleared on reconnect; notice shown |
| AppModel:123, 127 | Events from an old session change state | Fixed: identity checks (the loop also ends on cancellation) |
| AppModel:98 | Socket left open when connect throws | Not a bug: the session tears down on handshake failure |
| LiveView:95-97 | Stop/start race resets activity and sends a stale PreViewOff | Fixed: stop waits for the stream task; start waits for the stop |
| LiveView:43-45 | Preview started after stop | Fixed: cancellation checks; stop sends PreViewOff afterwards |
| LiveView:52-55 | Clock-sync failure discarded | Fixed: logged |
| LiveView:74, 79 | Activity stays `.live` after a failure | Not a bug: leaving the tab (the only way to reach other camera screens) stops it |
| RecordingsView:22, 25, 129, 187 | Overlapping list loads; saves or playback during a list request | Fixed: `.listing` activity, one load at a time |
| RecordingsView:30 | Late error shown under another folder | Fixed: errors per folder |
| RecordingsView:137, 164 | Hard-coded `.front` | Not a bug: front is the only channel when there is no rear |
| PlaybackView:45, 47, 59 | Work after the view closed | Fixed: cancellation checks before 8200 and before writing |
| PlaybackView:85, 129 | Save while audio is still written | Fixed: waits for the audio stream first |
| PlaybackView:207, 233 | Cleanup deletes files during a save | Fixed: cleanup deferred until the save ends |
| PlaybackView:78, 98 | Audio start / file errors discarded | Fixed: logged |
| PlaybackView:97 | Audio writes on the main actor | Not a bug: 16 KB/s |
| PlaybackView:72 | Pacing counts non-picture units | Not a bug: the assembler yields one unit per picture |
| MP4Exporter:88, 106, 124 | Partial .mp4 left on failure | Fixed: writer cancelled, file removed |
| MP4Exporter:104, 122 | Silently skipped samples | Fixed: counted and logged |
| MP4Exporter:78 | Zero frame rate | Fixed: rejected |
| MP4Exporter:30 | Reader handle not closed on early exit | Not a bug: FileHandle closes on deinit |
| SavedLibrary:24 | `urls(...)[0]` | Fixed: fallback |
| SettingsView:120 | Re-selecting the current option sends a command | Fixed |
| SettingsView:28, 34 | Empty host, port 0 | Fixed: port 1-65533; empty host refused at connect |
| DiagnosticsView:155 | Port test while connecting opens a second connection | Fixed |
| DiagnosticsView:14 | Log file name collision | Fixed |
| H264Renderer, PCMPlayer, RootView, App, SavedView | — | No findings |
