package com.aggreykibe.dayflow.domain

// GENERATED from a verified palette table: every text/background pair in every theme and mode meets
// WCAG AA contrast (4.5:1), checked by PaletteContrastTest. Colors are 0xAARRGGBB longs so this file stays
// plain Kotlin (no Compose imports) and can be unit-tested on any computer.

/** One complete color set (a theme in light mode, or in dark mode). */
data class PaletteSpec(
    val primary: Long, val onPrimary: Long,
    val primaryContainer: Long, val onPrimaryContainer: Long,
    val secondary: Long, val onSecondary: Long,
    val secondaryContainer: Long, val onSecondaryContainer: Long,
    val background: Long, val onBackground: Long,
    val surfaceVariant: Long, val onSurfaceVariant: Long,
    val surfaceContainer: Long, val surfaceContainerHigh: Long, val surfaceContainerHighest: Long,
    val outline: Long, val error: Long, val onError: Long,
)

/** Colors for the full-screen alarm: a calm two-stop background, white text, and one bright action color. */
data class AlarmPalette(val top: Long, val bottom: Long, val accent: Long, val onAccent: Long)

data class ThemeSpec(
    val id: ThemeId,
    val displayName: String,
    val light: PaletteSpec,
    val dark: PaletteSpec,
    val alarm: AlarmPalette,
)

