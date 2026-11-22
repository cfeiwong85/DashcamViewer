
# live
## sockets
- MainFreeView.java:685 Socket connect in SocKetConnect to MainProtocol.SERVERIP and serverPort with a timeout.  `socket.connect(new InetSocketAddress(str, MainProtocol.serverPort), PathInterpolatorCompat.MAX_NUM_POINTS);`
- MainFreeView.java:690 Creates the buffered output stream used by all raw packet writes.  `MainProtocol.m_out_stream = new BufferedOutputStream(MainProtocol.mSock.getOutputStream());`
- MainFreeView.java:693 Creates the input stream read by mCheckRecv.  `MainProtocol.m_in_stream = MainProtocol.mSock.getInputStream();`
- MainFreeView.java:902 WiFiConnect writes its 13-byte packet directly to m_out_stream, then flushes (line 904).  `MainProtocol.m_out_stream.write(bArr, 0, 13);`
- MainFreeView.java:937 IpCamStart writes its 17-byte packet directly to m_out_stream, then flushes (line 939).  `MainProtocol.m_out_stream.write(bArr, 0, 17);`
- MainFreeView.java:961 freeviewcon writes its 9-byte packet directly to m_out_stream, then flushes (line 963).  `MainProtocol.m_out_stream.write(bArr, 0, 9);`
- MainFreeView.java:1007 CamChange writes its 13-byte packet directly to m_out_stream, then flushes (line 1009).  `MainProtocol.m_out_stream.write(bArr, 0, 13);`
- MainFreeView.java:1141 PreViewOff writes its 9-byte packet directly to m_out_stream, then flushes (line 1143).  `MainProtocol.m_out_stream.write(bArr, 0, 9);`
- MainFreeView.java:1172 RecordOnOff writes its 13-byte packet directly to m_out_stream, then flushes (line 1174).  `MainProtocol.m_out_stream.write(bArr, 0, 13);`
## commands_sent
- MainFreeView.java:881 WiFiConnect builds a 13-byte frame: cmd bytes [-96,62] (0xA03E = 41022), length field 4, payload bytes 7-10 = 1,0,0,0, end marker 3 at index 11, XOR CRC at index 12 over indexes 1-11 (seeded from bArr[0]). Sets MainProtocol.targetActivity = 0 at line 899.  `bArr[1] = -96; bArr[2] = 62; bArr[3] = (byte) 4; bArr[7] = 1; bArr[11] = 3;`
- MainFreeView.java:911 IpCamStart builds a 17-byte frame: cmd bytes [32,1] (0x2001 = 8193), length field 8, payload bytes 7-14 with byte 7 = 1 and rest 0, end marker 3 at index 15, XOR CRC at index 16 over indexes 1-15.  `bArr[1] = 32; bArr[2] = 1; bArr[3] = (byte) 8; bArr[7] = 1; bArr[15] = 3;`
- MainFreeView.java:945 freeviewcon builds a 9-byte frame with cmd bytes [1,-1] (0x01FF = 511), length 0, end marker 3 at index 7, XOR CRC at index 8 over indexes 1-7. After 1000 ms it calls screen_mode(); after 2000 ms it calls setTime() once.  `bArr[1] = 1; bArr[2] = -1; bArr[3] = 0; bArr[7] = 3; bArr[8] = bArr[0];`
- MainFreeView.java:985 CamChange builds a 13-byte frame: cmd bytes [-96,54] (0xA036 = 40950), length field 4, payload bytes 7-10 all 0, end marker 3 at index 11, XOR CRC at index 12 over indexes 1-11.  `bArr[1] = -96; bArr[2] = 54; bArr[3] = (byte) 4; bArr[7] = 0; bArr[11] = 3;`
- MainFreeView.java:1123 PreViewOff builds a 9-byte frame with cmd bytes [32,2] (0x2002 = 8194), length field 4, but index 7 is the end marker 3 (no payload bytes), and CRC at index 8 over indexes 1-7. Length field and framing do not match; see unsure.  `bArr[1] = 32; bArr[2] = 2; bArr[3] = (byte) 4; bArr[7] = 3; bArr[8] = bArr[0];`
- MainFreeView.java:1150 RecordOnOff builds a 13-byte frame: cmd bytes [-96,58] (0xA03A = 41018, matching the literal 41018 used with MainProtocol.send at lines 1501, 1541, 1665), length 4, payload bytes 7-10 all 0, end marker 3 at index 11, XOR CRC at index 12.  `bArr[1] = -96; bArr[2] = 58; bArr[3] = (byte) 4; bArr[7] = 0; bArr[11] = 3;`
- MainFreeView.java:223 Receive loop replies to NAT_CMD_NET_CONNECT_HOT (40886) via MainProtocol.send with the RESP command and activity -1.  `MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_HOT_RESP, null, -1);`
- MainFreeView.java:226 Receive loop replies to NAT_CMD_NET_CONNECT_STOP (40888) via MainProtocol.send with the RESP command and activity 0.  `if (MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STOP_RESP, null, 0) < 0 && MainFreeView.this.main_ProgressDialog44 != null) {`
- MainFreeView.java:536 Photo button sends two CNSSimpleIndex payloads (index 4 and 0) via MainProtocol.sendData as NAT_CMD_SET_PHOTO2, then checks m_photo after 1000 ms.  `MainProtocol.sendData(MainProtocol.NAT_CMD_SET_PHOTO2, cNSSimpleIndex.getBytes(), cNSSimpleIndex2.getBytes(), 0);`
- MainFreeView.java:602 Voice record button sends NAT_CMD_SET_MUTE with payload index 0 or 1 via MainProtocol.send, activity 0.  `if (MainProtocol.send(MainProtocol.NAT_CMD_SET_MUTE, cNSSimpleIndex3.getBytes(), 0) < 0) {`
- MainFreeView.java:1035 setTime sends NAT_CMD_SET_TIME with a CNSTime payload via MainProtocol.send, activity 0.  `MainProtocol.send(MainProtocol.NAT_CMD_SET_TIME, cNSTime.getBytes(), 0);`
- MainFreeView.java:1191 Alert OK handler sends NAT_CMD_NET_CONNECT_STATE3 with index 0, activity -1; logs success or failure from the return value.  `if (MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1) < 0) {`
- MainFreeView.java:1240 displayErrorAlert OK handler sends NAT_CMD_NET_CONNECT_STATE3 with index 0, activity -1, before closing the socket.  `MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1);`
- MainFreeView.java:1270 displayErrorSocketAlert OK handler sends NAT_CMD_NET_CONNECT_STATE3 with index 0, activity -1.  `MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1);`
- MainFreeView.java:1422 After a successful NAT_CMD_NET_CONNECT_STATE3 reply (result 0), sends NAT_CMD_GET_CONFIG with null payload, activity 0.  `MainProtocol.send(MainProtocol.NAT_CMD_GET_CONFIG, null, 0);`
- MainFreeView.java:1501 Sends literal command 41018 (record on) with payload index 1, activity 0, from a delayed Handler in onResume.  `if (MainProtocol.send(41018, cNSSimpleIndex.getBytes(), 0) < 0) {`
- MainFreeView.java:1541 onPause sends literal command 41018 (record off) with payload index 0, activity 0.  `if (MainProtocol.send(41018, cNSSimpleIndex.getBytes(), 0) < 0) {`
- MainFreeView.java:1573 Exit-confirm dialog OK sends NAT_CMD_NET_CONNECT_STATE3 with index 0, activity -1.  `MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1);`
- MainFreeView.java:1596 onKeyDown default branch sends NAT_CMD_NET_CONNECT_STATE3 with index 0, activity -1.  `MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1);`
- MainFreeView.java:1623 displayPlayFailedAlert OK handler sends NAT_CMD_NET_CONNECT_STATE3 with index 0, activity -1.  `MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1);`
- MainFreeView.java:1665 TCPclient.run sends literal command 41018 (record on) with index 1, activity 0, after SocKetConnect succeeds.  `if (MainProtocol.send(41018, cNSSimpleIndex.getBytes(), 0) >= 0) {`
## responses_parsed
- MainFreeView.java:163 Receive loop reads a 7-byte header from the input stream in mCheckRecv.  `while (MainProtocol.m_in_stream.read(MainProtocol.readdata, 0, 7) > 0) {`
- MainFreeView.java:165 Header is processed only when byte 0 equals 1.  `if (MainProtocol.readdata[0] == 1) {`
- MainFreeView.java:171 Command is parsed as big-endian from header bytes 1-2.  `int byteArrayToInt_Big = Utils.byteArrayToInt_Big(bArr);`
- MainFreeView.java:172 Payload size is parsed as little-endian from header bytes 3-6.  `int byteArrayToInt_Little = Utils.byteArrayToInt_Little(bArr2);`
- MainFreeView.java:174 Payload sizes above 100000 are rejected and the read buffer is reset to 8192 bytes.  `if (byteArrayToInt_Little <= 100000) {`
- MainFreeView.java:176 When size is zero, only the 2-byte trailer is read.  `MainProtocol.m_in_stream.read(bArr3, 0, 2);`
- MainFreeView.java:189 After the payload is read, the 2-byte trailer is read.  `MainProtocol.m_in_stream.read(bArr3, 0, 2);`
## settings
- MainFreeView.java:1119 Sets the static MainProtocol.WIFI_SSID from the connection info.  `MainProtocol.WIFI_SSID = ssid;`
## unsure
- MainFreeView.java:1123 PreViewOff sets length field 4 but places end marker 3 at index 7, so no payload bytes are framed; the packet may be malformed as built.  `bArr[3] = (byte) 4; ... bArr[7] = 3;`
- MainFreeView.java:1191 Constant values for NAT_CMD_NET_CONNECT_STATE3, NAT_CMD_NET_CONNECT_STOP_RESP, NAT_CMD_NET_CONNECT_HOT_RESP, NAT_CMD_SET_MUTE, NAT_CMD_SET_TIME, NAT_CMD_SET_PHOTO2, NAT_CMD_GET_CONFIG were not resolved; MainProtocol.java was not grepped in this pass.  `MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, ...)`

