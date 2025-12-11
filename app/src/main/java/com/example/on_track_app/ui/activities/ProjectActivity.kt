package com.example.on_track_app.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.on_track_app.OnTrackApp
import com.example.on_track_app.model.LocalConfigurations
import com.example.on_track_app.ui.navigation.Destinations
import com.example.on_track_app.ui.navigation.ProjectNavigation
import com.example.on_track_app.ui.navigation.routes
import com.example.on_track_app.ui.fragments.reusable.header.ProjectsHeader
import com.example.on_track_app.ui.theme.OnTrackAppTheme
import com.example.on_track_app.utils.LocalConfig
import com.example.on_track_app.utils.LocalViewModelFactory
import com.example.on_track_app.utils.SettingsDataStore
import kotlinx.coroutines.launch

class ProjectActivity : ComponentActivity() {
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
            val project = intent.getStringExtra("PROJECT")!!
            val projectId = intent.getStringExtra("PROJECT_ID")!!
            val darkTheme by settings.darkThemeFlow.collectAsState(initial = false)
            val context = LocalConfigurations(config.get()!!.userID,projectId)
            CompositionLocalProvider(LocalViewModelFactory provides factory, LocalConfig provides context) {
                Project(project = project, projectId = projectId,darkTheme = darkTheme,
                    onToggleTheme = {
                        lifecycleScope.launch {
                            settings.setDarkTheme(!darkTheme)
                        }
                    })
            }
        }
    }
}

@Composable
fun Project(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    project: String,
    projectId: String
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
            ProjectNavigation(navController,projectId)
        }


    }
}