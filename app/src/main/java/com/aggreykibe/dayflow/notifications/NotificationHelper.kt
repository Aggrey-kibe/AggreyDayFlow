package com.aggreykibe.dayflow.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.aggreykibe.dayflow.MainActivity
import com.aggreykibe.dayflow.R
import com.aggreykibe.dayflow.domain.Reminder
import com.aggreykibe.dayflow.permissions.PermissionHelper
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object NotificationHelper {
    const val CHANNEL_ID = "reminders"

    /** Safe to call repeatedly; Android ignores an existing channel with the same id. */
    fun ensureChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply { description = context.getString(R.string.channel_description) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** Returns true if a notification was posted, false if notifications are blocked. */
    fun show(context: Context, reminder: Reminder): Boolean {
        if (!PermissionHelper.notificationsEnabled(context)) return false
        ensureChannel(context)
        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(reminder.title)
            .setContentText(reminder.time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)))
            .setCategory(Notification.CATEGORY_REMINDER)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(reminder.id.toInt(), notification)
        return true
    }
}
