# Car-test checklist

This is one round of testing Dashcam Viewer on the real Eurostyle T8. You run it and send me your notes, screenshots and the app's diagnostics log. I fix what failed and push, CI builds a new `.ipa`, and you repeat the steps that failed. The plan's goal is reached when Parts C to G pass: live view front and rear, browsing, playing, saving, deleting saved copies and changing settings all work, and the camera still records afterwards.

Nothing in Parts A to G formats the SD card, resets the camera or deletes recordings on it. Part H changes things that are harder to undo, and it is optional.

Allow about 1.5 hours. You can split the round at the end of any Part; send the log (Part I) at the end of each session.

## Before you start
- **Where:** parked somewhere safe with the ignition on, so the camera runs as it does when you drive. Never test while driving. If the engine is off for a long session, keep an eye on the car battery.
- **What you need:**
  - the iPhone 15 with Dashcam Viewer installed ([INSTALL_SIDESTORE.md](INSTALL_SIDESTORE.md));
  - the old iPhone or iPad with LogonT;
  - the camera's Wi-Fi password (the factory default is 12345678);
  - paper, or another device, for notes.
- **How to note results:** every step has an ID (B1, C2, …). For each one, write ✔, ✘ or "skipped", and describe what you saw whenever it differs from **Expect**. Screenshot every error message (side button + volume up); taking a screenshot doesn't disturb the app.
- **One phone at a time.** The camera accepts only one phone. Before you use the other phone, release the camera:
  - LogonT (old phone): swipe LogonT away in the app switcher, then turn off Wi-Fi in **Settings › Wi-Fi**.
  - Dashcam Viewer: go to the Home Screen, or tap **Disconnect** in the status bar at the top of the app.
- **Dashcam Viewer disconnects whenever it leaves the screen** (Home Screen, another app, iOS Settings). That is intended: it frees the camera for other phones. When you come back, tap **Connect** at the top, or **Try again** in the Live tab.
- **If a step fails,** note it and carry on with the next one. Only if Dashcam Viewer can't connect at all (C2), skip to Part I and send the log.

## Part A. At home, before the car (10 minutes)
- **A1.** Install the newest build ([INSTALL_SIDESTORE.md](INSTALL_SIDESTORE.md), Parts 2 and 3; Part 5 if an older build is installed).
- **A2.** In SideStore, with LocalDevVPN connected, tap **Refresh All**, so that the app can't expire during the round. SideStore can't refresh on the dashcam's Wi-Fi.
- **A3.** Optional rehearsal with the simulator. On the Mac, in the project folder, run `cd T8Kit && swift run t8sim`. On the iPhone (same home Wi-Fi), open Dashcam Viewer › **Settings › Connection**, choose **T8 simulator on a Mac** and enter the Mac's IP address (on the Mac: **System Settings › Wi-Fi › Details**). Try Live, Recordings, Saved and Settings.
- **A4.** In Dashcam Viewer › **Settings › Connection**, **Connect to** must show **Dashcam (192.168.100.1)**. Switch it back if you rehearsed.
- **A5.** Optional: **Diagnostics › Clear the log**, so that the log starts with the car test.

## Part B. In the car: LogonT first (10 minutes)
These notes are what Dashcam Viewer is compared with.
- **B1.** Before any phone connects, look at the camera's lights: the Security LED on the front, and the Status, GPS and Wi-Fi LEDs on the side. Note which are on, off or blinking, or take a photo. The manual says the Status LED is on when the camera works normally. This is how the camera looks while it records without a phone.
- **B2.** Old phone: join the "LogOn T8 …" Wi-Fi and open LogonT. **Expect:** live video. Note roughly how long it takes to appear.
- **B3.** LogonT › **FILE › DVR**. For each folder (DRIVE, EVENT, PARK, PHOTO), screenshot the top of the list. In DRIVE, also scroll to the bottom and screenshot the oldest files.
- **B4.** LogonT › **SETTINGS**: screenshot the whole list (scroll down), and **Blackbox Information** if it opens.
- **B5.** Release the camera: swipe LogonT away and turn off the old phone's Wi-Fi.
- **B6.** Look at the camera's lights again. Note any difference from B1.

