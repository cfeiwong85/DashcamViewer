# Changelog

## 2026-11-22 (repo made public)
- At the user's request, changed `cfeiwong85/DashcamViewer` from private to public, keeping all its contents, including the vendor APK, PDFs and decompiled sources. The user was told about the third-party material first and chose to keep it.
- Updated `CLAUDE.md` and `PLAN.md` to say the repo is public.

## 2026-11-22 (cloud handoff)
- The user approved `PLAN.md`. Added `HANDOFF.md` (state, phase-by-phase instructions for the cloud session) and `.gitignore` (excludes jadx and the unzipped APK).
- Initialised git and pushed the project to the private GitHub repo `cfeiwong85/DashcamViewer`.
- Updated `CLAUDE.md` with the repo name and the jadx download note.

## 2026-11-22 (/init)
- Rewrote `CLAUDE.md`: added the standard header, commands (syntax check, xcodegen, jadx re-decompile), the protocol-first architecture overview, and a note that the scaffold is throwaway. Kept the Haiku-only delegation policy, now also covering Workflow `agent()` calls.

## 2026-11-22 (plan)
- Added `PLAN.md`: interview decisions (SideStore for the 7-day issue, GitHub Actions cloud build, delete and Wi-Fi password controls only, simulator plus car testing) and a 7-phase build/test plan. Awaiting approval.

## 2026-11-22 (protocol research)
- Identified the camera: Eurostyle T8 (TOMO-CSE Autotrim, Singapore), old app LogonT (`com.logon_e100`).
- Downloaded the vendor LogonT APK, user manual and spec sheet into `research/vendor/`, and jadx 1.5.1 into `research/tools/`.
- Decompiled the APK into `research/decompiled/`. 10 Haiku 5.5 agents extracted the protocol line by line (`research/extraction.md`); the orchestrator verified the core.
- Added `research/T8_PROTOCOL.md`: IP 192.168.100.1, TCP 7777 control / 7778 video / 7779 audio, framing, session flow, file list, download, settings.
- Updated the `CLAUDE.md` build notes: the T8 uses a custom TCP protocol; the RTSP/VLC scaffold is to be replaced.

## 2026-11-22
- Created project `DashcamViewer` (SwiftUI, iOS 17+): Live view, Recordings browser (Novatek XML + HTML index parsing), Settings with presets, PlayerView (AVPlayer; MobileVLCKit for RTSP).
- Added `project.yml` (XcodeGen) and `Podfile` (MobileVLCKit).
- Added Haiku 5.5-only subagents `.claude/agents/haiku-line-reviewer.md` and `haiku-checker.md`.
- Added `CLAUDE.md` delegation policy: subagents must be Haiku 5.5 only; orchestrator edits and verifies.
- Not built: full Xcode is not installed on this Mac (only Command Line Tools).
