package com.aggreykibe.dayflow.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.aggreykibe.dayflow.R
import com.aggreykibe.dayflow.domain.Category
import com.aggreykibe.dayflow.domain.SoundIds

@Composable
fun soundLabel(id: String): String = stringResource(
    when (id) {
        SoundIds.CHIME -> R.string.sound_chime
        SoundIds.BELLS -> R.string.sound_bells
        SoundIds.BEEP -> R.string.sound_beep
        SoundIds.RISING -> R.string.sound_rising
        else -> R.string.sound_system
    },
)

@Composable
fun categoryLabel(category: Category): String = stringResource(
    when (category) {
        Category.PERSONAL -> R.string.category_personal
        Category.WORK -> R.string.category_work
        Category.SCHOOL -> R.string.category_school
        Category.FAMILY -> R.string.category_family
        Category.EXERCISE -> R.string.category_exercise
        Category.APPOINTMENT -> R.string.category_appointment
    },
)

/** "5 min", "30 sec". Used for snooze and ring-duration choices. */
@Composable
fun durationLabelMinutes(minutes: Int): String = stringResource(R.string.duration_minutes, minutes)

@Composable
fun durationLabelSeconds(seconds: Int): String =
    if (seconds < 60) stringResource(R.string.duration_seconds, seconds)
    else stringResource(R.string.duration_minutes, seconds / 60)

@Composable
fun modeLabel(mode: com.aggreykibe.dayflow.domain.ThemeMode): String = stringResource(
    when (mode) {
        com.aggreykibe.dayflow.domain.ThemeMode.SYSTEM -> R.string.mode_system
        com.aggreykibe.dayflow.domain.ThemeMode.LIGHT -> R.string.mode_light
        com.aggreykibe.dayflow.domain.ThemeMode.DARK -> R.string.mode_dark
    },
)
