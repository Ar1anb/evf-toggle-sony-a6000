# EVF Toggle

A small app for the Sony a6000 that lets the C1 button switch between the viewfinder and the rear screen.

The a6300 can put "Finder/Monitor" on a custom button. The a6000 can't; the option isn't in its firmware. The
setting behind it is, though. This app watches for a C1 press and flips that setting for you.

## What it does

You open the app once after switching the camera on. It shows BUTTON READY for a couple of seconds and closes.
After that, every C1 press moves the picture to the other display. It keeps working until you turn the camera off.
Next time you turn it on, open the app again.

## Install

You need the camera, a USB cable, a charged battery, and either `pmca-gui` or `pmca-console` from
[Sony-PMCA-RE](https://github.com/ma1co/Sony-PMCA-RE).

1. Download `EVFToggle.apk` (from the Actions tab of this repo, under Artifacts, or build it yourself).
2. On the camera, set MENU > Setup > USB Connection to Mass Storage and plug it in.
3. Install it. With pmca-gui, use the Install tab and pick the apk. With pmca-console:

   ```
   pmca-console install -f EVFToggle.apk
   ```

4. Unplug. The app is under MENU > Application > Application List.

If an older build is already on the camera, uninstall it first (Application List > Application Management >
Uninstall). Builds from GitHub are signed with a new key each time, and the camera won't install over a different
key.

## Using it

Open EVF Toggle from the Application List. You'll see one of these:

- BUTTON READY: the watcher started. Press C1.
- ALREADY ON: you already opened it since power-on. C1 works; nothing changed.
- COULD NOT START: something failed. The line underneath says what. It stays up until you press a button.

The app sets C1 to "Deactivate Monitor" for you, since that's the function it listens to. If you reassign C1 in
the menu, the switching stops until you open the app again.

## Limits

- You have to open the app after every power-on. Camera apps can't start by themselves at boot.
- It switches between Viewfinder and Monitor only. If FINDER/MONITOR was on Auto, the first press takes you off
  Auto. To get Auto back, set it in MENU > Setup > FINDER/MONITOR.
- It checks C1 four times a second, so there's a short delay (up to a quarter second) after a press.
- Only tested on an a6000 with firmware 3.21. Other bodies use different setting IDs and probably won't work.

## Status

It builds and installs. The part I haven't confirmed yet is whether the watcher survives the app closing. If C1
does nothing once you're back in shooting mode, that's probably why. Open an issue and say what you saw.

## How it works

The app uses three settings from the camera's settings store:

| ID | What it is | Values |
| --- | --- | --- |
| `0x01070c71` | C1 function | `0x34` = Deactivate Monitor |
| `0x01070b09` | Monitor deactivated | `01` = off, `00` = on. Flips on every C1 press |
| `0x010708e0` | Active display | `01` = Viewfinder, `02` = Monitor. Takes effect at once |

When you open the app, it sets C1 to Deactivate Monitor and starts a small native process. That process reads
`0x01070b09` every 250 ms. When it changes, the process writes `0x010708e0`: Viewfinder if the monitor was just
deactivated, Monitor if it came back on. The process detaches from the app so it can outlive it, and it holds an
abstract socket as a lock so a second launch doesn't start a second copy.

This started as a shell script (`evf1.sh`) run over telnet with `bk.elf`. The IDs were found by dumping
`/setting/Backup.bin` before and after changing things in the menu, and by scanning `0x01070000` to `0x01070dff`
live on the camera before and after a button press.

A side note from that digging: the a6000 firmware still has the full ND filter menu, even though the camera has no
ND filter. Setting C1 to `0x40` opens it.

## Building

GitHub can build it for you. Push the repo (or upload the files through the website) and the workflow in
`.github/workflows/build.yml` builds the apk. Download it from the run's Artifacts section.

To build locally you need JDK 17, the Android SDK (build-tools 30.0.3, platform 28), NDK r16b and git. Newer NDKs
can't target this camera.

```
ANDROID_NDK=/path/to/android-ndk-r16b ./build.sh     # Linux, WSL, macOS
build.cmd                                            # Windows
```

The first build clones [OpenMemories-Platform](https://github.com/ma1co/OpenMemories-Platform) into `jni/platform`.
`./tools/test.sh` runs the unit tests with a plain JDK.

The apk is signed with v1 only. The camera rejects v2 and v3 signatures.

## Uninstall

Application List > Application Management > Uninstall > EVF Toggle. To put C1 back, use MENU > Custom Key
Settings. To go back to automatic switching, MENU > Setup > FINDER/MONITOR > Auto.

## Credits

The project layout, build scripts and settings-store code come from
[Recipe Lab](https://github.com/voxivoid/recipe-lab-sony-pmca) by André Domingues (MIT, see
`NOTICE-RecipeLab-LICENSE.txt`). The native driver code is [OpenMemories-Platform](https://github.com/ma1co/OpenMemories-Platform)
by ma1co (MIT). Sideloading apps onto these cameras at all is possible because of ma1co's Sony-PMCA-RE.
