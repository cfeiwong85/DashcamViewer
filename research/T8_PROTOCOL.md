# Eurostyle T8 / LogonT Wi-Fi protocol (reverse-engineered)

Source: official LogonT Android APK (`com.logon_e100`) from the vendor support page tomocse.com.sg/eurostyle-t8-support/, decompiled with jadx 1.5.1 on 2026-11-22.
Status: built from static code reading only. **Nothing has been tested against a real camera yet.**
Extraction: 10 Haiku 5.5 line-by-line agents. The orchestrator checked every item marked ✔ against the source.

## Network
| Item | Value | Ref |
|---|---|---|
| Camera IP | `192.168.100.1` (the app checks that the DHCP gateway equals this) | MainProtocol.java:116 ✔ |
| SSID | Printed on the device; the manual shows `LogOn T8 xxxxxx`; code constant `WDVR` | MainProtocol.java:112, manual p.10 |
| Wi-Fi password | Default `12345678`; must be exactly 8 chars | vendor page, ActivityWifiPassword.java:177 |
| Control port | TCP **7777** (persistent socket) | MainProtocol.java:117 ✔ |
| File/video download | TCP **7778** (new socket per file; raw H.264 Annex-B until EOF) | MainFileListView_blackbox2.java:816-846 ✔ |
| Audio download | TCP **7779** (raw PCM, 8 kHz mono 16-bit) | blackbox2:746, MainFileFreeView.java:337 ✔ |
| Live video | Native FFmpeg (`libtutorial02.so`) opens its own TCP socket and demuxes `-f h264`. **Port not yet confirmed** (likely 7778 after cmd 0x2001) | .so strings ✔, port ✘ |

## Packet framing (control port 7777)
```
phone -> cam : [0x02][cmd_hi][cmd_lo][len u32 LE][payload ...][0x03][crc]
cam -> phone : [0x01][cmd_hi][cmd_lo][len u32 LE][payload ...][0x03][crc]
crc = XOR of every byte from index 0 through the 0x03 trailer
```
✔ MainProtocol.send (lines 169-206), Utils.makeCRC, MainFreeView.java:163-189 (receive: 7-byte header, payload if len>0 and ≤100000, then 2 trailer bytes).
Most payloads are a single `u32 LE` index (`CNSSimpleIndex`). A result reply (`CNSResult`) is an `i32 LE`, where <0 means error.

## Session flow (live view)
1. Connect TCP 7777 (3 s timeout).
2. Send `0xA03A` (41018) RECORD_ON_OFF with idx=1.
3. Send `0xA03E` (41022) NET_CONNECT_STATE with idx=1 → reply 41023 (result <0 = **another phone is already connected**).
4. Send 41012 GET_CONFIG → reply 41013 (256-byte config struct, CNSConfig; CRC at offset 252).
5. Send `0x2001` (8193) IPCAM_START, 8-byte payload `[1,0,0,0,0,0,0,0]` → reply 8193 → the native decoder starts.
6. Send `0x01FF` (511) "freeview" (len 0). After about 2 s send SET_TIME (40977, 36-byte `struct tm` of 9×i32 LE).
7. **Heartbeat:** the camera sends 40886; you must answer 40887 (empty).
8. The camera sends 40888 to stop → answer 40889.
9. To disconnect: 41022 with idx=0. Stop preview: `0x2002` (8194).
- Switch front/rear: `0xA036` (40950?) / NAT_CMD_CHANGE_CAM → reply 41015.
- Snapshot: 40904 SET_PHOTO2 (payload idx 4, then idx 0) → reply 40905.
- Voice-record toggle: 40973 SET_MUTE idx 0/1 → reply 40974.

## Recordings
- List: send 40997 (`0xA025`) with idx = folder (**2=normal/driving, 1=event?, 2=parking?, 3=photo**; bb2..bb5 used 6/7/8/9 as activity ids). Exact folder idx per screen still to be tabulated → reply 40998 (may come in multiple pages).
- Reply 40998 layout: `u32 total_pages, u32 current_page`, then 32-byte records from offset 8: bytes 0-20 = name (e.g. `20180511_052412_EA`), byte 31 = 0 means front only, otherwise front and rear. Byte 24 of the whole payload = state letter (I/E/P/J).
- Download: send `0x2008` (8200), len 12, payload `[type][..][idx u32 LE][channel u8]...` (type I=0,E=1,P=2,J=3; channel 0=front,1=rear) → reply 8201 `{u32 fileSize, u32 duration}`. Then read TCP 7778 (H.264) and 7779 (PCM) to EOF. The old app muxed them with fps 30 (front) / 25 (rear) and AAC audio into `.mp4`.
- Playback in the old app = the same request, streamed into the decoder.
- Delete: not yet located in the code (the manual says swipe to delete). **TODO**.

## Settings (send idx → `*_RESP` = cmd+1)
Impact sensitivity 40864, voice volume 40866, LCD brightness 40868, contrast 40870, switch screen 40872, parking low-power 40874, parking op-time 40876, timezone 40878, voice guide 40880, partition 40882, crash alarm 40884, LED 40892, record quality 40961, white balance 40967, exposure 40969, mute 40973, park impact 40985, time 40977, Wi-Fi pwd 41009 (ssid[64]+old[64]+new[64]), format SD 40991, factory reset 41044.
Option lists: SettingDefs.java (varies by VER_CHECK: product name contains "t8" → 1).

## Open items to confirm on hardware
1. Live-video socket port and whether the stream has any framing before the Annex-B NALs.
2. Folder index mapping for 40997.
3. The delete command.
4. Whether the firmware still accepts this flow (LogonT last updated 2021).
