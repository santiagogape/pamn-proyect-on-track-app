package com.example.on_track_app.ui.fragments.navigable.notifications



import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.on_track_app.ui.fragments.reusable.CardListContainer


@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = viewModel()
) {
    val text by viewModel.text.collectAsStateWithLifecycle()
    val items by viewModel.items.collectAsStateWithLifecycle()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (items.isEmpty()){
            Text(text = text, style = MaterialTheme.typography.headlineSmall)
        } else {
            CardListContainer(items)
        }
    }
}
