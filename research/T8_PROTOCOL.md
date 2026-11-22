# Eurostyle T8 / LogonT Wi-Fi protocol (reverse-engineered)

Source: official LogonT Android APK (`com.logon_e100`) from the vendor support page tomocse.com.sg/eurostyle-t8-support/, decompiled with jadx 1.5.1. Native libraries (`lib/armeabi-v7a/*.so`) disassembled with `llvm-objdump`.
Status: built from static analysis only. **Nothing has been tested against a real camera yet.**
Revision 2 (2026-11-22, cloud session): every row below was read by the orchestrator at the cited line, and cross-checked by one Haiku 5.5 checker per source file (see "Verification" at the end). Paths are relative to `research/decompiled/sources/com/`; `MP` = `logon_e100/MainProtocol.java`, `MFV` = `logon_e100/MainFreeView.java`, `MFFV` = `logon_e100/MainFileFreeView.java`, `BB2..BB5` = `logon_e100/MainFileListView_blackbox2..5.java`.

## Network
| Item | Value | Ref |
|---|---|---|
| Camera IP | `192.168.100.1` (LogonT only proceeds if the Wi-Fi DHCP gateway equals this) | MP:116, MFV:1473-1480 |
| SSID | Printed on the device; manual shows `LogOn T8 xxxxxx` | manual p.10 |
| Wi-Fi password | Default `12345678`; LogonT accepts exactly 8 chars, no spaces | vendor page, `logon/e100/setting/ActivityWifiPassword.java` checkPassword |
| Control | TCP **7777**, one persistent socket, 3000 ms connect timeout | MP:117, MFV:687 (`PathInterpolatorCompat.MAX_NUM_POINTS` = 3000) |
| Video (live, playback, download) | TCP **7778**, a new socket per stream, raw H.264 Annex-B until the camera closes it | MP:119, BB2:816-848, native analysis below |
| Audio (playback, download) | TCP **7779**, a new socket per stream, raw PCM 8 kHz mono signed 16-bit LE until EOF | MP:118, BB2:746-776, MFFV:337 (`AudioTrack(3, 8000, 4, 2, ...)`) |

### Live-video port (closed 2026-11-22 by disassembly)
- `MFV.screen_mode()` calls the native `naInit("abc","abc","a",0)` (MFV:759).
- `libtutorial02.so` `naInit` (0x2988) copies the literal `"socket: -f h264"` (rodata 0x5ff0) into `videofilename_buf`, sets FFmpeg option `probesize=4096` when the third argument is `"a"` (33554432 when `"b"`, used by playback MFFV:425), then calls `avformat_open_input(&ctx, "socket: -f h264", NULL, &opts)`. FFmpeg picks the protocol from the text before `:`.
- The bundled `libavformat-56.so` registers a **custom `socket` URL protocol** (URLProtocol struct at file offset 0x1b47b4, name ptr 0x18ee14 = `"socket"`). Its `url_open` (ARM code at 0x1384dc):
  - `ff_socket(AF_INET=2, SOCK_STREAM=1, 0)`;
  - `sin_addr` = `inet_addr("192.168.100.1")` (string at 0x18ede8, loaded via literal 0x568d4 + pc 0x138514);
  - `sin_port` halfword `0x621e` stored at `[sp,#10]` → network-order bytes `1E 62` → **port 7778**;
  - `ff_listen_connect(fd, &addr, 16, timeout=0x1388=5000 ms, h, 0)`; on success `h->is_streamed = 1`.
  - Its `url_read` (0x13847c) is a plain `recv(fd, buf, size, 0)`: **no framing** is added by the transport.
- The format is probed (NULL input format) from the first 4096 bytes, so the stream is whatever FFmpeg's probe recognises; the developer's intent (`-f h264`) and the download path (BB2 saves 7778 bytes to a file that `MainMP4ParSer` reads with mp4parser `H264TrackImpl`, an Annex-B parser) both say **raw H.264 Annex-B**.
- Order in LogonT: control reply 8193 → send 511 → 1000 ms later `naInit` opens 7778 (MFV:191-192, 945-972).

