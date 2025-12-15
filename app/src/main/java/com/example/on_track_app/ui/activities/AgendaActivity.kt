package com.example.on_track_app.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.on_track_app.model.toExpandable
import com.example.on_track_app.ui.fragments.reusable.cards.ExpandableCards
import com.example.on_track_app.ui.fragments.reusable.header.AgendaHeader
import com.example.on_track_app.ui.theme.OnTrackAppTheme
import com.example.on_track_app.utils.LocalConfig
import com.example.on_track_app.utils.LocalOwnership
import com.example.on_track_app.utils.LocalViewModelFactory
import com.example.on_track_app.utils.OwnershipContext
import com.example.on_track_app.utils.SettingsDataStore
import com.example.on_track_app.viewModels.main.CalendarViewModel
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val date = LocalDate.parse(intent.getStringExtra("LOCAL_DATE")!!)
            val projectId = intent.getStringExtra("PROJECT_ID")
            val groupId = intent.getStringExtra("GROUP_ID")
            val darkTheme by settings.darkThemeFlow.collectAsState(initial = false)
            val conf = config.get()
            val context = OwnershipContext(conf.userID,projectId,groupId)
            CompositionLocalProvider(
                LocalViewModelFactory provides factory,
                LocalConfig provides conf,
                LocalOwnership provides context
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
){
    val viewModelFactory = LocalViewModelFactory.current
    var currentDate by remember { mutableStateOf(date) }

    val viewModel: CalendarViewModel = viewModel(factory = viewModelFactory)
    //todo modify calendar viewmodel

    val tasksToday by viewModel.eventsFor(currentDate)
        .collectAsStateWithLifecycle()

    OnTrackAppTheme(darkTheme = darkTheme) {
        ActivityScaffold(
            header = {
                AgendaHeader(currentDate,darkTheme,onToggleTheme)
                     },
            footer = { NextPrev(
                { currentDate = currentDate.minusDays(1) },
                { currentDate = currentDate.plusDays(1) }
            ) }
        ){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (val state = tasksToday){
                    ItemStatus.Error -> {}
                    ItemStatus.Loading -> CircularProgressIndicator()
                    is ItemStatus.Success ->
                        if (state.elements.isEmpty()){
                            Text(text = stringResource(R.string.no_tasks_today), style = MaterialTheme.typography.headlineSmall)
                        } else {
                            ExpandableCards(state.elements.map { it.toExpandable() })
                        }
                }
            }

        }


    }
}