package com.example.on_track_app.utils

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModelProvider
import com.example.on_track_app.model.LocalConfigurations

val LocalViewModelFactory = staticCompositionLocalOf<ViewModelProvider.Factory> {
    error("No ViewModelFactory provided")
}

val LocalConfig = staticCompositionLocalOf<LocalConfigurations> {
    error("No LocalConfigurations provided")
}

val DefaultConfig = staticCompositionLocalOf<LocalConfigurations> {
    error("No LocalConfigurations provided")
}