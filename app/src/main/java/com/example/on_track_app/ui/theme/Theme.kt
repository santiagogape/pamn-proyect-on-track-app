package com.example.on_track_app.ui.theme

import android.os.Build
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults.colors
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
    outline = DarkPink,
    surfaceVariant = LightPink.copy(alpha = 0.4f)
)

private val DarkColorScheme = darkColorScheme(
    primary = Black,
    onPrimary = LightPink,
    background = Bourbon,
    onBackground = DarkPink,
    surface = Black,
    onSurface = DarkPink,
    outline = LightPink,
    surfaceVariant = Bourbon.copy(alpha = 0.4f)
)


@Composable
fun OutlinedTextFieldColors(
    textField: @Composable ((TextFieldColors)->Unit),
    ){
    val colors = colors(
            focusedBorderColor = MaterialTheme.colorScheme.outline,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            cursorColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = MaterialTheme.colorScheme.onSurface,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    textField(colors)
}

@Composable
fun ButtonColors(
    button: @Composable ((ButtonColors)->Unit)
){
    val buttonColors = ButtonColors(
        containerColor = MaterialTheme.colorScheme.onPrimary,
        contentColor = MaterialTheme.colorScheme.background,
        disabledContainerColor = MaterialTheme.colorScheme.background,
        disabledContentColor = MaterialTheme.colorScheme.onBackground
    )
    button(buttonColors)
}

@Composable
fun ButtonColorsReverse(
    button: @Composable ((ButtonColors)->Unit)
){
    val buttonColors = ButtonColors(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        disabledContainerColor = MaterialTheme.colorScheme.primary,
        disabledContentColor = MaterialTheme.colorScheme.onPrimary,
    )
    button(buttonColors)
}

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
