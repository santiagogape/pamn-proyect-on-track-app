package com.example.on_track_app.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.on_track_app.R
import com.example.on_track_app.ui.fragments.reusable.cards.ExpandableCards
import com.example.on_track_app.ui.theme.OnTrackAppTheme
import com.example.on_track_app.utils.SettingsDataStore
import com.example.on_track_app.viewModels.main.CalendarViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDate.now

class AgendaActivity : ComponentActivity() {
    private val settings by lazy { SettingsDataStore(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val date = LocalDate.parse(intent.getStringExtra("LOCAL_DATE")!!)
            val darkTheme by settings.darkThemeFlow.collectAsState(initial = false)
            Agenda(darkTheme = darkTheme,{
                lifecycleScope.launch {
                    settings.setDarkTheme(!darkTheme)
                }
            }, date = date)

        }
    }
}

@Composable
fun Agenda(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    date: LocalDate = now(),
    viewModel: CalendarViewModel = viewModel()
){
    var currentDate by remember { mutableStateOf(date) }

    val tasksToday by viewModel.tasksFor(currentDate)
        .collectAsStateWithLifecycle()

    OnTrackAppTheme(darkTheme = darkTheme) {
        ActivityScaffold(
            header = { Header(currentDate,onToggleTheme,darkTheme) },
            footer = { NextPrev(
                { currentDate = currentDate.minusDays(1) },
                { currentDate = currentDate.plusDays(1) }
            ) }
        ){
            if (tasksToday.isEmpty()){
                Text(text = stringResource(R.string.no_tasks_today), style = MaterialTheme.typography.headlineSmall)
            } else {
                ExpandableCards(tasksToday)
            }
        }


    }
}



@Composable
private fun Header(
    date: LocalDate,
    themeToggle: () -> Unit,
    darkTheme: Boolean
) {
    val activity = LocalActivity.current

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        IconButton(
            onClick = { activity?.finish() },
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.get_back)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfWeek.name.lowercase().replaceFirstChar { it.titlecase() },
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = date.toString().split("-").reversed().joinToString("/"),
                style = MaterialTheme.typography.titleMedium
            )
        }
        IconButton(
            onClick = themeToggle,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = if (darkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                contentDescription = stringResource(R.string.theme_toggle)
            )
        }
    }
}


@Preview
@Composable
fun Prev(){
    OnTrackAppTheme(darkTheme = false) {
           Agenda(false,{})
    }
}