<p align="center">
  <img src="dist/icon-512.png" width="96" alt="EVF Toggle icon">
</p>

<h1 align="center">EVF Toggle</h1>

<p align="center">
  Switch between the viewfinder and the rear screen with one button on the <b>Sony A6000</b>.<br>
  <sub>
    <a href="../../actions">Download the app</a> ·
    <a href="#installing">Install it</a> ·
    <a href="#how-it-works">How it works</a>
  </sub>
</p>

---

**Contents**

- [What it is](#what-it-is)
- [Why I made this](#why-i-made-this)
- [Compatibility](#compatibility)
- [Installing](#installing)
- [Using it](#using-it)
- [What it changes](#what-it-changes)
- [Uninstalling](#uninstalling)
- [Troubleshooting](#troubleshooting)
- [How it works](#how-it-works)
- [For developers](#for-developers)
- [Credits](#credits)

---

## What it is

EVF Toggle is a small app that runs on the Sony A6000 itself. Open it once, and from
then on every press of **C1** moves the picture between the viewfinder and the rear screen.

The A6300 lets you put *Finder/Monitor* on a custom button. The A6000 doesn't; that option isn't in its menu. The
setting underneath it is there, though, and this app flips it for you whenever you press C1.

> **Honest note.** Camera apps can't start themselves, so you open EVF Toggle once. It takes about three seconds.
> In my testing it then keeps working through normal power off and on, but taking the battery out stops it. See
> [How long it lasts](#how-long-it-lasts). And it only knows Viewfinder and Monitor: if FINDER/MONITOR was on *Auto*, the first
> C1 press takes you off Auto.

## Why I made this

I hate switching displays on the A6000. Doing it by hand means `MENU → Setup → FINDER/MONITOR`, scrolling to the
right option and backing out again, every single time. That's fine once a day. It's not fine when I'm moving between
shooting at eye level and holding the camera low.

Auto mode is supposed to fix that, and I hate it too. The eye sensor sits right under the viewfinder, so my hand, my
shirt or the camera strap flips it to the viewfinder when I don't want it. The rear screen goes black in the middle of
framing a shot, and I end up pulling the camera away from my body to get it back.

So now C1 does it. One press, and it goes where I tell it to.

## Compatibility

Built for and tested on one camera. Other bodies keep their settings under different IDs, so don't expect it to work
on them.

✅ tested · ❔ untested · ❌ won't work

| camera | model code | status | comment |
| --- | --- | --- | --- |
| **A6000** | ILCE-6000 | ✅ | firmware 3.21 |
| A6300 | ILCE-6300 | ❌ | not needed: Finder/Monitor is already a custom key option |
| A5000, A5100, A6500 | | ❔ | IDs probably differ |

## Installing

About ten minutes, once. You need the camera, a USB cable, a charged battery and a Windows, Mac or Linux computer.

**1. Get the installer tool.** It's called *Sony-PMCA-RE*, by ma1co. Download `pmca-gui` (or `pmca-console`) from
its [releases page](https://github.com/ma1co/Sony-PMCA-RE/releases). There's nothing to install; just run it.

**2. Get the app.** Open the [Actions tab](../../actions), click the newest green run, scroll to *Artifacts* and
download **EVFToggle**. You get a zip. `EVFToggle.apk` is inside it.

**3. Prepare the camera.** In the menu, `Setup → USB Connection → Mass Storage`. Turn the camera on and plug it in.
The screen should say *USB Mode*.

**4. Install.**

- *GUI:* open `pmca-gui` → **Install** tab → pick `EVFToggle.apk` → **Install**.
- *Terminal:*

  ```
  pmca-console install -f EVFToggle.apk
  ```

The camera flickers and switches modes by itself for about a minute. Leave it alone and watch the computer, which
prints `Task completed successfully` when it's done.

**5. Unplug.** The app is under `MENU → Application → Application List → EVF Toggle`.

Updating? Remove the old version first (see [Uninstalling](#uninstalling)). Every GitHub build is signed with a new
key, and the camera won't install over a different key.

## Using it

Open **EVF Toggle** from the Application List. What you see depends on whether it's already running:

| screen | meaning | buttons |
| --- | --- | --- |
| **ON** | it wasn't running, now it is. Press C1 | closes by itself, or press any button |
| **RUNNING** | it was already running | **centre** turns it off; any other button leaves it on |
| **OFF** | you just turned it off. C1 is back to plain Deactivate Monitor | closes by itself |
| **COULD NOT START** / **COULD NOT STOP** | something failed. The line underneath says what | any button closes |

So the app works like a switch: open it to turn switching on, open it again and press centre to turn it off.

Then press **C1**:

| press | result |
| --- | --- |
| first | picture moves to the viewfinder |
| second | back to the rear screen |

### How long it lasts

Longer than I expected. Turning the camera off with the power switch doesn't stop it: I've switched off and on many
times and C1 kept working, without opening the app again. My guess is that the A6000 doesn't fully shut down when you
flip the switch. It goes into a deep sleep and wakes up where it left off, watcher included.

**Taking the battery out does stop it.** That's a real shutdown, and the watcher is gone. After a battery swap, open
the app once more.

If you're not sure whether it's running, just open the app. **RUNNING** means it was, **ON** means it wasn't and now
is.

I've only seen this on my own camera, so treat it as "seems to", not a promise.

## What it changes

Two settings, both ones you could set by hand:

- **C1** is set to *Deactivate Monitor*. That's the function the app listens to, so if you give C1 another job in
  `Custom Key Settings`, the switching stops.
- **FINDER/MONITOR** is set to Viewfinder or Monitor on each press.

No firmware is touched and nothing is unlocked. The watcher lives in memory only. Turning it off in the app, or taking
the battery out, removes it completely.

## Uninstalling

**Removing the app:** turn it off first (open it, press centre on **RUNNING**). Then
`MENU → Application → Application Management → Manage and Remove → EVF Toggle`.

Removing the app doesn't change the two settings back. To do that:

- `MENU → Custom Key Settings → C1`, and pick whatever you had before.
- `MENU → Setup → FINDER/MONITOR → Auto`, to get the eye sensor back.

## Troubleshooting

| what you see | what to do |
| --- | --- |
| `No devices found` when installing | USB Connection must be *Mass Storage*; camera on and showing *USB Mode*; try another cable or port |
| Install refused | Old version still on the camera. Remove it first |
| **ON**, but C1 does nothing | Check C1 is still *Deactivate Monitor*. If it is, the watcher probably didn't survive the app closing. Please open an issue |
| C1 turns the screen black but the viewfinder stays off | Same as above: the watcher isn't running. Open the app again |
| **COULD NOT START** or **COULD NOT STOP** | Send a photo of the screen in an issue. To stop it anyway, take the battery out |
| Stopped working after a battery swap | Expected. Open the app once more |
| Stopped working after a normal power-off | Not what I've seen, but possible. Open the app again, and open an issue if it keeps happening |

## How it works

The app uses three settings from the camera's settings store:

| ID | what it is | values |
| --- | --- | --- |
| `0x01070c71` | C1 function | `0x34` = Deactivate Monitor |
| `0x01070b09` | monitor deactivated | `01` = off, `00` = on. Flips on every C1 press |
| `0x010708e0` | active display | `01` = Viewfinder, `02` = Monitor. Takes effect at once |

When you open the app, it sets C1 to *Deactivate Monitor* and starts a small native process. That process reads
`0x01070b09` four times a second. When it changes, the process writes `0x010708e0`: Viewfinder if the monitor was
just deactivated, Monitor if it came back on. The process detaches from the app so it can outlive it. It also holds
an abstract socket as a lock, so opening the app twice doesn't start two copies. The same socket is the off switch:
the app connects to it and sends `q`, and the watcher exits.

This began as a shell script (`evf1.sh`) run over telnet with `bk.elf`. The IDs were found by dumping
`/setting/Backup.bin` before and after changing things in the menu, and by scanning `0x01070000` to `0x01070dff`
live on the camera before and after a button press.

Something odd turned up along the way: the A6000 firmware still has the complete ND filter menu, although the camera
has no ND filter. Set C1 to `0x40` and it opens.

## For developers

| path | what's there |
| --- | --- |
| `src/com/artec/evftoggle/` | `MainActivity` (screen, starts the watcher), `Display` (IDs and rules, unit tested), `NativeBackup` (JNI) |
| `jni/jni.cpp` | settings-store read / write / sync, and the watcher |
| `tools/test.sh` | unit tests, plain JDK 17, no Android SDK |
| `.github/workflows/build.yml` | builds the apk on every push |

**Building on GitHub:** push, wait for the green tick, download from *Artifacts*.

**Building locally:** JDK 17, Android SDK (build-tools 30.0.3, platform 28), NDK **r16b** and git. Newer NDKs can't
target this camera.

```
ANDROID_NDK=/path/to/android-ndk-r16b ./build.sh     # Linux, WSL, macOS
build.cmd                                            # Windows
```

The first build clones [OpenMemories-Platform](https://github.com/ma1co/OpenMemories-Platform) into `jni/platform`.
The apk is signed v1 only, because the camera rejects v2 and v3 signatures.

## Credits

**Author:** [Ar1anb](https://github.com/Ar1anb). Found the setting IDs, wrote the original script, tested it on the
camera.

**[Recipe Lab](https://github.com/voxivoid/recipe-lab-sony-pmca)** by André Domingues. The project layout, build
scripts and settings-store code started as a copy of it, and this README follows its layout. MIT, see
`NOTICE-RecipeLab-LICENSE.txt`.

**[ma1co](https://github.com/ma1co)**, whose work is the reason apps can run on these cameras at all:

- [Sony-PMCA-RE](https://github.com/ma1co/Sony-PMCA-RE): the installer, and the updater shell used to dump the
  settings.
- [OpenMemories-Platform](https://github.com/ma1co/OpenMemories-Platform): the backup driver this app links against.
- [OpenMemories-Tweak](https://github.com/ma1co/OpenMemories-Tweak): telnet access, which is how the IDs were found.

Recipe Lab: MIT, © 2026 André Domingues. OpenMemories-Platform: MIT, © 2017 ma1co.
