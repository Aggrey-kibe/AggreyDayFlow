package com.aggreykibe.dayflow.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Accessibility rules for every theme in light and dark mode. If someone changes a color and makes text
 * hard to read, this test fails and the build is rejected.
 */
class PaletteContrastTest {
    private val textMinimum = 4.5      // WCAG AA for normal text
    private val nonTextMinimum = 3.0   // WCAG AA for icons, dots and outlines

    private fun palettes(): List<Triple<String, String, PaletteSpec>> =
        ThemeCatalog.all.flatMap { t -> listOf(Triple(t.displayName, "light", t.light), Triple(t.displayName, "dark", t.dark)) }

    private fun check(failures: MutableList<String>, where: String, pair: String, fg: Long, bg: Long, minimum: Double) {
        val ratio = contrastRatio(fg, bg)
        if (ratio < minimum) failures += "$where  $pair  ${"%.2f".format(ratio)} < $minimum"
    }

    @Test fun everyThemeIdHasExactlyOneSpec() {
        assertEquals(ThemeId.values().size, ThemeCatalog.all.size)
        assertEquals(ThemeId.values().toSet(), ThemeCatalog.all.map { it.id }.toSet())
        assertEquals(ThemeCatalog.all.size, ThemeCatalog.all.map { it.displayName }.toSet().size)
        assertTrue("need at least seven themes", ThemeCatalog.all.size >= 7)
    }

    @Test fun allTextPairsMeetAA_inEveryThemeAndMode() {
        val failures = mutableListOf<String>()
        for ((name, mode, p) in palettes()) {
            val w = "$name/$mode"
            check(failures, w, "onPrimary/primary", p.onPrimary, p.primary, textMinimum)
            check(failures, w, "onPrimaryContainer/primaryContainer", p.onPrimaryContainer, p.primaryContainer, textMinimum)
            check(failures, w, "onSecondary/secondary", p.onSecondary, p.secondary, textMinimum)
            check(failures, w, "onSecondaryContainer/secondaryContainer", p.onSecondaryContainer, p.secondaryContainer, textMinimum)
            check(failures, w, "onBackground/background", p.onBackground, p.background, textMinimum)
            check(failures, w, "onSurfaceVariant/surfaceVariant", p.onSurfaceVariant, p.surfaceVariant, textMinimum)
            check(failures, w, "onBackground/surfaceVariant", p.onBackground, p.surfaceVariant, textMinimum)
            check(failures, w, "onError/error", p.onError, p.error, textMinimum)
            // text drawn in the accent or error color on the page and on cards
            check(failures, w, "primary/background", p.primary, p.background, textMinimum)
            check(failures, w, "primary/surfaceVariant", p.primary, p.surfaceVariant, textMinimum)
            check(failures, w, "error/background", p.error, p.background, textMinimum)
            check(failures, w, "error/surfaceVariant", p.error, p.surfaceVariant, textMinimum)
            // dialogs, pickers and containers
            for ((label, bg) in listOf("surfaceContainer" to p.surfaceContainer, "surfaceContainerHigh" to p.surfaceContainerHigh, "surfaceContainerHighest" to p.surfaceContainerHighest)) {
                check(failures, w, "onBackground/$label", p.onBackground, bg, textMinimum)
                check(failures, w, "onSurfaceVariant/$label", p.onSurfaceVariant, bg, textMinimum)
            }
        }
        assertTrue("Low-contrast text:\n" + failures.joinToString("\n"), failures.isEmpty())
    }

    @Test fun outlinesAreVisible() {
        val failures = mutableListOf<String>()
        for ((name, mode, p) in palettes()) check(failures, "$name/$mode", "outline/background", p.outline, p.background, nonTextMinimum)
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }

    @Test fun alarmScreenIsReadableInEveryTheme() {
        val failures = mutableListOf<String>()
        val white = 0xFFFFFFFFL
        for (t in ThemeCatalog.all) {
            val a = t.alarm
            check(failures, t.displayName, "white/alarmTop", white, a.top, textMinimum)
            check(failures, t.displayName, "white/alarmBottom", white, a.bottom, textMinimum)
            check(failures, t.displayName, "onAccent/accent", a.onAccent, a.accent, textMinimum)
            check(failures, t.displayName, "accent/alarmBottom (button visible)", a.accent, a.bottom, nonTextMinimum)
            check(failures, t.displayName, "accent/alarmTop (button visible)", a.accent, a.top, nonTextMinimum)
        }
        assertTrue("Alarm screen problems:\n" + failures.joinToString("\n"), failures.isEmpty())
    }

    @Test fun categoryDotsAreVisibleOnEverySurface() {
        val failures = mutableListOf<String>()
        for ((name, mode, p) in palettes()) {
            for (c in Category.values()) {
                val dot = if (mode == "light") CategoryColors.light(c) else CategoryColors.dark(c)
                check(failures, "$name/$mode", "$c/background", dot, p.background, nonTextMinimum)
                check(failures, "$name/$mode", "$c/surfaceVariant", dot, p.surfaceVariant, nonTextMinimum)
            }
        }
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }

    @Test fun categoryColorsAreAllDifferent() {
        assertEquals(Category.values().size, Category.values().map { CategoryColors.light(it) }.toSet().size)
        assertEquals(Category.values().size, Category.values().map { CategoryColors.dark(it) }.toSet().size)
    }

    @Test fun contrastFunctionMatchesKnownValues() {
        assertEquals(21.0, contrastRatio(0xFF000000L, 0xFFFFFFFFL), 0.01)
        assertEquals(1.0, contrastRatio(0xFF336699L, 0xFF336699L), 0.001)
        assertEquals(4.48, contrastRatio(0xFF777777L, 0xFFFFFFFFL), 0.02)   // the classic "just fails AA" grey
        assertEquals(contrastRatio(0xFF123456L, 0xFFFFEEDDL), contrastRatio(0xFFFFEEDDL, 0xFF123456L), 1e-9) // order does not matter
    }

    @Test fun themeModeResolvesDarkCorrectly() {
        assertEquals(true, ThemeMode.SYSTEM.isDark(true)); assertEquals(false, ThemeMode.SYSTEM.isDark(false))
        assertEquals(false, ThemeMode.LIGHT.isDark(true)); assertEquals(true, ThemeMode.DARK.isDark(false))
    }
}
