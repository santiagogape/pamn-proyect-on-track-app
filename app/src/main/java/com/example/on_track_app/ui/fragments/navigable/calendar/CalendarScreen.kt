package com.example.on_track_app.ui.fragments.navigable.calendar

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.unit.dp
import com.example.on_track_app.ui.activities.AgendaActivity
import com.example.on_track_app.ui.fragments.reusable.calendar.Calendar
import com.example.on_track_app.ui.theme.OnTrackAppTheme
import com.example.on_track_app.viewModels.main.CalendarViewModel

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = viewModel(),
    projectId: String? = null
) {
    val text by viewModel.text.collectAsStateWithLifecycle()
    val items by viewModel.taskByDates.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (items.isEmpty()) Text(text = text, style = MaterialTheme.typography.headlineSmall)
        else {
            Calendar(
                tasksByDate = items,
                onDayClick = {
                    date ->
                        val intent = Intent(context, AgendaActivity::class.java)
                        intent.putExtra("LOCAL_DATE", date.toString())
                    //todo -> context of current project to agenda
                        context.startActivity(intent)
                }
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ExpandableCardItemPreview() {
    OnTrackAppTheme(darkTheme = false) {
        CalendarScreen()
    }
}