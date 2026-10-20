# Status (updated for the MVP)

## Implemented (code written)
- Create, view, edit, delete reminders (title, date, time). One-time and every-day repeats.
- Timetable for any day with previous/next day navigation; tick reminders done per day; missed/done labels.
- "Next reminder" card and daily progress counts.
- Local persistence (app-private storage, plain text format, tolerant of corrupt lines).
- Alarm mode: when a reminder is due, a foreground service loops the phone's alarm sound and vibrates, and a full-screen alert (time, daily/one-time label, large message) opens over the lock screen with **Done**, **Snooze 10 min** and **Stop**. The same three actions appear as notification buttons. If nobody answers within 2 minutes the sound stops and a normal notification stays. If Android refuses the ringing service, a normal notification is used instead.
- Alarms: exact when allowed, inexact fallback otherwise, with an in-app notice. Daily reminders re-arm after each firing and skip days already ticked off.
- Notification channel, notification permission request (Android 13+) on first save, banner with a fix button if notifications are off.
- Re-arm after reboot, app update and time-zone change; also re-armed every time the app opens.
- Accessibility: 48dp touch targets, text in sp, headings and checkbox descriptions for screen readers, selectable radio rows, dark theme.

## Actually verified (in the build sandbox, 2026-10)
- 39 JUnit tests pass (`GreetingTest` 5, `ReminderLogicTest` 27, `ReminderCodecTest` 7), compiled with Kotlin 2.0.20 and run on JUnit 4.13.2. They cover next-trigger rules including weekly repeat, sorting, completion, progress counts, the storage round trip, loading old saved data, and safe handling of invalid stored values.
- The framework-only layer (storage, permissions, notifications, scheduler, both receivers) compiles with no errors against the real Android API 34 `android.jar` (with a stand-in for the generated `R` class).
- Compose files parse with no syntax errors; resource and string references were cross-checked (47 strings, all defined, argument counts match).
- Gradle wrapper files (`gradlew`, `gradlew.bat`, `gradle-wrapper.jar`) added from the official Gradle 8.9.0 tag. The old `gradle-wrapper.properties` was in the wrong folder; it now lives in `gradle/wrapper/`.

## Confirmed on a real phone (by the owner)
- The project builds on GitHub Actions (runs #1 and #2, green) and the debug APK installs.
- Reminders work and a notification arrives at the scheduled time.
- Alarm mode (build #2) rings with an alarm sound.

## Stage 2 (this update): written, not yet confirmed on a phone
- Reminders now have an optional **message** (shown in the notification and on the alarm screen), a **category**, and **weekly** repeat.
- Per-reminder alarm options under "More options": ring like an alarm or quiet notification, sound (phone alarm sound plus four built-in offline sounds), vibration on/off, snooze length (5/10/15/30 min), ring duration (30 s to 5 min), with a sound sample button.
- **Settings screen**: defaults for all of the above, optional "start quietly and get louder", and plain-language status for notification, exact-alarm and full-screen permissions with fix buttons and a battery-settings shortcut.
- Alarm screen redesigned: live clock, scheduled time, message card, Dismiss / Snooze N min / Mark as done, glow animation that is switched off when the phone's animations are disabled.
- Data upgrade is backward compatible: reminders saved by earlier versions load with defaults (covered by a unit test).
- The four sounds are original synthesized tones generated for this project (no third-party audio).
- Not yet built from the master plan: themes and colors, per-category colors, backup/restore, search, first-run setup (planned for later stages).

## NOT verified (needs Android Studio / a phone)
- Alarm mode (sound, vibration, full-screen alert over the lock screen, Done/Snooze/Stop buttons) is new and has not been run on a phone yet. Its framework code compiles against Android API 34; the Compose alert screen is compiled by the GitHub build.
- Snooze alarms are not saved across a reboot (a snooze set before restarting the phone is lost).
- Full Gradle build and APK: not run. Compose, AndroidX and AGP can only be downloaded from Google's servers, which the sandbox cannot reach.
- Compose UI type-checking: the dashboard, editor, view model and app shell are not compiled yet. Expect that a first build may show a small compile error; send the first error line.
- `BUILD_APK_WINDOWS.bat` has not been run on Windows.
- No device testing: notification delivery, exact-alarm behaviour, reboot re-arming, dark mode, largest font size, TalkBack, rotation.
- No lint run.

## Known limitations
- Reminders that were due while the phone was off are not replayed after reboot.
- No custom repeat patterns, no backup/export. Snooze choices are 5, 10, 15 or 30 minutes.
- The alarm uses the phone's alarm volume. If that volume is at zero, only vibration and the screen alert will be noticed.
- On Android 14+ the user can block full-screen alerts; the app shows a button to re-enable them. While the phone is in use, Android shows a banner instead of taking over the screen (the sound still rings).
- Uses `SCHEDULE_EXACT_ALARM` (user-controlled on Android 12+). Chosen over `USE_EXACT_ALARM` because Google Play restricts the latter to alarm-clock/calendar apps.
- Editing the date of a one-time reminder keeps its old completion marks, which are harmless.
- Debug APK is signed with the local debug key; a release signing setup is a later step.

## Checks to run on a phone
1. Build succeeds; 30 unit tests pass in Android Studio.
2. Add a reminder 2 minutes ahead, close the app, confirm the notification.
3. Add a daily reminder, tick it done today, confirm no alert today and an alert tomorrow.
4. Restart the phone and confirm a future reminder still fires.
5. Light and dark mode, largest font size, rotate the screen while the editor is open.
