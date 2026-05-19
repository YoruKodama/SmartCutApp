package com.example.smartcutapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.smartcutapp.app.ui.theme.SmartCutColors

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
fun SmartCutAppTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}