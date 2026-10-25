package com.aggreykibe.dayflow.domain

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** The steps an alarm goes through. Each one that happens (or fails) is recorded so a missing step shows up. */
object AlarmStage {
    const val SCHEDULED = "SCHEDULED"                  // an alarm was handed to Android for a future time
    const val SCHEDULE_SKIPPED = "SCHEDULE_SKIPPED"    // nothing to arm (already past or completed)
    const val RECEIVER_FIRED = "RECEIVER_FIRED"        // Android delivered the alarm to the app
    const val RECEIVER_NO_REMINDER = "RECEIVER_NO_REMINDER"
    const val SERVICE_REQUESTED = "SERVICE_REQUESTED"  // the app asked Android to start the ringing service
    const val SERVICE_REFUSED = "SERVICE_REFUSED"      // Android refused
    const val SERVICE_STARTED = "SERVICE_STARTED"      // the ringing service is running in the foreground
    const val FOREGROUND_FAILED = "FOREGROUND_FAILED"
    const val SOUND_STARTED = "SOUND_STARTED"
    const val SOUND_FALLBACK = "SOUND_FALLBACK"
    const val SOUND_FAILED = "SOUND_FAILED"
    const val VIBRATING = "VIBRATING"
    const val NOTIFICATION_POSTED = "NOTIFICATION_POSTED"
    const val NOTIFICATION_BLOCKED = "NOTIFICATION_BLOCKED"
    const val SCREEN_SHOWN = "SCREEN_SHOWN"            // the full-screen alarm screen opened
    const val USER_DISMISS = "USER_DISMISS"
    const val USER_SNOOZE = "USER_SNOOZE"
    const val USER_DONE = "USER_DONE"
    const val RING_TIMEOUT = "RING_TIMEOUT"
    const val REARMED = "REARMED"                      // all alarms re-armed after a restart, update or clock change
    const val TEST_SCHEDULED = "TEST_SCHEDULED"
}

data class LogEntry(val atMillis: Long, val stage: String, val detail: String)

/** Stores the event log as plain text, capped so it can never grow without limit. */
object AlarmLogCodec {
    const val MAX_ENTRIES = 80
    private const val MAX_DETAIL = 160

    fun clean(text: String): String = text.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ').take(MAX_DETAIL)

    fun encode(entries: List<LogEntry>): String =
        entries.joinToString("\n") { "${it.atMillis}\t${it.stage}\t${clean(it.detail)}" }

    fun decode(text: String): List<LogEntry> = text.lines().mapNotNull { line ->
        val f = line.split("\t", limit = 3)
        val at = f.getOrNull(0)?.toLongOrNull() ?: return@mapNotNull null
        val stage = f.getOrNull(1)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        LogEntry(at, stage, f.getOrElse(2) { "" })
    }

    /** Adds [entry] and keeps only the newest [MAX_ENTRIES]. */
    fun append(existing: List<LogEntry>, entry: LogEntry): List<LogEntry> =
        (existing + entry.copy(detail = clean(entry.detail))).takeLast(MAX_ENTRIES)

    fun formatTime(atMillis: Long, zone: ZoneId): String =
        DateTimeFormatter.ofPattern("MMM d HH:mm:ss", Locale.ENGLISH).withZone(zone).format(Instant.ofEpochMilli(atMillis))
}
