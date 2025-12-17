package com.example.on_track_app.ui.fragments.navigable.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.on_track_app.model.MockTask
import com.example.on_track_app.ui.fragments.dialogs.EditTask
import com.example.on_track_app.ui.fragments.reusable.cards.ExpandableCards
import com.example.on_track_app.utils.LocalOwnership
import com.example.on_track_app.utils.LocalViewModelFactory
import com.example.on_track_app.viewModels.main.ItemStatus
import com.example.on_track_app.viewModels.raw.TasksViewModel

@Composable
fun DashboardScreen(
) {
    val config = LocalOwnership.current
    val viewModelFactory = LocalViewModelFactory.current

    val viewModel: TasksViewModel = viewModel(factory = viewModelFactory)
    val text by viewModel.text.collectAsStateWithLifecycle()
    val sourceFlow = remember(config.currentProject, config.currentGroup) {
        when {
            config.currentGroup != null && config.currentProject == null -> viewModel.byGroup(config.currentGroup)
            config.currentProject != null -> viewModel.byProject(config.currentProject)
            else -> viewModel.allTasks
        }
    }
    val items by sourceFlow.collectAsStateWithLifecycle()
    val projectsState by viewModel.projects(config.currentGroup).collectAsStateWithLifecycle()
    var taskToEdit by remember { mutableStateOf<MockTask?>(null) }


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
                if (state.elements.isEmpty()){
                    Text(text = text, style = MaterialTheme.typography.headlineSmall)
                } else {
                    ExpandableCards(state.elements,{taskToEdit=it},{viewModel.delete(it)})
                }
            }
        }
        EditTask(taskToEdit, projectsState, viewModel,{taskToEdit=null})
    }
}
