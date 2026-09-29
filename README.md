# EVF Toggle

A tiny app for the Sony a6000. **Open it, and the camera switches between the viewfinder and the rear screen.**
It shows which one is now on for a moment, then closes by itself. Press any button to close it sooner.

- Viewfinder → Monitor
- Monitor → Viewfinder
- Anything else (for example, if it was on Auto) → Viewfinder

It changes one camera setting (`0x010708e0`: 01 = Viewfinder, 02 = Monitor), the same one your shell script wrote.
Nothing keeps running after the app closes, so there is nothing to lose when you turn the camera off.

## Getting the .apk (easiest way: GitHub builds it for you)

You don't need to install any Android tools on your PC for this.

1. Make a free account at github.com if you don't have one.
2. Click **+** (top right) → **New repository**. Name it `evf-toggle`, set it to **Private**, click **Create repository**.
3. On the next page, click **uploading an existing file**.
4. Unzip `evf-toggle.zip` on your PC. Open the `evf-toggle` folder, select **everything inside it**, and drag it onto the
   GitHub page. Click **Commit changes**.
   - Make sure the `.github` folder is included:
     it contains the build instructions. If GitHub didn't take it, see "If the Actions tab is empty" below.
5. Click the **Actions** tab. A build called **build** starts on its own. Wait for the green tick (about 3–5 minutes).
6. Click the finished build, scroll down to **Artifacts**, and click **EVFToggle**. You get a zip; inside it is
   `EVFToggle.apk`. Put that file in `C:\Users\Arcti\OneDrive\Desktop\sonyupdate`.

**If the Actions tab is empty:** drag-and-drop sometimes skips folders that start with a dot. In the repository, click
**Add file → Create new file**, type the name `.github/workflows/build.yml`, paste the contents of that file from the
zip, and click **Commit changes**. The build then starts.

## Installing it on the camera

1. Camera: charged battery, connected by USB, USB Connection set to **Mass Storage** (MENU → Setup → USB Connection).
2. In PowerShell, in the `sonyupdate` folder:

   ```
   .\pmca-console-v0.18-win.exe install -f EVFToggle.apk
   ```

3. On the camera: **MENU → Application → Application List → EVF Toggle.**

Every build from GitHub is signed with a new throwaway key, so to install a newer build over an old one you must
**uninstall the old one first** (MENU → Application → Application List → Application Management → Delete).

## Using it

Open **EVF Toggle** from the Application List. It switches, shows e.g. `MONITOR — was VIEWFINDER`, and closes after
1.5 seconds. If it shows **COULD NOT SWITCH**, the message says why; press any button to close.

Tip: on the a6000 you can put the Application List on a custom key or the Fn menu, which makes this quicker.

## What needs testing on the camera

A green build only proves the app was put together correctly. It can't prove the camera behaves as expected.

1. Open the app on the rear screen → the picture should move to the viewfinder.
2. Open it again → back to the rear screen.
3. Leave it on one of them, **turn the camera off and on** → it should still be on that one.
4. With FINDER/MONITOR on **Auto** in the menu, open the app → it goes to Viewfinder. Check whether the eye sensor
   is now off (expected: the setting is no longer Auto) and tell me what you see.

## Undo

- Delete the app: MENU → Application → Application List → Application Management → Delete.
- Back to automatic switching: MENU → Setup → FINDER/MONITOR → Auto.

## For developers

- `src/com/artec/evftoggle/`: `MainActivity` (screen + toggle), `Display` (toggle logic, no Android imports, unit
  tested), `NativeBackup` (JNI binding).
- `jni/jni.cpp`: settings-store read/write/sync via OpenMemories-Platform's backup driver.
- `./tools/test.sh`: unit tests (plain JDK 17).
- `./build.sh` (Linux/CI) or `build.cmd` (Windows): needs JDK 17, Android SDK build-tools 30.0.3 + platform 28,
  NDK **r16b**, and git. The first build downloads OpenMemories-Platform into `jni/platform`.
- APKs are signed v1 only; the camera rejects v2/v3.

## Credits

Built on the structure of [Recipe Lab](https://github.com/voxivoid/recipe-lab-sony-pmca) (MIT, © André Domingues;
see `NOTICE-RecipeLab-LICENSE.txt`) and [OpenMemories-Platform](https://github.com/ma1co/OpenMemories-Platform)
(MIT, © ma1co).
