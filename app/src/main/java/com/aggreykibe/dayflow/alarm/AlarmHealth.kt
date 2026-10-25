package com.aggreykibe.dayflow.alarm

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.ApplicationExitInfo
import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import com.aggreykibe.dayflow.domain.AlarmLogCodec
import com.aggreykibe.dayflow.domain.HealthCheck
import com.aggreykibe.dayflow.notifications.NotificationHelper
import com.aggreykibe.dayflow.permissions.OemSettings
import com.aggreykibe.dayflow.permissions.PermissionHelper
import java.time.ZoneId

/** Reads what Android currently allows, so problems that silence an alarm are visible instead of guessed at. */
object AlarmHealth {
    fun collect(context: Context): List<HealthCheck> {
        val checks = mutableListOf<HealthCheck>()
        val nm = context.getSystemService(NotificationManager::class.java)

        val notificationsOk = PermissionHelper.notificationsEnabled(context)
        checks += HealthCheck(
            "Notifications", notificationsOk,
            if (notificationsOk) "Allowed" else "Blocked. The alarm screen and notification cannot appear.",
        )

        val alarmChannel = nm.getNotificationChannel(NotificationHelper.ALARM_CHANNEL_ID)
        checks += if (alarmChannel == null) {
            HealthCheck("Alarm notification channel", null, "Not created yet. It is created when the app opens.")
        } else {
            val high = alarmChannel.importance >= NotificationManager.IMPORTANCE_HIGH
            HealthCheck(
                "Alarm notification channel", high,
                if (high) "High priority" else "Importance level ${alarmChannel.importance} (needs 4, High). A full-screen alarm cannot open.",
            )
        }

        val fullScreenOk = PermissionHelper.fullScreenAlertsAllowed(context)
        checks += HealthCheck(
            "Full-screen alerts", fullScreenOk,
            if (fullScreenOk) "Allowed" else "Not allowed. A locked phone will only show a banner.",
        )

        val exactOk = PermissionHelper.exactAlarmsAllowed(context)
        checks += HealthCheck(
            "Exact alarms", if (exactOk) true else null,
            if (exactOk) "Allowed" else "Not allowed. Only affects quiet-notification reminders; ringing alarms do not need it.",
        )

        val unrestricted = context.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(context.packageName)
        checks += HealthCheck(
            "Battery use", if (unrestricted) true else if (OemSettings.isTranssion()) false else null,
            if (unrestricted) "Unrestricted"
            else "Optimised. Phone makers such as Tecno can stop optimised apps when the screen locks. Set it to Unrestricted.",
        )

        val audio = context.getSystemService(AudioManager::class.java)
        val volume = audio.getStreamVolume(AudioManager.STREAM_ALARM)
        val maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        checks += HealthCheck(
            "Alarm volume", volume > 0,
            if (volume > 0) "Level $volume of $maxVolume" else "Muted (0 of $maxVolume). The alarm would be silent. Turn up the alarm volume in Sound settings.",
        )

        checks += when (nm.currentInterruptionFilter) {
            NotificationManager.INTERRUPTION_FILTER_ALL -> HealthCheck("Do Not Disturb", true, "Off")
            NotificationManager.INTERRUPTION_FILTER_ALARMS -> HealthCheck("Do Not Disturb", true, "Alarms only: alarms are allowed")
            NotificationManager.INTERRUPTION_FILTER_PRIORITY -> {
                val allowsAlarms = nm.notificationPolicy.priorityCategories and NotificationManager.Policy.PRIORITY_CATEGORY_ALARMS != 0
                HealthCheck("Do Not Disturb", allowsAlarms, if (allowsAlarms) "On, alarms are allowed" else "On, and alarms are NOT allowed through")
            }
            NotificationManager.INTERRUPTION_FILTER_NONE -> HealthCheck("Do Not Disturb", false, "Total silence is on and blocks alarms")
            else -> HealthCheck("Do Not Disturb", null, "Unknown")
        }

        val next = context.getSystemService(AlarmManager::class.java).nextAlarmClock
        checks += if (next == null) {
            HealthCheck("Next alarm clock known to the phone", null, "none")
        } else {
            val mine = next.showIntent?.creatorPackage == context.packageName
            HealthCheck(
                "Next alarm clock known to the phone", null,
                AlarmLogCodec.formatTime(next.triggerTime, ZoneId.systemDefault()) + if (mine) " (set by this app)" else " (set by another app)",
            )
        }

        checks += HealthCheck("Phone maker", null, "${Build.MANUFACTURER} ${Build.MODEL}" + if (OemSettings.isTranssion()) " (Tecno/Infinix/itel: has its own battery manager)" else "")
        return checks
    }

    fun header(context: Context): List<String> {
        @Suppress("DEPRECATION")
        val info = try { context.packageManager.getPackageInfo(context.packageName, 0) } catch (e: Exception) { null }
        return listOf(
            "App: Aggrey DayFlow ${info?.versionName ?: "?"}",
            "Phone: ${Build.MANUFACTURER} ${Build.MODEL}",
            "Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            "System build: ${Build.DISPLAY}",
        )
    }

    /** Android's own record of why the app's process ended, newest first. A phone that kills the app when the screen locks shows up here. */
    fun recentExits(context: Context): List<String> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return emptyList()
        val infos = try {
            context.getSystemService(ActivityManager::class.java).getHistoricalProcessExitReasons(context.packageName, 0, 8)
        } catch (e: Exception) {
            emptyList()
        }
        return infos.map {
            "${AlarmLogCodec.formatTime(it.timestamp, ZoneId.systemDefault())}  ${reasonName(it.reason)}  ${it.description.orEmpty()}".trim()
        }
    }

    private fun reasonName(reason: Int): String = when (reason) {
        ApplicationExitInfo.REASON_USER_REQUESTED -> "USER_REQUESTED (force stop or swipe-away by the user or the phone's cleaner)"
        ApplicationExitInfo.REASON_USER_STOPPED -> "USER_STOPPED"
        ApplicationExitInfo.REASON_LOW_MEMORY -> "LOW_MEMORY"
        ApplicationExitInfo.REASON_CRASH -> "CRASH"
        ApplicationExitInfo.REASON_CRASH_NATIVE -> "CRASH_NATIVE"
        ApplicationExitInfo.REASON_ANR -> "ANR (not responding)"
        ApplicationExitInfo.REASON_EXIT_SELF -> "EXIT_SELF"
        ApplicationExitInfo.REASON_SIGNALED -> "SIGNALED (killed by the system)"
        ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE -> "EXCESSIVE_RESOURCE_USAGE"
        ApplicationExitInfo.REASON_PERMISSION_CHANGE -> "PERMISSION_CHANGE"
        ApplicationExitInfo.REASON_DEPENDENCY_DIED -> "DEPENDENCY_DIED"
        ApplicationExitInfo.REASON_OTHER -> "OTHER (often a phone maker's cleaner)"
        ApplicationExitInfo.REASON_FREEZER -> "FREEZER"
        ApplicationExitInfo.REASON_PACKAGE_STATE_CHANGE -> "PACKAGE_STATE_CHANGE"
        ApplicationExitInfo.REASON_PACKAGE_UPDATED -> "PACKAGE_UPDATED"
        else -> "UNKNOWN($reason)"
    }
}
