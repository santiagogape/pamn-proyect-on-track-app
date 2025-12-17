package com.example.on_track_app.ui.fragments.reusable.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.on_track_app.model.Described
import com.example.on_track_app.model.Identifiable
import com.example.on_track_app.model.Named
import com.example.on_track_app.model.Timed

@Composable
fun <T> ExpandableCards (
    contents: List<T>,
    onEditItem: (T) -> Unit,
    onDeleteItem: (T) -> Unit
) where T: Identifiable,T: Named,T: Described {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(contents, key = { it.id }) { item ->
            ExpandableCardItem(
                title = item.name,
                content = item.description,
                onEdit = {onEditItem(item)},
                onDelete = {onDeleteItem(item)}
            )
        }
    }

}


//todo -> when event -> check time at LocalDate, else toTime()
@Composable
fun <T> TimedExpandableCards (
    contents: List<T>,
    onEditItem: (T) -> Unit,
    onDeleteItem: (T) -> Unit
) where T: Identifiable,T: Named,T: Described, T: Timed {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(contents, key = { it.id }) { item ->
            ExpandableCardItem(
                title = item.name,
                content = item.description,
                time = item.toTime(),
                onEdit = { onEditItem(item) },
                onDelete = { onDeleteItem(item) },
            )
        }
    }

}

@Composable
fun <T>StaticCards(
    contents: List<T>, //todo -> delete para project -> propagate
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