## Packet framing (control port 7777)
```
phone -> cam : [0x02][cmd_hi][cmd_lo][len u32 LE][payload ...][0x03][crc]
cam -> phone : [0x01][cmd_hi][cmd_lo][len u32 LE][payload ...][0x03][crc]
crc = XOR of every byte from index 0 through the 0x03 trailer
```
- Send: MP:169-206. `Utils.makeCRC` XORs the whole zero-filled 1024-byte buffer, so the CRC equals the XOR of bytes 0..trailer (`logon/e100/utils/Utils.java:63-69`).
- Receive (MFV:163-189): read 7 header bytes; ignore the packet unless byte 0 is `0x01`; cmd = bytes 1-2 big-endian; len = bytes 3-6 little-endian; drop (and reset the buffer) if len > 100000; if len ≤ 0 read the 2 trailer bytes and **dispatch nothing**; else read `len` payload bytes, read the 2 trailer bytes, dispatch on cmd. LogonT never checks the received CRC.
- `CNSSimpleIndex` = one `i32 LE` (MP:253-272). `CNSResult` = one `i32 LE`, negative = error (MP:208-223).
- `MainProtocol.sendData(cmd, a, b, act)` writes the **length of `a`** but copies the **bytes of `b`** (MP:144 vs MP:147).

### Hand-built frames (bypass `send`)
LogonT builds some frames by hand. Their bytes, for test vectors:
| Function | Bytes (hex) | Notes | Ref |
|---|---|---|---|
| `WiFiConnect` / `wificon` (41022 idx 1) | `02 A0 3E 04 00 00 00 01 00 00 00 03 CRC` | well formed | MFV:880-909 |
| `IpCamStart` (8193) | `02 20 01 08 00 00 00 01 00 00 00 00 00 00 00 03 CRC` | well formed, 8-byte payload `[1,0,0,0,0,0,0,0]` | MFV:911-943 |
| `freeviewcon` (511) | `02 01 FF 00 00 00 00 03 CRC` | well formed, empty | MFV:945-958 |
| `CamChange` (41014) | `02 A0 36 04 00 00 00 00 00 00 00 03 CRC` | well formed, idx 0 | MFV:985-1003 |
| `RecordOnOff` (41018 idx 0) | `02 A0 3A 04 00 00 00 00 00 00 00 03 CRC` | well formed | MFV:1150-1169 |
| `PreViewOff` (8194) | `02 20 02 04 00 00 00 03 CRC` | **malformed**: length says 4, no payload | MFV:1123-1138, BB2:484-499 |
| `freeviewstop` (8194) | `02 20 02 04 00 00 00 00 03 CRC` | **malformed**: length 4, 1 payload byte | MFFV:577-592 |
| `Filedown` / `Fileview` (8200) | `02 20 08 0C 00 00 00 TT 00 00 00 II II II II CC 00 00 00 03 CRC` | well formed: payload = `type u32, index u32, channel u32` | BB2:691-719, MFFV:604-656 |
| `SdCardCheck` (0x2A04) | `02 2A 04 00 00 00 00 00 00 00 03 CRC` | malformed; **never called** | BB2:511-529 |
| `FileListTrans` (0x2004) | `02 20 04 00 00 00 01 00 00 00 03 CRC` | malformed; **never called** | BB2:550-568 |

The camera accepted LogonT's two malformed 8194 frames, so its parser is probably lenient (each frame arrives in its own TCP segment after a `flush()`). T8Kit sends the exact `PreViewOff` bytes as LogonT does.

