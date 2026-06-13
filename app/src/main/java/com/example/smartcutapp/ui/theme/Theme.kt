package com.example.smartcutapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import com.example.smartcutapp.app.ui.theme.SmartCutColors

val LocalDarkTheme = compositionLocalOf { false }

private val LightColorScheme = lightColorScheme(
    primary = SmartCutColors.Primary,
    onPrimary = SmartCutColors.OnPrimary,
    secondary = SmartCutColors.Secondary,
    background = SmartCutColors.BackgroundLight,
    surface = SmartCutColors.CardBackgroundLight,
    surfaceVariant = SmartCutColors.SurfaceLight,
    onBackground = SmartCutColors.TextPrimary,
    onSurface = SmartCutColors.TextPrimary,
    outline = SmartCutColors.Divider,
)

private val DarkColorScheme = darkColorScheme(
    primary = SmartCutColors.Primary,
    onPrimary = SmartCutColors.OnPrimary,
    secondary = SmartCutColors.Secondary,
    background = SmartCutColors.BackgroundDark,
    surface = SmartCutColors.CardBackgroundDark,
    surfaceVariant = SmartCutColors.SurfaceDark,
    onBackground = SmartCutColors.TextPrimaryDark,
    onSurface = SmartCutColors.TextPrimaryDark,
    outline = SmartCutColors.DividerDark,
)

@Composable
fun SmartCutAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            content = content
        )
    }
}
