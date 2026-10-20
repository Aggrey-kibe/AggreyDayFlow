package com.aggreykibe.dayflow.alarm

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import com.aggreykibe.dayflow.R
import com.aggreykibe.dayflow.data.ReminderStore
import com.aggreykibe.dayflow.data.SettingsStore
import com.aggreykibe.dayflow.domain.DEFAULT_SNOOZE_MINUTES
import com.aggreykibe.dayflow.domain.Reminder
import com.aggreykibe.dayflow.notifications.NotificationHelper
import com.aggreykibe.dayflow.scheduling.ReminderScheduler
import com.aggreykibe.dayflow.ui.alarm.AlarmActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Rings the alarm using the reminder's own options (sound, vibration, ring time, snooze length) until the
 * person taps Dismiss, Snooze or Done, or until the ring time runs out. Shows a full-screen alert when the
 * phone is locked or idle. Only one alarm rings at a time; a new one replaces the old one.
 */
class AlarmService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private val sound by lazy { SoundPlayer(this) }
    private var vibrator: Vibrator? = null
    private var currentId: Long = -1L
    private var wakeLock: PowerManager.WakeLock? = null
    private val timeout = Runnable { onTimeout() }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val id = intent?.getLongExtra(EXTRA_REMINDER_ID, -1L) ?: -1L
        when (intent?.action) {
            ACTION_STOP -> stopRinging()
            ACTION_DONE -> {
                markDone(id)
                stopRinging()
            }
            ACTION_SNOOZE -> {
                val minutes = findReminder(id)?.snoozeMinutes ?: DEFAULT_SNOOZE_MINUTES
                ReminderScheduler(this).snooze(id, minutes) // replaces any earlier snooze: never more than one
                cancelPlainNotification(id)
                stopRinging()
            }
            else -> startRinging(id)
        }
        return START_NOT_STICKY
    }

    private fun findReminder(id: Long): Reminder? = ReminderStore(this).load().firstOrNull { it.id == id }

    private fun startRinging(id: Long) {
        val reminder = findReminder(id)
        NotificationHelper.ensureAlarmChannel(this)
        val previousId = currentId
        isRinging = true // set before the alert screen can open, so the screen does not start a second ring
        try {
            val notification = buildNotification(reminder, id)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            // Could not become a foreground service. Post a loud notification that opens the alarm screen;
            // the screen is in the foreground, so it is allowed to start the ring itself.
            isRinging = false
            reminder?.let { NotificationHelper.show(this, it, openAlarmScreen = true) }
            stopSelf()
            return
        }
        if (reminder == null) {
            stopRinging()
            return
        }
        if (previousId != -1L && previousId != id) {
            // A second alarm arrived while the first was still ringing. Keep the first as a notification
            // so it is not silently lost.
            findReminder(previousId)?.let { NotificationHelper.show(this, it, openAlarmScreen = false) }
        }
        currentId = id
        stopSoundAndVibration() // in case a second alarm replaces one that is still ringing
        acquireWakeLock(reminder.ringSeconds)
        sound.start(reminder.soundId, loop = true, gradual = SettingsStore(this).load().gradualVolume)
        if (reminder.vibrate) startVibration()
        handler.removeCallbacks(timeout)
        handler.postDelayed(timeout, reminder.ringSeconds * 1000L)
    }

    private fun buildNotification(reminder: Reminder?, id: Long): Notification {
        val timeText = reminder?.time?.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)).orEmpty()
        val message = reminder?.message.orEmpty()
        val snoozeMinutes = reminder?.snoozeMinutes ?: DEFAULT_SNOOZE_MINUTES
        val openAlarm = PendingIntent.getActivity(
            this,
            REQUEST_OPEN,
            Intent(this, AlarmActivity::class.java).putExtra(EXTRA_REMINDER_ID, id),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val icon = Icon.createWithResource(this, R.drawable.ic_notification)
        fun action(label: String, action: String, request: Int) = Notification.Action.Builder(
            icon,
            label,
            PendingIntent.getService(
                this,
                request,
                Intent(this, AlarmService::class.java).setAction(action).putExtra(EXTRA_REMINDER_ID, id),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            ),
        ).build()

        val builder = Notification.Builder(this, NotificationHelper.ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(reminder?.title ?: getString(R.string.app_name))
            .setContentText(message.ifBlank { timeText })
            .setSubText(timeText)
            .setCategory(Notification.CATEGORY_ALARM)
            .setContentIntent(openAlarm)
            .setFullScreenIntent(openAlarm, true)
            .setOngoing(true)
            .addAction(action(getString(R.string.alarm_dismiss), ACTION_STOP, REQUEST_STOP))
            .addAction(action(getString(R.string.alarm_snooze_minutes, snoozeMinutes), ACTION_SNOOZE, REQUEST_SNOOZE))
            .addAction(action(getString(R.string.alarm_done), ACTION_DONE, REQUEST_DONE))
        if (message.isNotBlank()) builder.setStyle(Notification.BigTextStyle().bigText(message))
        return builder.build()
    }

    @Suppress("DEPRECATION")
    private fun startVibration() {
        val v = getSystemService(Vibrator::class.java) ?: return
        vibrator = v
        v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 600, 400), 0))
    }

    private fun stopSoundAndVibration() {
        sound.stop()
        vibrator?.cancel()
        vibrator = null
    }

    private fun markDone(id: Long) {
        val store = ReminderStore(this)
        val all = store.load()
        val reminder = all.firstOrNull { it.id == id } ?: return
        val changed = reminder.withCompletion(LocalDate.now(), true)
        store.save(all.map { if (it.id == id) changed else it })
        ReminderScheduler(this).apply {
            cancelSnooze(id)
            schedule(changed) // skips today, so a finished reminder will not ring again today
        }
        cancelPlainNotification(id)
    }

    private fun cancelPlainNotification(id: Long) {
        getSystemService(NotificationManager::class.java).cancel(id.toInt())
    }

    /** Nobody answered: leave a normal notification behind so the reminder is not lost, then go quiet. */
    private fun onTimeout() {
        findReminder(currentId)?.let { NotificationHelper.show(this, it, openAlarmScreen = false) }
        stopRinging()
    }

    /** Keeps the processor running while the alarm plays with the screen off; always released on stop. */
    private fun acquireWakeLock(ringSeconds: Int) {
        releaseWakeLock()
        val lock = getSystemService(PowerManager::class.java).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DayFlow:ringing")
        lock.acquire(ringSeconds * 1000L + WAKE_MARGIN_MS) // the timeout means it can never be held forever
        wakeLock = lock
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    private fun stopRinging() {
        isRinging = false
        currentId = -1L
        handler.removeCallbacks(timeout)
        releaseWakeLock()
        stopSoundAndVibration()
        stopForeground(STOP_FOREGROUND_REMOVE)
        sendBroadcast(Intent(ACTION_ALARM_STOPPED).setPackage(packageName)) // closes the alarm screen
        stopSelf()
    }

    override fun onDestroy() {
        isRinging = false
        handler.removeCallbacks(timeout)
        releaseWakeLock()
        stopSoundAndVibration()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val ACTION_STOP = "com.aggreykibe.dayflow.action.STOP"
        const val ACTION_DONE = "com.aggreykibe.dayflow.action.DONE"
        const val ACTION_SNOOZE = "com.aggreykibe.dayflow.action.SNOOZE"
        const val ACTION_ALARM_STOPPED = "com.aggreykibe.dayflow.action.ALARM_STOPPED"
        private const val NOTIFICATION_ID = 1_000_000
        private const val WAKE_MARGIN_MS = 15_000L

        /** True while an alarm is ringing. The alarm screen checks it so it never starts a duplicate ring. */
        @Volatile
        var isRinging: Boolean = false
            private set
        private const val REQUEST_OPEN = 1
        private const val REQUEST_DONE = 2
        private const val REQUEST_SNOOZE = 3
        private const val REQUEST_STOP = 4

        /** Returns false if Android refused to start the service, so the caller can fall back. */
        fun tryStart(context: Context, reminderId: Long): Boolean = try {
            context.startForegroundService(
                Intent(context, AlarmService::class.java).putExtra(EXTRA_REMINDER_ID, reminderId),
            )
            true
        } catch (e: Exception) {
            false
        }

        fun command(context: Context, action: String, reminderId: Long) {
            context.startService(
                Intent(context, AlarmService::class.java).setAction(action).putExtra(EXTRA_REMINDER_ID, reminderId),
            )
        }
    }
}
