package com.example.on_track_app.ui.fragments.dialogs

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.on_track_app.model.LinkedType
import com.example.on_track_app.ui.activities.Dialogs
import com.example.on_track_app.utils.LocalOwnership
import com.example.on_track_app.utils.LocalViewModelFactory
import com.example.on_track_app.viewModels.CreationViewModel
import com.example.on_track_app.viewModels.main.asSelectable
import com.example.on_track_app.viewModels.raw.EventsViewModel
import com.example.on_track_app.viewModels.raw.ProjectsViewModel
import com.example.on_track_app.viewModels.raw.TasksViewModel
import kotlinx.coroutines.launch


@Composable
fun GlobalDialogCoordinator(
    activeDialog: Dialogs,          // The current state
    onDismiss: () -> Unit,               // How to close the dialog
    snackBarHostState: SnackbarHostState, // To show success messages
) {
    val viewModelFactory = LocalViewModelFactory.current
    val ownershipContext = LocalOwnership.current
    val creator: CreationViewModel = viewModel(factory = viewModelFactory)

    val scope = rememberCoroutineScope()
    when (activeDialog) {
        Dialogs.NONE -> { /* Do nothing */ }

        Dialogs.TASK -> {
            val projectsViewModel: ProjectsViewModel = viewModel(factory = viewModelFactory)
            val state by  projectsViewModel.projects().collectAsStateWithLifecycle()
            TaskCreation(
                availableProjects = state,
                onDismiss = onDismiss,
                onSubmit = { name, desc, project, date, hour, min ->
                    creator.addNewTask(name, desc, project,ownershipContext.owner(), ownershipContext.ownerType(), date,hour,min )
                    onDismiss()
                    scope.launch {
                        snackBarHostState.showSnackbar("Task created successfully")
                    }
                },
                defaultProject = ownershipContext.currentProject?.let {
                    projectsViewModel.project(it)
                }
            )

        }

        Dialogs.EVENT ->  {
            val projectsViewModel: ProjectsViewModel = viewModel(factory = viewModelFactory)
            val state by  projectsViewModel.projects().collectAsStateWithLifecycle()

            EventCreation(ownershipContext.currentProject?.let {projectsViewModel.project(it)},state,onDismiss) {
                name, desc, project, start, end ->
                creator.addNewEvent(
                    name,
                    desc,
                    project,
                    ownershipContext.owner(),
                    ownershipContext.ownerType(),
                    start,
                    end
                )
                onDismiss()
                scope.launch {
                    snackBarHostState.showSnackbar("Task created successfully")
                }
            }
        }

        Dialogs.PROJECT ->  {

            ProjectCreation(
                onDismiss = onDismiss,
                onSubmit = { name, desc ->
                    //todo add group selection -> none selected means its mine
                    creator.addNewProject(name, desc,ownershipContext.owner(), ownershipContext.ownerType())
                    onDismiss()
                    scope.launch {
                        snackBarHostState.showSnackbar("Project created successfully")
                    }
                }
            )
        }

        Dialogs.REMINDER -> {
            val tasksViewModel: TasksViewModel = viewModel(factory = viewModelFactory)
            val eventsViewModel: EventsViewModel = viewModel(factory = viewModelFactory)

            //todo filter by ownership context
            val selectableTasks by tasksViewModel.allTasks.collectAsStateWithLifecycle()
            val selectableEvents by eventsViewModel.events.collectAsStateWithLifecycle()


            ReminderCreation(
                onDismiss = onDismiss,
                onSubmit = { name, desc, linkedTo, linkType, date, hour, minute ->
                    creator.addReminder(ownershipContext.owner(), ownershipContext.ownerType()
                        ,name, desc,linkedTo, linkType, date, hour, minute)
                    onDismiss()
                    scope.launch {
                        snackBarHostState.showSnackbar("Reminder created successfully")
                    }
                },
                builder = ReminderCreationBuilder(
                    sources = mapOf(
                        LinkedType.TASK to selectableTasks.asSelectable(),
                        LinkedType.EVENT to selectableEvents.asSelectable()
                    )
                ),
            )
        }
    }
}