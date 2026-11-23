t8sim: the T8 dashcam simulator, ready to run on a Mac
=======================================================

This folder holds a ready-made t8sim program. It runs on macOS 13 or newer,
on Intel and Apple silicon Macs. It does not need Swift, Xcode or the
Command Line Tools.

First time only (in Terminal):

  cd ~/Downloads/t8sim-macos
  chmod +x t8sim
  xattr -c t8sim

"chmod +x" makes the file runnable again (the download loses that setting).
"xattr -c" removes the "downloaded from the internet" mark, so macOS does not
block the program.

Start the simulator:

  cd ~/Downloads/t8sim-macos
  ./t8sim

It is ready when it prints:
  T8 simulator ready: control 7777, video 7778, audio 7779. Press Ctrl-C to stop.

If macOS asks whether t8sim may accept incoming network connections, click
Allow. Press Ctrl-C to stop it.

Every option works the same as with "swift run t8sim", for example:
  ./t8sim --already-connected
  ./t8sim --help

If the folder has a different name in Downloads, run "ls ~/Downloads" and use
the name it shows.
