package com.example.on_track_app.ui.fragments.reusable.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.on_track_app.model.Expandable

@Composable
fun SectionedExpandableCards (
    sections: Map<String, List<Expandable>>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        sections.forEach { (sectionName, items) ->
            if (items.isNotEmpty()) {
                item {
                    Text(
                        text = sectionName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                items(items, key = { it.id }) { item ->
                    ExpandableCardItem(item.name, item.description)
                }
            }
        }
    }
}