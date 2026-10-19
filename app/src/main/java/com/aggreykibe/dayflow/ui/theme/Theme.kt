package com.aggreykibe.dayflow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Teal700, onPrimary = Mist50,
    primaryContainer = Teal100, onPrimaryContainer = Ink900,
    secondary = Amber500, onSecondary = Amber900,
    background = Mist50, onBackground = Ink900,
    surface = Mist50, onSurface = Ink900,
    surfaceVariant = Mist200, onSurfaceVariant = Ink900,
    error = ErrorRed,
)

private val DarkColors = darkColorScheme(
    primary = Teal300, onPrimary = Ink900,
    primaryContainer = Teal700, onPrimaryContainer = Mist50,
    secondary = Amber500, onSecondary = Amber900,
    background = NightBg, onBackground = Mist50,
    surface = NightBg, onSurface = Mist50,
    surfaceVariant = NightSurface, onSurfaceVariant = Mist50,
    error = ErrorRedDark,
)

@Composable
fun DayFlowTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = DayFlowTypography,
        content = content,
    )
}
