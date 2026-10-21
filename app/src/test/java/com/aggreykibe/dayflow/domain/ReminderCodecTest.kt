package com.aggreykibe.dayflow.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class ReminderCodecTest {
    private val day = LocalDate.of(2026, 10, 19)

    @Test fun roundTripPreservesEverything() {
        val list = listOf(
            Reminder(1, "Take medicine", day, LocalTime.of(8, 30), Repeat.DAILY, setOf(day, day.plusDays(1))),
            Reminder(2, "Call Mama", day.plusDays(3), LocalTime.of(17, 5), Repeat.ONCE),
        )
        assertEquals(list, ReminderCodec.decode(ReminderCodec.encode(list)))
    }

    @Test fun awkwardTitlesSurvive() {
        val title = "Tab\there | comma, \"quotes\" & newline\nçafé 日本 🙂 %41"
        val list = listOf(Reminder(7, title, day, LocalTime.of(0, 0)))
        assertEquals(list, ReminderCodec.decode(ReminderCodec.encode(list)))
    }

    @Test fun emptyListRoundTrips() {
        assertEquals(emptyList<Reminder>(), ReminderCodec.decode(ReminderCodec.encode(emptyList())))
        assertEquals(emptyList<Reminder>(), ReminderCodec.decode(""))
    }

    @Test fun corruptLinesAreSkippedNotFatal() {
        val good = Reminder(1, "ok", day, LocalTime.of(9, 0))
        val text = "garbage\n" + ReminderCodec.encode(listOf(good)) + "\nx\ty\tz\t1\t2\n1\tt\tnot-a-date\t09:00\tONCE\t"
        assertEquals(listOf(good), ReminderCodec.decode(text))
    }

    @Test fun newFieldsRoundTrip() {
        val r = Reminder(
            id = 3, title = "Time to study", date = day, time = LocalTime.of(6, 45), repeat = Repeat.WEEKLY,
            completedDates = setOf(day), message = "Put your phone aside.\nOpen your notes & focus 30 min.",
            category = Category.SCHOOL, ringStyle = RingStyle.NOTIFICATION, vibrate = false,
            snoozeMinutes = 15, ringSeconds = 60, soundId = "lofi_morning",
        )
        assertEquals(listOf(r), ReminderCodec.decode(ReminderCodec.encode(listOf(r))))
    }

    @Test fun oldSavedLinesStillLoadWithDefaults() {
        // Exactly what the earlier app version wrote: only six fields, no message or alarm options.
        val legacy = "1\tTake%20medicine\t2026-10-19\t08:30\tDAILY\t2026-10-19,2026-10-20"
        val r = ReminderCodec.decode(legacy).single()
        assertEquals("Take medicine", r.title)
        assertEquals(Repeat.DAILY, r.repeat)
        assertEquals(setOf(day, day.plusDays(1)), r.completedDates)
        assertEquals("", r.message)
        assertEquals(Category.PERSONAL, r.category)
        assertEquals(RingStyle.ALARM, r.ringStyle)
        assertEquals(true, r.vibrate)
        assertEquals(DEFAULT_SNOOZE_MINUTES, r.snoozeMinutes)
        assertEquals(DEFAULT_RING_SECONDS, r.ringSeconds)
        assertEquals(SoundIds.DEFAULT, r.soundId)
    }

    @Test fun unknownOrOutOfRangeValuesFallBackSafely() {
        val line = "9\tx\t2026-10-19\t08:00\tONCE\t\t\tNOPE\tWHAT\t1\t99999\t0\tnot-a-sound"
        val r = ReminderCodec.decode(line).single()
        assertEquals(Category.PERSONAL, r.category)
        assertEquals(RingStyle.ALARM, r.ringStyle)
        assertEquals(120, r.snoozeMinutes) // clamped to the allowed maximum
        assertEquals(10, r.ringSeconds)    // clamped to the allowed minimum
        assertEquals(SoundIds.DEFAULT, r.soundId)
    }
}
