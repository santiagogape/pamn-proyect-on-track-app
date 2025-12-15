package com.example.on_track_app.ui.fragments.reusable.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Task
import java.time.LocalDate
import java.time.YearMonth.*

@Composable
fun Calendar(
    eventsByDate: Map<LocalDate, List<Event>>,
    tasksByDate: Map<LocalDate, List<Task>>,
    onDayClick: (LocalDate) -> Unit
) {
    var currentMonth by remember { mutableStateOf(now()) }
    val today = LocalDate.now()

    Column {
        CalendarHeader(
            currentMonth = currentMonth,
            onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
            onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
        )

        Spacer(Modifier.height(8.dp))

        DaysOfWeekRow()

        Spacer(Modifier.height(8.dp))

        MonthGrid(
            month = currentMonth,
            today = today,
            tasksByDate = tasksByDate,
            eventsByDate = eventsByDate,
            onDayClick = onDayClick
        )
    }
}