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
import com.example.on_track_app.model.MockEvent
import com.example.on_track_app.model.MockTask
import com.example.on_track_app.ui.activities.AgendaActivity
import com.example.on_track_app.ui.fragments.reusable.calendar.Calendar
import com.example.on_track_app.utils.LocalOwnership
import com.example.on_track_app.utils.LocalViewModelFactory
import com.example.on_track_app.viewModels.main.CalendarViewModel
import com.example.on_track_app.viewModels.main.ItemStatus
import java.time.LocalDate

@Composable
fun CalendarScreen(
) {
    val context = LocalContext.current

    val viewModelFactory = LocalViewModelFactory.current
    val config = LocalOwnership.current

    val viewModel: CalendarViewModel = viewModel(factory = viewModelFactory)
    val sourceFlow = remember(config.currentProject, config.currentGroup) {
        when {
            config.currentGroup != null && config.currentProject == null -> viewModel.byGroup(config.currentGroup)
            config.currentProject != null -> viewModel.byProject(config.currentProject)
            else -> viewModel.eventsByDates
        }
    }

    val tasksSourceFlow = remember(config.currentProject, config.currentGroup) {
        when {
            config.currentGroup != null && config.currentProject == null -> viewModel.tasksByGroup(config.currentGroup)
            config.currentProject != null -> viewModel.tasksByProject(config.currentProject)
            else -> viewModel.tasksByDates
        }
    }
    val items by sourceFlow.collectAsStateWithLifecycle()
    val tasks by tasksSourceFlow.collectAsStateWithLifecycle()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            items is ItemStatus.Loading || tasks is ItemStatus.Loading -> {
                CircularProgressIndicator()
            }

            items is ItemStatus.Error || tasks is ItemStatus.Error -> {
            }

            items is ItemStatus.Success && tasks is ItemStatus.Success -> {
                val currentEvents = (items as ItemStatus.Success<Map<LocalDate, List<MockEvent>>>).elements
                val currentTask = (tasks as ItemStatus.Success<Map<LocalDate, List<MockTask>>>).elements

                if ( currentTask.isEmpty() && currentEvents.isEmpty()){
                    Text(stringResource(R.string.calendar_empty))
                } else {
                    Calendar(
                        tasksByDate = currentTask,
                        eventsByDates = currentEvents,
                        onDayClick = { date ->
                            val intent = Intent(context, AgendaActivity::class.java)
                            intent.putExtra("LOCAL_DATE", date.toString())
                            intent.putExtra("PROJECT_ID", config.currentProject)
                            intent.putExtra("GROUP_ID", config.currentGroup)
                            context.startActivity(intent)
                        }
                    )
                }

            }
        }

    }
}