## Part C. Dashcam Viewer: connect and live view (10 minutes)
- **C1.** iPhone 15: **Settings › Wi-Fi**, join "LogOn T8 …". If iOS says the network has no internet, stay connected. Open LocalDevVPN and note whether it shows Connected.
- **C2.** Open Dashcam Viewer on the **Live** tab. If iOS asks to find and connect to devices on the local network, tap **Allow**. **Expect:** a green dot and "Connected to …" at the top, and a picture within 20 seconds. Note how long it took, and the resolution shown at the bottom right.
  - If it fails: screenshot the message. Open **Diagnostics**, tap **Test the connection** and screenshot the Checklist section.
  - If LocalDevVPN is connected, disconnect it, come back and tap **Try again**. Note whether that helped.
- **C3.** Watch the picture for a minute and compare it with LogonT's (B2): smoothness, delay (wave a hand in front of the lens), smearing or blocks.
- **C4.** Tap **Front/Rear**. **Expect:** the rear picture within a few seconds, and "Rear" under the picture. Tap it again: the front picture and "Front".
- **C5.** With the rear picture showing, tap **Mirror rear**. **Expect:** the picture flips left to right. Tap it again to undo. This only changes the app's display, not the camera.
- **C6.** Tap **Photo**. **Expect:** "Photo taken. It is in the camera's Photo folder." Note the time on the iPhone's clock.
- **C7.** Note whether the voice button shows **Voice on** or **Voice off**. Tap it. **Expect:** "Voice recording is off." (or "… is on."). Note anything the camera says. Tap it again to restore your setting.
- **C8.** Leave the live view running for 3 minutes without touching anything. **Expect:** it keeps playing, with no "Connection lost" at the top.
- **C9.** Open **Diagnostics**. Note **Heartbeats** ("N received, last …"). Wait one minute and note it again. **Expect:** the number grows and **Session** still says Connected.

