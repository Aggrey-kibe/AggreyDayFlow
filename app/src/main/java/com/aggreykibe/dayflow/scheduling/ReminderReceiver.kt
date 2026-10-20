package com.aggreykibe.dayflow.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.aggreykibe.dayflow.alarm.AlarmService
import com.aggreykibe.dayflow.data.ReminderStore
import com.aggreykibe.dayflow.domain.RingStyle
import com.aggreykibe.dayflow.notifications.NotificationHelper
import java.time.LocalDateTime

/** Fires when a reminder's alarm (or a snooze) goes off: rings the alarm, then arms the next occurrence. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(ReminderScheduler.EXTRA_REMINDER_ID, -1L)
        if (id < 0) return

        // Keep the phone awake for the few seconds it takes to start the ringing service.
        context.getSystemService(PowerManager::class.java)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DayFlow:alarmReceiver")
            .acquire(WAKE_MILLIS)

        val scheduler = ReminderScheduler(context)
        if (intent.data?.host == ReminderScheduler.SNOOZE_PATH) scheduler.forgetSnooze(id)

        val reminder = ReminderStore(context).load().firstOrNull { it.id == id } ?: return
        val now = LocalDateTime.now()
        if (!reminder.isCompletedOn(now.toLocalDate())) {
            if (reminder.ringStyle == RingStyle.NOTIFICATION) {
                NotificationHelper.show(context, reminder, openAlarmScreen = false)
            } else if (!AlarmService.tryStart(context, id)) {
                // Android refused to start the ringing service. Post a loud notification that opens the alarm
                // screen (which starts the ring itself, because an open screen is always allowed to).
                NotificationHelper.show(context, reminder, openAlarmScreen = true)
            }
        }
        // One minute of margin so a clock that reads a hair early cannot re-arm the same moment.
        scheduler.schedule(reminder, now.plusMinutes(1))
    }

    private companion object {
        const val WAKE_MILLIS = 10_000L
    }
}
