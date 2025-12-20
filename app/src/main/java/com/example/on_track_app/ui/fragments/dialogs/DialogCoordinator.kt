package com.example.on_track_app.ui.fragments.dialogs

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.on_track_app.model.LinkedType
import com.example.on_track_app.ui.activities.Dialogs
import com.example.on_track_app.utils.DebugLogcatLogger
import com.example.on_track_app.utils.LocalCreationContext
import com.example.on_track_app.utils.LocalOwnerContext
import com.example.on_track_app.utils.LocalViewModelFactory
import com.example.on_track_app.viewModels.CreationSourcesViewModel
import com.example.on_track_app.viewModels.CreationViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate


@Composable
fun GlobalDialogCoordinator(
    activeDialog: Dialogs,          // The current state
    onDismiss: () -> Unit,               // How to close the dialog
    snackBarHostState: SnackbarHostState,
    sources: CreationSourcesViewModel,
    currentDate: LocalDate? = null, // To show success messages
) {
    val viewModelFactory = LocalViewModelFactory.current
    val ownerContext = LocalOwnerContext.current
    val creationContext = LocalCreationContext.current
    val creator: CreationViewModel = viewModel(factory = viewModelFactory)
    val project = creationContext.projectId?.let { sources.project(it) }
    DebugLogcatLogger.log("project $project")
    val projectsFlow = remember(ownerContext) { sources.projects(ownerContext) }
    val tasksSourceFlow = remember(creationContext) { sources.tasks(creationContext) }
    val eventsSourceFlow = remember(creationContext) { sources.events(creationContext) }
    val scope = rememberCoroutineScope()
    when (activeDialog) {
        Dialogs.NONE -> { /* Do nothing */ }

        Dialogs.TASK -> {
            val state by  projectsFlow.collectAsStateWithLifecycle()
            TaskCreation(
                defaultProject = project,
                availableProjects = state,
                onDismiss = onDismiss,
                onSubmit = { name, desc, project, date, hour, min ->
                    DebugLogcatLogger.log(" selected project $project")
                    creator.addNewTask(name, desc, project,ownerContext, date,hour,min )
                    onDismiss()
                    scope.launch {
                        snackBarHostState.showSnackbar("Task created successfully")
                    }
                },
                currentDate = currentDate
            )

        }

        Dialogs.EVENT ->  {
            val state by  projectsFlow.collectAsStateWithLifecycle()

            EventCreation(
                project,
                state,
                onDismiss,
                { name, desc, project, start, end ->
                    creator.addNewEvent(
                        name,
                        desc,
                        project,
                        ownerContext,
                        start,
                        end
                    )
                    onDismiss()
                    scope.launch {
                        snackBarHostState.showSnackbar("Task created successfully")
                    }
                },
                currentDate = currentDate
            )
        }

        Dialogs.PROJECT ->  {

            ProjectCreation(
                onDismiss = onDismiss,
                onSubmit = { name, desc ->
                    //todo add group selection -> none selected means its mine
                    creator.addNewProject(name, desc,ownerContext)
                    onDismiss()
                    scope.launch {
                        snackBarHostState.showSnackbar("Project created successfully")
                    }
                }
            )
        }

        Dialogs.REMINDER -> {

            //todo filter by ownership context
            val selectableTasks by tasksSourceFlow.collectAsStateWithLifecycle()
            val selectableEvents by eventsSourceFlow.collectAsStateWithLifecycle()


            ReminderCreation(
                onDismiss = onDismiss,
                onSubmit = { name, desc, linkedTo, date, hour, minute ->
                    creator.addReminder(name, desc,
                        date, hour,minute, ownerContext, linkedTo)
                    onDismiss()
                    scope.launch {
                        snackBarHostState.showSnackbar("Reminder created successfully")
                    }
                },
                builder = ReminderCreationBuilder(
                    sources = mapOf(
                        LinkedType.TASK to selectableTasks,
                        LinkedType.EVENT to selectableEvents
                    )
                ), currentDate = currentDate
            )
        }
    }
}