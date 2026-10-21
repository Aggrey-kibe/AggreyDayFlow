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
- (Earlier count, superseded by the Stage A line below.) 39 JUnit tests passed (`GreetingTest` 5, `ReminderLogicTest` 27, `ReminderCodecTest` 7), compiled with Kotlin 2.0.20 and run on JUnit 4.13.2. They cover next-trigger rules including weekly repeat, sorting, completion, progress counts, the storage round trip, loading old saved data, and safe handling of invalid stored values.
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
- Not yet built from the master plan: backup/restore, search, first-run setup, and a redesign of the editor and timetable screens (planned for later stages).

## Stage A (theme system and home dashboard): written, build result pending
- **Seven themes** (Ocean Calm, Sunrise, Lavender Dream, Forest Focus, Midnight Premium, Rose Glow, Minimal Monochrome), each with a light and a dark version, plus Light / Dark / Follow phone mode. Choice is saved on the device.
- **Theme gallery** in Settings: tap a theme to preview it in a sample home screen, then press Apply theme. Nothing changes in the app until Apply.
- **Home dashboard**: greeting and date, a hero "Next reminder" card with a countdown ("in 2 h 15 min"), an animated daily progress bar ("3 of 5 done"), and a timetable whose rows show a category color dot. The progress animation is switched off when the phone's animations are disabled.
- **Alarm screen** now uses the chosen theme's colors; the message sits on a darker panel so white text stays readable.
- Status-bar and navigation-bar icons follow the app's chosen mode, not only the phone's.
- Colors are defined once in `domain/Palettes.kt` (no Compose imports), generated from a table that was checked for WCAG AA contrast.

## Sound library (modern tones): written, build result and listening test pending
- **32 bundled, offline alarm sounds** in five groups: Gentle wake-up (9), Trending (7), Energetic (6), Strong wake-up (6) and Classic (the 4 earlier tones). Plus the phone's own alarm sound.
- Style: glassy FM bells, lo-fi electric piano, bubble pops, synthwave arps, chiptune, trap hats and 808s, supersaw risers, "power-up" sweeps.
- **Picker**: Settings and the reminder editor show the chosen sound on a button; tapping opens a list grouped by mood with a **Play** button on every row, so you can listen before choosing. New reminders default to "Glass Drop". Reminders saved earlier keep the sound they had.
- **Every sound is original**: synthesized from math by `tools/sound-generator` (no samples, no third-party audio, no licences to track). Total size 1.25 MB as compressed OGG.
- If a chosen sound cannot be played, "Glass Drop" plays instead, so an alarm is never silent for that reason.
- Verified here: each sound was measured for clipping, silence, loudness, DC offset and clean endings (two real problems were found and fixed), and all 32 files decode. 54 unit tests pass, including a test that fails if a catalog entry has no audio file or an audio file is not in the catalog.
- **Not verified: how they actually sound.** The measurements cannot judge taste or pleasantness. That needs your ears: please listen to every sound and say which to keep, change or replace.
- Still planned from the sound brief: choose your own audio file from the phone, alarm-volume behavior setting.

## Phase 1 (alarm reliability): written, build and phone results pending
Problems found in the earlier alarm code, and what was changed:
1. Alarms depended on a permission that sideloaded apps do not get by default on Android 14+, falling back to alarms Android can delay for minutes. **Ringing alarms now use Android's alarm-clock API** (no special permission, not delayed by battery saving).
2. No wake lock. **A short wake lock covers the start-up, and a timed one is held while ringing.** It always has a timeout, so it cannot be held forever.
3. No audio focus. **The alarm now asks other apps (music, video) to pause.**
4. If Android refused to start the ringing service, the person got only a plain notification. **The fallback is now a loud notification that opens the alarm screen, which starts the ring itself.** If the chosen sound cannot be played, a bundled sound is used.
5. Alarms were only restored after the first unlock following a restart. **Reminders now live in device-protected storage and the boot receiver is direct-boot aware**, so an overnight restart no longer silences the morning alarm. Existing data is moved across automatically the first time the new version runs unlocked.
6. A snooze was lost on restart. **Snoozes are now saved and re-armed**; only one snooze per reminder can exist.
7. Manual clock changes were ignored. **They now re-arm the alarms.**
8. Two alarms due at once: the second silently replaced the first. **The first now stays as a notification.**
Also: the exact-alarm banner is shown only if quiet-notification reminders exist, because ringing alarms no longer need it.
Privacy note: because reminders are readable before the first unlock after a restart, they are not protected by the lock-screen PIN at that moment. They stay private to this app.
Verified here: the whole engine compiles against Android API 34; 49 unit tests pass (including snooze recovery). **Not verified: every row of `docs/ALARM_TESTING.md`. These need your phone.**

## Verified for Stage A (sandbox)
- 48 JUnit tests pass. New `PaletteContrastTest` fails the build if any text pair in any of the 14 theme/mode combinations drops below 4.5:1, if outlines or category dots fall below 3:1, or if the alarm screen's white text or action button becomes hard to read. A deliberate bad color was injected once to prove the test catches it.
- Framework layer compiles against Android API 34. Compose screens have no syntax errors; they are compiled by the GitHub build.
- Contrast numbers are calculated, not eyeballed. They do not replace looking at the screens on a real phone: check every theme in light and dark.

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
