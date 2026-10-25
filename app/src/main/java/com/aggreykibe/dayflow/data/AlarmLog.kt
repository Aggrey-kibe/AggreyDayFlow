package com.aggreykibe.dayflow.data

import android.content.Context
import com.aggreykibe.dayflow.domain.AlarmLogCodec
import com.aggreykibe.dayflow.domain.LogEntry

/**
 * A small on-device record of what the alarm chain did and when, shown in Settings so a missing step is visible.
 * It keeps no reminder messages. Every call is wrapped so that logging can never crash or delay an alarm.
 */
object AlarmLog {
    private const val PREFS = "dayflow_alarm_log"
    private const val KEY = "events"
    private val lock = Any()

    fun record(context: Context, stage: String, detail: String = "") {
        try {
            synchronized(lock) {
                val prefs = DeviceStorage.prefs(context, PREFS)
                val updated = AlarmLogCodec.append(
                    AlarmLogCodec.decode(prefs.getString(KEY, "").orEmpty()),
                    LogEntry(System.currentTimeMillis(), stage, detail),
                )
                prefs.edit().putString(KEY, AlarmLogCodec.encode(updated)).apply()
            }
        } catch (e: Exception) {
            // Logging is best effort only.
        }
    }

    fun entries(context: Context): List<LogEntry> = try {
        synchronized(lock) { AlarmLogCodec.decode(DeviceStorage.prefs(context, PREFS).getString(KEY, "").orEmpty()) }
    } catch (e: Exception) {
        emptyList()
    }

    fun clear(context: Context) {
        try {
            synchronized(lock) { DeviceStorage.prefs(context, PREFS).edit().remove(KEY).apply() }
        } catch (e: Exception) {
            // ignore
        }
    }
}
