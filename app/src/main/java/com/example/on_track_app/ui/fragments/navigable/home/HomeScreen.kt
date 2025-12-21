package com.example.on_track_app.ui.fragments.navigable.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.on_track_app.di.AppViewModelFactory
import com.example.on_track_app.di.DummyFactory
import com.example.on_track_app.model.Expandable
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.Task
import com.example.on_track_app.ui.fragments.dialogs.TaskCreation
import com.example.on_track_app.ui.fragments.reusable.cards.ExpandableCards
import com.example.on_track_app.ui.theme.OnTrackAppTheme
import com.example.on_track_app.domain.viewModels.main.HomeViewModel
import com.example.on_track_app.domain.viewModels.main.ItemStatus
import com.example.on_track_app.model.Event
import com.example.on_track_app.ui.fragments.dialogs.EventCreation
import com.example.on_track_app.ui.fragments.reusable.cards.SectionedExpandableCards

@Composable
fun HomeScreen(
    factory: AppViewModelFactory
) {
    val viewModel: HomeViewModel = viewModel(factory=factory)

    val text by viewModel.text.collectAsStateWithLifecycle()
    val uiState by viewModel.tasks.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()



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
                    val contents: Map<String, List<Expandable>> = mapOf(
                        "Tasks" to state.elements,
                        "Events" to events
                    )

                    var taskToEdit by remember { mutableStateOf<Task?>(null) }
                    var eventToEdit by remember { mutableStateOf<Event?>(null) }
                    var projectToEdit by remember { mutableStateOf<Project?>(null) }

                    SectionedExpandableCards(
                        groupedContents = contents,
                        onEditItem = { item ->
                            when(item) {
                                is Task -> {
                                    taskToEdit = item
                                }
                                is Event -> {
                                    eventToEdit = item
                                }
                            }
                        },
                        onDeleteItem = { item ->
                            when(item) {
                                is Task -> viewModel.deleteTask(item.id)
                                is Event -> viewModel.deleteEvent(item.id)
                            }

                        }
                    )

                    val uiState by viewModel.availableProjects.collectAsStateWithLifecycle()
                    var availableProjects = emptyList<Expandable>()
                    if (uiState is ItemStatus.Success) {
                        availableProjects = (uiState as ItemStatus.Success).elements
                    }

                    if (taskToEdit != null) {
                        TaskCreation(
                            isLoading = viewModel.isLoading(),
                            availableProjects = availableProjects as List<Project>,
                            existingTask = taskToEdit, // Pass the task to edit (or null if creating new)
                            onDismiss = {
                                taskToEdit = null // Reset editing state
                            },
                            onSubmit = { name, desc, projId, date, h, m ->
                                viewModel.updateTask(taskToEdit!!.id, name, desc, date, h, m, projId)
                                taskToEdit = null
                            }
                        )
                    }

                    if (eventToEdit != null) {
                        EventCreation(
                            isLoading = viewModel.isLoading(),
                            existingEvent = eventToEdit,
                            onDismiss = {
                                eventToEdit = null // Reset editing state
                            },
                            availableProjects = availableProjects as List<Project>,
                            onSubmit = { name, desc, projId, start, end ->
                                viewModel.updateEvent(eventToEdit!!.id, name, desc, projId, start, end)
                                eventToEdit = null
                            }
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ExpandableCardItemPreview() {
    OnTrackAppTheme(darkTheme = false) {
        HomeScreen(factory = DummyFactory as AppViewModelFactory)
    }
}