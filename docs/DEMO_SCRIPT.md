# Demo script: Dashcam Viewer

A 10-minute live demo of the app on a real iPhone, talking to the T8 simulator (`t8sim`) on the Mac instead of the dashcam. Lines in *italics* are what you say; the rest is what you do. Button names and messages are quoted exactly as the app shows them.

Release v0.1.0, 2026-11-23.

## 1. Before the day (once)

### 1.1 Get the source code onto the Mac
`T8Kit` is a folder inside the project's source code. The `.ipa` does not contain it, so download the code first. Use one of these two ways.

- **Download the zip (simplest):** open https://github.com/cfeiwong85/DashcamViewer/releases/tag/v0.1.0 and click **Source code (zip)** under Assets. Safari unzips it into `Downloads` as a folder named `DashcamViewer-0.1.0`. Then, in Terminal:
  ```bash
  cd ~/Downloads/DashcamViewer-0.1.0/T8Kit
  ```
- **Or clone it with git** (git comes with the Command Line Tools):
  ```bash
  cd ~
  git clone https://github.com/cfeiwong85/DashcamViewer.git
  cd ~/DashcamViewer/T8Kit
  ```

If `cd` says "No such file or directory", run `ls ~/Downloads` (or `ls ~`) to see the folder's real name, and use that.

### 1.2 Build and start the simulator
In the `T8Kit` folder:
```bash
swift run t8sim
```
The first build takes several minutes. It is ready when it prints `T8 simulator ready: control 7777, video 7778, audio 7779. Press Ctrl-C to stop.` If macOS asks whether `t8sim` may accept incoming network connections, click **Allow**. Later starts take a few seconds.

