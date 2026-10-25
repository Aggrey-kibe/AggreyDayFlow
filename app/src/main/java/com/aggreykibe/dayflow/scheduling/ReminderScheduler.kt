package com.aggreykibe.dayflow.scheduling

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.aggreykibe.dayflow.MainActivity
import com.aggreykibe.dayflow.data.AlarmLog
import com.aggreykibe.dayflow.data.SnoozeStore
import com.aggreykibe.dayflow.domain.AlarmStage
import com.aggreykibe.dayflow.domain.Reminder
import com.aggreykibe.dayflow.domain.RingStyle
import com.aggreykibe.dayflow.domain.nextTrigger
import com.aggreykibe.dayflow.domain.snoozesToRearm
import com.aggreykibe.dayflow.permissions.PermissionHelper
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Keeps one pending alarm per reminder, always for that reminder's next trigger, plus an optional
 * separate "snooze" alarm. Daily and weekly reminders are re-armed by [ReminderReceiver] each time they fire.
 *
 * How alarms are armed:
 *  - Ringing alarms use Android's alarm-clock API (setAlarmClock). It needs no special permission, is exempt
 *    from battery-saving delays (Doze), and lets the app start its ringing service. Android shows the next
 *    alarm in the status area, like a normal alarm clock.
 *  - Quiet-notification reminders use an exact alarm when the person allowed it, otherwise an inexact one
 *    that Android may delay by minutes.
 * Every alarm handed to Android is recorded in [AlarmLog] so the health check can show it.
 */
class ReminderScheduler(context: Context) {
    private val context = context.applicationContext
    private val alarms = this.context.getSystemService(AlarmManager::class.java)
    private val snoozes = SnoozeStore(this.context)

    fun schedule(reminder: Reminder, now: LocalDateTime = LocalDateTime.now(), log: Boolean = true) {
        cancel(reminder.id)
        val at = nextTrigger(reminder, now)
        if (at == null) {
            if (log) AlarmLog.record(context, AlarmStage.SCHEDULE_SKIPPED, "id=${reminder.id} nothing left to ring")
            return
        }
        val millis = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val pending = pendingIntent(reminder.id, REMINDER_PATH, create = true) ?: return
        val ringing = reminder.ringStyle == RingStyle.ALARM
        arm(millis, pending, ringing)
        if (log) AlarmLog.record(context, AlarmStage.SCHEDULED, "id=${reminder.id} \"${reminder.title.take(24)}\" at $at ${if (ringing) "alarm-clock" else "quiet"}")
    }

    fun cancel(id: Long) = cancelPending(id, REMINDER_PATH)

    /** Rings the reminder again in [minutes]. Replaces any earlier snooze, so there is never more than one. */
    fun snooze(id: Long, minutes: Int) {
        val millis = System.currentTimeMillis() + minutes * 60_000L
        val pending = pendingIntent(id, SNOOZE_PATH, create = true) ?: return
        snoozes.put(id, millis)
        arm(millis, pending, ringing = true)
        AlarmLog.record(context, AlarmStage.SCHEDULED, "id=$id snooze for $minutes min")
    }

    /** Cancels a pending snooze and forgets it. */
    fun cancelSnooze(id: Long) {
        cancelPending(id, SNOOZE_PATH)
        snoozes.remove(id)
    }

    /** Forgets the saved snooze after it has fired (its alarm is already used up). */
    fun forgetSnooze(id: Long) = snoozes.remove(id)

    /**
     * Re-arms every reminder and every snooze that is still in the future. Safe to call repeatedly.
     * Pass [logSource] only for rare events (restart, update); routine calls stay out of the event log.
     */
    fun rescheduleAll(reminders: List<Reminder>, now: LocalDateTime = LocalDateTime.now(), logSource: String? = null) {
        reminders.forEach { schedule(it, now, log = false) }
        rearmSnoozes(reminders.map { it.id }.toSet())
        if (logSource != null) AlarmLog.record(context, AlarmStage.REARMED, "$logSource: ${reminders.size} reminders")
    }

    private fun rearmSnoozes(existingIds: Set<Long>) {
        val saved = snoozes.all()
        val live = snoozesToRearm(saved.filterKeys { it in existingIds }, System.currentTimeMillis())
        saved.keys.filter { it !in live }.forEach { snoozes.remove(it) } // finished, or its reminder was deleted
        live.forEach { (id, millis) ->
            val pending = pendingIntent(id, SNOOZE_PATH, create = true) ?: return@forEach
            arm(millis, pending, ringing = true)
        }
    }

    private fun arm(millis: Long, pending: PendingIntent, ringing: Boolean) {
        if (ringing) {
            alarms.setAlarmClock(AlarmManager.AlarmClockInfo(millis, openAppIntent()), pending)
            return
        }
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

    /** Shown by Android when the person taps the next-alarm indicator: simply opens the app. */
    private fun openAppIntent(): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

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
        const val REMINDER_PATH = "reminder"
        const val SNOOZE_PATH = "snooze"
    }
}
