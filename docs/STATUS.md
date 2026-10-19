# Status (updated for the MVP)

## Implemented (code written)
- Create, view, edit, delete reminders (title, date, time). One-time and every-day repeats.
- Timetable for any day with previous/next day navigation; tick reminders done per day; missed/done labels.
- "Next reminder" card and daily progress counts.
- Local persistence (app-private storage, plain text format, tolerant of corrupt lines).
- Alarms: exact when allowed, inexact fallback otherwise, with an in-app notice. Daily reminders re-arm after each firing and skip days already ticked off.
- Notification channel, notification permission request (Android 13+) on first save, banner with a fix button if notifications are off.
- Re-arm after reboot, app update and time-zone change; also re-armed every time the app opens.
- Accessibility: 48dp touch targets, text in sp, headings and checkbox descriptions for screen readers, selectable radio rows, dark theme.

## Actually verified (in the build sandbox, 2026-10)
- 30 JUnit tests pass (`GreetingTest` 5, `ReminderLogicTest` 21, `ReminderCodecTest` 4), compiled with Kotlin 2.0.20 and run on JUnit 4.13.2. They cover next-trigger rules, sorting, completion, progress counts and the storage format round trip.
- The framework-only layer (storage, permissions, notifications, scheduler, both receivers) compiles with no errors against the real Android API 34 `android.jar` (with a stand-in for the generated `R` class).
- Compose files parse with no syntax errors; resource and string references were cross-checked (47 strings, all defined, argument counts match).
- Gradle wrapper files (`gradlew`, `gradlew.bat`, `gradle-wrapper.jar`) added from the official Gradle 8.9.0 tag. The old `gradle-wrapper.properties` was in the wrong folder; it now lives in `gradle/wrapper/`.

## NOT verified (needs Android Studio / a phone)
- Full Gradle build and APK: not run. Compose, AndroidX and AGP can only be downloaded from Google's servers, which the sandbox cannot reach.
- Compose UI type-checking: the dashboard, editor, view model and app shell are not compiled yet. Expect that a first build may show a small compile error; send the first error line.
- `BUILD_APK_WINDOWS.bat` has not been run on Windows.
- No device testing: notification delivery, exact-alarm behaviour, reboot re-arming, dark mode, largest font size, TalkBack, rotation.
- No lint run.

## Known limitations
- Reminders that were due while the phone was off are not replayed after reboot.
- No snooze, no weekly/custom repeats, no notification action buttons, no backup/export.
- Uses `SCHEDULE_EXACT_ALARM` (user-controlled on Android 12+). Chosen over `USE_EXACT_ALARM` because Google Play restricts the latter to alarm-clock/calendar apps.
- Editing the date of a one-time reminder keeps its old completion marks, which are harmless.
- Debug APK is signed with the local debug key; a release signing setup is a later step.

## Checks to run on a phone
1. Build succeeds; 30 unit tests pass in Android Studio.
2. Add a reminder 2 minutes ahead, close the app, confirm the notification.
3. Add a daily reminder, tick it done today, confirm no alert today and an alert tomorrow.
4. Restart the phone and confirm a future reminder still fires.
5. Light and dark mode, largest font size, rotate the screen while the editor is open.
