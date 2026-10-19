package com.aggreykibe.dayflow.scheduling

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.aggreykibe.dayflow.domain.Reminder
import com.aggreykibe.dayflow.domain.nextTrigger
import com.aggreykibe.dayflow.permissions.PermissionHelper
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Keeps one pending alarm per reminder, always for that reminder's next trigger.
 * Daily reminders are re-armed by [ReminderReceiver] each time they fire.
 * Without exact-alarm permission it falls back to an inexact alarm, which Android may delay by minutes.
 */
class ReminderScheduler(context: Context) {
    private val context = context.applicationContext
    private val alarms = this.context.getSystemService(AlarmManager::class.java)

    fun schedule(reminder: Reminder, now: LocalDateTime = LocalDateTime.now()) {
        cancel(reminder.id)
        val at = nextTrigger(reminder, now) ?: return
        val millis = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val pending = pendingIntent(reminder.id, create = true) ?: return
        if (PermissionHelper.exactAlarmsAllowed(context)) {
            try {
                alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pending)
                return
            } catch (e: SecurityException) {
                // Permission revoked between the check and the call; use the inexact fallback below.
            }
        }
        alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pending)
    }

    fun cancel(id: Long) {
        val pending = pendingIntent(id, create = false) ?: return
        alarms.cancel(pending)
        pending.cancel()
    }

    fun rescheduleAll(reminders: List<Reminder>, now: LocalDateTime = LocalDateTime.now()) {
        reminders.forEach { schedule(it, now) }
    }

    private fun pendingIntent(id: Long, create: Boolean): PendingIntent? {
        val intent = Intent(context, ReminderReceiver::class.java)
            .setData(Uri.parse("dayflow://reminder/$id")) // makes the intent unique per reminder
            .putExtra(EXTRA_REMINDER_ID, id)
        val flags = PendingIntent.FLAG_IMMUTABLE or
            if (create) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_NO_CREATE
        return PendingIntent.getBroadcast(context, id.toInt(), intent, flags)
    }

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
    }
}
