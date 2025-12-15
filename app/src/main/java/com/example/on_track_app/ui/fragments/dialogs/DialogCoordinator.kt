package com.example.on_track_app.ui.fragments.dialogs

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.on_track_app.di.AppViewModelFactory
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Expandable
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.Task
import com.example.on_track_app.domain.viewModels.main.CalendarViewModel
import com.example.on_track_app.domain.viewModels.main.ProjectsViewModel
import com.example.on_track_app.domain.viewModels.main.TasksViewModel
import kotlinx.coroutines.launch
import com.example.on_track_app.domain.viewModels.main.ItemStatus
import com.example.on_track_app.domain.viewModels.main.RemindersViewModel


@Composable
fun GlobalDialogCoordinator(
    activeDialog: ActiveDialog,          // The current state
    onDismiss: () -> Unit,               // How to close the dialog
    snackbarHostState: SnackbarHostState, // To show success messages
    viewModelFactory: AppViewModelFactory
) {
    val scope = rememberCoroutineScope()
    when (activeDialog) {
        ActiveDialog.None -> { /* Do nothing */ }

        ActiveDialog.CreateTask -> {
            // We get the specific ViewModel here, keeping Scaffold clean
            val tasksViewModel: TasksViewModel = viewModel(factory = viewModelFactory)
            // Reusing your creation logic
            TaskCreationCoordinator(
                viewModel = tasksViewModel,
                onDismiss = onDismiss,
                onSuccess = {
                    onDismiss()
                    scope.launch {
                        snackbarHostState.showSnackbar("Task created successfully")
                    }
                }
            )
        }

        ActiveDialog.CreateEvent -> {
            val calendarViewModel: CalendarViewModel = viewModel(factory = viewModelFactory)

            EventCreationCoordinator(
                viewModel = calendarViewModel,
                onDismiss = onDismiss,
                onSuccess = {
                    onDismiss()
                    scope.launch {
                        snackbarHostState.showSnackbar("Event created successfully")
                    }
                }
            )
        }

        ActiveDialog.CreateProject -> {
            val projectsViewModel: ProjectsViewModel = viewModel(factory = viewModelFactory)

            ProjectCreationCoordinator(
                viewModel = projectsViewModel,
                onDismiss = onDismiss,
                onSuccess = {
                    onDismiss()
                    scope.launch {
                        snackbarHostState.showSnackbar("Project created successfully")
                    }
                }
            )
        }

        ActiveDialog.CreateReminder -> {
            val remindersViewModel: RemindersViewModel = viewModel(factory = viewModelFactory)

            ReminderCreationCoordinator(
                viewModel = remindersViewModel,
                onDismiss = onDismiss,
                onSuccess = {
                    onDismiss()
                    scope.launch {
                        snackbarHostState.showSnackbar("Reminder created successfully")
                    }
                }
            )
        }
    }
}

@Composable
private fun TaskCreationCoordinator(
    viewModel: TasksViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val status by viewModel.creationStatus.collectAsStateWithLifecycle()
    val uiState by viewModel.availableProjects.collectAsStateWithLifecycle()
    var availableProjects = emptyList<Expandable>()
    if (uiState is ItemStatus.Success) {
        availableProjects = (uiState as ItemStatus.Success).elements
    }

    LaunchedEffect(status) {
        if (status is CreationStatus.Success) {
            viewModel.resetStatus()
            onSuccess()
        }
    }

    TaskCreation(
        isLoading = status == CreationStatus.Loading,
        availableProjects = availableProjects as List<Project>,
        onDismiss = onDismiss,
        onSubmit = { name, desc, project, date, hour, min ->
            viewModel.createTask(name, desc, date, hour, min, project)
        }
    )
}

@Composable
private fun ProjectCreationCoordinator(
    viewModel: ProjectsViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val status by viewModel.creationStatus.collectAsStateWithLifecycle()

    LaunchedEffect(status) {
        if (status is CreationStatus.Success) {
            viewModel.resetStatus()
            onSuccess()
        }
    }

    ProjectCreation(
        isLoading = status == CreationStatus.Loading,
        onDismiss = onDismiss,
        onSubmit = { name, desc ->
            viewModel.createProject(name, desc)
        }
    )
}

@Composable
private fun ReminderCreationCoordinator(
    viewModel: RemindersViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val status by viewModel.creationStatus.collectAsStateWithLifecycle()

    val uiState by viewModel.availableTasks.collectAsStateWithLifecycle()
    var availableTasks = emptyList<Expandable>()
    if (uiState is ItemStatus.Success) {
        availableTasks = (uiState as ItemStatus.Success).elements
    }

    val availableEvents = emptyList<Expandable>()

    LaunchedEffect(status) {
        if (status is CreationStatus.Success) {
            viewModel.resetStatus()
            onSuccess()
        }
    }

    ReminderCreation(
        isLoading = status == CreationStatus.Loading,
        onDismiss = onDismiss,
        availableTasks = availableTasks as List<Task>,
        availableEvents = availableEvents as List<Event>,
        onSubmit = { name, desc, taskId, eventId, date, hour, minute ->
            viewModel.createReminder(name, desc, taskId, eventId, date, hour, minute)
        }
    )
}

@Composable
private fun EventCreationCoordinator(
    viewModel: CalendarViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val status by viewModel.creationStatus.collectAsStateWithLifecycle()

    LaunchedEffect(status) {
        if (status is CreationStatus.Success) {
            viewModel.resetStatus()
            onSuccess()
        }
    }

    EventCreation(
        isLoading = status == CreationStatus.Loading,
        onDismiss = onDismiss,
        onSubmit = { name, desc, projId, start, end ->
            viewModel.createEvent(name, desc, projId, start, end)
        }
    )


}