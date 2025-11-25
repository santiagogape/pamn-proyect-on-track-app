package com.example.on_track_app.ui.fragments.reusable.header

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.on_track_app.R


@Composable
fun ThemeToggleIconButton(
    darkTheme: Boolean,
    onToggle: () -> Unit
) {
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (darkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
            contentDescription = stringResource(R.string.theme_toggle)
        )
    }
}
