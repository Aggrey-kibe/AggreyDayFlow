package com.aggreykibe.dayflow.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.aggreykibe.dayflow.MainActivity
import com.aggreykibe.dayflow.R
import com.aggreykibe.dayflow.data.AlarmLog
import com.aggreykibe.dayflow.domain.AlarmStage
import com.aggreykibe.dayflow.domain.Reminder
import com.aggreykibe.dayflow.permissions.PermissionHelper
import com.aggreykibe.dayflow.ui.alarm.AlarmActivity
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object NotificationHelper {
    const val CHANNEL_ID = "reminders"
    const val ALARM_CHANNEL_ID = "alarms"
    private const val EXTRA_REMINDER_ID = "reminder_id" // same key the alarm service and screen use

    /** Safe to call repeatedly; Android ignores an existing channel with the same id. */
    fun ensureChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply { description = context.getString(R.string.channel_description) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** Channel for the ringing alarm. The alarm service plays the sound itself, so the channel is silent. */
    fun ensureAlarmChannel(context: Context) {
        val channel = NotificationChannel(
            ALARM_CHANNEL_ID,
            context.getString(R.string.alarm_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.alarm_channel_description)
            setSound(null, null)
            enableVibration(false)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /**
     * Returns true if a notification was posted, false if notifications are blocked.
     * With [openAlarmScreen], tapping it (and, where Android allows, the lock screen itself) opens the
     * alarm screen, which starts the ringing. Without it, tapping opens the normal app.
     */
    fun show(context: Context, reminder: Reminder, openAlarmScreen: Boolean = false): Boolean {
        if (!PermissionHelper.notificationsEnabled(context)) {
            AlarmLog.record(context, AlarmStage.NOTIFICATION_BLOCKED, "id=${reminder.id}: notifications are turned off for the app")
            return false
        }
        ensureChannel(context)
        val target = if (openAlarmScreen) {
            Intent(context, AlarmActivity::class.java).putExtra(EXTRA_REMINDER_ID, reminder.id)
        } else {
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val open = PendingIntent.getActivity(
            context,
            reminder.id.toInt(),
            target,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val timeText = reminder.time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        val builder = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(reminder.title)
            .setContentText(reminder.message.ifBlank { timeText })
            .setSubText(timeText)
            .setCategory(Notification.CATEGORY_REMINDER)
            .setContentIntent(open)
            .setAutoCancel(true)
        if (openAlarmScreen) builder.setFullScreenIntent(open, true)
        if (reminder.message.isNotBlank()) builder.setStyle(Notification.BigTextStyle().bigText(reminder.message))
        context.getSystemService(NotificationManager::class.java).notify(reminder.id.toInt(), builder.build())
        AlarmLog.record(context, AlarmStage.NOTIFICATION_POSTED, "id=${reminder.id} ${if (openAlarmScreen) "loud, opens alarm screen" else "normal"}")
        return true
    }
}
