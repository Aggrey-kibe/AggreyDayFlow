package com.aggreykibe.dayflow.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GreetingTest {
    @Test fun midnightIsMorning() = assertEquals(DayPart.MORNING, dayPartFor(0))
    @Test fun elevenIsStillMorning() = assertEquals(DayPart.MORNING, dayPartFor(11))
    @Test fun noonIsAfternoon() = assertEquals(DayPart.AFTERNOON, dayPartFor(12))
    @Test fun fivePmIsEvening() = assertEquals(DayPart.EVENING, dayPartFor(17))
    @Test fun invalidHourIsRejected() {
        assertThrows(IllegalArgumentException::class.java) { dayPartFor(24) }
    }
}
