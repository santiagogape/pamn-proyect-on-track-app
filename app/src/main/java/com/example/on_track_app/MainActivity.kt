package com.example.on_track_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.on_track_app.model.LocalConfigurations
import com.example.on_track_app.ui.activities.Main
import com.example.on_track_app.ui.fragments.login.LoginScreen
import com.example.on_track_app.utils.DebugLogcatLogger
import com.example.on_track_app.utils.LocalConfig
import com.example.on_track_app.utils.LocalOwnership
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

    private val ownershipContext by lazy {
        (application as OnTrackApp).currentOwnership
    }

    private val checkAuth by lazy {
        (application as OnTrackApp).authenticationCheck
    }

    private val loginViewModelFactory by lazy {
        (application as OnTrackApp).authViewModelFactory
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val darkTheme by settings.darkThemeFlow.collectAsState(initial = false)
            var auth by remember { mutableStateOf(checkAuth()) }
            if (auth == null) {
                LoginScreen(
                    viewModel = loginViewModelFactory(),
                    onLoginSuccess = {auth = checkAuth()}
                )
            } else {
                CompositionLocalProvider(
                    LocalViewModelFactory provides factory,
                    LocalConfig provides LocalConfigurations(config.get().userID),
                    LocalOwnership provides ownershipContext
                ) {
                    config.get().let { DebugLogcatLogger.logConfig(checkAuth()!!, "main") }
                    Main(
                        darkTheme = darkTheme,
                        onToggleTheme = {
                            lifecycleScope.launch {
                                settings.setDarkTheme(!darkTheme)
                            }
                        })
                }
            }
        }
    }
}
