package com.aggreykibe.dayflow.data

import android.content.Context

/** Remembers when each snoozed reminder should ring again, so a snooze survives a restart of the phone. */
class SnoozeStore(context: Context) {
    private val prefs = DeviceStorage.prefs(context, PREFS_NAME)

    fun put(reminderId: Long, atMillis: Long) {
        prefs.edit().putLong(PREFIX + reminderId, atMillis).commit()
    }

    fun remove(reminderId: Long) {
        prefs.edit().remove(PREFIX + reminderId).commit()
    }

    /** Reminder id to the moment (epoch milliseconds) its snooze ends. */
    fun all(): Map<Long, Long> = prefs.all.mapNotNull { (key, value) ->
        val id = if (key.startsWith(PREFIX)) key.removePrefix(PREFIX).toLongOrNull() else null
        if (id != null && value is Long) id to value else null
    }.toMap()

    private companion object {
        const val PREFS_NAME = "dayflow_snoozes"
        const val PREFIX = "snooze_"
    }
}
