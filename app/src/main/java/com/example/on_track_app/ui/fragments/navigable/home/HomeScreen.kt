package com.example.on_track_app.ui.fragments.navigable.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Expandable
import com.example.on_track_app.model.Task
import com.example.on_track_app.ui.fragments.dialogs.EditEvent
import com.example.on_track_app.ui.fragments.dialogs.EditTask
import com.example.on_track_app.ui.fragments.reusable.cards.SectionedExpandableCards
import com.example.on_track_app.utils.LocalViewModelFactory
import com.example.on_track_app.viewModels.main.HomeViewModel
import com.example.on_track_app.viewModels.main.ItemStatus

@Composable
fun HomeScreen(
) {
    val viewModel: HomeViewModel = viewModel(factory = LocalViewModelFactory.current)
    val text by viewModel.text.collectAsStateWithLifecycle()

    val events by viewModel.events.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var eventToEdit by remember { mutableStateOf<Event?>(null) }

    val state by viewModel.projects(null).collectAsStateWithLifecycle()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            events is ItemStatus.Loading || tasks is ItemStatus.Loading -> {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            }

            events is ItemStatus.Error || tasks is ItemStatus.Error -> {
            }

            events is ItemStatus.Success && tasks is ItemStatus.Success -> {
                val currentEvents = (events as ItemStatus.Success<List<Event>>).elements
                val currentTask = (tasks as ItemStatus.Success<List<Task>>).elements
                if (currentTask.isEmpty() && currentEvents.isEmpty()) {
                    Text(text = text, style = MaterialTheme.typography.headlineSmall)
                } else {
                    val contents: Map<String, List<Expandable>> = mapOf(
                        "Tasks" to currentTask,
                        "Events" to currentEvents
                    )

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
                                is Task -> viewModel.delete(item)
                                is Event -> viewModel.delete(item)
                            }

                        }
                    )
                }
            }
        }

        EditTask(taskToEdit, state, viewModel) { taskToEdit = null }
        EditEvent(eventToEdit, state, viewModel) { eventToEdit = null }
    }
}


