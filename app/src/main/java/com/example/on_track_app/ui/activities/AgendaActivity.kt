package com.example.on_track_app.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.on_track_app.OnTrackApp
import com.example.on_track_app.R
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Task
import com.example.on_track_app.model.TimedExpandable
import com.example.on_track_app.model.sort
import com.example.on_track_app.ui.fragments.dialogs.EditEvent
import com.example.on_track_app.ui.fragments.dialogs.EditTask
import com.example.on_track_app.ui.fragments.reusable.cards.TimedExpandableCards
import com.example.on_track_app.ui.fragments.reusable.header.AgendaHeader
import com.example.on_track_app.ui.theme.OnTrackAppTheme
import com.example.on_track_app.utils.LocalCreationContext
import com.example.on_track_app.utils.LocalOwnerContext
import com.example.on_track_app.utils.LocalReminderCreationContext
import com.example.on_track_app.utils.LocalUserPFP
import com.example.on_track_app.utils.LocalViewModelFactory
import com.example.on_track_app.utils.SettingsDataStore
import com.example.on_track_app.viewModels.GroupCreationContext
import com.example.on_track_app.viewModels.GroupOwnerContext
import com.example.on_track_app.viewModels.GroupReminderCreationContext
import com.example.on_track_app.viewModels.ProjectCreationContext
import com.example.on_track_app.viewModels.UserCreationContext
import com.example.on_track_app.viewModels.UserOwnerContext
import com.example.on_track_app.viewModels.UserReminderCreationContext
import com.example.on_track_app.viewModels.main.AgendaViewModel
import com.example.on_track_app.viewModels.main.ItemStatus
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDate.now

class AgendaActivity : ComponentActivity() {
    private val settings by lazy { SettingsDataStore(this) }
    private val factory by lazy {
        (application as OnTrackApp).viewModelsFactory
    }

    private val config by lazy {
        (application as OnTrackApp).localConfig
    }

    val pfp = (application as OnTrackApp)
        .authClient
        .getProfilePictureUrl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val date = LocalDate.parse(intent.getStringExtra("LOCAL_DATE")!!)
            val projectId = intent.getStringExtra("PROJECT_ID")
            val groupId = intent.getStringExtra("GROUP_ID")
            val darkTheme by settings.darkThemeFlow.collectAsState(initial = false)
            val conf = config.get()
            val ownerContext = when{
                groupId != null -> GroupOwnerContext(groupId)
                else -> UserOwnerContext(conf.user)
            }
            val creationContext = when {
                projectId != null -> ProjectCreationContext(ownerContext.ownerId, projectId)
                else -> when(ownerContext){
                    is GroupOwnerContext -> GroupCreationContext(ownerContext.ownerId)
                    is UserOwnerContext -> UserCreationContext(conf.user)
                }
            }
            val reminderContext = when(ownerContext){
                is GroupOwnerContext -> GroupReminderCreationContext(ownerContext.ownerId)
                is UserOwnerContext -> UserReminderCreationContext(conf.user)
            }
            CompositionLocalProvider(
                LocalViewModelFactory provides factory,
                LocalOwnerContext provides ownerContext,
                LocalCreationContext provides creationContext,
                LocalReminderCreationContext provides reminderContext,
                LocalUserPFP provides pfp
            ) {
                Agenda(
                    darkTheme = darkTheme,{
                    lifecycleScope.launch {
                        settings.setDarkTheme(!darkTheme)
                    }
                },
                    date = date
                )
            }

        }
    }
}

@Composable
fun Agenda(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    date: LocalDate = now(),
) {
    val viewModelFactory = LocalViewModelFactory.current

    var currentDate by remember { mutableStateOf(date) }

    val ownerContext = LocalOwnerContext.current
    val creationContext = LocalCreationContext.current

    val viewModel: AgendaViewModel = viewModel(factory = viewModelFactory)
    val sourceFlow = remember(creationContext) {
        when(creationContext){
            is ProjectCreationContext -> viewModel.byProject(creationContext.projectId)
            is GroupCreationContext -> viewModel.byGroup(creationContext.ownerId)
            is UserCreationContext -> viewModel.eventsByDates
        }
    }

    val tasksSourceFlow = remember(creationContext) {
        when(creationContext){
            is ProjectCreationContext -> viewModel.tasksByProject(creationContext.projectId)
            is GroupCreationContext -> viewModel.tasksByGroup(creationContext.ownerId)
            is UserCreationContext -> viewModel.tasksByDates
        }
    }
    val events by sourceFlow.collectAsStateWithLifecycle()
    val tasks by tasksSourceFlow.collectAsStateWithLifecycle()

    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var eventToEdit by remember { mutableStateOf<Event?>(null) }

    val projectsSourceFlow = remember(ownerContext) {
        when(ownerContext){
            is GroupOwnerContext -> viewModel.projects(ownerContext.ownerId)
            is UserOwnerContext -> viewModel.projects(null)
        }
    }

    val state by projectsSourceFlow.collectAsStateWithLifecycle()



    OnTrackAppTheme(darkTheme = darkTheme) {
        ActivityScaffold(
            header = {
                AgendaHeader(currentDate, darkTheme, onToggleTheme)
            },
            footer = {
                NextPrev(
                    { currentDate = currentDate.minusDays(1) },
                    { currentDate = currentDate.plusDays(1) }
                )
            }, currentDate = currentDate
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    events is ItemStatus.Loading || tasks is ItemStatus.Loading -> {
                        CircularProgressIndicator()
                    }

                    events is ItemStatus.Error || tasks is ItemStatus.Error -> {
                    }

                    events is ItemStatus.Success && tasks is ItemStatus.Success -> {
                        val currentEvents =
                            (events as ItemStatus.Success<Map<LocalDate, List<Event>>>).elements
                        val currentTask =
                            (tasks as ItemStatus.Success<Map<LocalDate, List<Task>>>).elements
                        val taskedMap = mutableMapOf<String, Task>()
                        val eventMap = mutableMapOf<String, Event>()
                        val timedItems: List<TimedExpandable> =
                            currentEvents[currentDate].orEmpty().map {
                                eventMap[it.id] = it
                                it
                            } +
                                    currentTask[currentDate].orEmpty().map {
                                        taskedMap[it.id] = it
                                        it
                                    }

                        if (currentTask.isEmpty() && currentEvents.isEmpty()) {
                            Text(
                                text = stringResource(R.string.day_empty),
                                style = MaterialTheme.typography.headlineSmall
                            )
                        } else {
                            TimedExpandableCards(timedItems.sort(), {
                                taskedMap[it.id]?.let { task -> taskToEdit = task }
                                eventMap[it.id]?.let { event -> eventToEdit = event }
                            }, {
                                when(it) {
                                    is Task -> viewModel.delete(it)
                                    is Event -> viewModel.delete(it)
                                }

                            })
                        }
                    }
                }

                EditTask(taskToEdit, state, viewModel) {taskToEdit = null }
                EditEvent(eventToEdit, state, viewModel) { eventToEdit = null }

            }

        }
    }
}


@Composable
fun NextPrev(onPreviousDay: () -> Unit, onNextDay: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousDay) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous day")
        }
        IconButton(onClick = onNextDay) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next day")
        }
    }
}
