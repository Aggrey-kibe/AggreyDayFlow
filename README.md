# Aggrey DayFlow

Offline-first reminder and daily planning app for Android. No account, no backend, no tracking.

**Status:** Active development. Seven color themes (light, dark or follow the phone) with a preview gallery, a dashboard with progress and countdown, and an alarm screen that matches the chosen theme. Reminders with messages, one-time/daily/weekly repeats, a daily timetable, local storage, alarm-style ringing with a full-screen alert (Dismiss / Snooze / Done), per-reminder sound and vibration options, a Settings screen and boot re-arming are implemented. See `docs/STATUS.md` for exactly what has been verified on a phone.
Parts have been tested and parts have not: see `docs/STATUS.md` for the exact list. To get an APK, see `README_APK_INSTALL.md`.

## Identity
- Owner: Aggrey Kibe Kwamboka. Developer: Aggrey Kibe. Country: Kenya.
- Package ID: `com.aggreykibe.dayflow` (permanent once published on Google Play)
- The name "Aggrey DayFlow" is provisional until trademark and store availability checks are done.

## Requirements
- Android Studio (current stable) with its bundled JDK (17 or newer), Android SDK Platform 34.
- minSdk 26, target/compile SDK 34. Gradle 8.9 (via the included wrapper), AGP 8.5.2, Kotlin 2.0.20.

## Build and test
Windows: double-click `BUILD_APK_WINDOWS.bat`. Or from a terminal:
```
gradlew.bat assembleDebug
gradlew.bat testDebugUnitTest
```
Mac/Linux: `./gradlew assembleDebug`.

## Layout
- `domain/`: pure Kotlin logic (reminder model, next-trigger rules, progress, storage format). No Android imports, unit-tested.
- `data/`: local storage. `scheduling/`: alarms and the boot/alarm receivers. `notifications/`: channel and notification. `permissions/`: notification and exact-alarm checks.
- `ui/`: view model, navigation shell, dashboard, editor, theme.

## Download
Signed release APKs are published on the [Releases page](https://github.com/Aggrey-kibe/AggreyDayFlow/releases) once a version has been released; see `docs/RELEASING.md` for how a release is made and verified. Installing needs "Install unknown apps" to be allowed for the app you open the file from, because the app is shared directly and not through the Play Store.

## Alarm sounds
32 bundled, offline sounds grouped by mood (Gentle wake-up, Trending, Energetic, Strong wake-up, Classic), each with a preview button, plus the phone's own alarm sound. All are original, synthesized by the scripts in `tools/sound-generator` (no samples or third-party audio). Choosing your own audio file is planned, not built yet.

## Alarm reliability
Ringing alarms use Android's alarm-clock API, a foreground service with a wake lock and audio focus, and a loud full-screen notification. Alarms and snoozes are re-armed after a restart (even before the PIN is entered), an app update, and time or time-zone changes. Limits that cannot be fixed by any app: a phone that is switched off cannot ring, and **Force stop** in Android settings cancels scheduled alarms until the app is opened again. See `docs/ALARM_TESTING.md` for the phone test sheet; results are recorded in `docs/STATUS.md` only after they have been run.

## Data and privacy
Reminders are stored in the app's private device-protected storage (`allowBackup` is off), so alarms can be restored after a restart before the PIN is entered. Other apps cannot read them. Nothing is sent anywhere. The app requests no internet permission.

## Signing
Signing keys and `keystore.properties` are git-ignored. Never commit them. Release signing is not set up yet.

## Licenses
Dependencies: AndroidX, Jetpack Compose, Material 3 (Apache 2.0); JUnit (EPL 1.0). Full notices will be compiled before any public release.
The launcher icon is original artwork. The notification icon is the Material "check circle" glyph (Apache 2.0).