# connect
## sockets
- MainActivity.java:176 Checks the static MainProtocol.mSock is non-null before closing it in onStop.  `if (MainProtocol.mSock != null) {`
- MainActivity.java:178 Closes the static socket in onStop.  `MainProtocol.mSock.close();`
- MainActivity.java:180 Closes the static output stream in onStop.  `MainProtocol.m_out_stream.close();`
- MainActivity.java:182 Closes the static input stream in onStop.  `MainProtocol.m_in_stream.close();`
## commands_sent
- MainActivity.java:173 Sends NAT_CMD_NET_CONNECT_STATE3 with a CNSSimpleIndex index 0 payload and activity -1 when the app is stopping (OnStopFlag true).  `MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1);`
## file_ops
- WiFiDBHelper.java:25 SQLite database file name used by the helper, version 1.  `super(context, "WiFiDBHelper3.db", (SQLiteDatabase.CursorFactory) null, 1);`
- WiFiDBHelper.java:15 Creates the wifilist table with _id, ssid and password columns on database creation.  `sQLiteDatabase.execSQL("create table wifilist(_id INTEGER PRIMARY KEY AUTOINCREMENT, ssid TEXT,password TEXT);");`
- WiFiDBHelper.java:21 On upgrade to a newer version, drops the wifilist table and recreates it, discarding stored Wi-Fi rows.  `sQLiteDatabase.execSQL("DROP TABLE IF EXISTS wifilist");`
## settings
- MainActivity.java:189 Obtains the WifiManager system service from the application context in onStop.  `WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService("wifi");`
- MainActivity.java:190 Checks whether Wi-Fi is enabled.  `if (wifiManager.isWifiEnabled()) {`
- MainActivity.java:191 Turns Wi-Fi off when the activity stops with OnStopFlag true.  `wifiManager.setWifiEnabled(false);`
- MainActivity.java:152 Sets the static MainProtocol.OnStopFlag to true in onRestart.  `MainProtocol.OnStopFlag = true;`
- MainActivity.java:197 Kills the app process in onStop when no share flags are set to 1.  `Process.killProcess(Process.myPid());`
- MainActivity.java:193 Checks the three share flags; if none equals 1 the process is killed directly, otherwise it is killed after a 1000 ms delay.  `if (MainFileListView2.mShareValue_I != 1 && MainFileListView3.mShareValue_E != 1 && MainFileListView4.mShareValue_P != 1) {`
- MainIntro.java:56 Checks mobile data state on launch via reflection on ConnectivityManager.getMobileDataEnabled and stores it in mMobileData.  `Method declaredMethod = Class.forName(connectivityManager.getClass().getName()).getDeclaredMethod("getMobileDataEnabled", new Class[0]);`
- MainIntro.java:139 Requests WRITE_EXTERNAL_STORAGE and ACCESS_COARSE_LOCATION permissions at runtime.  `ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, "android.permission.ACCESS_COARSE_LOCATION"`
## unsure
- MainActivity.java:173 Numeric value of MainProtocol.NAT_CMD_NET_CONNECT_STATE3 and the send() packet behaviour were not resolved; MainProtocol.java was not grepped in this pass.  `MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1);`
- MainActivity.java:193 The share-flag fields MainFileListView2.mShareValue_I, MainFileListView3.mShareValue_E and MainFileListView4.mShareValue_P are defined in other files and were not read; their meaning is unknown.  `MainFileListView2.mShareValue_I`
- MainActivity.java:82 isSoftkeyYn and checkLgPhone are defined but checkLgPhone is never called in this file; not related to the reinstall issue as far as read.  `private int checkLgPhone() {`
- MainIntro.java:93 Only the WiFi DB and intro flow were read; no code in these three files references a 7-day timer, certificate, or expiry, so the reinstall cause is not located here.  `private void GotoActivityWarningAndQuit() {`

# bb2
## sockets
- MainFileListView_blackbox2.java:1 Socket to SERVERIP on FILEDOWNPORT (resolved in MainProtocol.java: SERVERIP="192.168.100.1", FILEDOWNPORT=7778); used for video download  `Socket socket = new Socket(MainProtocol.SERVERIP, MainProtocol.FILEDOWNPORT);`
- MainFileListView_blackbox2.java:746 Audio download socket to SERVERIP on AUDIOPORT (AUDIOPORT not resolved; grep of MainProtocol.java did not match it)  `Socket socket = new Socket(MainProtocol.SERVERIP, MainProtocol.AUDIOPORT);`
## commands_sent
- MainProtocol.java:1 Grep constants NAT_CMD_NET_CONNECT_STATE3, FILE_PATH_NORMAL, SERVERIP, FILEDOWNPORT  `grep -n "NAT_CMD_NET_CONNECT_STATE3\|FILE_PATH_NORMAL\|SERVERIP\|FILEDOWNPORT" MainProtocol.java`
## responses_parsed
- MainProtocol.java:63 NAT_CMD_NET_CONNECT_STATE3 = 41022 (0xA03E)  `public static final int NAT_CMD_NET_CONNECT_STATE3 = 41022;`
## video_audio
- MainFileListView_blackbox2.java:763 Audio download writes to a PCM file path built from FILE_PATH_NORMAL, filename, PCM_FILE  `sb2.append(MainProtocol.FILE_PATH_NORMAL);`
## file_ops
- MainFileListView_blackbox2.java:546 FileList sends command 40997 (0xA025) via MainProtocol.send with activity 6  `MainProtocol.send(40997, cNSSimpleIndex.getBytes(), 6);`
- MainFileListView_blackbox2.java:1026 Exit dialog sends NAT_CMD_NET_CONNECT_STATE3 (41022, 0xA03E) via MainProtocol.send with activity -1 before closing sockets  `MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1);`
- MainFileListView_blackbox2.java:439 wificon writes a hand-built 13-byte packet to m_out_stream (cmd bytes 0xA0 0x3E = 41022, payload length 4, payload 0x01 0 0 0)  `MainProtocol.m_out_stream.write(bArr, 0, 13);`
- MainFileListView_blackbox2.java:475 RecordOnOff writes a hand-built 13-byte packet (cmd bytes 0xA0 0x3A = 41018, payload length 4, payload all zero)  `MainProtocol.m_out_stream.write(bArr, 0, 13);`
- MainFileListView_blackbox2.java:502 PreViewOff writes a 9-byte packet (cmd bytes 0x20 0x02 = 8194, length 4, payload byte 3 at index 7, CRC at index 8); no 0x03 end marker and structure differs from the standard layout  `MainProtocol.m_out_stream.write(bArr, 0, 9);`
- MainFileListView_blackbox2.java:532 SdCardCheck writes a 12-byte packet (cmd bytes 0x2A 0x04, end marker 0x03 at index 10, CRC at index 11); layout differs from the standard  `MainProtocol.m_out_stream.write(bArr, 0, 12);`
- MainFileListView_blackbox2.java:571 FileListTrans writes a 12-byte packet (cmd bytes 0x20 0x04 = 8196, end marker 0x03 at index 10, CRC at index 11); layout differs from the standard  `MainProtocol.m_out_stream.write(bArr, 0, 12);`
- MainFileListView_blackbox2.java:722 Filedown writes a 21-byte packet (cmd bytes 0x20 0x08 = 8200, length 12, file index LE at 11-14, channel at 15, end marker 0x03 at 19, CRC at 20)  `MainProtocol.m_out_stream.write(bArr, 0, 21);`
## unsure
- MainFileListView_blackbox2.java:746 AUDIOPORT constant value not found in MainProtocol.java grep  `MainProtocol.AUDIOPORT`
- MainFileListView_blackbox2.java:471 Constant name MainProtocol.send is called with arbitrary activity values; the send method body was not in this file so the CRC/framing of send is not verified here  `MainProtocol.send(40997, cNSSimpleIndex.getBytes(), 6);`
- MainFileListView_blackbox2.java:525 SdCardCheck and FileListTrans byte layouts (length field and payload positions) do not match the standard framing described in the task; intended payload length cannot be pinned down  `bArr[10] = 3;`
- MainFileListView_blackbox2.java:525 Gateway IP check: format equals MainProtocol.SERVERIP gates the file-list flow; MainProtocol.SERVERIP resolved to 192.168.100.1  `if (format.equals(MainProtocol.SERVERIP)) {`

