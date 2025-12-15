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
import com.example.on_track_app.di.AppViewModelFactory
import com.example.on_track_app.ui.activities.ProjectActivity
import com.example.on_track_app.ui.fragments.reusable.cards.StaticCards
import com.example.on_track_app.domain.viewModels.main.ItemStatus
import com.example.on_track_app.domain.viewModels.main.ProjectsViewModel


@Composable
fun ProjectsScreen(
    factory: AppViewModelFactory
) {
    val viewModel: ProjectsViewModel = viewModel(factory = factory)
    val context = LocalContext.current

    val uiState by viewModel.projects.collectAsStateWithLifecycle()
    val text by viewModel.text.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // 2. Switch on the state type
        when (val state = uiState) {

            is ItemStatus.Loading -> {
                CircularProgressIndicator()
            }

            is ItemStatus.Error -> {
                Text("Something went wrong loading projects.")
            }

            is ItemStatus.Success -> {
                // Now we check if the successfully loaded list is empty
                if (state.elements.isEmpty()) {
                    Text(text = text, style = MaterialTheme.typography.headlineSmall)
                } else {
                    StaticCards(state.elements) { project ->
                        val intent = Intent(context, ProjectActivity::class.java)
                        intent.putExtra("PROJECT", project.name)
                        intent.putExtra("PROJECT_ID", project.id)
                        context.startActivity(intent)
                    }
                }
            }
        }
    }
}
