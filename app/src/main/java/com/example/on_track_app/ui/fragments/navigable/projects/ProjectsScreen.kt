package com.example.on_track_app.ui.fragments.navigable.projects



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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.on_track_app.R
import com.example.on_track_app.ui.activities.ProjectActivity
import com.example.on_track_app.ui.fragments.reusable.cards.StaticCards
import com.example.on_track_app.utils.LocalCreationContext
import com.example.on_track_app.utils.LocalOwnerContext
import com.example.on_track_app.utils.LocalViewModelFactory
import com.example.on_track_app.viewModels.GroupCreationContext
import com.example.on_track_app.viewModels.GroupOwnerContext
import com.example.on_track_app.viewModels.UserOwnerContext
import com.example.on_track_app.viewModels.main.ItemStatus
import com.example.on_track_app.viewModels.raw.ProjectsViewModel


@Composable
fun ProjectsScreen(
) {
    val viewModelFactory = LocalViewModelFactory.current
    val ownerContext = LocalOwnerContext.current
    val creationContext = LocalCreationContext.current


    val viewModel: ProjectsViewModel = viewModel(factory = viewModelFactory)

    val context = LocalContext.current

    val projectsSourceFlow = remember(ownerContext) {
        when(ownerContext){
            is GroupOwnerContext -> viewModel.projectsOf(ownerContext.ownerId)
            is UserOwnerContext -> viewModel.projects()
        }
    }
    val projects by projectsSourceFlow.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when(val state = projects){
            ItemStatus.Loading -> CircularProgressIndicator()
            ItemStatus.Error -> {}
            is ItemStatus.Success -> {
                if (state.elements.isEmpty()) {
                    Text(text = stringResource(R.string.projects_empty), style = MaterialTheme.typography.headlineSmall)
                } else {
                    StaticCards(state.elements,{viewModel.delete(it)}) { projectId,projectName ->
                        val intent = Intent(context, ProjectActivity::class.java)
                        intent.putExtra("PROJECT", projectName)
                        intent.putExtra("PROJECT_ID", projectId)
                        if (creationContext is GroupCreationContext)
                            intent.putExtra("GROUP_ID", creationContext.ownerId)
                        context.startActivity(intent)
                    }
                }
            }
        }
    }
}
