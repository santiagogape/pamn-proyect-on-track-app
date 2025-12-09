package com.example.on_track_app.utils

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModelProvider

val LocalViewModelFactory = staticCompositionLocalOf<ViewModelProvider.Factory> {
    error("No ViewModelFactory provided")
}