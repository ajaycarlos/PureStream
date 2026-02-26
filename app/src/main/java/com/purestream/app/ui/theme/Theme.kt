package com.purestream.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color // <-- Missing import added here
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Hardcoded Premium Dark Theme with "Stream" Blue Accents
private val PureStreamDarkColorScheme = darkColorScheme(
    primary = PureStreamAccent,
    onPrimary = Color.Black, // Dark text on the bright cyan accent for readability

    // Controls the highlight pill on the Bottom Navigation Bar
    secondaryContainer = PureStreamAccentFaint,
    onSecondaryContainer = PureStreamAccent,

    background = PureStreamDarkBackground,
    onBackground = PureStreamOnSurface,

    surface = PureStreamSurface,
    onSurface = PureStreamOnSurface,

    surfaceVariant = PureStreamSurfaceVariant,
    onSurfaceVariant = PureStreamOnSurfaceVariant
)

@Composable
fun PureStreamTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current

    // Forces the system notification bar and gesture bar to match the dark theme
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PureStreamDarkBackground.toArgb()
            window.navigationBarColor = PureStreamSurface.toArgb()

            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = PureStreamDarkColorScheme,
        typography = Typography,
        content = content
    )
}