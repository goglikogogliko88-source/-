package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MonadireDarkColorScheme = darkColorScheme(
    primary = HuntingGreenLight,
    onPrimary = ForestBlack,
    primaryContainer = ForestSurfaceVariant,
    onPrimaryContainer = TextPrimary,
    secondary = AccentGold,
    onSecondary = ForestBlack,
    secondaryContainer = ForestSurfaceVariant,
    onSecondaryContainer = AccentGoldLight,
    tertiary = EarthSand,
    onTertiary = ForestBlack,
    background = ForestBlack,
    onBackground = TextPrimary,
    surface = ForestDark,
    onSurface = TextPrimary,
    surfaceVariant = ForestSurface,
    onSurfaceVariant = TextSecondary,
    outline = ForestCardBorder,
    outlineVariant = ForestSurfaceVariant,
    error = AlertRed,
    onError = Color.White
)

@Composable
fun MonadireTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MonadireDarkColorScheme,
        typography = Typography,
        content = content
    )
}
