package com.example.on_track_app.ui.fragments.navigable.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.unit.dp
import com.example.on_track_app.di.AppViewModelFactory
import com.example.on_track_app.model.Expandable
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.Task
import com.example.on_track_app.ui.fragments.dialogs.TaskCreation
import com.example.on_track_app.ui.fragments.reusable.cards.ExpandableCards
import com.example.on_track_app.domain.viewModels.main.ItemStatus
import com.example.on_track_app.domain.viewModels.main.TasksViewModel

@Composable
fun DashboardScreen(
    factory: AppViewModelFactory,
    projectId: String? = null
) {
    val viewModel: TasksViewModel = viewModel(factory = factory)

    val text by viewModel.text.collectAsStateWithLifecycle()
    LaunchedEffect(projectId) {
        viewModel.setProjectId(projectId)
    }

    val uiState by viewModel.tasks.collectAsStateWithLifecycle()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is ItemStatus.Loading -> {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            is ItemStatus.Error -> {
                Text("Something went wrong loading tasks.")
            }
            is ItemStatus.Success -> {
                if (state.elements.isEmpty()) {
                    Text(text = text, style = MaterialTheme.typography.headlineSmall)
                } else {
                    var taskToEdit by remember { mutableStateOf<Task?>(null) }
                    var showDialog by remember { mutableStateOf(false) }

                    ExpandableCards(
                        contents = state.elements,
                        onEditItem = { item ->
                            if (item is Task) {
                                taskToEdit = item
                                showDialog = true
                            }
                        },
                        onDeleteItem = { item ->
                            viewModel.deleteTask(item.id)
                        }
                    )

                    val uiState by viewModel.availableProjects.collectAsStateWithLifecycle()
                    var availableProjects = emptyList<Expandable>()
                    if (uiState is ItemStatus.Success) {
                        availableProjects = (uiState as ItemStatus.Success).elements
                    }

                    if (showDialog) {
                        TaskCreation(
                            isLoading = viewModel.isLoading(),
                            availableProjects = availableProjects as List<Project>,
                            existingTask = taskToEdit, // Pass the task to edit (or null if creating new)
                            onDismiss = {
                                showDialog = false
                                taskToEdit = null // Reset editing state
                            },
                            onSubmit = { name, desc, projId, date, h, m ->
                                if (taskToEdit == null) {
                                    // CREATE Logic
                                    viewModel.createTask(name, desc, date, h, m,projId)
                                } else {
                                    // EDIT Logic
                                    viewModel.updateTask(taskToEdit!!.id, name, desc, date, h, m, projId)
                                }
                                showDialog = false
                                taskToEdit = null
                            }
                        )
                    }
                }
            }
        }
    }
}
