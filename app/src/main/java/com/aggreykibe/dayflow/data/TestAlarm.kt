package com.aggreykibe.dayflow.data

import android.content.Context
import com.aggreykibe.dayflow.domain.AlarmStage
import com.aggreykibe.dayflow.domain.Reminder
import com.aggreykibe.dayflow.domain.ReminderCodec
import com.aggreykibe.dayflow.domain.Repeat
import com.aggreykibe.dayflow.domain.RingStyle
import com.aggreykibe.dayflow.scheduling.ReminderScheduler
import java.time.LocalDateTime

/**
 * A real alarm that goes through exactly the same path as any reminder (alarm manager, receiver, ringing service,
 * notification, full-screen screen), but a few seconds from now. It is kept out of the person's reminder list.
 */
object TestAlarm {
    const val TEST_ID = 2_000_000_000L
    private const val PREFS = "dayflow_test_alarm"
    private const val KEY = "reminder"

    fun reminder(context: Context): Reminder? =
        ReminderCodec.decode(DeviceStorage.prefs(context, PREFS).getString(KEY, "").orEmpty()).firstOrNull()

    /** Schedules the test and returns the exact moment it will ring. */
    fun schedule(context: Context, seconds: Int = 20): LocalDateTime {
        val at = LocalDateTime.now().plusSeconds(seconds.toLong()).withNano(0)
        val settings = SettingsStore(context).load()
        val test = Reminder(
            id = TEST_ID,
            title = "Test alarm",
            date = at.toLocalDate(),
            time = at.toLocalTime(),
            repeat = Repeat.ONCE,
            message = "If you can see and hear this, alarms work while your phone is locked.",
            ringStyle = RingStyle.ALARM,
            vibrate = true,
            snoozeMinutes = 1,
            ringSeconds = 30,
            soundId = settings.soundId,
        )
        DeviceStorage.prefs(context, PREFS).edit().putString(KEY, ReminderCodec.encode(listOf(test))).commit()
        ReminderScheduler(context).schedule(test)
        AlarmLog.record(context, AlarmStage.TEST_SCHEDULED, "rings at $at")
        return at
    }
}

/** Finds a reminder by id, including the hidden test alarm. */
object ReminderLookup {
    fun find(context: Context, id: Long): Reminder? =
        if (id == TestAlarm.TEST_ID) TestAlarm.reminder(context)
        else ReminderStore(context).load().firstOrNull { it.id == id }
}
