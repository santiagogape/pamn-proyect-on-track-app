package com.example.on_track_app.ui.theme

import android.os.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// ============================================================
// Colors -> Themes
// ============================================================

private val LightColorScheme = lightColorScheme(
    primary = White,
    onPrimary = DarkPink,
    background = LightPink,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    onSurfaceVariant = Black
)

private val DarkColorScheme = darkColorScheme(
    primary = Black,
    onPrimary = LightPink,
    background = Black,
    onBackground = DarkPink,
    surface = Black,
    onSurface = DarkPink,
)

// ============================================================
// Composable global
// ============================================================

@Composable
fun OnTrackAppTheme(
    darkTheme: Boolean = false, //isSystemInDarkTheme()
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Material global theme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
