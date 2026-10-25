package com.aggreykibe.dayflow.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

class AlarmLogTest {
    private val utc = ZoneId.of("UTC")

    @Test fun roundTripKeepsOrderAndText() {
        val list = listOf(LogEntry(1000, AlarmStage.SCHEDULED, "id=1 at 09:00"), LogEntry(2000, AlarmStage.RECEIVER_FIRED, "id=1"))
        assertEquals(list, AlarmLogCodec.decode(AlarmLogCodec.encode(list)))
    }

    @Test fun tabsAndNewlinesCannotBreakTheFormat() {
        val tricky = LogEntry(5, AlarmStage.SOUND_FAILED, "a\tb\nc\r\nd")
        val back = AlarmLogCodec.decode(AlarmLogCodec.encode(listOf(tricky)))
        assertEquals(1, back.size)
        assertFalse(back[0].detail.contains('\t') || back[0].detail.contains('\n'))
        assertEquals("a b c  d", back[0].detail)
    }

    @Test fun logIsCappedAndKeepsTheNewest() {
        var log = emptyList<LogEntry>()
        for (i in 1..(AlarmLogCodec.MAX_ENTRIES + 25)) log = AlarmLogCodec.append(log, LogEntry(i.toLong(), AlarmStage.SCHEDULED, "n$i"))
        assertEquals(AlarmLogCodec.MAX_ENTRIES, log.size)
        assertEquals((26L), log.first().atMillis)
        assertEquals((AlarmLogCodec.MAX_ENTRIES + 25).toLong(), log.last().atMillis)
    }

    @Test fun longDetailIsShortened() {
        val e = AlarmLogCodec.append(emptyList(), LogEntry(1, AlarmStage.SOUND_FAILED, "x".repeat(1000))).single()
        assertTrue(e.detail.length <= 160)
    }

    @Test fun badLinesAreSkipped() {
        val text = "garbage\n123\tSCHEDULED\tok\nnot-a-number\tX\ty\n456\t\tmissing stage\n789\tUSER_DISMISS"
        assertEquals(listOf(LogEntry(123, "SCHEDULED", "ok"), LogEntry(789, "USER_DISMISS", "")), AlarmLogCodec.decode(text))
    }

    @Test fun timeIsFormattedInTheGivenZone() {
        assertEquals("Jan 1 00:00:01", AlarmLogCodec.formatTime(1000, utc))
        assertEquals("Jan 1 03:00:01", AlarmLogCodec.formatTime(1000, ZoneId.of("Africa/Nairobi")))
    }

    @Test fun reportShowsEverySectionAndTheRightStatusWords() {
        val report = HealthReport.build(
            header = listOf("Phone: Tecno CK7n", "Android: 15 (API 35)"),
            checks = listOf(HealthCheck("Notifications", true, "Allowed"), HealthCheck("Battery", false, "Optimised"), HealthCheck("Exact alarms", null, "Not needed")),
            exits = listOf("Oct 21 20:01:00  USER_REQUESTED  force stop"),
            events = listOf(LogEntry(1000, AlarmStage.SCHEDULED, "id=1"), LogEntry(2000, AlarmStage.RECEIVER_FIRED, "id=1")),
            zone = utc, nowMillis = 3000,
        )
        for (expected in listOf("AGGREY DAYFLOW ALARM REPORT", "Phone: Tecno CK7n", "[OK] Notifications: Allowed", "[PROBLEM] Battery: Optimised",
            "[INFO] Exact alarms", "USER_REQUESTED", "SCHEDULED  id=1", "RECEIVER_FIRED  id=1", "Report made:")) {
            assertTrue("report is missing: $expected", report.contains(expected))
        }
        assertTrue(report.indexOf("SCHEDULED  id=1") < report.indexOf("RECEIVER_FIRED  id=1")) // oldest first
    }

    @Test fun emptyReportSaysSoInsteadOfLookingBroken() {
        val report = HealthReport.build(emptyList(), emptyList(), emptyList(), emptyList(), utc, 0)
        assertTrue(report.contains("none recorded yet"))
        assertTrue(report.contains("none recorded"))
    }
}
