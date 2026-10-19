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
}