# bb3
## sockets
- MainFileListView_blackbox3.java:788 Opens TCP socket to SERVERIP (192.168.100.1 per MainProtocol.java line 116) on FILEDOWNPORT (7778 per MainProtocol.java line 119)  `Socket socket = new Socket(MainProtocol.SERVERIP, MainProtocol.FILEDOWNPORT);`
- MainFileListView_blackbox3.java:718 Opens TCP socket to SERVERIP on AUDIOPORT (7779 per MainProtocol.java line 118) in TCPclient_audio  `Socket socket = new Socket(MainProtocol.SERVERIP, MainProtocol.AUDIOPORT);`
## commands_sent
- MainProtocol.java:0 Grep for constants NAT_CMD_NET_CONNECT_STATE3, FILE_PATH_EVENT, SERVERIP, FILEDOWNPORT, AUDIOPORT  `grep -n "NAT_CMD_NET_CONNECT_STATE3\|FILE_PATH_EVENT\|SERVERIP\|FILEDOWNPORT\|AUDIOPORT" MainProtocol.java`
## responses_parsed
- MainProtocol.java:63 NAT_CMD_NET_CONNECT_STATE3 = 41022  `public static final int NAT_CMD_NET_CONNECT_STATE3 = 41022;`
## file_ops
- MainFileListView_blackbox3.java:1 Read whole file (lines 1-1035), read-only  `Read /Users/ahbao/DashcamViewer/research/decompiled/sources/com/logon_e100/MainFileListView_blackbox3.java`
## unsure
- MainFileListView_blackbox3.java:518 MainProtocol.send(40997, cNSSimpleIndex.getBytes(), 7) is the only call to MainProtocol.send in this file that uses the send() helper; 40997 and the activity arg 7 were not resolved (40997 not grepped)  `MainProtocol.send(40997, cNSSimpleIndex.getBytes(), 7);`
- MainFileListView_blackbox3.java:972 MainProtocol.send(NAT_CMD_NET_CONNECT_STATE3, ..., -1) uses the send() helper with activity -1, which the task did not ask to interpret; the constant resolves to 41022 per MainProtocol.java line 63  `MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1);`
- MainFileListView_blackbox3.java:389 Hand-built packets (not via MainProtocol.send) in wificon, RecordOnOff, PreViewOff, SdCardCheck, FileListTrans, Filedown write directly to MainProtocol.m_out_stream; these are outside the send() path and are listed here for reference only  `public void wificon() { byte[] bArr = new byte[256]; bArr[0] = 2; ...`

# bb4
## sockets
- MainFileListView_blackbox4.java:722 Opens a TCP socket to MainProtocol.SERVERIP on MainProtocol.AUDIOPORT inside TCPclient_audio.run.  `Socket socket = new Socket(MainProtocol.SERVERIP, MainProtocol.AUDIOPORT);`
- MainFileListView_blackbox4.java:792 Opens a TCP socket to MainProtocol.SERVERIP on MainProtocol.FILEDOWNPORT inside TCPclient.run.  `Socket socket = new Socket(MainProtocol.SERVERIP, MainProtocol.FILEDOWNPORT);`
- MainFileListView_blackbox4.java:205 Compares the Wi-Fi DHCP gateway address against MainProtocol.SERVERIP before starting list and record commands.  `if (format.equals(MainProtocol.SERVERIP)) {`
## commands_sent
- MainFileListView_blackbox4.java:525 FileList sends command 40997 via MainProtocol.send with payload from CNSSimpleIndex (index 2) and activity argument 8.  `MainProtocol.send(40997, cNSSimpleIndex.getBytes(), 8);`
- MainFileListView_blackbox4.java:966 Exit dialog sends MainProtocol.NAT_CMD_NET_CONNECT_STATE3 via MainProtocol.send with CNSSimpleIndex index 0 and activity argument -1.  `MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1);`
- MainFileListView_blackbox4.java:398 wificon hand-builds a 13-byte frame (STX, cmd bytes 0xA1 0x3E, length 4, payload 0x00000001 0x00000000... see code, ETX 0x03, XOR CRC) and writes it directly to MainProtocol.m_out_stream, bypassing MainProtocol.send.  `bArr[0] = 2;`
- MainFileListView_blackbox4.java:418 wificon writes the 13-byte hand-built frame to MainProtocol.m_out_stream and flushes.  `MainProtocol.m_out_stream.write(bArr, 0, 13);`
- MainFileListView_blackbox4.java:433 RecordOnOff hand-builds a 13-byte frame with cmd bytes 0xA1 0x3A and payload 0x00000000 (record on/off), XOR CRC at index 12.  `bArr[2] = 58;`
- MainFileListView_blackbox4.java:454 RecordOnOff writes the hand-built frame directly to MainProtocol.m_out_stream, bypassing MainProtocol.send.  `MainProtocol.m_out_stream.write(bArr, 0, 13);`
- MainFileListView_blackbox4.java:464 PreViewOff hand-builds a 9-byte frame with cmd bytes 0x20 0x02 and payload 0x00000003 (preview stop), XOR CRC at index 8.  `bArr[1] = 32;`
- MainFileListView_blackbox4.java:481 PreViewOff writes the 9-byte frame directly to MainProtocol.m_out_stream.  `MainProtocol.m_out_stream.write(bArr, 0, 9);`
- MainFileListView_blackbox4.java:491 SdCardCheck hand-builds a 12-byte frame with cmd bytes 0x2A 0x04 and an all-zero payload (length field at index 2 is 4), XOR CRC at index 11.  `bArr[1] = 42;`
- MainFileListView_blackbox4.java:511 SdCardCheck writes the 12-byte frame directly to MainProtocol.m_out_stream.  `MainProtocol.m_out_stream.write(bArr, 0, 12);`
- MainFileListView_blackbox4.java:530 FileListTrans hand-builds a 12-byte frame with cmd bytes 0x20 0x04 and payload 0x00000001, XOR CRC at index 11.  `bArr[1] = 32;`
- MainFileListView_blackbox4.java:550 FileListTrans writes the 12-byte frame directly to MainProtocol.m_out_stream.  `MainProtocol.m_out_stream.write(bArr, 0, 12);`
- MainFileListView_blackbox4.java:668 Filedown hand-builds a 21-byte download request with cmd bytes 0x20 0x08, length field 12, payload = file index (4 bytes LE, bytes 11-14) plus channel byte at index 15, XOR CRC at index 20.  `bArr[2] = 8;`
- MainFileListView_blackbox4.java:698 Filedown writes the 21-byte download request directly to MainProtocol.m_out_stream, bypassing MainProtocol.send.  `MainProtocol.m_out_stream.write(bArr, 0, 21);`
## unsure
- MainFileListView_blackbox4.java:205 Numeric values of MainProtocol.SERVERIP, AUDIOPORT, FILEDOWNPORT, NAT_CMD_NET_CONNECT_STATE3, FILE_PATH_PARK, PCM_FILE and AAC_FILE were not resolved; MainProtocol.java was not grepped.  `MainProtocol.NAT_CMD_NET_CONNECT_STATE3`
- MainFileListView_blackbox4.java:398 wificon bArr[1] = -96 is the byte value of 0xA0, so the command word is 0xA03E; the file does not state this as a numeric command constant, so the mapping to cmd 0xA03E is inferred from the bytes, not confirmed.  `bArr[1] = -96;`
- MainFileListView_blackbox4.java:436 RecordOnOff bArr[1] = -96 is the byte value of 0xA0, so the command word is 0xA03A; inferred from the bytes, not confirmed against MainProtocol constants.  `bArr[1] = -96;`

