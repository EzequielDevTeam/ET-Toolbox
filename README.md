<div align="center">

# ET Toolbox

**The ultimate toolbox for your rooted Android — everything in one app**

[![Release](https://img.shields.io/github/v/release/EzequielDevTeam/ET-Toolbox?style=for-the-badge&logo=git&label=Release)](https://github.com/EzequielDevTeam/ET-Toolbox/releases)
[![Build](https://img.shields.io/github/actions/workflow/status/EzequielDevTeam/ET-Toolbox/build.yml?branch=master&style=for-the-badge&label=Build)](https://github.com/EzequielDevTeam/ET-Toolbox/actions/workflows/build.yml)
[![Platform](https://img.shields.io/badge/Android_8.0%2B-26%2B-34A853?style=for-the-badge&logo=android&logoColor=white)](#requirements)
[![Root](https://img.shields.io/badge/Magisk-Required-black?style=for-the-badge&logo=magisk)](https://github.com/topjohnwu/Magisk)
[![Language](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](#project-structure)
[![UI](https://img.shields.io/badge/Material_3-Dynamic_Colors-6750A4?style=for-the-badge&logo=materialdesign&logoColor=white)](#what-this-app-does)
[![Privacy](https://img.shields.io/badge/Telemetry-None-success?style=for-the-badge&logo=shield&logoColor=white)](#why-we-do-this)
[![Ads](https://img.shields.io/badge/Ads-Zero-critical?style=for-the-badge&logo=shield&logoColor=white)](#why-we-do-this)

</div>

---

## Table of Contents

- [Introduction](#introduction)
- [What this app does](#what-this-app-does)
  - [Boost (Game Mode)](#1-boost-game-mode)
  - [Troll (Notifications)](#2-troll-notifications)
  - [Cleaner (Bloatware & Cache)](#3-cleaner-bloatware--cache)
  - [Modules (Magisk Manager)](#4-modules-magisk-manager)
  - [Device (Info, CPU & Identity)](#5-device-info-cpu--identity)
- [Requirements](#requirements)
- [Installation](#installation)
- [About Android 17 Support](#about-android-17-support)
- [Why we do this](#why-we-do-this)
- [Building yourself](#building-yourself)
- [Project structure](#project-structure)
- [Credits & license](#credits--license)

---

## Introduction

**ET Toolbox** is a complete toolbox for rooted Android devices, developed and maintained by **EzequielDevTeam Technology**.

The project was born from the real need to gather, in a single lightweight and straightforward app, several functions that would normally require half a dozen separate apps: speed up the system for games, clean factory junk, manage Magisk modules, control the CPU, and even prank friends with fake notifications.

> All of this in a small APK, **no ads**, **no telemetry**, **no data collection**, and absolutely no monetization.

---

## What this app does

| Tab | Function | Requires root |
|---|---|:---:|
| **Boost** | Game mode / full RAM cleanup | Yes |
| **Troll** | Custom notifications with delay | No |
| **Cleaner** | Disable bloatware + clear cache | Yes |
| **Modules** | Full Magisk module manager | Yes |
| **Device** | System info, CPU governor, identity spoof | Partial |

The interface uses **Material 3 with dynamic colors**: the app automatically adopts your wallpaper's color palette on Android 12+.

### 1. Boost (Game Mode)

The main tab for maximum performance before gaming. With a single button, the app executes a system-level memory cleanup sequence:

1. Kills all background processes (`am kill-all`);
2. Trims package manager caches (`pm trim-caches`);
3. Frees kernel physical memory by dropping disk caches (`drop_caches`).

Before applying anything, the tab shows real-time free RAM and total device RAM. After applying game mode, numbers update so you can see exactly how much memory was recovered.

> On devices with low RAM (3–4 GB), the difference is often **hundreds of megabytes freed instantly**.

### 2. Troll (Notifications)

Pure fun: sends high-priority fake notifications, perfect for pranks. The app comes with several ready-to-use presets:

- Critical system explosion warning;
- Battery at 1% dropping in seconds;
- Massive system update downloading on mobile data;
- "Your mom is arriving";
- And more.

You can also write custom title and body, plus set a **delay in seconds**: the person grabs the phone, you trigger it hidden, and the notification appears minutes later when they've forgotten.

> Harmless, doesn't change anything on the system, just for laughs. Works even without root — only asks for notification permission on Android 13+.

### 3. Cleaner (Bloatware & Cache)

One of the app's most useful features. The bloatware list comes pre-loaded with the most common factory packages that run in the background for no reason: carrier apps, redundant voice assistants, duplicate cloud services, manufacturer-imposed browsers, and so on.

For each item, ET Toolbox shows:

- Friendly name and description of what that package does;
- Whether it's installed on your device;
- Whether it's enabled or disabled;
- One-tap button to disable or re-enable.

Important: the app uses `pm disable-user`, which is **reversible** — nothing is truly uninstalled. If something breaks, just re-enable the package from the list. No real brick risk.

Plus, there's a dedicated button for general cache cleaning that frees space instantly and shows how much was freed.

### 4. Modules (Magisk Manager)

A complete manager for installed Magisk modules, right from the app UI:

- Lists all modules in `/data/adb/modules` with name and version read from `module.prop`;
- Shows each module's state (enabled, disabled, or pending removal);
- Toggle any module with one tap (via `disable` file);
- Mark modules for removal on next reboot (via `remove` file).

This uses the same mechanics Magisk uses internally, respecting the official module protocol. Nothing is deleted immediately — removal happens safely on reboot, exactly as Magisk expects.

### 5. Device (Info, CPU & Identity)

The technical tab, split into three cards:

**Device Info** — manufacturer, model, Android version, security patch level, SoC, core count, total RAM, kernel version, and root status. All plain text, straight to the point.

**CPU Governor** — real-time reading of all core frequencies and current governor. You can change the governor for **all cores at once**, choosing from those available in your kernel (`performance`, `powersave`, `schedutil`, etc.):

| Goal | Governor |
|---|---|
| Max performance / gaming | `performance` |
| Maximum battery saving | `powersave` |
| Smart balance (default) | `schedutil` |

One tap and done.

**Identity Spoof** — temporarily change the model and brand reported by the device using `resetprop`, with automatic backup of original values to `/data/local/tmp/ettoolbox_spoof_backup` and a dedicated restore button. Useful for testing app compatibility, fooling dumb hardware checks, or just trolling whoever checks your phone settings. All reversible with one click.

---

## Requirements

| Requirement | Detail |
|---|---|
| **Android** | 8.0 (API 26) or higher |
| **Root** | Magisk (required for boost, cleaner, modules, CPU, spoof) |
| **Architecture** | arm64-v8a (standard on virtually all modern devices) |

Troll tab functions work even without root; on Android 13+ they request notification permission.

## Installation

1. Download the latest APK from the [Releases](https://github.com/EzequielDevTeam/ET-Toolbox/releases) page of this repository;
2. Install normally (allow "install from unknown sources" if first time);
3. Open the app and grant root access to Magisk when prompted;
4. Done. All tabs will be functional.

> No Play Store version and never will be. This is an independent project distributed directly to users.

---

## About Android 17 Support

Many people have asked, so let's put this on record clearly and definitively:

> **Android 17 support will come, but only after all phones and custom ROMs are updated.**

This isn't laziness or lack of technical ability — it's a conscious engineering decision. Every new Android version changes internal behaviors, private APIs, SELinux restrictions, and security mechanisms. Releasing an "Android 17 ready" version now, before the real ecosystem absorbs the novelty, would deliver an app tested against a system almost no one has, full of code paths that will still change until final release and stable ROMs.

Our strategy is the same one that has always worked well for this project:

1. Wait for Android 17 to be finalized and published by AOSP;
2. Wait for major custom ROMs (LineageOS, crDroid, Pixel Experience and derivatives) to port and stabilize the base;
3. Verify in practice, on real devices running those ROMs, what actually changes;
4. Only then publish the version with official Android 17 support, tested for real, not guessing.

Meanwhile, ET Toolbox keeps working normally on current versions, including Android 16. When the time is right, the update drops here first.

---

## Why we do this

This project was made **purely out of goodwill**. No company behind it, no sponsors demanding returns, no subscriptions, no "premium" version, no ads, and no data collection whatsoever. Not a single byte leaves your device because of this app.

We maintain ET Toolbox because we enjoy what we do, because we believe users deserve honest tools on the device they paid for, and because the open-source Android scene has given us so much — this is our way of giving back.

On update cadence, to avoid wrong expectations:

> **We don't promise daily updates** — that would be a lie and no one can maintain that pace with quality. What we guarantee is **constant and responsive maintenance**: fixes ship fast when issues appear, improvements land continuously as we have time, and every version passes real-device testing before release. We'd rather release less often and release right than flood the history with broken builds.

If the project helped you, consider starring the repo. It's free, takes two seconds, and is the fuel that keeps us going.

---

## Building yourself

The project uses Gradle 8.7 + AGP 8.5 + Kotlin 1.9.24. To build locally:

```bash
git clone https://github.com/EzequielDevTeam/ET-Toolbox.git
cd ET-Toolbox
./gradlew :app:assembleDebug
```

The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

There's also a GitHub Actions workflow in this repo (`.github/workflows/build.yml`) that builds automatically on every push — the resulting APK is available as an artifact, so you can grab fresh builds without installing anything locally.

## Project structure

```
ET-Toolbox/
├── .github/workflows/build.yml      # CI: builds APK on every push
├── .github/workflows/release.yml    # CD: builds & signs APK+AAB on tag push
├── app/
│   ├── build.gradle.kts             # Module config (libsu, Material 3)
│   └── src/main/
│       ├── AndroidManifest.xml      # Permissions (notifications, vibrate)
│       └── java/technology/ezequieldevteam/ettoolbox/
│           ├── EtApp.kt             # Application class (dynamic colors + root)
│           ├── MainActivity.kt      # Host of 5 tabs + bottom nav
│           ├── data/
│           │   ├── BloatCatalog.kt  # Known bloatware catalog
│           │   └── TrollPresets.kt  # Troll notification presets
│           ├── root/
│           │   └── Su.kt            # Root execution layer (libsu)
│           └── ui/
│               ├── boost/           # Game mode / RAM cleanup
│               ├── clean/           # Bloatware + cache (list + adapter)
│               ├── device/          # Info, CPU & identity spoof
│               ├── modules/         # Magisk module manager
│               ├── troll/           # Custom notifications
│               ├── scripts/         # Custom shell scripts
│               ├── logs/            # Filtered logcat + export
│               ├── benchmark/       # CPU, memory, storage bench
│               ├── appmanager/      # App freeze/unfreeze, batch ops
│               ├── network/         # Ping, traceroute, DNS changer
│               └── settings/        # Auto-update, export/import
└── settings.gradle.kts              # Repositories (Google, Maven Central, JitPack)
```

## Credits & license

- UI built with [Material Components Android](https://github.com/material-components/material-components-android);
- Root execution via [libsu](https://github.com/topjohnwu/libsu), by topjohnwu, creator of Magisk;
- Inspired by the philosophy of projects like LSPosed and Shamiko, which proved that advanced user tools can be free, lightweight, and trustworthy.

This project is distributed "as is", without warranties. Use common sense — especially system functions. Disabling bloatware and tweaking CPU governors are safe and reversible operations, but remember: the device is yours and so are the decisions.

---

<div align="center">

**ET Toolbox** — EzequielDevTeam Technology

Handcrafted, with root and no rush. Constant updates, quality first.

</div>