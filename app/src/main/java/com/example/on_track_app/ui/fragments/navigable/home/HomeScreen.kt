package com.example.on_track_app.ui.fragments.navigable.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.on_track_app.ui.fragments.reusable.ExpandableCards
import com.example.on_track_app.ui.theme.OnTrackAppTheme
import com.example.on_track_app.viewModels.HomeViewModel
import kotlinx.coroutines.android.awaitFrame

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
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
            ExpandableCards(items)
        }
    }
}

@Composable
fun TestHomeScreen(viewModel: HomeViewModel = viewModel()) {

    var ready by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        awaitFrame()
        ready = true
    }

    if (!ready) {
        // UI ULTRALIGHT PARA EL PRIMER FRAME
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
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
                ExpandableCards(items)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ExpandableCardItemPreview() {
    OnTrackAppTheme(darkTheme = false) {
        HomeScreen()
    }
}