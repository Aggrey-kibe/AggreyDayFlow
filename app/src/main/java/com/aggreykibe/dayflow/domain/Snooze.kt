package com.aggreykibe.dayflow.domain

/**
 * After a restart, which saved snoozes should be armed again: only those that end in the future.
 * [saved] maps a reminder id to the moment its snooze ends (epoch milliseconds).
 */
fun snoozesToRearm(saved: Map<Long, Long>, nowMillis: Long): Map<Long, Long> =
    saved.filterValues { it > nowMillis }
