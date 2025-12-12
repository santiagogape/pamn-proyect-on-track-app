package com.example.on_track_app.ui.fragments.navigable.home

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.on_track_app.data.FirestoreRepository
import com.example.on_track_app.di.AppViewModelFactory
import com.example.on_track_app.di.DummyFactory
import com.example.on_track_app.model.Task
import com.example.on_track_app.ui.activities.ProjectActivity
import com.example.on_track_app.ui.fragments.reusable.cards.ExpandableCards
import com.example.on_track_app.ui.fragments.reusable.cards.StaticCards
import com.example.on_track_app.ui.theme.OnTrackAppTheme
import com.example.on_track_app.viewModels.main.HomeViewModel
import com.example.on_track_app.viewModels.main.ItemStatus

@Composable
fun HomeScreen(
    factory: AppViewModelFactory
) {
    val viewModel: HomeViewModel = viewModel(factory=factory)

    val text by viewModel.text.collectAsStateWithLifecycle()
    val uiState by viewModel.tasks.collectAsStateWithLifecycle()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {

            is ItemStatus.Loading -> {
                CircularProgressIndicator()
            }
            is ItemStatus.Error -> {
                Text("Something went wrong loading tasks.")
            }
            is ItemStatus.Success -> {
                if (state.elements.isEmpty()) {
                    Text(text = text, style = MaterialTheme.typography.headlineSmall)
                } else {
                    ExpandableCards(state.elements)
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ExpandableCardItemPreview() {
    OnTrackAppTheme(darkTheme = false) {
        HomeScreen(factory = DummyFactory as AppViewModelFactory)
    }
}