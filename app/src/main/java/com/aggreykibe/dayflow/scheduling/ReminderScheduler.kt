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
 * Keeps one pending alarm per reminder, always for that reminder's next trigger, plus an optional
 * separate "snooze" alarm. Daily reminders are re-armed by [ReminderReceiver] each time they fire.
 * Without exact-alarm permission it falls back to an inexact alarm, which Android may delay by minutes.
 */
class ReminderScheduler(context: Context) {
    private val context = context.applicationContext
    private val alarms = this.context.getSystemService(AlarmManager::class.java)

    fun schedule(reminder: Reminder, now: LocalDateTime = LocalDateTime.now()) {
        cancel(reminder.id)
        val at = nextTrigger(reminder, now) ?: return
        val millis = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val pending = pendingIntent(reminder.id, REMINDER_PATH, create = true) ?: return
        setAlarm(millis, pending)
    }

    fun cancel(id: Long) = cancelPending(id, REMINDER_PATH)

    /** Rings the reminder again in [minutes]. Independent of the reminder's normal schedule. */
    fun snooze(id: Long, minutes: Int) {
        val pending = pendingIntent(id, SNOOZE_PATH, create = true) ?: return
        setAlarm(System.currentTimeMillis() + minutes * 60_000L, pending)
    }

    fun cancelSnooze(id: Long) = cancelPending(id, SNOOZE_PATH)

    fun rescheduleAll(reminders: List<Reminder>, now: LocalDateTime = LocalDateTime.now()) {
        reminders.forEach { schedule(it, now) }
    }

    private fun setAlarm(millis: Long, pending: PendingIntent) {
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

    private fun cancelPending(id: Long, path: String) {
        val pending = pendingIntent(id, path, create = false) ?: return
        alarms.cancel(pending)
        pending.cancel()
    }

    private fun pendingIntent(id: Long, path: String, create: Boolean): PendingIntent? {
        val intent = Intent(context, ReminderReceiver::class.java)
            .setData(Uri.parse("dayflow://$path/$id")) // makes the intent unique per reminder and kind
            .putExtra(EXTRA_REMINDER_ID, id)
        val flags = PendingIntent.FLAG_IMMUTABLE or
            if (create) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_NO_CREATE
        return PendingIntent.getBroadcast(context, id.toInt(), intent, flags)
    }

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
        private const val REMINDER_PATH = "reminder"
        private const val SNOOZE_PATH = "snooze"
    }
}
