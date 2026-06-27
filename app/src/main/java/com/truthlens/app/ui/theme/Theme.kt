package com.truthlens.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary          = TruthLensPrimary,
    onPrimary        = TruthLensOnPrimary,
    background       = TruthLensBackground,
    surface          = TruthLensSurface,
    onBackground     = Color(0xFF1A1C1E),
    onSurface        = Color(0xFF1A1C1E),
    surfaceVariant   = Color(0xFFF1F3F4),
    onSurfaceVariant = Color(0xFF5F6368),
    outline          = Color(0xFFDADCE0),
    secondaryContainer = Color(0xFFE8F0FE),
    onSecondaryContainer = TruthLensPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary          = TruthLensPrimaryDark,
    onPrimary        = Color(0xFF003064),
    background       = TruthLensBackgroundDark,
    surface          = TruthLensSurfaceDark,
    onBackground     = Color(0xFFE3E2E6),
    onSurface        = Color(0xFFE3E2E6),
    surfaceVariant   = Color(0xFF3C3E40),
    onSurfaceVariant = Color(0xFFBFC1C3),
    outline          = Color(0xFF8A8C8E)
)

@Composable
fun TruthLensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TruthLensTypography,
        content = content
    )
}