### 1.3 Connect the iPhone
1. Install the app with SideStore (`docs/INSTALL_SIDESTORE.md`) if it is not installed yet.
2. Find the Mac's IP address in a second Terminal window: `ipconfig getifaddr en0` (for example `192.168.1.23`).
3. Put the iPhone on the same Wi-Fi as the Mac.
4. In the app, open **Settings**. Under Connection, set **Connect to** to **T8 simulator on a Mac**, type the Mac's IP address, and leave the port at 7777. The app remembers this.
5. Open the **Live** tab. When iOS asks to find devices on the local network, tap **Allow**. (If you tapped Don't Allow: iPhone Settings › Privacy & Security › Local Network › Dashcam Viewer.)

### 1.4 Rehearse, and record a backup
- Run the whole of section 3 once, start to finish, with a timer.
- Rehearse the hotspot fallback too (section 4, first row). Classroom and office Wi-Fi often blocks devices from talking to each other.
- Record the rehearsal. In the QuickTime window from 2.3, click the red record button. If the live demo fails on the day, play this recording instead.

## 2. On the day (10 minutes before)

1. Start the simulator: `cd` into `T8Kit` (1.1), then `swift run t8sim`.
2. Check the Mac's IP address again (`ipconfig getifaddr en0`). It can change between networks; if it did, update it in the app's Settings.
3. Plug the iPhone into the Mac with a cable. Open **QuickTime Player** › File › **New Movie Recording**. Click the small arrow next to the record button and choose the iPhone as the camera. The iPhone's screen appears on the Mac.
4. Arrange the Mac's screen: QuickTime (the phone) on the left, the Terminal running `t8sim` on the right. Make the Terminal font large (Cmd and +).
5. On the iPhone: turn on Do Not Disturb, set the brightness high, and turn off Auto-Lock (Settings › Display & Brightness › Auto-Lock › Never) for the demo.
6. Open the app once and check that **Live** shows the moving test picture. Then go to the Settings tab, so the demo starts from a calm screen.
7. Share the Mac's screen to the projector or the video call.

## 3. The script (about 10 minutes)

### 3.1 The problem (1 minute)
*"I have a Eurostyle T8 dashcam with a front and a rear camera. The only way to watch its recordings on a phone was an app called LogonT, and LogonT has been discontinued. So I built my own iPhone app to replace it."*

*"The camera doesn't use any standard video protocol. It speaks its own binary language over Wi-Fi. To learn that language, I took the old Android app apart and read its code. Everything the app does is based on that."*

### 3.2 Why a simulator (30 seconds)
Point at the Terminal.

*"The dashcam is in my car, so instead I'm using a simulator I wrote. It runs on this Mac and behaves like the camera: same ports, same messages, same rules. The video is a computer-generated test picture, not real road footage. The phone app has no idea it isn't talking to the real camera."*

### 3.3 Connecting (1 minute)
In **Settings**, show the Connection section: **Connect to** is set to **T8 simulator on a Mac**, with the Mac's IP address.

*"Normally this says Dashcam, at the camera's fixed address. For the demo, it points at my Mac."*

Scroll to the **Camera** section: Model `logon t8`, Serial `SIM00000001`, Firmware `T8_FH_SIM_1.0`.

*"These details come from the simulator, the same way the real camera reports them."*

### 3.4 Live view (2 minutes)
Open the **Live** tab. The banner turns green with "Connected to logon t8", and the picture starts moving.

*"This is the live picture from the front camera."* Point at the Terminal as lines scroll. *"Every message between the phone and the camera shows up here. When I opened this tab, the phone asked the camera to start the live view; that's `FREEVIEW_START`. The camera also sends a `HEARTBEAT` every few seconds, and the app has to answer it, or the camera hangs up."*

Tap **Front/Rear**. *"The rear camera."* Tap **Mirror rear**. *"Mirrored, like a rear-view mirror."* Tap **Front/Rear** again.

Tap **Photo**. The app says "Photo taken. It is in the camera's Photo folder." *"That told the camera to take a photo. We'll find it in a moment."*

### 3.5 Recordings and playback (2 minutes)
Open the **Recordings** tab. It loads the **Driving** folder.

*"These are the recordings on the camera's memory card. F + R means there's a front and a rear video for that moment."*

Tap the **Photo** folder, then pull the list down to refresh. The photo from 3.4 is at the top. *"There's the photo we just took."*

Go back to **Driving**, tap the top recording, and choose **Front**. Playback opens full screen. *"The video streams straight from the camera. Nothing is downloaded first."* Let it play to the end (the simulator's clips are 3 seconds long).

### 3.6 Saving a clip (1 minute)
When the clip ends, a **Save to iPhone** button appears under the picture. Tap it. Wait for the status to finish, then tap **Close**. If iOS asks for access to Photos, allow it.

*"The app converts the camera's raw video into a normal MP4, so it can go into Photos or be sent to someone, say for an insurance claim."*

Open the **Saved** tab and show the file with its size and date. Optionally open the Photos app to show it there too.

*"One thing the app can't do is delete recordings on the camera. LogonT never had that command, so the camera doesn't offer it. I checked instead of guessing."*

### 3.7 Camera settings (1 minute)
Open **Settings**, tap **LCD Brightness**, and pick another value. Point at the Terminal, where the setting command appears.

*"The old app's settings are here, with one exception: anything that can wipe the memory card, like formatting it, is left out on purpose."*

### 3.8 When things go wrong (1.5 minutes)
*"A dashcam in a car loses Wi-Fi all the time, so handling failures matters as much as the features."*

1. Open **Live**. Click the Terminal and press **Ctrl-C** to stop the simulator. *"I've just switched the camera off."* The app shows that the connection was lost, in plain words instead of crashing.
2. Start it again: `swift run t8sim` (press the up arrow to get the command back). Tap **Connect** in the banner, or **Try again** on the picture. The picture comes back.
3. Optional, if time allows: stop it again and start `swift run t8sim --already-connected`. Tap **Connect**. The app says "Another phone is already connected. Disconnect it, then try again." *"The camera only allows one phone at a time, and the app explains that, instead of just failing."* Afterwards, restart it with plain `swift run t8sim`.

*"With a real camera, I can't make these problems happen on cue. With the simulator I can, and the automated tests do it on every change."*

### 3.9 Diagnostics and wrap-up (1 minute)
Open **Diagnostics** and scroll the packet log.

*"When I test in the car, this log records everything, and I can share it to fix problems. It used to contain the camera's Wi-Fi password, so the app now hides it as two stars."*

Close with:

*"So far: the camera's protocol is worked out from the old app, the iPhone app does live view, playback, saving and settings, and about 80 automated tests run on GitHub on every change. What's still missing is the test in the car with the real camera. I've already written the checklist for that."*

## 4. If something goes wrong

| Problem | Fix |
|---|---|
| The banner says "Cannot reach the camera" | Check the Mac's IP address (`ipconfig getifaddr en0`) and the app's Settings. Check that `t8sim` is still running. If the venue Wi-Fi blocks phone-to-Mac traffic, turn on the iPhone's **Personal Hotspot**, join the Mac to it, and enter the Mac's new IP address in the app. |
| No Local Network prompt appeared, and the app can't connect | iPhone Settings › Privacy & Security › Local Network › turn on Dashcam Viewer. |
| Recordings says "Stop the live view first." | The live view was still closing. Pull the list down to refresh. |
| Live shows "No picture after 20 seconds…" | Tap **Try again**. If it repeats, restart `t8sim`. |
| The app was installed more than 7 days ago and won't open | Refresh it in SideStore (a free Apple account only allows 7 days). Do this the day before. |
| The phone doesn't appear in QuickTime | Unlock the iPhone and tap **Trust** on it, then reopen New Movie Recording. |
| Anything else | Play the backup recording from 1.4. |

## 5. Likely questions

- **"Does it work with the real camera?"** Not tested yet. The protocol comes from the old app's code, and the car test is next. Say this plainly; it is the honest status.
- **"How did you work out the protocol?"** By decompiling LogonT's Android app with a tool called jadx and reading the Java code. Where the video comes from was found by looking inside the video library the old app bundled.
- **"Why isn't it on the App Store?"** It's a personal project. It is installed with SideStore and a free Apple account.
- **"Is it safe to share the diagnostics log?"** Yes. The Wi-Fi password is replaced with `**` before anything is stored.
- **"Who wrote the code?"** Answer truthfully about how it was built, including the AI coding assistant (Claude Code) and your part in directing, deciding and testing it.
