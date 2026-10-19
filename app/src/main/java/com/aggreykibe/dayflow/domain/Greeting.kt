package com.aggreykibe.dayflow.domain

enum class DayPart { MORNING, AFTERNOON, EVENING }

/** Pure function so it can be unit-tested without Android. [hour] is 0..23. */
fun dayPartFor(hour: Int): DayPart {
    require(hour in 0..23) { "hour must be 0..23, was $hour" }
    return when (hour) {
        in 0..11 -> DayPart.MORNING
        in 12..16 -> DayPart.AFTERNOON
        else -> DayPart.EVENING
    }
}
