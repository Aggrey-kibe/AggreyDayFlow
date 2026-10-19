package com.aggreykibe.dayflow.domain

import java.net.URLDecoder
import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalTime

/**
 * Plain-text storage format, one reminder per line, tab-separated:
 * id, title (URL-encoded), date, time, repeat, completed dates (comma-separated).
 * Malformed lines are skipped on decode so one bad line cannot lose the rest of the data.
 */
object ReminderCodec {
    fun encode(reminders: List<Reminder>): String = reminders.joinToString("\n") { r ->
        listOf(
            r.id.toString(),
            URLEncoder.encode(r.title, "UTF-8"),
            r.date.toString(),
            r.time.toString(),
            r.repeat.name,
            r.completedDates.sorted().joinToString(","),
        ).joinToString("\t")
    }

    fun decode(text: String): List<Reminder> = text.lines().mapNotNull { line ->
        if (line.isBlank()) return@mapNotNull null
        try {
            val f = line.split("\t")
            if (f.size < 5) return@mapNotNull null
            Reminder(
                id = f[0].toLong(),
                title = URLDecoder.decode(f[1], "UTF-8"),
                date = LocalDate.parse(f[2]),
                time = LocalTime.parse(f[3]),
                repeat = Repeat.valueOf(f[4]),
                completedDates = f.getOrNull(5).orEmpty().split(",").filter { it.isNotBlank() }.map { LocalDate.parse(it) }.toSet(),
            )
        } catch (e: Exception) {
            null
        }
    }
}
