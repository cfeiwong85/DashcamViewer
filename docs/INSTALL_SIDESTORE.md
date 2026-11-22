# Installing Dashcam Viewer with SideStore

This guide puts Dashcam Viewer on the iPhone with a free Apple Account and keeps it working past Apple's 7-day limit. You need the Mac once, for setup. After that the iPhone refreshes the app itself, as long as it reaches home Wi-Fi now and then.

The steps were checked on 2026-11-22 against the official SideStore documentation (docs.sidestore.io), the source of iloader 2.4.2 (whose "Stable" option installs SideStore's latest GitHub release) and the source of SideStore 0.7.0-alpha, its newest release tag. SideStore changes often. If a screen looks different from this guide, follow the official pages listed at the end.

## What you need
- The iPhone 15 (iOS 26) with a passcode set.
- The Mac. SideStore's installer needs macOS High Sierra or later, so macOS 14.8 is fine. The iloader download is a universal build, which runs on the 2015 Intel MacBook Pro.
- A USB cable for the iPhone.
- An Apple Account. A free one is enough. It doesn't have to be the account signed in on the iPhone. The email address is case-sensitive when you type it in.
- Home Wi-Fi with internet. SideStore's documentation says mobile data is not suitable.
- A GitHub sign-in in Safari on the iPhone, to download the app (GitHub only lets signed-in users download build files, even from a public repository).

Setup takes about half an hour, plus time for any troubleshooting.

## Part 1: One-time setup

### Step 1. iPhone: install LocalDevVPN
SideStore uses a VPN app that only talks to the iPhone itself (no outside server), so that it can install apps without a computer.
1. Install **LocalDevVPN** from the App Store: https://apps.apple.com/app/localdevvpn/id6755608044
2. Open it and tap **Connect**. If iOS asks to "Allow VPN Configurations", tap **Allow** and enter the passcode.

LocalDevVPN must be connected whenever SideStore installs, updates or refreshes an app.

### Step 2. Mac: install iloader
iloader is the Mac program that puts SideStore on the iPhone. The only official sources are https://iloader.app and https://github.com/nab138/iloader.
1. Download the Mac version: https://github.com/nab138/iloader/releases/latest/download/iloader-darwin-universal.dmg
2. Open the `.dmg` and drag **iloader** into Applications.
3. Open iloader. If macOS says it can't verify the app, open **System Settings › Privacy & Security**, scroll down and click **Open Anyway**.

### Step 3. Mac: install SideStore onto the iPhone
1. Connect the iPhone to the Mac with the USB cable and unlock it. If the iPhone asks "Trust This Computer?", tap **Trust** and enter the passcode.
2. In iloader, sign in with the Apple Account. Enter the verification code if Apple sends one.
3. Select the iPhone.
4. Choose **SideStore (Stable)** to install it. iloader downloads SideStore, signs it with the Apple Account, installs it and places the pairing file (a small file that lets SideStore talk to the iPhone's installer without the Mac).
5. Wait for "SideStore Installed!".

If iloader shows an error, it lists suggested fixes under the message. SideStore's support does not cover iloader problems; those go to the idevice Discord (https://discord.gg/mACqxMxP3X).

### Step 4. iPhone: trust the app, turn on Developer Mode, first refresh
These are SideStore's steps for iOS 18 and later, which includes iOS 26.
1. Open **Settings › General › VPN & Device Management**. Under "Developer App", tap the entry named after the Apple Account, tap **Trust "…"**, then **Allow & Restart**, and enter the passcode.
2. Open **Settings › Privacy & Security**, scroll to the bottom and turn on **Developer Mode**. The iPhone restarts. If it then asks whether to turn on Developer Mode, tap **Turn On** and enter the passcode.
3. Open LocalDevVPN and tap **Connect**.
4. Open SideStore and sign in with the same Apple Account you used in iloader.
5. Go to **My Apps** and tap the **7 DAYS** counter next to SideStore. This first refresh finishes the setup. If SideStore asks to revoke or create a signing certificate, tap **Yes** or **Refresh Now**.
6. If it works, SideStore closes to the Home Screen with a notification and is usable again after a few seconds.

Do this first refresh before installing anything else in SideStore. The official FAQ says that apps installed before it can disappear from SideStore's list.

## Part 2: Download the app file (.ipa)
CI builds a new unsigned app file after every push that passes its tests and keeps each one for 30 days. You can download it on the iPhone or on the Mac.

**On the iPhone (simplest):**
1. In Safari, sign in at https://github.com.
2. Open the list of passing builds:
   - after PR #1 is merged: https://github.com/cfeiwong85/DashcamViewer/actions/workflows/ci.yml?query=branch%3Amain+is%3Asuccess
   - until then: https://github.com/cfeiwong85/DashcamViewer/actions/workflows/ci.yml?query=branch%3Aclaude%2Fgreat-dijkstra-tarcht+is%3Asuccess
3. Tap the newest run (green check).
4. Scroll down to **Artifacts**, tap **DashcamViewer-unsigned-ipa** and confirm the download.
5. Open the **Files** app › **Downloads** and tap `DashcamViewer-unsigned-ipa.zip`. Files unzips it into a folder that contains `DashcamViewer.ipa`.

**On the Mac:** download the artifact the same way in Safari, then double-click the `.zip` if Safari hasn't unzipped it already. AirDrop `DashcamViewer.ipa` to the iPhone and save it in Files.

## Part 3: Install Dashcam Viewer
1. Make sure the iPhone is on home Wi-Fi and LocalDevVPN shows **Connected**.
2. Open SideStore › **My Apps** and tap **+** at the top left. Newer SideStore builds then ask how to install. If yours does, tap **Choose from Files**.
3. Pick `DashcamViewer.ipa` (in **Downloads**, inside the unzipped folder).
4. Wait for the installation to finish. Dashcam Viewer appears in My Apps with its own day counter, and on the Home Screen.

Install the app from SideStore on the iPhone, not with iloader's **Import IPA** button on the Mac. SideStore only lists and refreshes apps that it installed itself.

### First launch
1. Join the dashcam's Wi-Fi in **Settings › Wi-Fi**. If iOS says the network has no internet, stay connected.
2. Open Dashcam Viewer. When iOS asks to let it find and connect to devices on the local network, tap **Allow**. If you tapped Don't Allow, turn it on later in **Settings › Privacy & Security › Local Network › Dashcam Viewer**.

The in-car tests come next, in `docs/CAR_TEST_CHECKLIST.md`.

## Part 4: Keeping it working
With a free Apple Account, every sideloaded app stops opening 7 days after its last refresh. SideStore refreshes its apps in the background when it can. A refresh needs LocalDevVPN connected and Wi-Fi with internet. The dashcam's Wi-Fi has no internet, so refreshes only work at home or on another normal Wi-Fi network.

- **Routine:** every few days, at home, connect LocalDevVPN, open SideStore › **My Apps** and tap **Refresh All**. Don't rely on background refresh alone.
- **If Dashcam Viewer has expired:** it won't open, but nothing is lost. Refresh it in SideStore (tap its day counter or **Refresh All**).
- **If SideStore itself has expired:** it can't refresh anything, so reinstall it from the Mac with iloader (Part 1, step 3), then refresh. Don't delete Dashcam Viewer first. If it's missing from My Apps afterwards, install the `.ipa` again (Part 3); its data stays.

## Part 5: Installing a newer build
When a fix is pushed, download the newest artifact (Part 2) and install it the same way (Part 3). The new build replaces the old one and keeps its settings and the clips in the **Saved** tab.

Never delete Dashcam Viewer to reinstall it. Deleting the app also deletes the clips in its Saved tab that you haven't exported to Photos. (The Saved clips are also visible in Files › On My iPhone › Dashcam Viewer.)

The build number (the CI run number) is on the first line of the diagnostics log that the app's Diagnostics tab shares.

## Limits of a free Apple Account
- At most **3 sideloaded apps** at a time, SideStore included. SideStore plus Dashcam Viewer uses 2. LocalDevVPN comes from the App Store and doesn't count.
- At most **10 new App IDs in 7 days**. Dashcam Viewer has no app extensions, so it needs one App ID. Installing a newer build of the same app reuses it.

## When something goes wrong
**SideStore can't connect ("No Wi-Fi or VPN connection" or "AFC Connection Failure"), for example after an iOS update.** Try these in order, and tap refresh again after each one:
1. Make sure LocalDevVPN and Wi-Fi are both connected (not mobile data, not the dashcam's Wi-Fi).
2. Turn off any DNS blocker or content-blocker VPN.
3. Try again; tapping the button a few times sometimes helps.
4. Close and reopen SideStore, then disconnect and reconnect LocalDevVPN.
5. Replace the pairing file from the Mac (iOS updates, resets and sometimes nothing at all make the old one stop working):
   1. Open iloader. If it offers an update, click **Yes**.
   2. Connect the iPhone by USB.
   3. Click **Delete Stored Pairing**.
   4. Click the iPhone in the device list and tap **Trust** on the iPhone.
   5. Click **Manage Pairing File**, then **Place** next to SideStore. "Pairing file placed successfully!" appears in green.
   6. If SideStore still shows an error, restart the iPhone and the Mac and repeat these steps.

**SideStore won't sign in.** In SideStore's Settings, choose a different **Anisette server** and try again; you may need to try several. If the verification code never arrives: open **Settings**, tap your name, turn off Wi-Fi and mobile data, tap **Sign-In & Security › Get Verification Code**, and type that code into SideStore.

**SideStore won't open.** Reinstall it from the Mac with iloader (Part 1, step 3).

**Dashcam Viewer can't reach the camera while LocalDevVPN is connected.** This hasn't been tested yet. The app sends camera traffic over Wi-Fi only, so the VPN shouldn't get in the way. If it does, disconnect LocalDevVPN while you use the camera, reconnect it at home before refreshing, and note it in the car-test log.

**Still stuck.** SideStore's Discord: https://dis.sidestore.io/ (iloader problems: the idevice Discord linked in step 3).

## Sources (checked 2026-11-22)
- SideStore prerequisites: https://docs.sidestore.io/docs/installation/prerequisites
- SideStore install: https://docs.sidestore.io/docs/installation/install
- Replacing the pairing file: https://docs.sidestore.io/docs/advanced/pairing-file
- FAQ (app limits, first refresh): https://docs.sidestore.io/docs/faq
- Common issues and troubleshooting: https://docs.sidestore.io/docs/troubleshooting/common-issues and https://docs.sidestore.io/docs/troubleshooting
- iloader: https://github.com/nab138/iloader (source 2.4.2: the "SideStore (Stable)" option downloads SideStore's latest GitHub release; the "Import IPA", "Delete Stored Pairing" and "Manage Pairing File" buttons)
- SideStore source, tag 0.7.0-alpha: the **+** button in My Apps opens a Files picker for `.ipa` files; newer builds ask first, with a "Choose from Files" option
