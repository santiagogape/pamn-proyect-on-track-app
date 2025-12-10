package com.example.on_track_app.ui.fragments.navigable.notifications



import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.on_track_app.ui.fragments.reusable.cards.StaticCards
import com.example.on_track_app.viewModels.main.NotificationsViewModel


@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = viewModel(),
    projectId: String? = null
) {
    val text by viewModel.text.collectAsStateWithLifecycle()
    val sourceFlow = remember(projectId) {
        projectId?.let { viewModel.byProject(it) } ?: viewModel.events
    }
    val items by sourceFlow.collectAsStateWithLifecycle()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (items.isEmpty()){
            Text(text = text, style = MaterialTheme.typography.headlineSmall)
        } else {
            StaticCards(items.map { it.name}){}//todo -> create activity logic
        }
    }
}
