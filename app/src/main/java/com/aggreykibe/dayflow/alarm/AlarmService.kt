package com.aggreykibe.dayflow.alarm

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import com.aggreykibe.dayflow.R
import com.aggreykibe.dayflow.data.ReminderStore
import com.aggreykibe.dayflow.domain.Reminder
import com.aggreykibe.dayflow.notifications.NotificationHelper
import com.aggreykibe.dayflow.scheduling.ReminderScheduler
import com.aggreykibe.dayflow.ui.alarm.AlarmActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Rings the alarm: loops the phone's alarm sound and vibrates until the person taps Done, Snooze or Stop,
 * or until [TIMEOUT_MS] passes. Shows a full-screen alert when the phone is locked or idle.
 */
class AlarmService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var currentId: Long = -1L
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
                ReminderScheduler(this).snooze(id, SNOOZE_MINUTES)
                cancelPlainNotification(id)
                stopRinging()
            }
            else -> startRinging(id)
        }
        return START_NOT_STICKY
    }

    private fun startRinging(id: Long) {
        val reminder = ReminderStore(this).load().firstOrNull { it.id == id }
        NotificationHelper.ensureAlarmChannel(this)
        try {
            val notification = buildNotification(reminder, id)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            // Could not become a foreground service; fall back to a normal notification.
            reminder?.let { NotificationHelper.show(this, it) }
            stopSelf()
            return
        }
        if (reminder == null) {
            stopRinging()
            return
        }
        currentId = id
        stopSoundAndVibration() // in case a second alarm replaces one that is still ringing
        startSound()
        startVibration()
        handler.removeCallbacks(timeout)
        handler.postDelayed(timeout, TIMEOUT_MS)
    }

    private fun buildNotification(reminder: Reminder?, id: Long): Notification {
        val timeText = reminder?.time?.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)).orEmpty()
        val openAlarm = PendingIntent.getActivity(
            this,
            REQUEST_OPEN,
            Intent(this, AlarmActivity::class.java).putExtra(EXTRA_REMINDER_ID, id),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val icon = Icon.createWithResource(this, R.drawable.ic_notification)
        fun action(label: Int, action: String, request: Int) = Notification.Action.Builder(
            icon,
            getString(label),
            PendingIntent.getService(
                this,
                request,
                Intent(this, AlarmService::class.java).setAction(action).putExtra(EXTRA_REMINDER_ID, id),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            ),
        ).build()

        return Notification.Builder(this, NotificationHelper.ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(reminder?.title ?: getString(R.string.app_name))
            .setContentText(timeText)
            .setCategory(Notification.CATEGORY_ALARM)
            .setContentIntent(openAlarm)
            .setFullScreenIntent(openAlarm, true)
            .setOngoing(true)
            .addAction(action(R.string.alarm_done, ACTION_DONE, REQUEST_DONE))
            .addAction(action(R.string.alarm_snooze, ACTION_SNOOZE, REQUEST_SNOOZE))
            .addAction(action(R.string.alarm_stop, ACTION_STOP, REQUEST_STOP))
            .build()
    }

    private fun startSound() {
        val uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        try {
            player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                setDataSource(this@AlarmService, uri)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            player?.release()
            player = null
        }
    }

    @Suppress("DEPRECATION")
    private fun startVibration() {
        val v = getSystemService(Vibrator::class.java) ?: return
        vibrator = v
        v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 600, 400), 0))
    }

    private fun stopSoundAndVibration() {
        try { player?.stop() } catch (e: IllegalStateException) { /* already stopped */ }
        player?.release()
        player = null
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
        ReminderStore(this).load().firstOrNull { it.id == currentId }?.let { NotificationHelper.show(this, it) }
        stopRinging()
    }

    private fun stopRinging() {
        handler.removeCallbacks(timeout)
        stopSoundAndVibration()
        stopForeground(STOP_FOREGROUND_REMOVE)
        sendBroadcast(Intent(ACTION_ALARM_STOPPED).setPackage(packageName)) // closes the alarm screen
        stopSelf()
    }

    override fun onDestroy() {
        handler.removeCallbacks(timeout)
        stopSoundAndVibration()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val ACTION_STOP = "com.aggreykibe.dayflow.action.STOP"
        const val ACTION_DONE = "com.aggreykibe.dayflow.action.DONE"
        const val ACTION_SNOOZE = "com.aggreykibe.dayflow.action.SNOOZE"
        const val ACTION_ALARM_STOPPED = "com.aggreykibe.dayflow.action.ALARM_STOPPED"
        const val SNOOZE_MINUTES = 10
        private const val TIMEOUT_MS = 2 * 60 * 1000L
        private const val NOTIFICATION_ID = 1_000_000
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
