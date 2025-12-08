package com.example.on_track_app.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.on_track_app.App
import com.example.on_track_app.di.AppContainer
import com.example.on_track_app.di.AppViewModelFactory
import com.example.on_track_app.navigation.Destinations
import com.example.on_track_app.navigation.ProjectNavigation
import com.example.on_track_app.navigation.routes
import com.example.on_track_app.ui.fragments.reusable.header.ProjectsHeader
import com.example.on_track_app.ui.theme.OnTrackAppTheme
import com.example.on_track_app.utils.SettingsDataStore
import kotlinx.coroutines.launch

class ProjectActivity : ComponentActivity() {
    private val settings by lazy { SettingsDataStore(this) }

    private val appContainer by lazy {
        (application as App).container
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val project = intent.getStringExtra("PROJECT")!!
            val projectId = intent.getStringExtra("PROJECT_ID")!!
            val darkTheme by settings.darkThemeFlow.collectAsState(initial = false)
            val appViewModelFactory = appContainer.viewModelFactory
            Project(
                project = project,
                projectId = projectId,
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

@Composable
fun Project(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    project: String,
    projectId: String,
    factory: AppViewModelFactory
) {
    OnTrackAppTheme(darkTheme = darkTheme) {
        val navController = rememberNavController()

        // Bottom navigation items
        val items = routes(listOf(
            Destinations.TASKS,
            Destinations.NOTIFICATIONS
        ))

        ActivityScaffold(
            header = {
                ProjectsHeader(project,darkTheme,onToggleTheme,null)
                },
            footer = { NavBar(navController,items) }
        ){
            ProjectNavigation(navController, projectId, factory)
        }


    }
}