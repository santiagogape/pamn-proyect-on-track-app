package com.example.on_track_app.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.on_track_app.App
import com.example.on_track_app.R
import com.example.on_track_app.di.AppViewModelFactory
import com.example.on_track_app.di.DummyFactory
import com.example.on_track_app.model.Expandable
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.Reminder
import com.example.on_track_app.model.Task
import com.example.on_track_app.ui.fragments.dialogs.TaskCreation
import com.example.on_track_app.ui.fragments.reusable.cards.ExpandableCards
import com.example.on_track_app.ui.fragments.reusable.header.AgendaHeader
import com.example.on_track_app.ui.theme.OnTrackAppTheme
import com.example.on_track_app.utils.SettingsDataStore
import com.example.on_track_app.domain.viewModels.main.CalendarViewModel
import com.example.on_track_app.domain.viewModels.main.ItemStatus
import com.example.on_track_app.domain.viewModels.main.RemindersViewModel
import com.example.on_track_app.domain.viewModels.main.TasksViewModel
import com.example.on_track_app.model.Event
import com.example.on_track_app.ui.fragments.dialogs.EventCreation
import com.example.on_track_app.ui.fragments.reusable.cards.SectionedExpandableCards
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDate.now

class AgendaActivity : ComponentActivity() {
    private val settings by lazy { SettingsDataStore(this) }

    private val appContainer by lazy {
        (application as App).container
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val projectId = intent.getStringExtra("PROJECT_ID")
            val date = LocalDate.parse(intent.getStringExtra("LOCAL_DATE")!!)
            val darkTheme by settings.darkThemeFlow.collectAsState(initial = false)
            Agenda(
                darkTheme = darkTheme,{
                    lifecycleScope.launch {
                        settings.setDarkTheme(!darkTheme)
                    }
                },
                date = date,
                factory = appContainer.viewModelFactory,
                projectId = projectId
            )

        }
    }
}

@Composable
fun Agenda(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    date: LocalDate = now(),
    factory: AppViewModelFactory,
    projectId: String? = null
){
    var currentDate by remember { mutableStateOf(date) }

    val viewModel: CalendarViewModel = viewModel(factory = factory)

    LaunchedEffect(projectId) {
        viewModel.setProjectId(projectId)
    }

    val tasksToday by viewModel.tasksFor(currentDate)
        .collectAsStateWithLifecycle()

    val eventsToday by viewModel.eventsFor(currentDate).collectAsStateWithLifecycle()

    val remindersViewModel: RemindersViewModel = viewModel(factory = factory)
    val targetFlow = remember(projectId) {
        if (projectId != null) {
            remindersViewModel.projectReminders(projectId)
        } else {
            remindersViewModel.reminders
        }
    }

    val reminders by targetFlow.collectAsStateWithLifecycle()

    OnTrackAppTheme(darkTheme = darkTheme) {
        ActivityScaffold(
            factory = factory,
            header = {
                AgendaHeader(currentDate,darkTheme,onToggleTheme,reminders as List<Reminder>,null)
                     },
            footer = { NextPrev(
                { currentDate = currentDate.minusDays(1) },
                { currentDate = currentDate.plusDays(1) }
            ) }
        ){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (tasksToday.isEmpty() && eventsToday.isEmpty()){
                    Text(text = stringResource(R.string.no_tasks_today), style = MaterialTheme.typography.headlineSmall)
                } else {
                    val contents: Map<String, List<Expandable>> = mapOf(
                        "Tasks" to tasksToday,
                        "Events" to eventsToday
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

@Preview
@Composable
fun Prev(){
    OnTrackAppTheme(darkTheme = false) {
           Agenda(false,{}, factory = DummyFactory as AppViewModelFactory)
    }
}