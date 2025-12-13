package com.example.on_track_app.ui.fragments.reusable.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.on_track_app.model.Expandable
import com.example.on_track_app.model.Identifiable
import com.example.on_track_app.model.Named

@Composable
fun <T: Expandable> ExpandableCards (
    contents: List<T>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(contents, key = { it.id }) { item ->
            ExpandableCardItem(
                title = item.name,
                content = item.description
            )
        }
    }

}

@Composable
fun <T>StaticCards(
    contents: List<T>,
    action: (String, String) -> Unit
) where T: Identifiable,T: Named {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(contents, key = { it.id }) { item ->
            StaticCard(
                id = item.id,
                title = item.name,
                action = action
            )

        }
    }

}