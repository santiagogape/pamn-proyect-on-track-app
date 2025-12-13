package com.example.on_track_app.ui.fragments.navigable.projects



import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.on_track_app.ui.activities.ProjectActivity
import com.example.on_track_app.ui.fragments.reusable.cards.StaticCards
import com.example.on_track_app.utils.DebugLogcatLogger
import com.example.on_track_app.utils.DefaultConfig
import com.example.on_track_app.utils.LocalViewModelFactory
import com.example.on_track_app.viewModels.main.ProjectsViewModel


@Composable
fun ProjectsScreen(
) {
    val viewModelFactory = LocalViewModelFactory.current

    val viewModel: ProjectsViewModel = viewModel(factory = viewModelFactory)

    val context = LocalContext.current
    val default = DefaultConfig.current
    val text by viewModel.text.collectAsStateWithLifecycle()
    val items by viewModel.items.collectAsStateWithLifecycle()
    val projects by viewModel.projects(default.defaultProjectID).collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (items.isEmpty()){
            Text(text = text, style = MaterialTheme.typography.headlineSmall)
        } else {
            projects.forEach { DebugLogcatLogger.logMockProject(it) }
            StaticCards(projects) { projectId,projectName ->
                val intent = Intent(context, ProjectActivity::class.java)
                intent.putExtra("PROJECT", projectName)
                intent.putExtra("PROJECT_ID", projectId)
                context.startActivity(intent)
            }

        }
    }
}
