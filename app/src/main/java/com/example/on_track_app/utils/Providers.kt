package com.example.on_track_app.utils

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModelProvider
import com.example.on_track_app.model.LinkedType
import com.example.on_track_app.model.LocalConfigurations
import com.example.on_track_app.model.OwnerType
import com.example.on_track_app.ui.theme.ThemeExtensions

val LocalUserPFP = staticCompositionLocalOf<String?> { null }
val LocalThemeExtensions = staticCompositionLocalOf<ThemeExtensions> { error("them extensions not provided") }
val LocalViewModelFactory = staticCompositionLocalOf<ViewModelProvider.Factory> {
    error("No ViewModelFactory provided")
}

val LocalConfig = staticCompositionLocalOf<LocalConfigurations> {
    error("No LocalConfigurations provided")
}

// main activity -> context user y project default

val LocalOwnership = staticCompositionLocalOf<OwnershipContext> {
    error("No OwnershipContext provided")
}

val LocalReference = staticCompositionLocalOf<ReferenceContext?> {
    null
}

data class OwnershipContext(val userId:String, val currentProject:String?, val currentGroup:String?){
    fun owner(): String =  currentGroup ?: userId
    fun ownerType(): OwnerType = if (currentGroup == null) OwnerType.USER else OwnerType.GROUP
}
data class ReferenceContext(val linkTo:String, val type: LinkedType, val ownership: OwnershipContext)