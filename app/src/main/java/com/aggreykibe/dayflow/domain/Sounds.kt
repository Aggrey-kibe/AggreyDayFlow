package com.aggreykibe.dayflow.domain

// GENERATED from tools/sound-generator (recipes.py). Every sound is original: synthesized from math, no samples.

/** The mood groups shown in the sound picker. */
enum class SoundVibe { GENTLE, TRENDING, ENERGETIC, STRONG, CLASSIC }

data class SoundInfo(val id: String, val name: String, val vibe: SoundVibe)

object SoundCatalog {
    /** Bundled, offline sounds. Each id is also the name of its file in res/raw. */
    val bundled: List<SoundInfo> = listOf(
        SoundInfo("glass_drop", "Glass Drop", SoundVibe.GENTLE),
        SoundInfo("lofi_morning", "Lo-Fi Morning", SoundVibe.GENTLE),
        SoundInfo("moonlight", "Moonlight", SoundVibe.GENTLE),
        SoundInfo("cloud_nine", "Cloud Nine", SoundVibe.GENTLE),
        SoundInfo("dreamy_harp", "Dreamy Harp", SoundVibe.GENTLE),
        SoundInfo("velvet_keys", "Velvet Keys", SoundVibe.GENTLE),
        SoundInfo("mellow_drip", "Mellow Drip", SoundVibe.GENTLE),
        SoundInfo("kawaii_bells", "Kawaii Bells", SoundVibe.GENTLE),
        SoundInfo("golden_hour", "Golden Hour", SoundVibe.GENTLE),
        SoundInfo("bubble_pop", "Bubble Pop", SoundVibe.TRENDING),
        SoundInfo("crystal_ping", "Crystal Ping", SoundVibe.TRENDING),
        SoundInfo("soft_marimba", "Soft Marimba", SoundVibe.TRENDING),
        SoundInfo("sunrise_synth", "Sunrise Synth", SoundVibe.TRENDING),
        SoundInfo("starlight_arp", "Starlight Arp", SoundVibe.TRENDING),
        SoundInfo("hyper_pluck", "Hyper Pluck", SoundVibe.TRENDING),
        SoundInfo("glitch_pop", "Glitch Pop", SoundVibe.TRENDING),
        SoundInfo("neon_pulse", "Neon Pulse", SoundVibe.ENERGETIC),
        SoundInfo("retro_wave", "Retro Wave", SoundVibe.ENERGETIC),
        SoundInfo("pixel_quest", "Pixel Quest", SoundVibe.ENERGETIC),
        SoundInfo("trap_tick", "Trap Tick", SoundVibe.ENERGETIC),
        SoundInfo("digital_heartbeat", "Digital Heartbeat", SoundVibe.ENERGETIC),
        SoundInfo("pulse_drive", "Pulse Drive", SoundVibe.ENERGETIC),
        SoundInfo("power_up", "Power Up", SoundVibe.STRONG),
        SoundInfo("bass_drop", "Bass Drop Alert", SoundVibe.STRONG),
        SoundInfo("future_bass_rise", "Future Bass Rise", SoundVibe.STRONG),
        SoundInfo("wake_up_wave", "Wake-Up Wave", SoundVibe.STRONG),
        SoundInfo("hard_reset", "Hard Reset", SoundVibe.STRONG),
        SoundInfo("rocket_launch", "Rocket Launch", SoundVibe.STRONG),
        SoundInfo("chime", "Classic Chime", SoundVibe.CLASSIC),
        SoundInfo("bells", "Classic Bells", SoundVibe.CLASSIC),
        SoundInfo("beep", "Classic Beep", SoundVibe.CLASSIC),
        SoundInfo("rising", "Classic Rise", SoundVibe.CLASSIC),
    )

    fun find(id: String): SoundInfo? = bundled.firstOrNull { it.id == id }
}

object SoundIds {
    /** The phone's own alarm sound. */
    const val SYSTEM = "system"
    /** What new reminders use, and what plays if the chosen sound cannot be loaded. */
    const val DEFAULT = "glass_drop"
    val all: List<String> = listOf(SYSTEM) + SoundCatalog.bundled.map { it.id }

    fun orDefault(id: String?): String = if (id != null && id in all) id else DEFAULT
}
