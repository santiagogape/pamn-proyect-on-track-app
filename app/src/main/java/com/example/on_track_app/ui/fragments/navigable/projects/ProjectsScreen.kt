package com.example.on_track_app.ui.fragments.navigable.projects



import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.on_track_app.ui.activities.ProjectActivity
import com.example.on_track_app.ui.fragments.reusable.cards.StaticCards
import com.example.on_track_app.utils.LocalOwnership
import com.example.on_track_app.utils.LocalViewModelFactory
import com.example.on_track_app.viewModels.main.ItemStatus
import com.example.on_track_app.viewModels.raw.ProjectsViewModel


@Composable
fun ProjectsScreen(
) {
    val viewModelFactory = LocalViewModelFactory.current

    val viewModel: ProjectsViewModel = viewModel(factory = viewModelFactory)

    val context = LocalContext.current
    val default = LocalOwnership.current
    val projects by viewModel.projects().collectAsStateWithLifecycle()
    val text by viewModel.text.collectAsStateWithLifecycle()

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
                    Text(text = text, style = MaterialTheme.typography.headlineSmall)
                } else {
                    StaticCards(state.elements) { projectId,projectName ->
                        val intent = Intent(context, ProjectActivity::class.java)
                        intent.putExtra("PROJECT", projectName)
                        intent.putExtra("PROJECT_ID", projectId)
                        intent.putExtra("GROUP_ID", default.currentGroup)
                        context.startActivity(intent)
                    }
                }
            }
        }
    }
}