## Part D. Recordings and playback (20 minutes)
- **D1.** **Recordings › Driving**. **Expect:** a list, newest first; each entry shows a date and time with the camera's file name underneath, and "F + R" when there is also a rear recording. Compare with LogonT (B3): the newest names at the top, and the oldest at the bottom. Note any differences, and whether the orange note "The camera stopped sending the list early …" appears.
- **D2.** **Event**, **Parking** and **Photo**: the same comparison. A folder that is empty in LogonT should show "No recordings in this folder …".
- **D3.** **Photo**: the photo from C6 should be at the top, with a time within a minute of the one you noted in C6. (Live view sets the camera's clock from the iPhone.) Tap it. **Expect:** the photo opens. Tap **Close**.
- **D4.** **Driving**: tap the second-newest recording (the newest may still be being written) and choose **Front**. **Expect:** picture and sound within a few seconds, a progress bar and times. Let it play to the end. Note the sound, the smoothness and the clip's length.
- **D5.** When it ends, tap **Save to iPhone**. The first time, iOS asks for access to Photos: allow it. **Expect:** "Saved to Photos and the Saved tab." Tap **Close**.
- **D6.** Play the same recording again and choose **Rear**. **Expect:** as in D4. Close it without saving.
- **D7.** On a different recording, swipe left, tap **Save** and choose **Front**. **Expect:** "Downloading…" with a growing size, then "Converting to MP4…", then "Saved to Photos and the Saved tab." Note how long the download took and how long the clip is.
- **D8.** If Event has recordings, play one.

**Checkpoint:** send the log now (I1), then carry on. The log keeps only its newest 5,000 lines.

## Part E. Saved copies (5 minutes)
- **E1.** **Saved**: the clips from D5 and D7 are listed with their size and date. Tap one. **Expect:** it plays with sound.
- **E2.** Open the Photos app. **Expect:** the clip from D5 is there and plays with sound. Return to Dashcam Viewer.
- **E3.** In **Saved**, swipe left on one clip, tap **Delete**, then **Delete** again. **Expect:** "Deleted …" and the clip disappears. Then open **Recordings › Driving** and pull down to refresh. **Expect:** the recording is still on the camera.

## Part F. Settings (15 minutes)
- **F1.** **Settings**. **Expect:** **Camera** shows Model, Serial and Firmware, and **Camera settings** lists every value. Screenshot the whole page (scroll down). Compare each value with LogonT's (B4) and note any differences.
- **F2.** Note the current **LCD Brightness**, then choose another value. **Expect:** "LCD Brightness changed." Pull down to reload. **Expect:** the new value stays.
- **F3.** Release the camera (Home Screen). On the old phone, turn Wi-Fi on and open LogonT. **Expect:** LogonT connects, which shows that Dashcam Viewer let go of the camera, and its LCD Brightness shows the new value. Set it back to the old value in LogonT, then release the camera (as in B5).
- **F4.** In Dashcam Viewer › **Settings**, tap **Connect** if needed and pull down to reload. **Expect:** LCD Brightness shows the old value again.
- **F5.** Tap **Set the camera clock to this iPhone's time**. **Expect:** "Camera clock set to …" with the iPhone's date and time.
- **F6.** One phone at a time:
  1. With Dashcam Viewer connected (green dot), turn on the old phone's Wi-Fi and open LogonT. Note what LogonT shows.
  2. Release Dashcam Viewer (Home Screen) and let LogonT connect.
  3. Open Dashcam Viewer and tap **Connect**. **Expect:** "Another phone is already connected. Disconnect it, then try again." Screenshot it.
  4. Release the camera from LogonT (B5), then tap **Connect** in Dashcam Viewer. **Expect:** connected again.

## Part G. Does the camera keep recording? (10 minutes)
Before listing files and when leaving live view, LogonT sends a "record off" command whose effect is unknown, and it never sends "record on" when it exits. Dashcam Viewer does the same. This part checks that the camera records again once no phone is connected.
- **G1.** Release the camera from both phones. Note the time.
- **G2.** Wait 5 minutes without connecting any phone (driving normally is fine). Then look at the camera's lights and compare them with B1.
- **G3.** Open Dashcam Viewer, go to **Recordings › Driving** and pull down to refresh. **Expect:** recordings with times after the time you noted in G1. Also note whether there are recordings from the time when a phone was connected (Parts B to F); either answer helps.

If there are no recordings after G1, the camera has stopped recording. Tell me straight away. Then try restarting the camera: switch the ignition off, wait until the camera's lights change, switch it on again, and repeat G1 to G3. Note whether that brought recording back.

## Part H. Optional: changes that are harder to undo
Do these only after Parts A to G, and only if you want to.
- **H1. Video Quality.** The camera restarts after this change. In **Settings**, note the current **Video Quality**, choose another value and tap **Change**. **Expect:** "Video quality changed. The camera restarts; reconnect in a minute." Wait a minute, tap **Connect** and check the value. Then change it back the same way.
- **H2. Wi-Fi password.** The manual shows no button on the camera that resets the Wi-Fi password, and LogonT's Factory Reset needs a Wi-Fi connection. If the new password is lost, neither app can connect, so write both passwords down first.
  1. On paper, write the current password and the new one (exactly 8 characters, no spaces).
  2. **Settings › Change the camera's Wi-Fi password**: enter the new password twice, tap **Change Wi-Fi password**, then **Change**.
  3. **Expect:** "Wi-Fi password has been changed. …". About 10 seconds later the app disconnects.
  4. iPhone **Settings › Wi-Fi**: tap ⓘ next to "LogOn T8 …", tap **Forget This Network**, then join it again with the new password. Open Dashcam Viewer and tap **Connect**. **Expect:** connected.
  5. Change the password back to the old one the same way, and rejoin with the old password, so that LogonT on the old phone keeps working.

  If the app shows an error in step 3 but the Wi-Fi drops anyway, try joining with the new password first.

## Part I. Send the results
- **I1.** **Diagnostics › Share the diagnostics log**, then **Save to Files** (or AirDrop it to the Mac). Do this before you close the app: the log lives only in the app's memory, and it is lost when the app is closed or when iOS closes it after a while in the background.
- **I2.** Send me here: the log file or files, the screenshots, your notes by step ID, and anything else that seemed odd.

The log contains the camera's Wi-Fi password and serial number (inside the camera's settings reply, in hex), and after H2 also the new password. Keep it out of public places such as GitHub issues; the repository is public.

A format for the notes, if it helps:
```
LocalDevVPN during the test: connected / not connected
B1 lights before LogonT:
B6 lights after LogonT:
C2 ✔ picture after __ s, resolution __
C4 ✘ "…" (screenshot 3)
…
```

## What each part checks
| Question | Steps |
|---|---|
| Is the live stream on port 7778 plain H.264 from the first byte? (The log shows any bytes skipped.) | C2, C3 |
| Does the camera's heartbeat carry a payload, and does the session stay up? | C8, C9 |
| File names, and long lists that arrive in several pages | D1, D2 |
| Does the app read the camera's settings correctly? | F1 to F4 |
| Clock and snapshot | C6, D3, F5 |
| One phone at a time, and releasing the camera | F3, F6 |
| What "record off" does, and whether the camera records again afterwards | B1, B6, G1 to G3 |
| Which reply the camera sends to a Wi-Fi password change | H2 |
| Does the 2021-era command flow still work on this firmware? | All |
