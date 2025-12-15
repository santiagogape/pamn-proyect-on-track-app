package com.example.on_track_app.ui.fragments.navigable.calendar

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.on_track_app.ui.activities.AgendaActivity
import com.example.on_track_app.ui.fragments.reusable.calendar.Calendar
import com.example.on_track_app.utils.LocalOwnership
import com.example.on_track_app.utils.LocalViewModelFactory
import com.example.on_track_app.viewModels.main.CalendarViewModel
import com.example.on_track_app.viewModels.main.ItemStatus

@Composable
fun CalendarScreen(
) {
    val context = LocalContext.current

    val viewModelFactory = LocalViewModelFactory.current
    val config = LocalOwnership.current

    val viewModel: CalendarViewModel = viewModel(factory = viewModelFactory)
    val text by viewModel.text.collectAsStateWithLifecycle()
    val sourceFlow = remember(config.currentProject, config.currentGroup) {
        config.currentProject?.let { viewModel.eventsByDatesAndProject(it) } ?: viewModel.eventsByDates
    }
    val items by sourceFlow.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when(val state = items){
            ItemStatus.Error -> {}
            ItemStatus.Loading -> CircularProgressIndicator()
            is ItemStatus.Success -> {
                if (state.elements.isEmpty()) Text(
                    text = text,
                    style = MaterialTheme.typography.headlineSmall
                )
                else {
                    Calendar(
                        tasksByDate = state.elements,
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