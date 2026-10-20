package com.aggreykibe.dayflow.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * A reminder. For [Repeat.ONCE], [date] is the day it fires. For DAILY and WEEKLY, [date] is the first
 * day it applies to. [completedDates] records which days the person ticked it off, for every kind.
 * [message] is the optional longer text shown on the alarm screen.
 */
data class Reminder(
    val id: Long,
    val title: String,
    val date: LocalDate,
    val time: LocalTime,
    val repeat: Repeat = Repeat.ONCE,
    val completedDates: Set<LocalDate> = emptySet(),
    val message: String = "",
    val category: Category = Category.PERSONAL,
    val ringStyle: RingStyle = RingStyle.ALARM,
    val vibrate: Boolean = true,
    val snoozeMinutes: Int = DEFAULT_SNOOZE_MINUTES,
    val ringSeconds: Int = DEFAULT_RING_SECONDS,
    val soundId: String = SoundIds.SYSTEM,
) {
    fun occursOn(day: LocalDate): Boolean = when (repeat) {
        Repeat.ONCE -> day == date
        Repeat.DAILY -> !day.isBefore(date)
        Repeat.WEEKLY -> !day.isBefore(date) && day.dayOfWeek == date.dayOfWeek
    }

    fun isCompletedOn(day: LocalDate): Boolean = day in completedDates

    fun withCompletion(day: LocalDate, completed: Boolean): Reminder =
        copy(completedDates = if (completed) completedDates + day else completedDates - day)
}

/** The next moment this reminder should notify, strictly after [now]; null if none remains. Completed days are skipped. */
fun nextTrigger(reminder: Reminder, now: LocalDateTime): LocalDateTime? {
    var day = maxOf(reminder.date, now.toLocalDate())
    // Repeating reminders skip days already completed. The bound (about ten years) keeps this loop finite.
    repeat(MAX_LOOKAHEAD_DAYS) {
        if (reminder.occursOn(day) && !reminder.isCompletedOn(day)) {
            val at = day.atTime(reminder.time)
            if (at.isAfter(now)) return at
        }
        if (reminder.repeat == Repeat.ONCE) return null
        day = day.plusDays(1)
    }
    return null
}

/** The soonest upcoming trigger across all reminders, with the reminder that owns it. */
fun nextUpcoming(reminders: List<Reminder>, now: LocalDateTime): Pair<Reminder, LocalDateTime>? =
    reminders.mapNotNull { r -> nextTrigger(r, now)?.let { r to it } }.minByOrNull { it.second }

/** Reminders that apply on [day], earliest first (ties broken by title, then id so the order is stable). */
fun remindersForDay(reminders: List<Reminder>, day: LocalDate): List<Reminder> =
    reminders.filter { it.occursOn(day) }
        .sortedWith(compareBy<Reminder>({ it.time }, { it.title.lowercase() }, { it.id }))

data class DayProgress(val completed: Int, val upcoming: Int, val missed: Int)

/** Counts for [day]. Past days have no "upcoming"; future days have no "missed". */
fun progressFor(reminders: List<Reminder>, day: LocalDate, now: LocalDateTime): DayProgress {
    var completed = 0
    var upcoming = 0
    var missed = 0
    for (r in remindersForDay(reminders, day)) {
        when {
            r.isCompletedOn(day) -> completed++
            day.atTime(r.time).isAfter(now) -> upcoming++
            else -> missed++
        }
    }
    return DayProgress(completed, upcoming, missed)
}

private const val MAX_LOOKAHEAD_DAYS = 3660