object ThemeCatalog {
    val all: List<ThemeSpec> = listOf(
        ThemeSpec(
            id = ThemeId.OCEAN_CALM,
            displayName = "Ocean Calm",
            light = PaletteSpec(
            primary = 0xFF0E6B7CL, onPrimary = 0xFFFFFFFFL,
            primaryContainer = 0xFFCBE9EFL, onPrimaryContainer = 0xFF0A2E36L,
            secondary = 0xFF1F5FA8L, onSecondary = 0xFFFFFFFFL,
            secondaryContainer = 0xFFD3E4F7L, onSecondaryContainer = 0xFF0B2A4AL,
            background = 0xFFF4FAFBL, onBackground = 0xFF0F2A30L,
            surfaceVariant = 0xFFDCEBEEL, onSurfaceVariant = 0xFF0F2A30L,
            surfaceContainer = 0xFFEDF6F7L, surfaceContainerHigh = 0xFFE7F2F4L, surfaceContainerHighest = 0xFFDCEBEEL,
            outline = 0xFF76888BL, error = 0xFFB3261EL, onError = 0xFFFFFFFFL,
        ),
            dark = PaletteSpec(
            primary = 0xFF6FD0E0L, onPrimary = 0xFF00252CL,
            primaryContainer = 0xFF0E5563L, onPrimaryContainer = 0xFFD6F3F8L,
            secondary = 0xFF8EC5FFL, onSecondary = 0xFF00315CL,
            secondaryContainer = 0xFF1F4A78L, onSecondaryContainer = 0xFFD6E9FFL,
            background = 0xFF0C1A1DL, onBackground = 0xFFE6F3F5L,
            surfaceVariant = 0xFF16292DL, onSurfaceVariant = 0xFFE6F3F5L,
            surfaceContainer = 0xFF0F1E22L, surfaceContainerHigh = 0xFF122226L, surfaceContainerHighest = 0xFF16292DL,
            outline = 0xFF849194L, error = 0xFFF2B8B5L, onError = 0xFF601410L,
        ),
            alarm = AlarmPalette(top = 0xFF0E6B7CL, bottom = 0xFF06262DL, accent = 0xFF7FE3F0L, onAccent = 0xFF062B33L),
        ),
        ThemeSpec(
            id = ThemeId.SUNRISE,
            displayName = "Sunrise",
            light = PaletteSpec(
            primary = 0xFFB34700L, onPrimary = 0xFFFFFFFFL,
            primaryContainer = 0xFFFFDCC2L, onPrimaryContainer = 0xFF3A1A00L,
            secondary = 0xFF8A5200L, onSecondary = 0xFFFFFFFFL,
            secondaryContainer = 0xFFFCE7B8L, onSecondaryContainer = 0xFF3B2600L,
            background = 0xFFFFF8F1L, onBackground = 0xFF3A2112L,
            surfaceVariant = 0xFFFBE6D4L, onSurfaceVariant = 0xFF3A2112L,
            surfaceContainer = 0xFFFEF3E8L, surfaceContainerHigh = 0xFFFDEEE1L, surfaceContainerHighest = 0xFFFBE6D4L,
            outline = 0xFF938276L, error = 0xFFB3261EL, onError = 0xFFFFFFFFL,
        ),
            dark = PaletteSpec(
            primary = 0xFFFFB784L, onPrimary = 0xFF4A2000L,
            primaryContainer = 0xFF8A3A00L, onPrimaryContainer = 0xFFFFE6D3L,
            secondary = 0xFFFFCB6BL, onSecondary = 0xFF3F2A00L,
            secondaryContainer = 0xFF6B4800L, onSecondaryContainer = 0xFFFFE9B8L,
            background = 0xFF1F140CL, onBackground = 0xFFFFEDDDL,
            surfaceVariant = 0xFF33231AL, onSurfaceVariant = 0xFFFFEDDDL,
            surfaceContainer = 0xFF251810L, surfaceContainerHigh = 0xFF2A1C14L, surfaceContainerHighest = 0xFF33231AL,
            outline = 0xFF9A8B7FL, error = 0xFFF2B8B5L, onError = 0xFF601410L,
        ),
            alarm = AlarmPalette(top = 0xFFB34700L, bottom = 0xFF3B1A06L, accent = 0xFFFFC36BL, onAccent = 0xFF3B2200L),
        ),
        ThemeSpec(
            id = ThemeId.LAVENDER_DREAM,
            displayName = "Lavender Dream",
            light = PaletteSpec(
            primary = 0xFF6B46C1L, onPrimary = 0xFFFFFFFFL,
            primaryContainer = 0xFFE4D9FBL, onPrimaryContainer = 0xFF26104FL,
            secondary = 0xFF7B3FA8L, onSecondary = 0xFFFFFFFFL,
            secondaryContainer = 0xFFF0DAFBL, onSecondaryContainer = 0xFF3A0A5EL,
            background = 0xFFFAF7FFL, onBackground = 0xFF231A33L,
            surfaceVariant = 0xFFEAE3F7L, onSurfaceVariant = 0xFF231A33L,
            surfaceContainer = 0xFFF5F1FDL, surfaceContainerHigh = 0xFFF1ECFBL, surfaceContainerHighest = 0xFFEAE3F7L,
            outline = 0xFF847D8FL, error = 0xFFB3261EL, onError = 0xFFFFFFFFL,
        ),
            dark = PaletteSpec(
            primary = 0xFFC7B0FFL, onPrimary = 0xFF2E1668L,
            primaryContainer = 0xFF4B2B96L, onPrimaryContainer = 0xFFEADFFFL,
            secondary = 0xFFE0B3FFL, onSecondary = 0xFF3B0A5EL,
            secondaryContainer = 0xFF5B2680L, onSecondaryContainer = 0xFFF3DDFFL,
            background = 0xFF17121FL, onBackground = 0xFFEFE8FBL,
            surfaceVariant = 0xFF261E33L, onSurfaceVariant = 0xFFEFE8FBL,
            surfaceContainer = 0xFF1C1625L, surfaceContainerHigh = 0xFF1F192AL, surfaceContainerHighest = 0xFF261E33L,
            outline = 0xFF8E8898L, error = 0xFFF2B8B5L, onError = 0xFF601410L,
        ),
            alarm = AlarmPalette(top = 0xFF6B46C1L, bottom = 0xFF1E1138L, accent = 0xFFD9C2FFL, onAccent = 0xFF2A1559L),
        ),
        ThemeSpec(
            id = ThemeId.FOREST_FOCUS,
            displayName = "Forest Focus",
            light = PaletteSpec(
            primary = 0xFF2F6B3AL, onPrimary = 0xFFFFFFFFL,
            primaryContainer = 0xFFD3E8CFL, onPrimaryContainer = 0xFF10301AL,
            secondary = 0xFF596A1FL, onSecondary = 0xFFFFFFFFL,
            secondaryContainer = 0xFFE3EBC2L, onSecondaryContainer = 0xFF283100L,
            background = 0xFFF7F8F2L, onBackground = 0xFF1C2A1FL,
            surfaceVariant = 0xFFE2E8DAL, onSurfaceVariant = 0xFF1C2A1FL,
            surfaceContainer = 0xFFF1F3EBL, surfaceContainerHigh = 0xFFEBEFE5L, surfaceContainerHighest = 0xFFE2E8DAL,
            outline = 0xFF7F877EL, error = 0xFFB3261EL, onError = 0xFFFFFFFFL,
        ),
            dark = PaletteSpec(
            primary = 0xFF8FD49BL, onPrimary = 0xFF0A3315L,
            primaryContainer = 0xFF24562FL, onPrimaryContainer = 0xFFD6F0D6L,
            secondary = 0xFFC5D86BL, onSecondary = 0xFF2A3300L,
            secondaryContainer = 0xFF4A5A12L, onSecondaryContainer = 0xFFEAF3B8L,
            background = 0xFF111A13L, onBackground = 0xFFE6EFE2L,
            surfaceVariant = 0xFF1C2A20L, onSurfaceVariant = 0xFFE6EFE2L,
            surfaceContainer = 0xFF141F17L, surfaceContainerHigh = 0xFF17231AL, surfaceContainerHighest = 0xFF1C2A20L,
            outline = 0xFF868F85L, error = 0xFFF2B8B5L, onError = 0xFF601410L,
        ),
            alarm = AlarmPalette(top = 0xFF2F6B3AL, bottom = 0xFF0E2414L, accent = 0xFFB7E07AL, onAccent = 0xFF13280FL),
        ),
        ThemeSpec(
            id = ThemeId.MIDNIGHT_PREMIUM,
            displayName = "Midnight Premium",
            light = PaletteSpec(
            primary = 0xFF1B2F6EL, onPrimary = 0xFFFFFFFFL,
            primaryContainer = 0xFFD9E2FFL, onPrimaryContainer = 0xFF0B1A45L,
            secondary = 0xFF0A6F8AL, onSecondary = 0xFFFFFFFFL,
            secondaryContainer = 0xFFC9ECF5L, onSecondaryContainer = 0xFF052F3AL,
            background = 0xFFF5F7FCL, onBackground = 0xFF101830L,
            surfaceVariant = 0xFFE1E6F3L, onSurfaceVariant = 0xFF101830L,
            surfaceContainer = 0xFFEFF2F9L, surfaceContainerHigh = 0xFFEAEEF7L, surfaceContainerHighest = 0xFFE1E6F3L,
            outline = 0xFF777C8CL, error = 0xFFB3261EL, onError = 0xFFFFFFFFL,
        ),
            dark = PaletteSpec(
            primary = 0xFF7DD3FCL, onPrimary = 0xFF002B3DL,
            primaryContainer = 0xFF1E3A8AL, onPrimaryContainer = 0xFFDCE6FFL,
            secondary = 0xFFB39DFFL, onSecondary = 0xFF20086BL,
            secondaryContainer = 0xFF3E2C85L, onSecondaryContainer = 0xFFE6DEFFL,
            background = 0xFF0A1022L, onBackground = 0xFFE8ECFAL,
            surfaceVariant = 0xFF141C38L, onSurfaceVariant = 0xFFE8ECFAL,
            surfaceContainer = 0xFF0D1429L, surfaceContainerHigh = 0xFF10172EL, surfaceContainerHighest = 0xFF141C38L,
            outline = 0xFF848999L, error = 0xFFF2B8B5L, onError = 0xFF601410L,
        ),
            alarm = AlarmPalette(top = 0xFF1B2F6EL, bottom = 0xFF060B1AL, accent = 0xFF7DD3FCL, onAccent = 0xFF05283AL),
        ),
        ThemeSpec(
            id = ThemeId.ROSE_GLOW,
            displayName = "Rose Glow",
            light = PaletteSpec(
            primary = 0xFFA02A52L, onPrimary = 0xFFFFFFFFL,
            primaryContainer = 0xFFFFD9E2L, onPrimaryContainer = 0xFF3A0A1DL,
            secondary = 0xFF6E2A5CL, onSecondary = 0xFFFFFFFFL,
            secondaryContainer = 0xFFF6D9EEL, onSecondaryContainer = 0xFF3A0F32L,
            background = 0xFFFFF7F8L, onBackground = 0xFF33141FL,
            surfaceVariant = 0xFFF6E1E5L, onSurfaceVariant = 0xFF33141FL,
            surfaceContainer = 0xFFFCF0F2L, surfaceContainerHigh = 0xFFFAEBEEL, surfaceContainerHighest = 0xFFF6E1E5L,
            outline = 0xFF8F7A81L, error = 0xFFB3261EL, onError = 0xFFFFFFFFL,
        ),
            dark = PaletteSpec(
            primary = 0xFFFFB1C6L, onPrimary = 0xFF5C0F2DL,
            primaryContainer = 0xFF86264AL, onPrimaryContainer = 0xFFFFD9E2L,
            secondary = 0xFFE8B0D8L, onSecondary = 0xFF43123AL,
            secondaryContainer = 0xFF6B2A5DL, onSecondaryContainer = 0xFFFAD9F0L,
            background = 0xFF1E1014L, onBackground = 0xFFFFE9EEL,
            surfaceVariant = 0xFF331C25L, onSurfaceVariant = 0xFFFFE9EEL,
            surfaceContainer = 0xFF241419L, surfaceContainerHigh = 0xFF2A171DL, surfaceContainerHighest = 0xFF331C25L,
            outline = 0xFF9A878CL, error = 0xFFF2B8B5L, onError = 0xFF601410L,
        ),
            alarm = AlarmPalette(top = 0xFFA02A52L, bottom = 0xFF3A0F20L, accent = 0xFFFFB6C9L, onAccent = 0xFF4A0E27L),
        ),
        ThemeSpec(
            id = ThemeId.MINIMAL_MONOCHROME,
            displayName = "Minimal Monochrome",
            light = PaletteSpec(
            primary = 0xFF1A1A1AL, onPrimary = 0xFFFFFFFFL,
            primaryContainer = 0xFFE0E0E0L, onPrimaryContainer = 0xFF141414L,
            secondary = 0xFF4A4A4AL, onSecondary = 0xFFFFFFFFL,
            secondaryContainer = 0xFFE6E6E6L, onSecondaryContainer = 0xFF1A1A1AL,
            background = 0xFFFAFAFAL, onBackground = 0xFF141414L,
            surfaceVariant = 0xFFE8E8E8L, onSurfaceVariant = 0xFF141414L,
            surfaceContainer = 0xFFF5F5F5L, surfaceContainerHigh = 0xFFF0F0F0L, surfaceContainerHighest = 0xFFE8E8E8L,
            outline = 0xFF7C7C7CL, error = 0xFFB3261EL, onError = 0xFFFFFFFFL,
        ),
            dark = PaletteSpec(
            primary = 0xFFEDEDEDL, onPrimary = 0xFF141414L,
            primaryContainer = 0xFF3A3A3AL, onPrimaryContainer = 0xFFF2F2F2L,
            secondary = 0xFFBDBDBDL, onSecondary = 0xFF141414L,
            secondaryContainer = 0xFF333333L, onSecondaryContainer = 0xFFEDEDEDL,
            background = 0xFF0F0F0FL, onBackground = 0xFFF2F2F2L,
            surfaceVariant = 0xFF1E1E1EL, onSurfaceVariant = 0xFFF2F2F2L,
            surfaceContainer = 0xFF141414L, surfaceContainerHigh = 0xFF171717L, surfaceContainerHighest = 0xFF1E1E1EL,
            outline = 0xFF8C8C8CL, error = 0xFFF2B8B5L, onError = 0xFF601410L,
        ),
            alarm = AlarmPalette(top = 0xFF2B2B2BL, bottom = 0xFF0A0A0AL, accent = 0xFFF2F2F2L, onAccent = 0xFF141414L),
        ),
    )

    fun get(id: ThemeId): ThemeSpec = all.first { it.id == id }
}

/** Dot colors that tell reminder categories apart. Light and dark sets are tuned for 3:1 against every theme surface. */
object CategoryColors {
    fun light(category: Category): Long = when (category) {
        Category.PERSONAL -> 0xFF0F766EL
        Category.WORK -> 0xFF1D4ED8L
        Category.SCHOOL -> 0xFF7E22CEL
        Category.FAMILY -> 0xFFBE185DL
        Category.EXERCISE -> 0xFF15803DL
        Category.APPOINTMENT -> 0xFFB45309L
    }

    fun dark(category: Category): Long = when (category) {
        Category.PERSONAL -> 0xFF5EEAD4L
        Category.WORK -> 0xFF93C5FDL
        Category.SCHOOL -> 0xFFD8B4FEL
        Category.FAMILY -> 0xFFF9A8D4L
        Category.EXERCISE -> 0xFF86EFACL
        Category.APPOINTMENT -> 0xFFFCD34DL
    }
}
