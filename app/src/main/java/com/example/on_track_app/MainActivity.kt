package com.example.on_track_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.example.on_track_app.ui.activities.Main
import com.example.on_track_app.utils.DebugLogcatLogger
import com.example.on_track_app.utils.DefaultConfig
import com.example.on_track_app.utils.LocalConfig
import com.example.on_track_app.utils.LocalViewModelFactory
import com.example.on_track_app.utils.SettingsDataStore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val settings by lazy { SettingsDataStore(this) }
    private val factory by lazy {
        (application as OnTrackApp).viewModelsFactory
    }

    private val config by lazy {
        (application as OnTrackApp).localConfig
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val darkTheme by settings.darkThemeFlow.collectAsState(initial = false)
            config.get()?.let { DebugLogcatLogger.logConfig(it, "main") }
            CompositionLocalProvider(LocalViewModelFactory provides factory, LocalConfig provides config.get()!!, DefaultConfig provides config.get()!!) {
                Main(darkTheme = darkTheme,
                    onToggleTheme = {
                        lifecycleScope.launch {
                            settings.setDarkTheme(!darkTheme)
                        }
                    })
            }
        }
    }
}
