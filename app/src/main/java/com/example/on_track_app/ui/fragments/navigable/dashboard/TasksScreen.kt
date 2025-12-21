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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.on_track_app.R
import com.example.on_track_app.model.Task
import com.example.on_track_app.ui.fragments.dialogs.EditTask
import com.example.on_track_app.ui.fragments.reusable.cards.ExpandableCards
import com.example.on_track_app.utils.LocalCreationContext
import com.example.on_track_app.utils.LocalOwnerContext
import com.example.on_track_app.utils.LocalViewModelFactory
import com.example.on_track_app.viewModels.GroupCreationContext
import com.example.on_track_app.viewModels.GroupOwnerContext
import com.example.on_track_app.viewModels.ProjectCreationContext
import com.example.on_track_app.viewModels.UserCreationContext
import com.example.on_track_app.viewModels.UserOwnerContext
import com.example.on_track_app.viewModels.main.ItemStatus
import com.example.on_track_app.viewModels.raw.TasksViewModel

@Composable
fun DashboardScreen(
) {
    val ownerContext = LocalOwnerContext.current
    val creationContext = LocalCreationContext.current
    val viewModelFactory = LocalViewModelFactory.current

    val viewModel: TasksViewModel = viewModel(factory = viewModelFactory)
    val sourceFlow = remember(creationContext) {
        when(creationContext){
            is ProjectCreationContext -> viewModel.byProject(creationContext.projectId)
            is GroupCreationContext -> viewModel.byGroup(creationContext.ownerId)
            is UserCreationContext -> viewModel.allTasks
        }
    }
    val items by sourceFlow.collectAsStateWithLifecycle()
    val projectsSourceFlow = remember(ownerContext) {
        when(ownerContext){
            is GroupOwnerContext -> viewModel.projects(ownerContext.ownerId)
            is UserOwnerContext -> viewModel.projects(null)
        }
    }
    val projectsState by projectsSourceFlow.collectAsStateWithLifecycle()
    var taskToEdit by remember { mutableStateOf<Task?>(null) }


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
                    Text(text = stringResource(R.string.tasks_empty), style = MaterialTheme.typography.headlineSmall)
                } else {
                    ExpandableCards(state.elements,{taskToEdit=it},{viewModel.delete(it)})
                }
            }
        }
        EditTask(taskToEdit, projectsState, viewModel) { taskToEdit = null }
    }
}