## Command table
Reply = the command the receive loop dispatches on (MFV:190-289). "—" = LogonT does not handle a reply.
| Cmd | Hex | Name | Payload sent | Reply | Ref |
|---|---|---|---|---|---|
| 511 | 0x01FF | freeview start | none | — | MFV:945 |
| 8193 | 0x2001 | IPCAM_START | 8 bytes `01 00 00 00 00 00 00 00` | **8193** (any payload > 0 triggers 511) | MFV:911, MFV:191 |
| 8194 | 0x2002 | preview stop | see malformed frame | — | MFV:1123 |
| 8200 | 0x2008 | FILE_TRANS_START (play/download) | `type, index, channel` (3×u32) | 8201 `{fileSize u32, duration u32}`; duration = **frame count** | BB2:691, MP:225-251, MFFV:1017-1031, MFFV:541 |
| 40864 | 0x9FA0 | driving impact sensitivity | idx | 40865 result | MP:80 |
| 40866 | | voice volume | idx | 40867 | MP:105 |
| 40868 | | LCD brightness | idx | 40869 | MP:76 |
| 40870 | | contrast | idx | 40871 | MP:70 |
| 40872 | | screen off time | idx | 40873 | MP:95 |
| 40874 | | block low voltage (parking low power) | idx | 40875 | MP:82 |
| 40876 | | parking recording time | idx | 40877 | MP:84 |
| 40878 | | world standard time (time zone) | idx | 40879 | MP:98 |
| 40880 | | voice guide | idx | 40881 | MP:103 |
| 40882 | | SD partition | idx (**formats the SD card**) | 40883 | MP:88, string `setting_partition_info` |
| 40884 | | collision detection sound | idx | 40885 | MP:72 |
| 40886 | 0x9FB6 | heartbeat (camera → phone) | — | phone answers **40887**, empty | MFV:221-224 |
| 40888 | 0x9FB8 | camera stop (camera → phone) | — | phone answers **40889**, empty; LogonT shows "DVR is stopped" | MFV:225-235 |
| 40892 | | LED | idx | 40893 | MP:101 |
| 40904 | 0x9FC8 | snapshot (SET_PHOTO2) | idx **0** (see sendData quirk) | 40905 result ≥ 0 = ok | MFV:532-536, MFV:1294 |
| 40961 | | video quality (camera restarts) | idx | 40962 | MP:93, string `setting_recorder_quality_info` |
| 40967 | | white balance | idx | 40968 | MP:107 |
| 40969 | | exposure | idx | 40970 | MP:74 |
| 40973 | | voice record (SET_MUTE) | idx 0 = record, 1 = don't record | 40974 result | MP:78, MFV:597-602 |
| 40977 | 0xA011 | set time | `CNSTime` 36 bytes | 40978 | MP:304-369, MFV:1015-1035 |
| 40985 | | parking impact sensitivity | idx | 40986 | MP:86 |
| 40991 | | format SD (**excluded**) | idx | 40992 | MP:51 |
| 40997 | 0xA025 | file list | idx = folder | 40998, one packet per page | MP:57, BB2:541-546 |
| 41009 | 0xA031 | Wi-Fi password | `CNSWiFiSetting` 192 bytes | **41009** (not 41010) result | MP:109, MFV:264 |
| 41012 | 0xA034 | get config | none | 41013, 256-byte `CNSConfig` | MP:53 |
| 41014 | 0xA036 | switch front/rear | idx 0 | 41015 result | MFV:985-1003, MFV:1369 |
| 41018 | 0xA03A | record on/off (phone-view mode) | idx 1 on entering live, 0 on leaving / before file list | — | MFV:1501, MFV:1541, BB2:226 |
| 41022 | 0xA03E | net connect state | idx 1 = connect, 0 = disconnect | 41023 result (see session) | MP:63 |
| 41044 | | factory reset (**excluded**) | idx | 41045 | MP:49 |

The spec's earlier "0xA036 (40950?)" was wrong: 0xA036 = 41014, matching the reply 41015 (MP:46).

## Session flow (live view), as LogonT does it (MFV:1467-1509)
1. If the gateway is not `192.168.100.1`, show the Wi-Fi help screen and stop.
2. After 500 ms: connect TCP 7777 (3 s timeout) and start the receive loop.
3. After 1000 ms more: send 41022 idx 1. After 1500 ms: send 41018 idx 1.
4. Reply 41023 (MFV:1405-1446): result `0` → send 41012 GET_CONFIG; `< 0` → "Another phone is already connected"; `2` → "You cannot enter while replaying the recording"; `3` → "You cannot enter while viewing the photos".
5. Reply 41013 → parse config, send 8193 with `[1,0,0,0,0,0,0,0]` (MFV:1360-1367).
6. Reply 8193 → send 511; 1000 ms later open TCP 7778 and decode H.264; 2000 ms later send 40977 SET_TIME once per session (MFV:945-983).
7. If no frame is rendered within 20 s, LogonT shows "Wi-Fi signal is weak" (MFV:1482, 1720-1735).
8. Heartbeat: the camera sends 40886; answer 40887 with an empty payload. LogonT only dispatches packets whose payload is > 0 bytes, so it answers only heartbeats that carry a payload; T8Kit answers every 40886.
9. The camera sends 40888 to stop → answer 40889 and leave live view.
10. Leaving live view (onPause, MFV:1523-1546): send 41018 idx 0, then 500 ms later the 9-byte `PreViewOff` frame.
11. Exit / errors: send 41022 idx 0, then close the socket (MFV:1573-1580 and others).

