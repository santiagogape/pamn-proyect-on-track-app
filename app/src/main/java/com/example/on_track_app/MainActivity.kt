package com.example.on_track_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.example.on_track_app.ui.activities.Main
import com.example.on_track_app.ui.fragments.login.LoginScreen
import com.example.on_track_app.utils.DebugLogcatLogger
import com.example.on_track_app.utils.LocalCreationContext
import com.example.on_track_app.utils.LocalOwnerContext
import com.example.on_track_app.utils.LocalReminderCreationContext
import com.example.on_track_app.utils.LocalUserPFP
import com.example.on_track_app.utils.LocalViewModelFactory
import com.example.on_track_app.utils.SettingsDataStore
import com.example.on_track_app.viewModels.UserCreationContext
import com.example.on_track_app.viewModels.UserOwnerContext
import com.example.on_track_app.viewModels.UserReminderCreationContext
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val settings by lazy { SettingsDataStore(this) }
    private val factory by lazy {
        (application as OnTrackApp).viewModelsFactory
    }

    private val localConfig by lazy {
        (application as OnTrackApp).localConfig
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
            val config by localConfig.config.collectAsState()
            val darkTheme by settings.darkThemeFlow.collectAsState(initial = false)
            if (config == null) {
                LoginScreen(
                    viewModel = loginViewModelFactory(),
                    onLoginSuccess = {}
                )
            } else {
                val current = config!!
                val pfp = (application as OnTrackApp)
                    .authClient
                    .getProfilePictureUrl()
                CompositionLocalProvider(
                    LocalViewModelFactory provides factory,
                    LocalOwnerContext provides UserOwnerContext(current.user),
                    LocalCreationContext provides UserCreationContext(current.user),
                    LocalReminderCreationContext provides UserReminderCreationContext(current.user),
                    LocalUserPFP provides pfp
                ) {
                    current.let { DebugLogcatLogger.logConfig(checkAuth()!!, "main") }
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
