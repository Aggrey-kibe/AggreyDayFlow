package com.aggreykibe.dayflow.domain

import java.net.URLDecoder
import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalTime

/**
 * Plain-text storage format, one reminder per line, tab-separated:
 *  0 id, 1 title (URL-encoded), 2 date, 3 time, 4 repeat, 5 completed dates (comma-separated),
 *  6 message (URL-encoded), 7 category, 8 ring style, 9 vibrate (1/0), 10 snooze minutes,
 *  11 ring seconds, 12 sound id.
 * Fields 6 and up were added later: older saved lines have only 6 fields and decode with defaults,
 * so upgrading the app never loses reminders. Malformed lines are skipped so one bad line cannot lose the rest.
 */
object ReminderCodec {
    fun encode(reminders: List<Reminder>): String = reminders.joinToString("\n") { r ->
        listOf(
            r.id.toString(),
            enc(r.title),
            r.date.toString(),
            r.time.toString(),
            r.repeat.name,
            r.completedDates.sorted().joinToString(","),
            enc(r.message),
            r.category.name,
            r.ringStyle.name,
            if (r.vibrate) "1" else "0",
            r.snoozeMinutes.toString(),
            r.ringSeconds.toString(),
            r.soundId,
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
                message = f.getOrNull(6)?.let { URLDecoder.decode(it, "UTF-8") }.orEmpty(),
                category = enumOr(f.getOrNull(7), Category.PERSONAL),
                ringStyle = enumOr(f.getOrNull(8), RingStyle.ALARM),
                vibrate = f.getOrNull(9) != "0",
                snoozeMinutes = f.getOrNull(10)?.toIntOrNull()?.coerceIn(1, 120) ?: DEFAULT_SNOOZE_MINUTES,
                ringSeconds = f.getOrNull(11)?.toIntOrNull()?.coerceIn(10, 900) ?: DEFAULT_RING_SECONDS,
                soundId = SoundIds.orDefault(f.getOrNull(12)),
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun enc(text: String): String = URLEncoder.encode(text, "UTF-8")

    private inline fun <reified E : Enum<E>> enumOr(name: String?, default: E): E =
        enumValues<E>().firstOrNull { it.name == name } ?: default
}
