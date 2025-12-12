package com.example.on_track_app.ui.fragments.dialogs

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.on_track_app.di.AppViewModelFactory
import com.example.on_track_app.viewModels.main.ProjectsViewModel
import com.example.on_track_app.viewModels.main.TasksViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
            // Future implementation
            // EventCreationDialog(...)
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

    LaunchedEffect(status) {
        if (status is CreationStatus.Success) {
            viewModel.resetStatus()
            onSuccess()
        }
    }

    TaskCreation(
        isLoading = status == CreationStatus.Loading,
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