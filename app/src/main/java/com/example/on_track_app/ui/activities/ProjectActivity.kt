package com.example.on_track_app.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.on_track_app.OnTrackApp
import com.example.on_track_app.ui.fragments.reusable.header.ProjectsHeader
import com.example.on_track_app.ui.navigation.Destinations
import com.example.on_track_app.ui.navigation.ProjectNavigation
import com.example.on_track_app.ui.navigation.routes
import com.example.on_track_app.ui.theme.OnTrackAppTheme
import com.example.on_track_app.utils.LocalCreationContext
import com.example.on_track_app.utils.LocalOwnerContext
import com.example.on_track_app.utils.LocalReminderCreationContext
import com.example.on_track_app.utils.LocalViewModelFactory
import com.example.on_track_app.utils.SettingsDataStore
import com.example.on_track_app.viewModels.GroupOwnerContext
import com.example.on_track_app.viewModels.GroupReminderCreationContext
import com.example.on_track_app.viewModels.ProjectCreationContext
import com.example.on_track_app.viewModels.UserOwnerContext
import com.example.on_track_app.viewModels.UserReminderCreationContext
import com.example.on_track_app.viewModels.raw.ProjectsViewModel
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
            val conf = config.get()
            val projectName = intent.getStringExtra("PROJECT")!!
            val projectId = intent.getStringExtra("PROJECT_ID")!!
            val groupId = intent.getStringExtra("GROUP_ID")
            val darkTheme by settings.darkThemeFlow.collectAsState(initial = false)


            val ownerContext = when{
                groupId != null -> GroupOwnerContext(groupId)
                else -> UserOwnerContext(conf.user)
            }
            val creationContext = ProjectCreationContext(
                ownerId = ownerContext.ownerId,
                projectId = projectId
            )
            val reminderContext = when(ownerContext){
                is GroupOwnerContext -> GroupReminderCreationContext(ownerContext.ownerId)
                is UserOwnerContext -> UserReminderCreationContext(conf.user)
            }

            CompositionLocalProvider(
                LocalViewModelFactory provides factory,
                LocalOwnerContext provides ownerContext,
                LocalCreationContext provides creationContext,
                LocalReminderCreationContext provides reminderContext
            ) {
                Project(
                    label = projectName,
                    darkTheme = darkTheme,
                    onToggleTheme = {
                        lifecycleScope.launch {
                            settings.setDarkTheme(!darkTheme)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun Project(
    label:String,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
) {
    val factory = LocalViewModelFactory.current
    val viewModel: ProjectsViewModel = viewModel(factory = factory)

    val creationContext = LocalCreationContext.current


    val project by creationContext.projectId!!.let { viewModel.liveProject(it).collectAsStateWithLifecycle() }
    OnTrackAppTheme(darkTheme = darkTheme) {
        val navController = rememberNavController()

        // Bottom navigation items
        val items = routes(listOf(
            Destinations.TASKS,
            Destinations.CALENDAR
        ))

        ActivityScaffold(
            header = {
                ProjectsHeader(project?.name ?: label,darkTheme,onToggleTheme)
                },
            footer = { NavBar(navController,items) },
        ){
            ProjectNavigation(navController)
        }


    }
}