## Recordings
### Folders (40997 index) — closed 2026-11-22
| idx | Folder | Type letter / 8200 type | LogonT activity | Phone folder | Ref |
|---|---|---|---|---|---|
| 0 | Driving (normal) | `I` / 0 | 6 | `/LOGON/NORMAL/` | BB2:543-546 |
| 1 | Event | `E` / 1 | 7 | `/LOGON/EVENT/` | BB3:515-518 |
| 2 | Parking | `P` / 2 | 8 | `/LOGON/PARK/` | BB4:522-525 |
| 3 | Photo | `J` / 3 | 9 | `/LOGON/PHOTO/` | BB5:513-516 |

Before listing, the list screens send 41018 idx 0 (+1500 ms) and then 40997 (+2000 ms) on the existing control socket (BB2:221-235).

### 40998 page layout (MFV:1318-1358)
- `u32 LE total_pages`, `u32 LE current_page` (0-based); the list is complete when `current_page + 1 >= total_pages`.
- Records of 32 bytes from offset 8. Bytes 0-20 = ASCII name (21 chars, e.g. `20180511_052412_I.avi`-style; the type letter is char 16). LogonT keeps a record only if its first char is `'2'`. Byte 31 = 0 → front camera only; otherwise front and rear.
- The **file index** for 8200 is the record's 0-based position in the order the camera sent it (BB2:252-257; LogonT reverses it when the list is sorted descending).

### Play and download (8200)
- Payload: `type u32` (I=0, E=1, P=2, J=3), `index u32`, `channel u32` (0 = front, 1 = rear).
- Reply 8201: `fileSize u32`, `duration u32` = number of video frames (MFFV:541 divides it by the frame rate).
- Download (BB2:610-689): send 8200 at +500 ms; at +2000 ms open 7778 (video) and 7779 (audio, not for photos) and read each to EOF. Saved names: first 17 chars of the record name + `A` (front) or `B` (rear).
- Playback (MFFV:604-688): send 8200; open 7779 at +1500 ms (not for photos), 7778 at +2000 ms.
- Photos (type J): 7778 carries the file bytes (saved under the record name, which includes its extension); no audio (BB5:720-760).
- Frame rate: front 30 fps, rear 25 fps (MFFV:327-335, `MainMP4ParSer.java:140-143`). Audio chunk per 4 frames: 2144 bytes front, 2560 bytes rear.
- LogonT's MP4: H.264 passthrough + PCM → AAC-LC 8 kHz mono, 128 kbit/s (`MainMP4ParSer.java`).

### Delete — closed 2026-11-22: **no camera-side delete exists in LogonT**
- Every command LogonT sends is listed above (all `send`/`sendData` call sites and every hand-built frame in the source tree). None deletes a recording on the camera.
- LogonT's swipe-to-delete (manual p.13 "Video Deletion") works on the **downloaded** list: `MainFileListView2.java:85-106` → `deleteDir2` → `new File(path).delete()` (`MainFileListView2.java:191-192`) on the phone's `/LOGON/NORMAL/` copy. The manual's screenshot shows `.mp4` names, which only exist after download.
- The only camera-side destructive commands are format SD (40991), factory reset (41044) and partition change (40882, formats the card).

