package com.example.on_track_app.ui.fragments.reusable.cards

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.on_track_app.model.Expandable

@OptIn(ExperimentalFoundationApi::class) // Required for stickyHeader
@Composable
fun SectionedExpandableCards(
    groupedContents: Map<String, List<Expandable>>,
    onEditItem: (Expandable) -> Unit,
    onDeleteItem: (Expandable) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
    ) {
        // Iterate through the Map
        groupedContents.forEach { (headerTitle, itemsInGroup) ->

            stickyHeader {
                Text(
                    text = headerTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background) // Opaque background is needed for sticky
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                )
            }

            items(
                items = itemsInGroup,
                key = { it.id }
            ) { item ->
                ExpandableCardItem(
                    title = item.name,
                    content = item.description,
                    onEdit = { onEditItem(item) },
                    onDelete = { onDeleteItem(item) }
                )
            }
        }
    }
}