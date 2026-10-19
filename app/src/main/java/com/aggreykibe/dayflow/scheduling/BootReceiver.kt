package com.aggreykibe.dayflow.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aggreykibe.dayflow.data.ReminderStore

/** Alarms are cleared by reboot, app update and timezone changes; re-arm every reminder. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIMEZONE_CHANGED,
            "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED",
            -> ReminderScheduler(context).rescheduleAll(ReminderStore(context).load())
        }
    }
}
