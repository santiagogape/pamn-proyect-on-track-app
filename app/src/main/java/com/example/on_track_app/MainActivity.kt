package com.example.on_track_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.on_track_app.di.AppViewModelFactory
import com.example.on_track_app.navigation.AppNavigation
import com.example.on_track_app.ui.activities.OnTrackApp
import com.example.on_track_app.utils.SettingsDataStore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val settings by lazy { SettingsDataStore(this) }
    private val appContainer
        get() = (application as App).container
    private val appViewModelFactory = AppViewModelFactory(appContainer)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val darkTheme by settings.darkThemeFlow.collectAsState(initial = false)
            OnTrackApp(
                darkTheme = darkTheme,
                onToggleTheme = {
                    lifecycleScope.launch {
                        settings.setDarkTheme(!darkTheme)
                    }
                },
                factory = appViewModelFactory
            )
        }
    }
}
