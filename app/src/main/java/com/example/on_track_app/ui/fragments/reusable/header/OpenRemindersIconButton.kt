package com.example.on_track_app.ui.fragments.reusable.header

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.on_track_app.model.LinkedType
import com.example.on_track_app.model.MockReminder
import com.example.on_track_app.model.sortByTime
import com.example.on_track_app.model.toDate
import com.example.on_track_app.model.toTime
import com.example.on_track_app.utils.LocalOwnership
import com.example.on_track_app.utils.LocalViewModelFactory
import com.example.on_track_app.viewModels.main.ItemStatus
import com.example.on_track_app.viewModels.raw.RemindersViewModel
import kotlinx.coroutines.flow.StateFlow
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenRemindersIconButton(
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    val ownership = LocalOwnership.current
    val viewModel: RemindersViewModel = viewModel(factory = LocalViewModelFactory.current)
    val sourceFlow: StateFlow<ItemStatus<List<MockReminder>>> = remember(ownership.currentProject,ownership.currentGroup) {
        when {
            ownership.currentGroup != null && ownership.currentProject == null ->
                viewModel.byGroup(ownership.currentGroup)

            ownership.currentProject != null ->
                viewModel.byProject(ownership.currentProject)
            else -> viewModel.all(ownership.userId)

        }
    }
    val reminders by sourceFlow.collectAsStateWithLifecycle()
    var showPopup by remember { mutableStateOf(false) }

    // Formatter example: "Oct 05" and "14:30"
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    IconButton(
        onClick = { showPopup = true },
        modifier = modifier
    ) {
        BadgedBox(
            badge = {
                when(val state=reminders) {
                    ItemStatus.Error -> {}
                    ItemStatus.Loading ->
                        Badge(
                            containerColor = MaterialTheme.colorScheme.onPrimary,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            CircularProgressIndicator()
                        }
                    is ItemStatus.Success ->
                        if (state.elements.isNotEmpty()) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.onPrimary,
                                contentColor = MaterialTheme.colorScheme.primary
                            ) {
                                Text(state.elements.size.toString())
                            }
                        }
                }
            }
        ) {
            Icon(
                imageVector = Icons.Filled.AccessAlarm,
                contentDescription = "Open reminders",
                tint = tint
            )
        }
    }

    if (showPopup) {
        ModalBottomSheet(
            onDismissRequest = { showPopup = false }
        ) {
            Column(modifier = Modifier.padding(bottom = 20.dp)) {

                Text(
                    text = "Reminders",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                when (val state = reminders){
                    ItemStatus.Error -> {}
                    ItemStatus.Loading -> CircularProgressIndicator()
                    is ItemStatus.Success ->
                        if (state.elements.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No active reminders",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn {
                                items(state.elements.sortByTime()) { item ->

                                    ListItem(
                                        // CHANGE 2: Added explicit size and standard icons
                                        leadingContent = {
                                            Icon(
                                                imageVector = when(item.linked?.ofType) {
                                                    LinkedType.TASK  -> Icons.AutoMirrored.Filled.Assignment
                                                    LinkedType.EVENT  -> Icons.Filled.DateRange
                                                    else -> Icons.Filled.AccessAlarm
                                                },
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colorScheme.onPrimary
                                            )
                                        },
                                        headlineContent = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = item.name.ifEmpty { "Untitled" },
                                                    fontWeight = FontWeight.SemiBold
                                                )

                                                // Small Label logic
                                                item.linked?.ofType?.let {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    //todo -> redirect to task activity or so
                                                    SuggestionChip(
                                                        onClick = { /* No-op, just visual */ },
                                                        label = { Text(it.name) },
                                                        modifier = Modifier.height(24.dp),
                                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                                        ),
                                                        border = null // Remove border for cleaner look
                                                    )
                                                }
                                            }
                                        },
                                        supportingContent = {
                                            val dateStr = item.at.toDate().format(dateFormatter)
                                            val timeStr = item.at.toTime()?.format(timeFormatter)
                                            Text(text = dateStr + timeStr?.let { "at $it" })
                                        }
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                }
            }
        }
    }
}