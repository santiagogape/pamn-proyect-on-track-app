package com.example.on_track_app.ui.fragments.reusable.header

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.Assignment // Better icon for Tasks
import androidx.compose.material.icons.filled.DateRange  // Better icon for Events
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.on_track_app.model.Reminder
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenRemindersIconButton(
    reminders: List<Reminder>,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
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
                if (reminders.isNotEmpty()) {
                    Badge(
                        // CHANGE 1: Use a soft secondary color, not Red/Error
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(reminders.size.toString())
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

                if (reminders.isEmpty()) {
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
                        items(reminders) { item ->
                            val isLinkedToTask = !item.taskId.isNullOrEmpty()
                            val isLinkedToEvent = !item.eventId.isNullOrEmpty()

                            ListItem(
                                // CHANGE 2: Added explicit size and standard icons
                                leadingContent = {
                                    Icon(
                                        imageVector = when {
                                            isLinkedToTask -> Icons.AutoMirrored.Filled.Assignment
                                            isLinkedToEvent -> Icons.Filled.DateRange
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
                                        if (isLinkedToTask) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            SuggestionChip(
                                                onClick = { /* No-op, just visual */ },
                                                label = { Text("Task") },
                                                modifier = Modifier.height(24.dp),
                                                colors = SuggestionChipDefaults.suggestionChipColors(
                                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                                ),
                                                border = null // Remove border for cleaner look
                                            )
                                        }
                                        if (isLinkedToEvent) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            SuggestionChip(
                                                onClick = { /* No-op, just visual */ },
                                                label = { Text("Event") },
                                                modifier = Modifier.height(24.dp),
                                                colors = SuggestionChipDefaults.suggestionChipColors(
                                                    containerColor = MaterialTheme.colorScheme.background,
                                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                                ),
                                                border = null // Remove border for cleaner look
                                            )
                                        }
                                    }
                                },
                                supportingContent = {
                                    val dateStr = item.date.format(dateFormatter)
                                    val timeStr = item.time.format(timeFormatter)
                                    Text(text = "$dateStr at $timeStr")
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