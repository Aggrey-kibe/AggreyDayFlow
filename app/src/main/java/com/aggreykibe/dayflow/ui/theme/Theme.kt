package com.aggreykibe.dayflow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.aggreykibe.dayflow.domain.Category
import com.aggreykibe.dayflow.domain.CategoryColors
import com.aggreykibe.dayflow.domain.ThemeCatalog
import com.aggreykibe.dayflow.domain.ThemeId
import com.aggreykibe.dayflow.domain.ThemeMode

/** Colors that are not part of Material's color scheme: the dot color for each reminder category. */
@Immutable
class DayFlowExtras(private val categoryColors: Map<Category, Color>) {
    fun categoryColor(category: Category): Color = categoryColors[category] ?: Color.Gray
}

fun extrasFor(dark: Boolean): DayFlowExtras = DayFlowExtras(
    Category.values().associateWith { Color(if (dark) CategoryColors.dark(it) else CategoryColors.light(it)) },
)

val LocalDayFlowExtras = compositionLocalOf { extrasFor(dark = false) }

/** Builds the Material color scheme for any theme in light or dark mode. Used by the app and by the theme preview. */
fun colorSchemeFor(id: ThemeId, dark: Boolean): ColorScheme {
    val spec = ThemeCatalog.get(id)
    val p = if (dark) spec.dark else spec.light
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = Color(p.primary), onPrimary = Color(p.onPrimary),
        primaryContainer = Color(p.primaryContainer), onPrimaryContainer = Color(p.onPrimaryContainer),
        secondary = Color(p.secondary), onSecondary = Color(p.onSecondary),
        secondaryContainer = Color(p.secondaryContainer), onSecondaryContainer = Color(p.onSecondaryContainer),
        background = Color(p.background), onBackground = Color(p.onBackground),
        surface = Color(p.background), onSurface = Color(p.onBackground),
        surfaceVariant = Color(p.surfaceVariant), onSurfaceVariant = Color(p.onSurfaceVariant),
        surfaceContainer = Color(p.surfaceContainer),
        surfaceContainerHigh = Color(p.surfaceContainerHigh),
        surfaceContainerHighest = Color(p.surfaceContainerHighest),
        outline = Color(p.outline),
        error = Color(p.error), onError = Color(p.onError),
        surfaceTint = Color(p.primary),
    )
}

@Composable
fun DayFlowTheme(
    theme: ThemeId = ThemeId.OCEAN_CALM,
    mode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = mode.isDark(isSystemInDarkTheme())
    val scheme = remember(theme, dark) { colorSchemeFor(theme, dark) }
    val extras = remember(dark) { extrasFor(dark) }
    CompositionLocalProvider(LocalDayFlowExtras provides extras) {
        MaterialTheme(colorScheme = scheme, typography = DayFlowTypography, content = content)
    }
}
