# Aggrey DayFlow: get it on your phone (Windows 11)

## What you need (one time)
1. **Android Studio** (free): https://developer.android.com/studio . Install with the default options.
2. A USB cable and your Android phone (only for the optional "Way B" below).

## Step 1: Open the project in Android Studio (first time only)
1. Unzip `AggreyDayFlow` somewhere simple, for example `C:\Projects\AggreyDayFlow`.
2. Open Android Studio, choose **Open**, and pick the **AggreyDayFlow** folder (the one that contains the `app` folder and `build.gradle.kts`).
3. Wait for the bar at the bottom to finish ("Gradle sync"). The first time this downloads files, so keep the internet on.
4. If Android Studio shows a message to install something, accept it. These are the ones it may ask for:
   - **Android SDK Platform 34**
   - **Android SDK Build-Tools** (any 34.x)
   - **Android SDK Platform-Tools**
   You can also check them under **Tools > SDK Manager > SDK Platforms / SDK Tools**.

## Step 2: Build the APK (Way A, easiest)
1. Open the `AggreyDayFlow` folder in File Explorer.
2. Double-click **`BUILD_APK_WINDOWS.bat`**.
3. Wait. The first build takes 5 to 15 minutes. Later builds take about a minute.
4. At the end you will see **BUILD SUCCESSFUL** and the full path to the file. If you see **BUILD FAILED**, scroll up, copy the first line that says `error` or `What went wrong`, and send it to me.

The APK is:
`AggreyDayFlow\app\build\outputs\apk\debug\app-debug.apk`
The script opens that folder for you.

## Step 2 alternative (Way B): install straight from Android Studio with the cable
1. On the phone: **Settings > About phone**, tap **Build number** 7 times. Then **Settings > System > Developer options**, turn on **USB debugging**.
2. Plug the phone into the computer. Accept "Allow USB debugging" on the phone.
3. In Android Studio pick your phone at the top, then press the green **Run** triangle. The app installs and opens.

## Step 3: Put the APK on your phone (Way A)
Pick one:
- **Cable:** connect the phone, choose "File transfer" on the phone, and copy `app-debug.apk` into the phone's **Download** folder.
- **WhatsApp / Telegram / email / Google Drive:** send the file to yourself and download it on the phone.

## Step 4: Install it on the phone
1. On the phone open **Files** (or Downloads) and tap `app-debug.apk`.
2. If Android says it is blocked, tap **Settings** and allow **Install unknown apps** for the app you opened the file from (Files, Chrome, WhatsApp...). Go back and tap the file again.
3. If **Play Protect** warns about an unknown app, tap **More details > Install anyway**. This is normal for an app that is not from the Play Store.
4. Tap **Install**, then **Open**.

## First launch: make the reminders reliable
- Allow **notifications** when asked. If you tapped "Don't allow", use the button on the app's home screen to open the setting.
- If the home screen shows **Allow exact alarms**, tap it and switch on "Alarms and reminders". Without it, reminders can arrive a few minutes late.
- Some phones (Xiaomi, Tecno, Infinix, Oppo, Vivo, Samsung) stop apps in the background to save battery. If a reminder does not ring, set the app's battery usage to **Unrestricted** and turn on **Autostart** if your phone has it.
- If the home screen shows **Allow full-screen alerts**, tap it. This lets the big alarm screen open over a locked phone.
- The alarm uses your phone's **alarm volume**. Turn it up in Settings > Sound.
- Test it: add a reminder for 2 minutes from now, then close the app. A notification should appear.

## Sharing with a friend
Send them the same `app-debug.apk` file and ask them to follow Step 4. Their data stays on their own phone. Nothing is uploaded anywhere.

Important: this is a **debug** build, signed with a test key from *your* computer. If your friend later installs a build made on a different computer, Android will refuse to update it. They must uninstall the old copy first, which also erases their reminders.

## Uninstall
Long-press the app icon > **App info** > **Uninstall**. This deletes its reminders. Or: **Settings > Apps > Aggrey DayFlow > Uninstall**.

## What works and what does not (be honest with your friends)
Works (written, see docs/STATUS.md for what has been tested): create, edit and delete reminders, one-time and every-day repeats, a timetable for any day, tick reminders done, saved on the phone, notifications and alarms, re-arming after a restart.
Not built yet: search, backup and restore, widgets, first-run guide.
