package com.autowalls.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AutoWallsColorScheme = darkColorScheme(
    primary = AutoWallsColors.Champagne,
    onPrimary = AutoWallsColors.Black,
    primaryContainer = AutoWallsColors.ChampagneDim,
    onPrimaryContainer = AutoWallsColors.TextPrimary,
    secondary = AutoWallsColors.Silver,
    onSecondary = AutoWallsColors.Black,
    background = AutoWallsColors.Black,
    onBackground = AutoWallsColors.TextPrimary,
    surface = AutoWallsColors.Black,
    onSurface = AutoWallsColors.TextPrimary,
    surfaceVariant = AutoWallsColors.Graphite,
    onSurfaceVariant = AutoWallsColors.TextSecondary,
    surfaceContainer = AutoWallsColors.Graphite,
    surfaceContainerHigh = AutoWallsColors.GraphiteElevated,
    outline = AutoWallsColors.Hairline,
    outlineVariant = AutoWallsColors.Hairline,
    error = AutoWallsColors.Error,
    onError = Color.Black
)

@Composable
fun AutoWallsTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = AutoWallsColorScheme,
        typography = AutoWallsTypography,
        content = content
    )
}
