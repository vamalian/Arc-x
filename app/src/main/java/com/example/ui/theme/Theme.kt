package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ArcxColorScheme = darkColorScheme(
    primary = ArcxCyan,
    onPrimary = Color(0xFF03101C),
    primaryContainer = Color(0xFF00364F),
    onPrimaryContainer = ArcxCyanGlow,
    secondary = ArcxElectricBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0C2448),
    onSecondaryContainer = Color(0xFFD6E3FF),
    tertiary = ArcxQuantumPurple,
    onTertiary = Color.White,
    background = ArcxVoid,
    onBackground = ArcxTextPrimary,
    surface = ArcxDeepSurface,
    onSurface = ArcxTextPrimary,
    surfaceVariant = ArcxSurface,
    onSurfaceVariant = ArcxTextSecondary,
    outline = ArcxHudBorder,
    error = ArcxAlertRed,
    onError = Color.White
)

@Composable
fun ArcxTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ArcxColorScheme,
        typography = Typography,
        content = content
    )
}