# bb5
## sockets
- MainFileListView_blackbox5.java:698 onBtnConnect opens a Socket to MainProtocol.SERVERIP on MainProtocol.FILEDOWNPORT.  `this.socket = new Socket(MainProtocol.SERVERIP, MainProtocol.FILEDOWNPORT);`
- MainFileListView_blackbox5.java:727 TCPclient.run opens a new Socket to MainProtocol.SERVERIP on MainProtocol.FILEDOWNPORT for each file download.  `Socket socket = new Socket(MainProtocol.SERVERIP, MainProtocol.FILEDOWNPORT);`
## commands_sent
- MainFileListView_blackbox5.java:394 wificon() builds 13-byte packet: header 0x02, cmd bytes 0xA0 0x3E, length field 4 LE, payload 1,0,0,0, end 0x03, XOR CRC over indices 0-11 (loop i=1..11 starting from bArr[0]).  `bArr[0] = 2; bArr[1] = -96; bArr[2] = 62; bArr[3] = (byte) 4;`
- MainFileListView_blackbox5.java:412 wificon() writes the 13-byte packet directly to MainProtocol.m_out_stream, bypassing MainProtocol.send.  `MainProtocol.m_out_stream.write(bArr, 0, 13);`
- MainFileListView_blackbox5.java:425 RecordOnOff() builds 13-byte packet: header 0x02, cmd bytes 0xA0 0x3A (58), length 4, payload 0,0,0,0, end 0x03, XOR CRC over indices 0-11.  `bArr[1] = -96; bArr[2] = 58; bArr[3] = (byte) 4;`
- MainFileListView_blackbox5.java:445 RecordOnOff() writes the 13-byte packet directly to MainProtocol.m_out_stream, bypassing MainProtocol.send.  `MainProtocol.m_out_stream.write(bArr, 0, 13);`
- MainFileListView_blackbox5.java:456 PreViewOff() builds 9-byte packet: header 0x02, cmd bytes 0x20 0x02, length byte 4, one payload byte 3 at index 7, CRC at index 8 (XOR over 0-7). Length field and payload size do not match, see unsure.  `bArr[1] = 32; bArr[2] = 2; bArr[3] = (byte) 4;`
- MainFileListView_blackbox5.java:472 PreViewOff() writes the 9-byte packet directly to MainProtocol.m_out_stream.  `MainProtocol.m_out_stream.write(bArr, 0, 9);`
- MainFileListView_blackbox5.java:482 SdCardCheck() builds 12-byte packet: header 0x02, cmd bytes 0x2A 0x04, length field zeros, end 0x03 at index 10, CRC at index 11 (XOR over 0-10).  `bArr[1] = 42; bArr[2] = (byte) 4;`
- MainFileListView_blackbox5.java:502 SdCardCheck() writes the 12-byte packet directly to MainProtocol.m_out_stream.  `MainProtocol.m_out_stream.write(bArr, 0, 12);`
- MainFileListView_blackbox5.java:513 FileList(int) creates a CNSSimpleIndex with index 3 for the file list request.  `cNSSimpleIndex.setIndex(3);`
- MainFileListView_blackbox5.java:516 FileList(int) sends command 40997 (0xA025) through MainProtocol.send with activity 9.  `MainProtocol.send(40997, cNSSimpleIndex.getBytes(), 9);`
- MainFileListView_blackbox5.java:521 FileListTrans() builds 12-byte packet: header 0x02, cmd bytes 0x20 0x04, length bytes 0,0,0,1 (value 1 in the high byte under LE), end 0x03 at index 10, CRC at index 11.  `bArr[1] = 32; bArr[2] = (byte) 4; ... bArr[6] = 1;`
- MainFileListView_blackbox5.java:541 FileListTrans() writes the 12-byte packet directly to MainProtocol.m_out_stream.  `MainProtocol.m_out_stream.write(bArr, 0, 12);`
- MainFileListView_blackbox5.java:654 Filedown(int i, int i2) builds 21-byte packet: header 0x02, cmd bytes 0x20 0x08, length 12 (byte 3 = 12), payload starts at index 7 with 0x03, index i as 4 LE bytes at 11-14, channel i2 at index 15, end 0x03 at index 19, CRC at index 20 (XOR over 0-19).  `bArr[1] = 32; bArr[2] = 8; bArr[3] = (byte) 12;`
- MainFileListView_blackbox5.java:685 Filedown() writes the 21-byte packet directly to MainProtocol.m_out_stream.  `MainProtocol.m_out_stream.write(bArr, 0, 21);`
- MainFileListView_blackbox5.java:817 onKeyDown back-key dialog positive path sends NAT_CMD_NET_CONNECT_STATE3 via MainProtocol.send with activity -1, after setIndex(0).  `MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1);`
## unsure
- MainFileListView_blackbox5.java:456 PreViewOff length field is 4 but only one payload byte (index 7) is written before the 0x03 end marker position is taken by CRC at index 8; mismatch between declared length and packet body.  `bArr[3] = (byte) 4; ... bArr[7] = 3; bArr[8] = bArr[0];`
- MainFileListView_blackbox5.java:482 SdCardCheck writes a length region of 4 bytes (indices 3-6) plus three extra zero bytes (7-9) before the 0x03 end marker at index 10; not clear whether the layout matches the header rule.  `bArr[3] = b; bArr[4] = b; bArr[5] = b; bArr[6] = 0; bArr[7] = 0; bArr[8] = 0; bArr[9] = 0; bArr[10] = 3;`
- MainFileListView_blackbox5.java:521 FileListTrans length bytes are 0,0,0,1 at indices 3-6; under little-endian this would be 0x01000000, not 1. Could be an intended big-endian length or a bug; not pinned down.  `bArr[6] = 1;`
- MainFileListView_blackbox5.java:209 Constants SERVERIP, FILEDOWNPORT, NAT_CMD_NET_CONNECT_STATE3 and FILE_PATH_PHOTO are referenced but their numeric values were not resolved in this pass (not grepped from MainProtocol.java).  `MainProtocol.NAT_CMD_NET_CONNECT_STATE3`

