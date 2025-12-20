package com.example.on_track_app.ui.fragments.navigable.calendar

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.on_track_app.R
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Task
import com.example.on_track_app.ui.activities.AgendaActivity
import com.example.on_track_app.ui.fragments.reusable.calendar.Calendar
import com.example.on_track_app.utils.LocalCreationContext
import com.example.on_track_app.utils.LocalViewModelFactory
import com.example.on_track_app.viewModels.GroupCreationContext
import com.example.on_track_app.viewModels.ProjectCreationContext
import com.example.on_track_app.viewModels.UserCreationContext
import com.example.on_track_app.viewModels.main.CalendarViewModel
import com.example.on_track_app.viewModels.main.ItemStatus
import java.time.LocalDate

@Composable
fun CalendarScreen(
) {
    val context = LocalContext.current

    val viewModelFactory = LocalViewModelFactory.current
    val creationContext = LocalCreationContext.current

    val viewModel: CalendarViewModel = viewModel(factory = viewModelFactory)

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
    val eventsStatus by sourceFlow.collectAsStateWithLifecycle()
    val tasksStatus by tasksSourceFlow.collectAsStateWithLifecycle()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            eventsStatus is ItemStatus.Loading || tasksStatus is ItemStatus.Loading -> {
                CircularProgressIndicator()
            }

            eventsStatus is ItemStatus.Error || tasksStatus is ItemStatus.Error -> {
            }

            eventsStatus is ItemStatus.Success && tasksStatus is ItemStatus.Success -> {
                val events = (eventsStatus as ItemStatus.Success<Map<LocalDate, List<Event>>>).elements
                val tasks = (tasksStatus as ItemStatus.Success<Map<LocalDate, List<Task>>>).elements

                if ( tasks.isEmpty() && events.isEmpty()){
                    Text(stringResource(R.string.calendar_empty))
                } else {
                    Calendar(
                        tasksByDate = tasks,
                        eventsByDates = events,
                        onDayClick = { date ->
                            val intent = Intent(context, AgendaActivity::class.java)
                            intent.putExtra("LOCAL_DATE", date.toString())
                            intent.putExtra("PROJECT_ID", creationContext.projectId)
                            if (creationContext is GroupCreationContext)
                                intent.putExtra("GROUP_ID", creationContext.ownerId)
                            context.startActivity(intent)
                        }
                    )
                }

            }
        }

    }
}