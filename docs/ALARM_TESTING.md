# Alarm test sheet (run on a real phone)

## Fastest test: the built-in 20-second test (version 1.0.1 and newer)
1. Open the app, tap the gear icon, scroll to **Alarm health check**.
2. Read the list. Anything marked **PROBLEM** is a likely reason an alarm can fail: fix it first (buttons are provided).
3. Press **Test alarm in 20 seconds**, then **press the power button to lock the phone** and wait. The test goes through the
   same path as a real reminder.
4. After it rings (or does not), unlock, open Settings again and press **Refresh**. **Recent alarm events** shows each stage
   that happened, in order:
   `SCHEDULED` (handed to Android) then `RECEIVER_FIRED` (Android woke the app) then `SERVICE_REQUESTED`, `SERVICE_STARTED`,
   `SOUND_STARTED`, `NOTIFICATION_POSTED` / `SCREEN_SHOWN`. **The first stage that is missing is where the alarm breaks.**
5. Press **Copy report** and paste it to the developer. The report also lists why Android last stopped the app. It contains no
   reminder titles or messages.

How to read the result
- `SCHEDULED` but never `RECEIVER_FIRED`: Android never woke the app. On Tecno, Infinix and itel phones this almost always
  means the phone's own battery manager stopped the app when the screen locked; follow the Tecno steps in the app.
- `RECEIVER_FIRED` but `SERVICE_REFUSED` or `FOREGROUND_FAILED`: Android woke the app but refused the ringing service. The
  app then posts a loud notification instead; the error text shows the reason.
- `SOUND_FAILED`: the sound could not play (the error text says why). `Alarm volume` showing Muted also explains silence.
- `NOTIFICATION_BLOCKED`: notifications are off for the app.
- Everything present but nothing seen: check `Full-screen alerts` and `Do Not Disturb` in the list.

Nothing here counts as "works" until you have ticked it on your own phone. Write **Pass**, **Fail** or **Not tried**,
and a note of what you saw. Send the table back and any **Fail** gets fixed first.

## Before you start
1. Install the latest APK (uninstall the old copy first if Android refuses to update it).
2. Open **Settings** in the app. Notifications, Exact alarms and Full-screen alerts should say **On** (exact alarms only
   matter for quiet reminders). Tap **Open battery settings** and set Aggrey DayFlow to **Unrestricted** if it is listed.
3. Turn the phone's **alarm volume** up (Settings > Sound).
4. Make a reminder named `Alarm test` with a short message, and keep **Ring like an alarm** selected.

## The tests

| # | Situation | What to do | What should happen | Result / notes |
|---|---|---|---|---|
| 1 | App open, screen on | Reminder 2 min ahead. Stay in the app. | Sound and vibration start; full alarm screen with title, message and time. | |
| 2 | App in background | Reminder 2 min ahead. Press Home. | Same as 1. | |
| 3 | Screen off and locked | Reminder 2 min ahead. Press the power button. | Screen wakes, alarm screen shows over the lock screen, sound plays. If Android only shows a banner, note it. | |
| 4 | Locked for a long time | Reminder 15 min ahead. Lock the phone and leave it untouched. | Rings on time even after the phone has gone into deep sleep. | |
| 5 | Removed from recent apps | Reminder 3 min ahead. Open recent apps and swipe the app away. | Still rings. (This is a normal swipe, not Force stop.) | |
| 6a | Restart, then unlock | Reminder 5 min ahead. Restart the phone, enter your PIN. | Rings on time. | |
| 6b | Restart, do NOT unlock | Reminder 5 min ahead. Restart the phone, do not touch it. | Rings on time even though the PIN was never entered. | |
| 7 | Do Not Disturb | Turn Do Not Disturb on. Reminder 2 min ahead. | Alarm still rings (Android normally allows alarms). Note if it did not. | |
| 8 | Battery saver | Turn Battery saver on. Reminder 2 min ahead. | Rings on time. | |
| 9 | Snooze | When it rings, press Snooze. | Silence now; rings again after the snooze length, **once**. | |
| 9b | Snooze + restart | Snooze for 10 min, restart the phone right away. | Still rings when the snooze ends. | |
| 10 | Dismiss | When it rings, press Dismiss. | Sound and vibration stop immediately; it does not ring again. | |
| 11 | Edit | Make a reminder 10 min ahead, edit its time to 3 min ahead. | Rings at the new time only, not at the old one. | |
| 12 | Delete | Make a reminder 3 min ahead, then delete it. | Does not ring. | |
| 13 | Daily repeat | Daily reminder 2 min ahead, dismiss it. | Rings again the next day, not again the same day. | |
| 14 | Two at once | Two reminders for the same minute. | One rings; the other stays as a notification (it is not lost). | |
| 15 | Ring runs out | Set Ring for to 30 sec and ignore the alarm. | Stops after 30 sec and leaves a notification. | |

## Two things that are NOT bugs
- **Force stop.** If you open Settings > Apps > Aggrey DayFlow and press **Force stop**, Android cancels the app's scheduled
  alarms and blocks it from running until you open the app yourself. This is Android's rule for every app. A normal swipe
  away from recent apps does not do this. After a force stop, open the app once and the alarms are armed again.
- **Phone switched completely off.** No ordinary app can turn a phone on or ring while it is powered off. Alarms resume
  after the phone is switched back on (as in test 6).

## Known limits
- Some makers (Xiaomi, Tecno, Infinix, Oppo, Vivo, Samsung) add their own battery rules on top of Android's. If a test
  fails only on your phone, check the maker's "Autostart" and "battery" screens for this app.
- A one-time reminder set weeks ahead across a daylight-saving change can shift by an hour until the app is opened or the
  phone restarts. Kenya has no daylight saving, so this will not affect you personally.
- Clock changes: the app re-arms alarms when the time or time zone changes.