# fileview
## sockets
- MainFileFreeView.java:354 Audio socket opened to MainProtocol.SERVERIP on MainProtocol.AUDIOPORT (not a command packet).  `this.socket_audio = new Socket(str, MainProtocol.AUDIOPORT);`
## commands_sent
- MainFileFreeView.java:547 wificon(): builds 13-byte frame, cmd bytes 0xA0,0x3E, length 4, payload bytes 4-7 with byte 7 = 1, 0x03 at index 11, XOR CRC at index 12 over bytes 0-11 (initialized from bArr[0]).  `public void wificon() {`
- MainFileFreeView.java:549 Header STX byte 0x02 in wificon frame.  `bArr[0] = 2;`
- MainFileFreeView.java:550 Command high byte -96 (0xA0) in wificon frame.  `bArr[1] = -96;`
- MainFileFreeView.java:551 Command low byte 62 (0x3E) in wificon frame.  `bArr[2] = 62;`
- MainFileFreeView.java:552 Length field byte 0 = 4 in wificon frame.  `bArr[3] = (byte) 4;`
- MainFileFreeView.java:561 Terminator 0x03 at index 11 in wificon frame.  `bArr[11] = 3;`
- MainFileFreeView.java:562 CRC seeded with header byte in wificon frame.  `bArr[12] = bArr[0];`
- MainFileFreeView.java:563 CRC XOR loop over indices 1-11 in wificon frame.  `for (int i = 1; i < 12; i++) {`
- MainFileFreeView.java:568 wificon writes 13 bytes to MainProtocol.m_out_stream.  `MainProtocol.m_out_stream.write(bArr, 0, 13);`
- MainFileFreeView.java:577 freeviewstop(): builds 10-byte frame, cmd bytes 0x20,0x02, length 4, payload byte 7 = 0, 0x03 at index 8, XOR CRC at index 9 over bytes 0-8.  `public void freeviewstop() {`
- MainFileFreeView.java:580 Command high byte 32 (0x20) in freeviewstop frame.  `bArr[1] = 32;`
- MainFileFreeView.java:581 Command low byte 2 (0x02) in freeviewstop frame.  `bArr[2] = 2;`
- MainFileFreeView.java:588 Terminator 0x03 at index 8 in freeviewstop frame.  `bArr[8] = 3;`
- MainFileFreeView.java:590 CRC XOR loop over indices 1-8 in freeviewstop frame.  `for (int i = 1; i < 9; i++) {`
- MainFileFreeView.java:595 freeviewstop writes 10 bytes to MainProtocol.m_out_stream.  `MainProtocol.m_out_stream.write(bArr, 0, 10);`
- MainFileFreeView.java:604 Fileview(): file-playback request, cmd bytes 0x20,0x08, length byte 12, type code in byte 7 (I=0, E=1, P=2, J=3, other=7 offset), then file index little-endian, channel byte, 0x03 terminator and XOR CRC.  `public void Fileview(int i, String str, int i2) {`
- MainFileFreeView.java:609 Header STX byte 0x02 in Fileview frame.  `bArr[0] = 2;`
- MainFileFreeView.java:610 Command high byte 32 (0x20) in Fileview frame.  `bArr[1] = 32;`
- MainFileFreeView.java:612 Command low byte 8 (0x08) in Fileview frame.  `bArr[2] = 8;`
- MainFileFreeView.java:613 Length field byte 0 = 12 in Fileview frame.  `bArr[3] = (byte) 12;`
- MainFileFreeView.java:618 File-state type code selection begins: I maps to 0.  `if (str.equals("I")) {`
- MainFileFreeView.java:619 Type code I encoded as 0.  `bArr[7] = 0;`
- MainFileFreeView.java:621 Type code E encoded as 1.  `bArr[7] = 1;`
- MainFileFreeView.java:623 Type code P encoded as 2.  `bArr[7] = 2;`
- MainFileFreeView.java:625 Type code J encoded as 3.  `bArr[7] = 3;`
- MainFileFreeView.java:627 Unknown type: offset set to 7, so the following zero-fill starts at byte 7.  `i3 = 7;`
- MainFileFreeView.java:636 File index written little-endian, low byte first.  `bArr[i6] = (byte) (i & 255);`
- MainFileFreeView.java:644 Channel byte i2 written into frame.  `bArr[i10] = (byte) i2;`
- MainFileFreeView.java:652 Terminator 0x03 written in Fileview frame.  `bArr[i15] = 3;`
- MainFileFreeView.java:653 CRC seeded with header byte in Fileview frame.  `bArr[i15] = bArr[0];`
- MainFileFreeView.java:654 CRC XOR loop over indices 1 up to i15 in Fileview frame.  `for (int i16 = 1; i16 < i15; i16++) {`
- MainFileFreeView.java:658 Sets MainProtocol.targetActivity to 10 before sending Fileview.  `MainProtocol.targetActivity = 10;`
- MainFileFreeView.java:661 Fileview writes i17 bytes to MainProtocol.m_out_stream.  `MainProtocol.m_out_stream.write(bArr, 0, i17);`
- MainFileFreeView.java:821 FileList(): list request, cmd bytes 0xA0,0x25, length 4, 0x03 at index 11, XOR CRC at index 12.  `public void FileList() {`
- MainFileFreeView.java:826 Command low byte 37 (0x25) in FileList frame.  `bArr[2] = 37;`
- MainFileFreeView.java:836 Terminator 0x03 at index 11 in FileList frame.  `bArr[11] = 3;`
- MainFileFreeView.java:843 FileList writes 13 bytes to MainProtocol.m_out_stream.  `MainProtocol.m_out_stream.write(bArr, 0, 13);`
- MainFileFreeView.java:852 PreViewOff(): cmd bytes 0x20,0x02, length 4, payload byte 7 = 3, CRC at index 8 over bytes 0-7; no 0x03 byte written in this frame.  `public void PreViewOff() {`
- MainFileFreeView.java:863 Payload value 3 at index 7 in PreViewOff frame.  `bArr[7] = 3;`
- MainFileFreeView.java:864 CRC seeded with header byte; CRC stored at index 8 (where a 0x03 terminator would be in other frames).  `bArr[8] = bArr[0];`
- MainFileFreeView.java:870 PreViewOff writes 9 bytes to MainProtocol.m_out_stream.  `MainProtocol.m_out_stream.write(bArr, 0, 9);`
- MainFileFreeView.java:879 RecordOnOff(): cmd bytes 0xA0,0x3A, length 4, 0x03 at index 11, XOR CRC at index 12.  `public void RecordOnOff() {`
- MainFileFreeView.java:884 Command low byte 58 (0x3A) in RecordOnOff frame.  `bArr[2] = 58;`
- MainFileFreeView.java:894 Terminator 0x03 at index 11 in RecordOnOff frame.  `bArr[11] = 3;`
- MainFileFreeView.java:901 RecordOnOff writes 13 bytes to MainProtocol.m_out_stream.  `MainProtocol.m_out_stream.write(bArr, 0, 13);`
## responses_parsed
- MainFileFreeView.java:1017 Static onReceive(cmd, payload) ignores every command except 8201 (0x2009); for 8201 it sets targetActivity to 2 and parses CNSResultFileStart.  `if (i != 8201) {`
- MainFileFreeView.java:1022 Sets MainProtocol.targetActivity to 2 on a 8201 response.  `MainProtocol.targetActivity = 2;`
- MainFileFreeView.java:690 intToByteArray(): putInt is called before setting LITTLE_ENDIAN order, so the returned array is big-endian; not used in the shown send paths.  `allocate.order(ByteOrder.LITTLE_ENDIAN);`
## video_audio
- MainFileFreeView.java:337 AudioTrack constructed locally, not part of the protocol packet.  `myAudioTrack = new AudioTrack(3, 8000, 4, 2, minBufferSize, 1);`
## unsure
- MainFileFreeView.java:577 MainProtocol constants (SERVERIP, AUDIOPORT, targetActivity field types, m_out_stream) were not resolved; no Grep of MainProtocol.java was performed, so numeric values of these are unknown.  `MainProtocol.m_out_stream.write(bArr, 0, 10);`
- MainFileFreeView.java:690 Whether intToByteArray is ever called is not visible in this file.  `private static byte[] intToByteArray(int i) {`
- MainFileFreeView.java:852 PreViewOff frame has no 0x03 terminator and a 9-byte total; the CRC position differs from the other frames. Intent not determinable from this file.  `bArr[8] = bArr[0];`
- MainFileFreeView.java:1017 Only cmd 8201 is handled; the CNSResultFileStart class layout is defined in MainProtocol, which was not read.  `MainProtocol.CNSResultFileStart cNSResultFileStart = new MainProtocol.CNSResultFileStart(bArr);`

