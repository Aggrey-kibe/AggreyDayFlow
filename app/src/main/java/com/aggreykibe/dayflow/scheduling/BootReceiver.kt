package com.aggreykibe.dayflow.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aggreykibe.dayflow.data.ReminderStore

/**
 * Alarms are cleared by a restart, an app update, and time or time-zone changes. Re-arm everything.
 * LOCKED_BOOT_COMPLETED arrives right after a restart, before the PIN is entered, so morning alarms
 * still ring after an overnight restart. Re-arming is safe to repeat, so BOOT_COMPLETED does it again.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED",
            -> ReminderScheduler(context).rescheduleAll(ReminderStore(context).load())
        }
    }
}
