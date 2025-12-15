package com.example.on_track_app.ui.fragments.reusable.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Task
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

data class CalendarDay(
    val date: LocalDate,
    val isFromCurrentMonth: Boolean
)

fun daysOfMonthGrid(month: YearMonth): List<CalendarDay> {
    val firstOfMonth = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()

    // DayOfWeek va de MONDAY=1 a SUNDAY=7 (depende de tu Locale)
    val firstDayOfWeek = firstOfMonth.dayOfWeek.value  // 1..7

    // Normalizar para que el calendario empiece en Lunes
    val shift = (firstDayOfWeek - DayOfWeek.MONDAY.value).let {
        if (it < 0) it + 7 else it
    }

    val result = mutableListOf<CalendarDay>()

    // 1) Días del mes anterior
    val previousMonth = month.minusMonths(1)
    val daysInPrevious = previousMonth.lengthOfMonth()

    for (i in (daysInPrevious - shift + 1)..daysInPrevious) {
        result += CalendarDay(
            date = previousMonth.atDay(i),
            isFromCurrentMonth = false
        )
    }

    // 2) Días del mes actual
    for (i in 1..daysInMonth) {
        result += CalendarDay(
            date = month.atDay(i),
            isFromCurrentMonth = true
        )
    }

    // 3) Días del mes siguiente para completar la fila
    if (result.size == 35) return result
    val total = when {
        result.size < 35 -> 35
        else -> 42
    }
    val nextMonth = month.plusMonths(1)

    var nextDay = 1
    while (result.size < total) {
        result += CalendarDay(
            date = nextMonth.atDay(nextDay),
            isFromCurrentMonth = false
        )
        nextDay++
    }

    return result
}


@Composable
fun MonthGrid(
    month: YearMonth,
    today: LocalDate,
    tasksByDate: Map<LocalDate, List<Task>>,
    eventsByDate: Map<LocalDate, List<Event>>,
    onDayClick: (LocalDate) -> Unit,
) {

    val days =  daysOfMonthGrid(month)

    LazyVerticalGrid(columns = GridCells.Fixed(7)) {
        items(days) { date ->
            DayCell(
                date = date,
                isToday = date == today,
                onClick = onDayClick,
                hasEvents = !tasksByDate[date.date].isNullOrEmpty() or !eventsByDate[date.date].isNullOrEmpty()
            )
        }
    }
}


@Composable
fun DayCell(
    date: CalendarDay,
    isToday: Boolean,
    onClick: (LocalDate) -> Unit,
    hasEvents: Boolean
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable {date.date.let(onClick)}
            .background(
                if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else Color.Transparent
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${date.date.dayOfMonth}",
                color =
                    if (date.isFromCurrentMonth) MaterialTheme.colorScheme.onBackground
                    else MaterialTheme.colorScheme.onPrimary
            )
            if (hasEvents) {
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary)
                )
            }
        }

    }
}