# lists
## sockets
- MainFileListView2.java:419 Shutdown path sends NAT_CMD_NET_CONNECT_STATE3 with index 0 via MainProtocol.send before closing the socket.  `if (MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1) < 0) {`
- MainFileListView3.java:417 Same shutdown send of NAT_CMD_NET_CONNECT_STATE3 as the MainFileListView2 exit dialog.  `if (MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1) < 0) {`
- MainFileListView4.java:416 Same shutdown send of NAT_CMD_NET_CONNECT_STATE3 as the other exit dialogs.  `if (MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1) < 0) {`
- MainFileListView5.java:400 Same shutdown send of NAT_CMD_NET_CONNECT_STATE3 as the other exit dialogs.  `if (MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1) < 0) {`
## commands_sent
- MainFileListView2.java:419 MainProtocol.send call on exit: cmd NAT_CMD_NET_CONNECT_STATE3, payload from CNSSimpleIndex(index 0), arg -1.  `if (MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1) < 0) {`
- MainFileListView3.java:417 MainProtocol.send call on exit: cmd NAT_CMD_NET_CONNECT_STATE3, payload from CNSSimpleIndex(index 0), arg -1.  `if (MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1) < 0) {`
- MainFileListView4.java:416 MainProtocol.send call on exit: cmd NAT_CMD_NET_CONNECT_STATE3, payload from CNSSimpleIndex(index 0), arg -1.  `if (MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1) < 0) {`
- MainFileListView5.java:400 MainProtocol.send call on exit: cmd NAT_CMD_NET_CONNECT_STATE3, payload from CNSSimpleIndex(index 0), arg -1.  `if (MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1) < 0) {`
- MainFileListView2.java:304 PreViewOff builds a 9-byte raw packet: cmd bytes 0x20 0x02, length field 4, bytes 4-6 zero, 0x03 at index 7, XOR CRC over indices 0-7 at index 8. Length field says 4 but payload bytes written are 3 zeros.  `public void PreViewOff() { byte[] bArr = new byte[256]; bArr[0] = 2; bArr[1] = 32; bArr[2] = 2; bArr[3] = (byte) 4; ... bArr[7] = 3; bArr[8]`
- MainFileListView2.java:305 PreViewOff sends raw bytes through the static MainProtocol.m_out_stream, bypassing MainProtocol.send.  `MainProtocol.m_out_stream.write(bArr, 0, 9);`
- MainFileListView2.java:333 wificon builds a 13-byte raw packet: cmd bytes 0xA0 0x3E, length 4, payload bArr[7..10] = 1,0,0,0, 0x03 at index 11, XOR CRC over indices 0-11 at index 12.  `public void wificon() { ... bArr[1] = -96; bArr[2] = 62; bArr[3] = (byte) 4; ... bArr[7] = 1; ... bArr[11] = 3; bArr[12] = bArr[0]; ... m_ou`
- MainFileListView2.java:364 RecordOnOff builds a 13-byte raw packet: cmd bytes 0xA0 0x3A, length 4, payload bArr[7..10] all 0, 0x03 at index 11, XOR CRC at index 12.  `public void RecordOnOff() { ... bArr[1] = -96; bArr[2] = 58; bArr[3] = (byte) 4; ... bArr[7] = 0; ... bArr[11] = 3; bArr[12] = bArr[0]; ... `
- MainFileListView3.java:305 PreViewOff in MainFileListView3 builds the same 9-byte packet (cmd 0x20 0x02) and writes it to m_out_stream.  `bArr[1] = 32; bArr[2] = 2; ... MainProtocol.m_out_stream.write(bArr, 0, 9);`
- MainFileListView3.java:340 wificon in MainFileListView3 builds the same 13-byte packet (cmd 0xA0 0x3E, payload 1,0,0,0).  `bArr[1] = -96; bArr[2] = 62; ... bArr[7] = 1; ... MainProtocol.m_out_stream.write(bArr, 0, 13);`
- MainFileListView3.java:388 RecordOnOff in MainFileListView3 builds the same 13-byte packet (cmd 0xA0 0x3A, payload zeros).  `bArr[1] = -96; bArr[2] = 58; ... bArr[7] = 0; ... MainProtocol.m_out_stream.write(bArr, 0, 13);`
- MainFileListView4.java:302 PreViewOff in MainFileListView4 builds the same 9-byte packet (cmd 0x20 0x02) and writes it to m_out_stream.  `bArr[1] = 32; bArr[2] = 2; ... MainProtocol.m_out_stream.write(bArr, 0, 9);`
- MainFileListView4.java:329 wificon in MainFileListView4 builds the same 13-byte packet (cmd 0xA0 0x3E, payload 1,0,0,0).  `bArr[1] = -96; bArr[2] = 62; ... bArr[7] = 1; ... MainProtocol.m_out_stream.write(bArr, 0, 13);`
- MainFileListView4.java:387 RecordOnOff in MainFileListView4 builds the same 13-byte packet (cmd 0xA0 0x3A, payload zeros).  `bArr[1] = -96; bArr[2] = 58; ... bArr[7] = 0; ... MainProtocol.m_out_stream.write(bArr, 0, 13);`
- MainFileListView5.java:287 PreViewOff in MainFileListView5 builds the same 9-byte packet (cmd 0x20 0x02) and writes it to m_out_stream.  `bArr[1] = 32; bArr[2] = 2; ... MainProtocol.m_out_stream.write(bArr, 0, 9);`
- MainFileListView5.java:312 wificon in MainFileListView5 builds the same 13-byte packet (cmd 0xA0 0x3E, payload 1,0,0,0).  `bArr[1] = -96; bArr[2] = 62; ... bArr[7] = 1; ... MainProtocol.m_out_stream.write(bArr, 0, 13);`
- MainFileListView5.java:343 RecordOnOff in MainFileListView5 builds the same 13-byte packet (cmd 0xA0 0x3A, payload zeros).  `bArr[1] = -96; bArr[2] = 58; ... bArr[7] = 0; ... MainProtocol.m_out_stream.write(bArr, 0, 13);`
## unsure
- MainFileListView2.java:419 Numeric values of NAT_CMD_NET_CONNECT_STATE3 and PCM_FILE, FILE_PATH_* constants were not resolved (no Grep run on MainProtocol.java); the three-command packets in the PreViewOff/wificon/RecordOnOff builders bypass MainProtocol.send and are not framed by its CRC helper as far as read here.  `MainProtocol.NAT_CMD_NET_CONNECT_STATE3`
- MainFileListView2.java:304 PreViewOff length field is 4 but the payload actually written is 3 zero bytes (indices 4-6) before the 0x03 terminator at index 7; the same pattern appears in MainFileListView3/4/5.  `bArr[3] = (byte) 4; ... bArr[7] = 3;`

