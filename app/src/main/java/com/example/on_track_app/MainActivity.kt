package com.example.on_track_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.setValue
import com.example.on_track_app.ui.activities.OnTrackApp
import com.example.on_track_app.ui.fragments.login.LoginScreen
import com.example.on_track_app.utils.SettingsDataStore
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    private val settings by lazy { SettingsDataStore(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (application as App).container
        val authClient = appContainer.googleAuthClient
        val user = Firebase.auth.currentUser
        var photoUrl by mutableStateOf(user?.photoUrl?.toString())

        lifecycleScope.launch {
            try {
                user?.reload()?.await()
                photoUrl = Firebase.auth.currentUser?.photoUrl?.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        setContent {
            val darkTheme by settings.darkThemeFlow.collectAsState(initial = false)
            val appViewModelFactory = appContainer.viewModelFactory
            var isLoggedIn by remember { mutableStateOf((authClient.getUserId()) != null) }
            if (isLoggedIn) {
                OnTrackApp(
                    darkTheme = darkTheme,
                    onToggleTheme = {
                        lifecycleScope.launch {
                            settings.setDarkTheme(!darkTheme)
                        }
                    },
                    factory = appViewModelFactory,
                    userPhotoUrl = photoUrl,
                    authClient = authClient
                )
            } else {
                LoginScreen(
                    onLoginSuccess = { isLoggedIn = true },
                    viewModelFactory = appViewModelFactory
                )
            }

        }
    }
}
