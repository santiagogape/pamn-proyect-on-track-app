package com.example.on_track_app.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.on_track_app.R
import com.example.on_track_app.navigation.Destinations
import com.example.on_track_app.navigation.ProjectNavigation
import com.example.on_track_app.navigation.routes
import com.example.on_track_app.ui.theme.OnTrackAppTheme
import com.example.on_track_app.utils.SettingsDataStore
import kotlinx.coroutines.launch

class ProjectActivity : ComponentActivity() {
    private val settings by lazy { SettingsDataStore(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val project = intent.getStringExtra("PROJECT")!!
            val projectId = intent.getStringExtra("PROJECT_ID")!!
            val darkTheme by settings.darkThemeFlow.collectAsState(initial = false)
            Project(project = project, projectId = projectId,darkTheme = darkTheme,
                onToggleTheme = {
                    lifecycleScope.launch {
                        settings.setDarkTheme(!darkTheme)
                    }
                })
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
            header = {Header(project,onToggleTheme,darkTheme)},
            footer = { NavBar(navController,items) }
        ){
            ProjectNavigation(navController,projectId)
        }


    }
}



@Composable
private fun Header(
    label: String,
    themeToggle: () -> Unit,
    darkTheme: Boolean
) {
    val activity = LocalActivity.current

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        IconButton(
            onClick = { activity?.finish() },
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.get_back)
            )
        }

        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground
        )
        IconButton(
            onClick = themeToggle,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = if (darkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                contentDescription = stringResource(R.string.theme_toggle)
            )
        }
    }
}