# settings1
## sockets
- ActivitySetting.java:72 Guard that closes the socket and streams if MainProtocol.mSock is non-null (reboot handler, runs 50s after start).  `if (MainProtocol.mSock != null) {`
- ActivitySetting.java:74 Closes MainProtocol socket inside the reboot handler.  `MainProtocol.mSock.close();`
- ActivitySetting.java:75 Closes output stream inside the reboot handler.  `MainProtocol.m_out_stream.close();`
- ActivitySetting.java:76 Closes input stream inside the reboot handler.  `MainProtocol.m_in_stream.close();`
- ActivitySetting.java:628 Closes socket and streams in the exit-app (back key) confirm handler.  `MainProtocol.mSock.close();`
- ActivitySetting.java:629 Closes output stream in the exit-app confirm handler.  `MainProtocol.m_out_stream.close();`
- ActivitySetting.java:630 Closes input stream in the exit-app confirm handler.  `MainProtocol.m_in_stream.close();`
- ActivitySetting.java:83 Kills the whole process after the reboot delay.  `Process.killProcess(Process.myPid());`
- ActivitySetting.java:639 Kills the whole process in the exit-app confirm handler.  `Process.killProcess(Process.myPid());`
- ActivitySelect.java:73 Finishes ActivitySetting before the reboot sequence (ActivitySelect reboot handler).  `((ActivitySetting) ActivitySetting.mContext).finish();`
- ActivitySelect.java:76 Socket-null guard in ActivitySelect reboot handler.  `if (MainProtocol.mSock != null) {`
- ActivitySelect.java:78 Closes socket in ActivitySelect reboot handler.  `MainProtocol.mSock.close();`
- ActivitySelect.java:79 Closes output stream in ActivitySelect reboot handler.  `MainProtocol.m_out_stream.close();`
- ActivitySelect.java:80 Closes input stream in ActivitySelect reboot handler.  `MainProtocol.m_in_stream.close();`
- ActivitySelect.java:87 Kills the whole process in ActivitySelect reboot handler.  `Process.killProcess(Process.myPid());`
## commands_sent
- ActivitySetting.java:70 Sends NET_CONNECT_STATE3 with index 0 and timeout -1 before reboot.  `MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1);`
- ActivitySetting.java:203 Sends GET_CONFIG on resume unless returning from a child activity; disables list on failure.  `} else if (MainProtocol.send(MainProtocol.NAT_CMD_GET_CONFIG, null, 2) < 0) {`
- ActivitySetting.java:293 Sends FORMAT_TF_CARD with an empty index payload after SD card format confirm.  `if (MainProtocol.send(MainProtocol.NAT_CMD_FORMAT_TF_CARD, new MainProtocol.CNSSimpleIndex().getBytes(), 2) < 0) {`
- ActivitySetting.java:321 Sends FACTORY_RESET with an empty index payload after factory reset confirm.  `if (MainProtocol.send(MainProtocol.NAT_CMD_FACTORY_RESET, new MainProtocol.CNSSimpleIndex().getBytes(), 2) < 0) {`
- ActivitySetting.java:621 Sends NET_CONNECT_STATE3 with index 0 and timeout -1 in the exit-app confirm handler.  `if (MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1) < 0) {`
- ActivitySelect.java:118 Sends SET_NORMAL_IMPACT_SENSITIVITY with selected index, timeout 3.  `if (MainProtocol.send(MainProtocol.NAT_CMD_SET_NORMAL_IMPACT_SENSITIVITY, cNSSimpleIndex.getBytes(), 3) < 0) {`
- ActivitySelect.java:131 Sends SET_VOICE_VOLUME with selected index, timeout 3.  `if (MainProtocol.send(MainProtocol.NAT_CMD_SET_VOICE_VOLUME, cNSSimpleIndex.getBytes(), 3) < 0) {`
- ActivitySelect.java:144 Sends SET_MUTE with selected index, timeout 3.  `if (MainProtocol.send(MainProtocol.NAT_CMD_SET_MUTE, cNSSimpleIndex.getBytes(), 3) < 0) {`
- ActivitySelect.java:157 Sends SET_LCD_BRIGHTNESS with selected index, timeout 3.  `if (MainProtocol.send(MainProtocol.NAT_CMD_SET_LCD_BRIGHTNESS, cNSSimpleIndex.getBytes(), 3) < 0) {`
- ActivitySelect.java:170 Sends SET_WHITE_BALANCE with selected index, timeout 3.  `if (MainProtocol.send(MainProtocol.NAT_CMD_SET_WHITE_BALANCE, cNSSimpleIndex.getBytes(), 3) < 0) {`
- ActivitySelect.java:183 Sends SET_CONTRAST with selected index, timeout 3.  `if (MainProtocol.send(MainProtocol.NAT_CMD_SET_CONTRAST, cNSSimpleIndex.getBytes(), 3) < 0) {`
- ActivitySelect.java:196 Sends SET_EXPOSURE with selected index, timeout 3.  `if (MainProtocol.send(MainProtocol.NAT_CMD_SET_EXPOSURE, cNSSimpleIndex.getBytes(), 3) < 0) {`
- ActivitySelect.java:209 Sends SET_SWITCH_SCREEN with selected index, timeout 3.  `if (MainProtocol.send(MainProtocol.NAT_CMD_SET_SWITCH_SCREEN, cNSSimpleIndex.getBytes(), 3) < 0) {`
- ActivitySelect.java:228 Sends SET_CRASH_EVENT_ALARM with selected index, timeout 3.  `if (MainProtocol.send(MainProtocol.NAT_CMD_SET_CRASH_EVENT_ALARM, cNSSimpleIndex.getBytes(), 3) < 0) {`
- ActivitySelect.java:241 Sends SET_PARKING_LOW_POWER with selected index, timeout 3.  `if (MainProtocol.send(MainProtocol.NAT_CMD_SET_PARKING_LOW_POWER, cNSSimpleIndex.getBytes(), 3) < 0) {`
- ActivitySelect.java:254 Sends SET_PARK_IMPACT_SENSITIVITY with selected index, timeout 3.  `if (MainProtocol.send(MainProtocol.NAT_CMD_SET_PARK_IMPACT_SENSITIVITY, cNSSimpleIndex.getBytes(), 3) < 0) {`
- ActivitySelect.java:267 Sends SET_PARKING_OPTIME with selected index, timeout 3.  `if (MainProtocol.send(MainProtocol.NAT_CMD_SET_PARKING_OPTIME, cNSSimpleIndex.getBytes(), 3) < 0) {`
- ActivitySelect.java:280 Sends SET_TIMEZONE with selected index, timeout 3.  `if (MainProtocol.send(MainProtocol.NAT_CMD_SET_TIMEZONE, cNSSimpleIndex.getBytes(), 3) < 0) {`
- ActivitySelect.java:293 Sends SET_VOICE_GUIDE with selected index, timeout 3.  `if (MainProtocol.send(MainProtocol.NAT_CMD_SET_VOICE_GUIDE, cNSSimpleIndex.getBytes(), 3) < 0) {`
- ActivitySelect.java:306 Sends SET_USE_LED with selected index, timeout 3.  `if (MainProtocol.send(MainProtocol.NAT_CMD_SET_USE_LED, cNSSimpleIndex.getBytes(), 3) < 0) {`
- ActivitySelect.java:418 Sends SET_RECORDER_QUALITY from the confirm dialog, timeout 3.  `if (MainProtocol.send(MainProtocol.NAT_CMD_SET_RECORDER_QUALITY, cNSSimpleIndex.getBytes(), 3) < 0) {`
- ActivitySelect.java:446 Sends SET_PARTITION from the confirm dialog, timeout 3.  `if (MainProtocol.send(MainProtocol.NAT_CMD_SET_PARTITION, cNSSimpleIndex.getBytes(), 3) < 0) {`
## responses_parsed
- ActivitySetting.java:356 onReceive dispatch: compares cmd against 40992 (SD card format response) first.  `if (i != 40992) {`
- ActivitySetting.java:357 Handles response 41013 by storing config bytes and enabling the list.  `if (i == 41013) {`
- ActivitySetting.java:360 Handles response 41045 (factory reset response).  `} else if (i == 41045) {`
- ActivitySetting.java:375 SD card format response: negative result shows error alert, otherwise disables list and reboots.  `} else if (new MainProtocol.CNSResult(bArr).getResult() < 0) {`
- ActivitySelect.java:478 Response case constant NAT_CMD_SET_NORMAL_IMPACT_SENSITIVITY_RESP (value 40865 per comment).  `case MainProtocol.NAT_CMD_SET_NORMAL_IMPACT_SENSITIVITY_RESP /* 40865 */:`
- ActivitySelect.java:489 Response case for SET_VOICE_VOLUME_RESP (value 40867 per comment).  `case MainProtocol.NAT_CMD_SET_VOICE_VOLUME_RESP /* 40867 */:`
- ActivitySelect.java:500 Response case for SET_LCD_BRIGHTNESS_RESP (40869).  `case MainProtocol.NAT_CMD_SET_LCD_BRIGHTNESS_RESP /* 40869 */:`
- ActivitySelect.java:511 Response case for SET_CONTRAST_RESP (40871).  `case MainProtocol.NAT_CMD_SET_CONTRAST_RESP /* 40871 */:`
- ActivitySelect.java:522 Response case for SET_SWITCH_SCREEN_RESP (40873).  `case MainProtocol.NAT_CMD_SET_SWITCH_SCREEN_RESP /* 40873 */:`
- ActivitySelect.java:533 Response case for SET_PARKING_LOW_POWER_RESP (40875).  `case MainProtocol.NAT_CMD_SET_PARKING_LOW_POWER_RESP /* 40875 */:`
- ActivitySelect.java:544 Response case for SET_PARKING_OPTIME_RESP (40877).  `case MainProtocol.NAT_CMD_SET_PARKING_OPTIME_RESP /* 40877 */:`
- ActivitySelect.java:555 Response case for SET_TIMEZONE_RESP (40879).  `case MainProtocol.NAT_CMD_SET_TIMEZONE_RESP /* 40879 */:`
- ActivitySelect.java:566 Response case for SET_VOICE_GUIDE_RESP (40881).  `case MainProtocol.NAT_CMD_SET_VOICE_GUIDE_RESP /* 40881 */:`
- ActivitySelect.java:577 Response case for SET_PARTITION_RESP (40883).  `case MainProtocol.NAT_CMD_SET_PARTITION_RESP /* 40883 */:`
- ActivitySelect.java:589 Response case for SET_CRASH_EVENT_ALARM_RESP (40885).  `case MainProtocol.NAT_CMD_SET_CRASH_EVENT_ALARM_RESP /* 40885 */:`
- ActivitySelect.java:600 Response case for SET_USE_LED_RESP (40893).  `case MainProtocol.NAT_CMD_SET_USE_LED_RESP /* 40893 */:`
- ActivitySelect.java:611 Response case for SET_RECORDER_QUALITY_RESP (40962).  `case MainProtocol.NAT_CMD_SET_RECORDER_QUALITY_RESP /* 40962 */:`
- ActivitySelect.java:623 Response case for SET_WHITE_BALANCE_RESP (40968).  `case MainProtocol.NAT_CMD_SET_WHITE_BALANCE_RESP /* 40968 */:`
- ActivitySelect.java:634 Response case for SET_EXPOSURE_RESP (40970).  `case MainProtocol.NAT_CMD_SET_EXPOSURE_RESP /* 40970 */:`
- ActivitySelect.java:645 Response case for SET_MUTE_RESP (40974).  `case MainProtocol.NAT_CMD_SET_MUTE_RESP /* 40974 */:`
- ActivitySelect.java:656 Response case for SET_PARK_IMPACT_SENSITIVITY_RESP (40986).  `case MainProtocol.NAT_CMD_SET_PARK_IMPACT_SENSITIVITY_RESP /* 40986 */:`
## settings
- ActivitySetting.java:144 If VER_CHECK equals 1, items 7 and 11 are blocked with a version error toast.  `if (MainProtocol.VER_CHECK == 1 && (i == 7 || i == 11)) {`
- ActivitySetting.java:474 Adapter greys out items 7 and 11 when VER_CHECK equals 1.  `if (MainProtocol.VER_CHECK == 1) {`
- ActivitySetting.java:184 Shows progress when the DHCP gateway IP equals MainProtocol.SERVERIP.  `if (format.equals(MainProtocol.SERVERIP)) {`
- ActivitySetting.java:181 Reads gateway from WifiManager DHCP info.  `int i = ((WifiManager) getApplicationContext().getSystemService("wifi")).getDhcpInfo().gateway;`
- ActivitySetting.java:95 Disabled-list gate: non-enabled taps show setting_error except item 25.  `if (!ActivitySetting.mSettingAdapter.getEnable() && i != 25) {`
- ActivitySetting.java:70 Reboot handler timeout is 50000 ms.  `}, 50000L);`
- ActivitySelect.java:89 Reboot handler timeout is 45000 ms.  `}, 45000L);`
- ActivitySelect.java:98 Select screen shows error toast when adapter is not enabled, but still proceeds with send.  `if (!ActivitySelect.mSettingSelectAdapter.getEnable()) {`
## unsure
- ActivitySetting.java:114 Numeric values of NAT_CMD_* send constants are not in these two files; they are defined in MainProtocol.java and were not resolved by this extractor (Grep not run). Response values 40865..40986 appear only as comments in ActivitySelect.java.  `MainProtocol.NAT_CMD_* (send constants) -> not resolved`
- ActivitySetting.java:40992 Literal 40992 in onReceive is assumed to be the SD card format response, based on the branch contents at lines 375-382, not on a named constant.  `if (i != 40992) {`
- ActivitySetting.java:41013 Literal 41013 in onReceive has no named constant in this file; purpose is inferred from the branch that stores config bytes.  `if (i == 41013) {`
- ActivitySetting.java:41045 Literal 41045 in onReceive has no named constant in this file; purpose is inferred from the factory reset alert branch.  `} else if (i == 41045) {`
- ActivitySetting.java:203 Send with null payload and timeout 2 on resume; the meaning of timeout value 2 is not defined in these files.  `MainProtocol.send(MainProtocol.NAT_CMD_GET_CONFIG, null, 2)`

