package com.mrmustard.activelistening.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ActivePurple80,
    onPrimary = ActiveText,
    secondary = ActivePurpleGrey80,
    tertiary = ActivePurpleLight,
    background = ActiveBackgroundDark,
    onBackground = ActiveSurface,
    surface = ActiveSurfaceDark,
    onSurface = ActiveSurface,
    surfaceVariant = ActiveSurfaceDark,
    onSurfaceVariant = ActivePurpleGrey80,
    outline = ActiveOutline,
)

private val LightColorScheme = lightColorScheme(
    primary = ActivePurple,
    onPrimary = ActiveSurface,
    primaryContainer = ActivePurple,
    onPrimaryContainer = ActiveSurface,
    secondary = ActivePurpleLight,
    onSecondary = ActiveSurface,
    secondaryContainer = ActiveBackground,
    onSecondaryContainer = ActiveText,
    tertiary = ActivePurpleDark,
    background = ActiveBackground,
    onBackground = ActiveText,
    surface = ActiveSurface,
    onSurface = ActiveText,
    surfaceVariant = ActiveBackground,
    onSurfaceVariant = ActiveTextVariant,
    outline = ActiveOutline,
    outlineVariant = ActiveOutline,
    error = Color(0xFFBA1A1A),
    onError = ActiveSurface,
)

@Composable
fun ActiveListeningTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        darkTheme && dynamicColor -> DarkColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
