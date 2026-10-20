package com.aggreykibe.dayflow.data

import android.content.Context
import com.aggreykibe.dayflow.domain.AppSettings
import com.aggreykibe.dayflow.domain.DEFAULT_RING_SECONDS
import com.aggreykibe.dayflow.domain.DEFAULT_SNOOZE_MINUTES
import com.aggreykibe.dayflow.domain.RingStyle
import com.aggreykibe.dayflow.domain.SoundIds
import com.aggreykibe.dayflow.domain.ThemeId
import com.aggreykibe.dayflow.domain.ThemeMode

/** App-wide defaults, saved on the device. Invalid or missing values fall back to safe defaults. */
class SettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): AppSettings = AppSettings(
        ringStyle = RingStyle.values().firstOrNull { it.name == prefs.getString(K_STYLE, null) } ?: RingStyle.ALARM,
        vibrate = prefs.getBoolean(K_VIBRATE, true),
        snoozeMinutes = prefs.getInt(K_SNOOZE, DEFAULT_SNOOZE_MINUTES).coerceIn(1, 120),
        ringSeconds = prefs.getInt(K_RING, DEFAULT_RING_SECONDS).coerceIn(10, 900),
        soundId = SoundIds.orDefault(prefs.getString(K_SOUND, null)),
        gradualVolume = prefs.getBoolean(K_GRADUAL, false),
        theme = ThemeId.values().firstOrNull { it.name == prefs.getString(K_THEME, null) } ?: ThemeId.OCEAN_CALM,
        themeMode = ThemeMode.values().firstOrNull { it.name == prefs.getString(K_MODE, null) } ?: ThemeMode.SYSTEM,
    )

    fun save(settings: AppSettings) {
        prefs.edit()
            .putString(K_STYLE, settings.ringStyle.name)
            .putBoolean(K_VIBRATE, settings.vibrate)
            .putInt(K_SNOOZE, settings.snoozeMinutes)
            .putInt(K_RING, settings.ringSeconds)
            .putString(K_SOUND, settings.soundId)
            .putBoolean(K_GRADUAL, settings.gradualVolume)
            .putString(K_THEME, settings.theme.name)
            .putString(K_MODE, settings.themeMode.name)
            .commit()
    }

    private companion object {
        const val PREFS_NAME = "dayflow_settings"
        const val K_STYLE = "ring_style"
        const val K_VIBRATE = "vibrate"
        const val K_SNOOZE = "snooze_minutes"
        const val K_RING = "ring_seconds"
        const val K_SOUND = "sound_id"
        const val K_GRADUAL = "gradual_volume"
        const val K_THEME = "theme"
        const val K_MODE = "theme_mode"
    }
}