# settings2
## sockets
- ActivityWifiPassword.java:56 Closes the shared MainProtocol socket before the app kills its own process (Wi-Fi password flow).  `if (MainProtocol.mSock != null) {`
- ActivityWifiPassword.java:58 Closes the socket instance.  `MainProtocol.mSock.close();`
- ActivityWifiPassword.java:59 Closes the output stream.  `MainProtocol.m_out_stream.close();`
- ActivityWifiPassword.java:60 Closes the input stream.  `MainProtocol.m_in_stream.close();`
- ActivityPartitionSelect.java:77 Same socket teardown in the partition-select reboot path.  `if (MainProtocol.mSock != null) {`
- ActivityPartitionSelect.java:79 Closes the socket instance in the partition-select reboot path.  `MainProtocol.mSock.close();`
## commands_sent
- ActivityDateAndTime.java:58 Sends NAT_CMD_SET_TIME with CNSTime payload and activity code 5.  `if (MainProtocol.send(MainProtocol.NAT_CMD_SET_TIME, cNSTime.getBytes(), 5) < 0) {`
- ActivityWifiPassword.java:97 Sends NAT_CMD_SET_WIFI_PWD with CNSWiFiSetting payload and activity code 4.  `if (MainProtocol.send(MainProtocol.NAT_CMD_SET_WIFI_PWD, cNSWiFiSetting.getBytes(), 4) < 0) {`
- ActivityWifiPassword.java:53 Sends NAT_CMD_NET_CONNECT_STATE3 with index 0 and activity code -1 after a 10 s delay.  `MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1);`
- ActivityPartitionSelect.java:75 Sends NAT_CMD_NET_CONNECT_STATE3 with index 0 and activity code -1 after a 30 s delay.  `MainProtocol.send(MainProtocol.NAT_CMD_NET_CONNECT_STATE3, cNSSimpleIndex.getBytes(), -1);`
- ActivityPartitionSelect.java:212 Sends NAT_CMD_SET_RECORDER_QUALITY with index payload and activity code 3.  `if (MainProtocol.send(MainProtocol.NAT_CMD_SET_RECORDER_QUALITY, cNSSimpleIndex.getBytes(), 3) < 0) {`
- ActivityPartitionSelect.java:240 Sends NAT_CMD_SET_PARTITION with index payload and activity code 3.  `if (MainProtocol.send(MainProtocol.NAT_CMD_SET_PARTITION, cNSSimpleIndex.getBytes(), 3) < 0) {`
## responses_parsed
- ActivityDateAndTime.java:138 onReceive accepts only command 40978 and parses CNSResult.  `if (i != 40978) {`
- ActivityWifiPassword.java:226 onReceive handles command 41009 for the Wi-Fi password result.  `if (i == 41009) {`
- ActivityWifiPassword.java:238 On success, deletes existing wifilist row for the SSID.  `m_db.delete("wifilist", "ssid=?", new String[]{mWiFi_Ssid});`
- ActivityWifiPassword.java:239 Stores the SSID and plaintext password in local SQLite.  `m_db.execSQL("insert into wifilist(ssid, password) values('" + mWiFi_Ssid + "', '" + mSendPW + "');");`
- ActivityPartitionSelect.java:271 onReceive handles command 40883 for the partition result.  `if (i == 40883) {`
- ActivityPartitionSelect.java:278 On success, sets the local config partition to the selected option.  `mConfig.nPartition = mSelectedOpt;`
## settings
- CNSConfig.java:21 Default field values set in the constructor (e.g. nPartition defaults to 2).  `this.nPartition = 2;`
- CNSConfig.java:121 CRC read from byte offset 252 of the config buffer.  `this.nCRC = Utils.byteArrayToInt_Little(bArr, 252);`
- CNSConfig.java:288 Product name containing 't8' selects VER_CHECK=1, otherwise VER_CHECK=2.  `if (str.contains("t8")) {`
- CNSConfig.java:290 Sets MainProtocol.VER_CHECK to 1 for the T8 model.  `MainProtocol.VER_CHECK = 1;`
- CNSConfig.java:295 Firmware version string selects FHD_CHECK (FH=1, FF=2, HH=3).  `if (str2.contains("FH")) {`
- SettingDefs.java:41 Record-quality option list chosen by FHD_CHECK.  `if (MainProtocol.FHD_CHECK == 1) {`
- SettingDefs.java:60 Parking OPTime option list chosen by VER_CHECK (v2 list when VER_CHECK==1).  `if (MainProtocol.VER_CHECK == 1) {`
- ActivityDateAndTime.java:46 Date validation accepts years 2014 to 2037 only.  `if (year > 2037 || year < 2014 || month > 12 || month < 1 || dayOfMonth > 31 || dayOfMonth < 1 || intValue > 23 || intValue < 0 || intValue2`
- ActivityWifiPassword.java:177 Wi-Fi password must be exactly 8 characters (MIN and MAX both 8).  `if (obj.length() < 8 || obj.length() > 8) {`
- ActivityFirmware.java:10 Firmware activity reads the version string from an intent extra and shows it.  `this.mFWVersion = getIntent().getStringExtra("logon.e100.firmware.version");`
## unsure
- ActivityDateAndTime.java:58 NAT_CMD_SET_TIME, NAT_CMD_SET_WIFI_PWD, NAT_CMD_NET_CONNECT_STATE3, NAT_CMD_SET_RECORDER_QUALITY and NAT_CMD_SET_PARTITION numeric values are not resolved; MainProtocol.java was not grepped in this pass.  `MainProtocol.NAT_CMD_SET_TIME`
- CNSConfig.java:21 Line 21 is the array size in a CREATOR newArray; the 'nPartition default 2' item is on line 74, not 21. Line 21 entry above was recorded in error and should be disregarded.  `return new CNSConfig[i];`
- ActivityWifiPassword.java:43 Password is written to SQLite in plaintext (line 239); noted as observed, not assessed.  `m_db.execSQL("insert into wifilist(ssid, password) values('" + mWiFi_Ssid + "', '" + mSendPW + "');");`
