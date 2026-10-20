package com.aggreykibe.dayflow.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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
        val reminder = ReminderStore(context).load().firstOrNull { it.id == id } ?: return
        val now = LocalDateTime.now()
        if (!reminder.isCompletedOn(now.toLocalDate())) {
            // A "quiet notification" reminder never rings. An alarm reminder rings, but Android may refuse to
            // start the ringing service (for example with only an inexact alarm); the person then still
            // gets a normal notification, never silence.
            if (reminder.ringStyle == RingStyle.NOTIFICATION || !AlarmService.tryStart(context, id)) {
                NotificationHelper.show(context, reminder)
            }
        }
        // One minute of margin so a clock that reads a hair early cannot re-arm the same moment.
        ReminderScheduler(context).schedule(reminder, now.plusMinutes(1))
    }
}
