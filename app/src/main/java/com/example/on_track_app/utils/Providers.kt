package com.example.on_track_app.utils

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModelProvider
import com.example.on_track_app.ui.theme.ThemeExtensions
import com.example.on_track_app.viewModels.CreationContext
import com.example.on_track_app.viewModels.OwnerContext
import com.example.on_track_app.viewModels.ReminderCreationContext

val LocalUserPFP = staticCompositionLocalOf<String?> { null }
val LocalThemeExtensions = staticCompositionLocalOf<ThemeExtensions> { error("them extensions not provided") }
val LocalViewModelFactory = staticCompositionLocalOf<ViewModelProvider.Factory> {
    error("No ViewModelFactory provided")
}

// main activity -> context user y project default

val LocalOwnerContext = staticCompositionLocalOf<OwnerContext> {
    error("No ProjectCreationContext provided")
}

val LocalCreationContext = staticCompositionLocalOf<CreationContext> {
    error("No CreationContext provided")
}

val LocalReminderCreationContext = staticCompositionLocalOf<ReminderCreationContext>{
    error("No ReminderCreationContext provided")
}