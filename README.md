<div align="center">

# ET Toolbox

**One app for your rooted Android: game boost, bloatware cleaner, Magisk modules, CPU, spoof and troll notifications**

[![Release](https://img.shields.io/github/v/release/EzequielDevTeam/ET-Toolbox?style=for-the-badge&logo=git&label=Release)](https://github.com/EzequielDevTeam/ET-Toolbox/releases)
[![Build](https://img.shields.io/github/actions/workflow/status/EzequielDevTeam/ET-Toolbox/build.yml?branch=master&style=for-the-badge&label=Build)](https://github.com/EzequielDevTeam/ET-Toolbox/actions/workflows/build.yml)
[![Platform](https://img.shields.io/badge/Android_8.0%2B-26%2B-34A853?style=for-the-badge&logo=android&logoColor=white)](#requirements)
[![Root](https://img.shields.io/badge/Magisk-Required-black?style=for-the-badge&logo=magisk)](https://github.com/topjohnwu/Magisk)
[![Language](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](#build-it-yourself)
[![UI](https://img.shields.io/badge/Material_3-Dynamic_Colors-6750A4?style=for-the-badge&logo=materialdesign&logoColor=white)](#what-it-does)
[![Privacy](https://img.shields.io/badge/Telemetry-None-success?style=for-the-badge&logo=shield&logoColor=white)](#no-ads-no-tracking)
[![Ads](https://img.shields.io/badge/Ads-Zero-critical?style=for-the-badge&logo=shield&logoColor=white)](#no-ads-no-tracking)

</div>

---

## Why this exists

I got tired of having half a dozen apps installed to take care of my phone — one to clean RAM before gaming, another to disable factory junk, another to manage Magisk modules, and half of them full of ads. So I put everything in one small APK.

No ads. No tracking. Nothing leaves your phone.

---

## What it does

| Tab | Does | Needs root |
|---|---|:---:|
| **Boost** | Game mode / full RAM cleanup | Yes |
| **Troll** | Fake notifications with delay | No |
| **Cleaner** | Disable bloatware + clear cache | Yes |
| **Modules** | Magisk module manager | Yes |
| **Device** | Info, CPU governor, identity spoof | Partly |

Material 3 with your wallpaper colors on Android 12+.

### 1. Boost (Game Mode)

One button before gaming. It kills background processes (`am kill-all`), trims caches (`pm trim-caches`) and drops kernel disk caches (`drop_caches`).

Shows your free RAM before and after, so you see what it freed. On 3–4 GB phones that's usually hundreds of MB back instantly.

### 2. Troll (Notifications)

Fake high-priority notifications for pranks. Comes with presets:

- Fake critical system explosion warning
- Battery at 1% dropping fast
- Huge update downloading on mobile data
- "Your mom is arriving"

And more. You can write your own title and body, plus a **delay in seconds**: trigger it hidden and the notification pops up minutes later when everyone forgot.

Harmless, changes nothing on the system. Works without root — on Android 13+ it just asks for notification permission.

### 3. Cleaner (Bloatware & Cache)

Pre-loaded list of the usual factory junk running in background for no reason: carrier apps, duplicate assistants, extra cloud services, forced browsers.

For each one you see what it does, if it's installed, if it's on or off — and a button to turn it off or back on.

It uses `pm disable-user`, so it's **reversible**. Nothing gets uninstalled for real. Broke something? Turn it back on. Plus a cache button that frees space and tells you how much.

### 4. Modules (Magisk Manager)

Your Magisk modules without opening Magisk:

- Lists everything in `/data/adb/modules` with name and version
- Shows enabled, disabled or pending removal
- One tap to toggle, one tap to mark for removal on next reboot

Same files Magisk itself uses (`disable`, `remove`). Nothing deletes on the spot.

### 5. Device (Info, CPU & Identity)

Three cards:

**Device Info** — brand, model, Android version, patch level, SoC, cores, RAM, kernel, root status. Plain text.

**CPU Governor** — live frequency of every core + current governor. Switch all cores at once: `performance` for gaming, `powersave` for battery, `schedutil` for daily use. One tap.

**Identity Spoof** — temporarily change reported model/brand via `resetprop`, with automatic backup and a restore button. Good for testing apps or messing with whoever opens your settings. One click to undo.

---

## Requirements

- Android 8.0+
- Magisk root (everything except Troll needs it)
- arm64 (pretty much every phone today)

## Install

1. Grab the APK from [Releases](https://github.com/EzequielDevTeam/ET-Toolbox/releases)
2. Install it (allow unknown sources if asked)
3. Open and grant root on Magisk
4. Done

No Play Store version. There never will be.

---

## Android 17

Asked a lot, so straight answer: **not yet**. I only ship support after AOSP final + big ROMs stable + tested on real phones. Shipping "support" before that means guessing. It runs fine on 16 meanwhile — the update lands here first when it's real.

---

## Updates

I don't do daily updates and I won't promise them. When something breaks, the fix ships fast. Star the repo if it helps you — that's the fuel.

---

## Build it yourself

```bash
git clone https://github.com/EzequielDevTeam/ET-Toolbox.git
cd ET-Toolbox
./gradlew :app:assembleDebug
```

APK lands in `app/build/outputs/apk/debug/`. CI also builds every push (Actions artifacts).

## Credits

- UI: [Material Components Android](https://github.com/material-components/material-components-android)
- Root: [libsu](https://github.com/topjohnwu/libsu) by topjohnwu
- License in `LICENSE`. As-is: your phone, your call — bloatware toggle and CPU stuff is reversible, use your head anyway.

---

<div align="center">

**ET Toolbox** — EzequielDevTeam Technology

</div>
