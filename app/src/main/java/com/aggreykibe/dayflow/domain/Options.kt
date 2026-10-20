package com.aggreykibe.dayflow.domain

/** How a reminder repeats. WEEKLY repeats on the same weekday as its start date. */
enum class Repeat { ONCE, DAILY, WEEKLY }

enum class Category { PERSONAL, WORK, SCHOOL, FAMILY, EXERCISE, APPOINTMENT }

/** ALARM rings with sound and a full-screen alert; NOTIFICATION is a quiet notification only. */
enum class RingStyle { ALARM, NOTIFICATION }

/** Built-in sounds are bundled with the app and work offline. SYSTEM uses the phone's own alarm sound. */
object SoundIds {
    const val SYSTEM = "system"
    const val CHIME = "chime"
    const val BELLS = "bells"
    const val BEEP = "beep"
    const val RISING = "rising"
    val all = listOf(SYSTEM, CHIME, BELLS, BEEP, RISING)
    fun orDefault(id: String?): String = if (id != null && id in all) id else SYSTEM
}

val SNOOZE_CHOICES_MINUTES = listOf(5, 10, 15, 30)
val RING_CHOICES_SECONDS = listOf(30, 60, 120, 300)

const val DEFAULT_SNOOZE_MINUTES = 10
const val DEFAULT_RING_SECONDS = 120

/** The seven color themes. Their colors live in [ThemeCatalog]. */
enum class ThemeId { OCEAN_CALM, SUNRISE, LAVENDER_DREAM, FOREST_FOCUS, MIDNIGHT_PREMIUM, ROSE_GLOW, MINIMAL_MONOCHROME }

/** SYSTEM follows the phone's light/dark setting. */
enum class ThemeMode {
    SYSTEM, LIGHT, DARK;

    fun isDark(systemIsDark: Boolean): Boolean = when (this) {
        SYSTEM -> systemIsDark
        LIGHT -> false
        DARK -> true
    }
}

/** App-wide settings. Saved on the device. The alarm values are the defaults used to pre-fill new reminders. */
data class AppSettings(
    val ringStyle: RingStyle = RingStyle.ALARM,
    val vibrate: Boolean = true,
    val snoozeMinutes: Int = DEFAULT_SNOOZE_MINUTES,
    val ringSeconds: Int = DEFAULT_RING_SECONDS,
    val soundId: String = SoundIds.SYSTEM,
    val gradualVolume: Boolean = false,
    val theme: ThemeId = ThemeId.OCEAN_CALM,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)
