package com.aggreykibe.dayflow.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ReminderLogicTest {
    private val mon = LocalDate.of(2026, 10, 19)
    private val tue = mon.plusDays(1)
    private val wed = mon.plusDays(2)

    private fun once(id: Long, day: LocalDate, h: Int, m: Int = 0, title: String = "r$id") =
        Reminder(id, title, day, LocalTime.of(h, m), Repeat.ONCE)

    private fun daily(id: Long, start: LocalDate, h: Int, m: Int = 0, done: Set<LocalDate> = emptySet()) =
        Reminder(id, "d$id", start, LocalTime.of(h, m), Repeat.DAILY, done)

    // --- occursOn
    @Test fun onceOccursOnlyOnItsDate() {
        val r = once(1, mon, 9)
        assertTrue(r.occursOn(mon)); assertFalse(r.occursOn(tue)); assertFalse(r.occursOn(mon.minusDays(1)))
    }

    @Test fun dailyOccursFromStartDateOnward() {
        val r = daily(1, tue, 9)
        assertFalse(r.occursOn(mon)); assertTrue(r.occursOn(tue)); assertTrue(r.occursOn(tue.plusDays(400)))
    }

    // --- nextTrigger: one-time
    @Test fun onceLaterTodayTriggersToday() {
        assertEquals(mon.atTime(15, 0), nextTrigger(once(1, mon, 15), mon.atTime(9, 0)))
    }

    @Test fun oncePastHasNoTrigger() {
        assertNull(nextTrigger(once(1, mon, 8), mon.atTime(9, 0)))
    }

    @Test fun onceExactlyNowIsNotInTheFuture() {
        assertNull(nextTrigger(once(1, mon, 9), mon.atTime(9, 0)))
    }

    @Test fun onceCompletedHasNoTrigger() {
        val r = once(1, mon, 15).withCompletion(mon, true)
        assertNull(nextTrigger(r, mon.atTime(9, 0)))
    }

    @Test fun onceInFutureDayTriggersThere() {
        assertEquals(wed.atTime(7, 30), nextTrigger(once(1, wed, 7, 30), mon.atTime(23, 0)))
    }

    // --- nextTrigger: daily
    @Test fun dailyLaterTodayTriggersToday() {
        assertEquals(mon.atTime(18, 0), nextTrigger(daily(1, mon.minusDays(5), 18), mon.atTime(9, 0)))
    }

    @Test fun dailyAlreadyPassedTodayTriggersTomorrow() {
        assertEquals(tue.atTime(8, 0), nextTrigger(daily(1, mon.minusDays(5), 8), mon.atTime(9, 0)))
    }

    @Test fun dailyCompletedTodaySkipsToTomorrow() {
        val r = daily(1, mon.minusDays(5), 18, done = setOf(mon))
        assertEquals(tue.atTime(18, 0), nextTrigger(r, mon.atTime(9, 0)))
    }

    @Test fun dailyStartingInFutureWaitsForStartDate() {
        assertEquals(wed.atTime(6, 0), nextTrigger(daily(1, wed, 6), mon.atTime(9, 0)))
    }

    @Test fun dailySkipsSeveralCompletedDays() {
        val r = daily(1, mon, 18, done = setOf(mon, tue))
        assertEquals(wed.atTime(18, 0), nextTrigger(r, mon.atTime(9, 0)))
    }

    // --- nextUpcoming
    @Test fun nextUpcomingPicksEarliestAcrossReminders() {
        val a = once(1, mon, 20); val b = once(2, mon, 12); val c = once(3, mon, 8) // c already past
        val (r, at) = nextUpcoming(listOf(a, b, c), mon.atTime(9, 0))!!
        assertEquals(2L, r.id); assertEquals(mon.atTime(12, 0), at)
    }

    @Test fun nextUpcomingIsNullWhenNothingRemains() {
        assertNull(nextUpcoming(listOf(once(1, mon, 8)), mon.atTime(9, 0)))
        assertNull(nextUpcoming(emptyList(), mon.atTime(9, 0)))
    }

    // --- remindersForDay
    @Test fun dayListIsSortedByTimeThenTitle() {
        val list = listOf(once(1, mon, 14), once(2, mon, 9, title = "b"), once(3, mon, 9, title = "A"), once(4, tue, 7))
        assertEquals(listOf(3L, 2L, 1L), remindersForDay(list, mon).map { it.id })
    }

    @Test fun dayListIncludesDailyAndExcludesOtherDays() {
        val list = listOf(daily(1, mon, 8), once(2, tue, 9))
        assertEquals(listOf(1L), remindersForDay(list, mon).map { it.id })
        assertEquals(listOf(1L, 2L), remindersForDay(list, tue).map { it.id })
    }

    // --- completion
    @Test fun completionIsPerDayForDailyReminders() {
        val r = daily(1, mon, 8).withCompletion(mon, true)
        assertTrue(r.isCompletedOn(mon)); assertFalse(r.isCompletedOn(tue))
        assertFalse(r.withCompletion(mon, false).isCompletedOn(mon))
    }

    // --- progress
    @Test fun progressTodayMixesAllThreeStates() {
        val list = listOf(
            once(1, mon, 8).withCompletion(mon, true),
            once(2, mon, 7),                      // passed, not done -> missed
            once(3, mon, 20),                     // later -> upcoming
            once(4, mon, 21),
        )
        assertEquals(DayProgress(1, 2, 1), progressFor(list, mon, mon.atTime(12, 0)))
    }

    @Test fun progressPastDayHasNoUpcoming() {
        val list = listOf(once(1, mon, 23, 59))
        assertEquals(DayProgress(0, 0, 1), progressFor(list, mon, tue.atTime(0, 1)))
    }

    @Test fun progressFutureDayHasNoMissed() {
        val list = listOf(daily(1, mon, 6))
        assertEquals(DayProgress(0, 1, 0), progressFor(list, wed, mon.atTime(23, 0)))
    }

    @Test fun progressEmptyDay() {
        assertEquals(DayProgress(0, 0, 0), progressFor(emptyList(), mon, mon.atTime(9, 0)))
    }

    // --- weekly
    private fun weekly(id: Long, start: LocalDate, h: Int, done: Set<LocalDate> = emptySet()) =
        Reminder(id, "w$id", start, LocalTime.of(h, 0), Repeat.WEEKLY, done)

    @Test fun weeklyOccursOnlyOnSameWeekdayFromStart() {
        val r = weekly(1, mon, 9)
        assertTrue(r.occursOn(mon)); assertTrue(r.occursOn(mon.plusDays(7))); assertTrue(r.occursOn(mon.plusDays(70)))
        assertFalse(r.occursOn(tue)); assertFalse(r.occursOn(mon.plusDays(6))); assertFalse(r.occursOn(mon.minusDays(7)))
    }

    @Test fun weeklyPassedTodayTriggersNextWeek() {
        assertEquals(mon.plusDays(7).atTime(8, 0), nextTrigger(weekly(1, mon, 8), mon.atTime(9, 0)))
    }

    @Test fun weeklyLaterTodayTriggersToday() {
        assertEquals(mon.atTime(18, 0), nextTrigger(weekly(1, mon, 18), mon.atTime(9, 0)))
    }

    @Test fun weeklyCompletedSkipsToFollowingWeek() {
        val r = weekly(1, mon, 18, done = setOf(mon))
        assertEquals(mon.plusDays(7).atTime(18, 0), nextTrigger(r, mon.atTime(9, 0)))
    }

    @Test fun weeklyStartingInFutureWaitsForStart() {
        val start = mon.plusDays(10)
        assertEquals(start.atTime(7, 0), nextTrigger(weekly(1, start, 7), mon.atTime(9, 0)))
    }

    @Test fun weeklyAppearsInDayListOnlyOnItsWeekday() {
        val list = listOf(weekly(1, mon, 8))
        assertEquals(listOf(1L), remindersForDay(list, mon.plusDays(14)).map { it.id })
        assertEquals(emptyList<Long>(), remindersForDay(list, tue).map { it.id })
    }

    // --- countdown
    @Test fun minutesUntilRoundsUpAndNeverGoesNegative() {
        val t = mon.atTime(9, 0)
        assertEquals(0L, minutesUntil(t, t))
        assertEquals(0L, minutesUntil(t, t.minusMinutes(5)))
        assertEquals(1L, minutesUntil(t, t.plusSeconds(1)))
        assertEquals(1L, minutesUntil(t, t.plusSeconds(60)))
        assertEquals(2L, minutesUntil(t, t.plusSeconds(61)))
        assertEquals(135L, minutesUntil(t, t.plusHours(2).plusMinutes(15)))
        assertEquals(1440L, minutesUntil(t, t.plusDays(1)))
    }
}
