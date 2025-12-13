package com.example.on_track_app.ui.fragments.navigable.notifications



import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.on_track_app.utils.LocalViewModelFactory
import com.example.on_track_app.viewModels.main.NotificationsViewModel


@Composable
fun NotificationsScreen(
    projectId: String? = null
) {
    val viewModelFactory = LocalViewModelFactory.current

    val viewModel: NotificationsViewModel = viewModel(factory = viewModelFactory)
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
        }
    }
}