## Settings
- `CNSConfig` (reply 41013, 256 bytes, all `i32 LE` unless noted; `logon/e100/setting/CNSConfig.java:93-121`): 0 record quality, 4 rear mirror, 8 driving impact sensitivity, 12 voice volume, 16 mute (≠0 = not recording voice), 20 LCD brightness, 24 white balance, 28 contrast, 32 exposure, 36 screen off time, 40 time zone, 44 crash alarm, 48 parking low power, 52 park impact sensitivity, 56 parking op-time, 60 partition, 64 voice guide, 68 product name [16], 84 serial [16], 100 firmware version [20], 120 velocity, 124 Wi-Fi password [16], 140 language, 144 bitrate, 148 LED, 152 ADAS, 252 CRC (not checked).
- Product name containing `t8` → `VER_CHECK = 1` (parking time list has a 6th option `OFF`). Firmware version containing `FH` / `FF` / `HH` selects the quality list.
- Each setting sends the 0-based row index of its option list (`ActivitySelect.java:93-310`). Option lists (`res/values/arrays.xml`, decoded with jadx `--no-src`):

| Setting | Options (index 0..n) |
|---|---|
| Video quality | FH: `FHD+HD`, `HD+HD`; FF: `FHD+FHD`, `HD+HD`; HH: `HD+HD` |
| Driving / parking impact sensitivity | `1 (High)`, 2..9, `10 (Low)` |
| Voice volume | 1..5 |
| Voice record | `Record`, `Do not Record` |
| LCD brightness | 1..7 |
| White balance | `Automatic`, `Sunny`, `Blur`, `Incandescent`, `Fluorescent Lamp` |
| Contrast | 0..6 |
| Exposure | -3..+3 |
| Screen off time | `10Seconds`, `20Seconds`, `30Seconds`, `None` |
| Collision detection sound | `ON`, `OFF` |
| Block low voltage | `11.5V(23.5V)`, `11.7V(23.7V)`, `11.9V(24.1V)`, `12.1V(24.1V)`, `12.3V(24.3V)` |
| Parking recording time | `6Hours`, `12Hours`, `24Hours`, `36Hours`, `48Hours` (+ `OFF` on T8) |
| World standard time | -11..+12 |
| Voice guide | `Voice Guidance`, `Not Guide` |
| LED | `ON`, `OFF` |
| SD partition | `A-Type`, `B-Type`, `C-Type` (formats the card) |

- `CNSTime` (40977): 9 × `i32 LE` = sec, min, hour, mday, **month 1-12**, **full year**, wday 0, yday 0, isdst 0 (MP:339-369, MFV:1016-1033). LogonT only sends years 2014-2037.
- `CNSWiFiSetting` (41009): `ssid[64]`, `oldPwd[64]`, `newPwd[64]`. LogonT fills **only `newPwd`**; ssid and old password are zeros (`ActivityWifiPassword.java` confirm handler). After a success reply LogonT waits 10 s, sends 41022 idx 0 and closes; the camera's Wi-Fi then needs the new password.

## Open items (need hardware)
1. Whether the 7778 live stream is pure Annex-B from the first byte, or has a vendor header that FFmpeg's probe skips. T8Kit's parser skips anything before the first start code and logs it.
2. Whether the camera's heartbeat carries a payload (LogonT ignores empty ones).
3. Whether 41009's reply really is 41009 (T8Kit accepts 41009 and 41010).
4. The exact record-name format and whether more than one 40998 page arrives for long lists.
5. Whether the firmware still accepts this flow (LogonT was last updated in 2021).
6. Delete on the camera: not possible with LogonT's command set. A firmware command may exist, but guessing one risks hitting format/reset handlers, so the app only deletes saved copies on the iPhone.

## Verification
Rev. 2 cross-check (2026-11-22): six `haiku-checker` agents (Claude Haiku 5.5), one per source file or small file group, checked 98 claims against `MainProtocol.java`, `MainFreeView.java`, `MainFileFreeView.java`, `MainFileListView_blackbox2..5.java`, `CNSConfig.java`, `ActivityWifiPassword.java`, `ActivitySelect.java` and `MainFileListView2.java`, each answer quoting the source line. Result: 97 confirmed, 1 mismatch. The mismatch was a wording error in the claim (blackbox2 calls `FileList(up_down_flag)`, where the argument is the sort order, not the folder); the orchestrator re-read BB2:233 and BB2:541-546 and the folder index sent is 0 as stated. The native-library findings (live port 7778) were checked by the orchestrator only; Haiku agents cannot disassemble.
