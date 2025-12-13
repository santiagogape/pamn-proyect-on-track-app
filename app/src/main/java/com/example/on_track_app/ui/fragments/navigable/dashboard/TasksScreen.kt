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
import com.example.on_track_app.utils.DefaultConfig
import com.example.on_track_app.utils.LocalViewModelFactory
import com.example.on_track_app.viewModels.main.TasksViewModel

@Composable
fun DashboardScreen(
    projectId: String? = null
) {
    val viewModelFactory = LocalViewModelFactory.current
    val config = DefaultConfig.current

    val viewModel: TasksViewModel = viewModel(factory = viewModelFactory)
    val text by viewModel.text.collectAsStateWithLifecycle()
    val sourceFlow = remember(projectId) {
        if (projectId != config.defaultProjectID) projectId?.let { viewModel.byProject(it) } ?: viewModel.tasks
        else viewModel.tasks
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
                override val id: String
                    get() = it.id
            }
            })
        }
    }
}
