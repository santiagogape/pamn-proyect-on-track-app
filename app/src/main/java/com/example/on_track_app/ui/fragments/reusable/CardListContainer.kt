package com.example.on_track_app.ui.fragments.reusable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableCards(
    contents: List<String>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(contents, key = { it }) { item ->
            ExpandableCardItem(
                title = item,
                content = item
            )
        }
    }

}

@Composable
fun StaticCards(
    contents: List<String>,
    action: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(contents, key = { it }) { item ->
            StaticCard(
                title = item,
                action = action
            )

        }
    }

}