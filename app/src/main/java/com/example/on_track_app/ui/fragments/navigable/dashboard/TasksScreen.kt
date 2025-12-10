package com.example.on_track_app.ui.fragments.navigable.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.unit.dp
import com.example.on_track_app.model.Expandable
import com.example.on_track_app.ui.fragments.reusable.cards.ExpandableCards
import com.example.on_track_app.viewModels.main.TasksViewModel

@Composable
fun DashboardScreen(
    viewModel: TasksViewModel = viewModel(),
    projectId: String? = null
) {
    val text by viewModel.text.collectAsStateWithLifecycle()
    val sourceFlow = remember(projectId) {
        projectId?.let { viewModel.byProject(it) } ?: viewModel.tasks
    }
    val items by sourceFlow.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (items.isEmpty()){
            Text(text = text, style = MaterialTheme.typography.headlineSmall)
        } else {
            ExpandableCards(items.map { object: Expandable {
                    override val name: String
                        get() = it.name
                    override val description: String
                        get() = it.description
                }
            })
        }
    }
}
