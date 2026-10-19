package com.aggreykibe.dayflow.data

import android.content.Context
import com.aggreykibe.dayflow.domain.Reminder
import com.aggreykibe.dayflow.domain.ReminderCodec

/** Local-only persistence. Reminders live in app-private storage; nothing leaves the device. */
class ReminderStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): List<Reminder> = ReminderCodec.decode(prefs.getString(KEY, "").orEmpty())

    /** Uses commit() so the write is on disk before returning; the list is tiny. */
    fun save(reminders: List<Reminder>) {
        prefs.edit().putString(KEY, ReminderCodec.encode(reminders)).commit()
    }

    private companion object {
        const val PREFS_NAME = "dayflow_reminders"
        const val KEY = "reminders_v1"
    }
}
