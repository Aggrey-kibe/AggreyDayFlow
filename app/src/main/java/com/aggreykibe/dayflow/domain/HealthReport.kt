package com.aggreykibe.dayflow.domain

import java.time.ZoneId

/** One line of the alarm health check. [ok] is true (fine), false (a problem) or null (just information). */
data class HealthCheck(val label: String, val ok: Boolean?, val detail: String)

object HealthReport {
    fun statusWord(ok: Boolean?): String = when (ok) {
        true -> "OK"
        false -> "PROBLEM"
        null -> "INFO"
    }

    /** A plain-text report the person can copy and send. It contains no reminder titles, messages or contacts. */
    fun build(
        header: List<String>,
        checks: List<HealthCheck>,
        exits: List<String>,
        events: List<LogEntry>,
        zone: ZoneId,
        nowMillis: Long,
    ): String = buildString {
        appendLine("AGGREY DAYFLOW ALARM REPORT")
        header.forEach { appendLine(it) }
        appendLine()
        appendLine("CHECKS")
        checks.forEach { appendLine("[${statusWord(it.ok)}] ${it.label}: ${it.detail}") }
        appendLine()
        appendLine("WHY ANDROID LAST STOPPED THE APP (newest first)")
        if (exits.isEmpty()) appendLine("none recorded") else exits.forEach { appendLine(it) }
        appendLine()
        appendLine("ALARM EVENTS (oldest first)")
        if (events.isEmpty()) {
            appendLine("none recorded yet")
        } else {
            events.takeLast(40).forEach { appendLine("${AlarmLogCodec.formatTime(it.atMillis, zone)}  ${it.stage}  ${it.detail}") }
        }
        appendLine()
        append("Report made: ${AlarmLogCodec.formatTime(nowMillis, zone)}")
    }
}
