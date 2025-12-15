package com.example.on_track_app.ui.fragments.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.on_track_app.ui.fragments.reusable.calendar.Calendar
import com.example.on_track_app.ui.fragments.reusable.time.DateTimeField
import com.example.on_track_app.ui.theme.OutlinedTextFieldColors // Assuming this exists based on your snippet
import java.time.LocalDate
import java.time.LocalTime
// Assuming you have these models. If not, replace with your actual package path
import com.example.on_track_app.model.Task
import com.example.on_track_app.model.Event

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderCreation(
    isLoading: Boolean,
    availableTasks: List<Task>,
    availableEvents: List<Event>,
    onDismiss: () -> Unit,
    // onSubmit passes: Name, Desc, TaskId, EventId, Date, Hour, Minute
    onSubmit: (String, String, String?, String?, LocalDate, Int, Int) -> Unit
) {
    var calendarOpen by remember { mutableStateOf(false) }

    // Input State
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Task Selection State
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var taskDropdownExpanded by remember { mutableStateOf(false) }

    // Event Selection State
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    var eventDropdownExpanded by remember { mutableStateOf(false) }

    // Date/Time State (Default to now + 1 hour)
    var date by remember { mutableStateOf(LocalDate.now()) }
    var hour by remember { mutableIntStateOf(LocalTime.now().hour + 1) }
    var minute by remember { mutableIntStateOf(0) }

    // Logic: Auto-fill name if user selects a task/event and name is empty
    LaunchedEffect(selectedTask) {
        if (selectedTask != null && name.isEmpty()) name = selectedTask?.name ?: ""
    }
    LaunchedEffect(selectedEvent) {
        if (selectedEvent != null && name.isEmpty()) name = selectedEvent?.name ?: ""
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .background(
                    MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // HEADER
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        modifier = Modifier.align(Alignment.CenterStart),
                        onClick = onDismiss
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                    Text(
                        text = "Set Reminder",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (calendarOpen) {
                    Calendar(
                        eventsByDate = emptyMap(), // Pass empty map
                        tasksByDate = emptyMap(),  // Pass empty map
                    ) { chosen ->
                        date = chosen
                        calendarOpen = false
                    }
                } else {
                    // Reuse your custom color wrapper
                    OutlinedTextFieldColors { colors ->

                        // 1. NAME FIELD
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            isError = name.isEmpty(),
                            colors = colors
                        )

                        // 2. DESCRIPTION FIELD (Optional for reminders, but good to have)
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = colors
                        )

                        // 3. TASK DROPDOWN
                        ExposedDropdownMenuBox(
                            expanded = taskDropdownExpanded,
                            onExpandedChange = { taskDropdownExpanded = !taskDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedTask?.name ?: "",
                                onValueChange = {}, // Read-only
                                readOnly = true,
                                label = { Text("Link to Task (Optional)") },
                                placeholder = { Text("Select a task") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = taskDropdownExpanded)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = colors,
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = taskDropdownExpanded,
                                onDismissRequest = { taskDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("None") },
                                    onClick = {
                                        selectedTask = null
                                        taskDropdownExpanded = false
                                    }
                                )
                                availableTasks.forEach { task ->
                                    DropdownMenuItem(
                                        text = { Text(task.name) },
                                        onClick = {
                                            selectedTask = task
                                            taskDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // 4. EVENT DROPDOWN
                        ExposedDropdownMenuBox(
                            expanded = eventDropdownExpanded,
                            onExpandedChange = { eventDropdownExpanded = !eventDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedEvent?.name ?: "",
                                onValueChange = {}, // Read-only
                                readOnly = true,
                                label = { Text("Link to Event (Optional)") },
                                placeholder = { Text("Select an event") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = eventDropdownExpanded)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = colors,
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = eventDropdownExpanded,
                                onDismissRequest = { eventDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("None") },
                                    onClick = {
                                        selectedEvent = null
                                        eventDropdownExpanded = false
                                    }
                                )
                                availableEvents.forEach { event ->
                                    DropdownMenuItem(
                                        text = { Text(event.name) },
                                        onClick = {
                                            selectedEvent = event
                                            eventDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // DATE & TIME PICKER
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        DateTimeField(
                            onOpenCalendar = { calendarOpen = true },
                            onTime = { h, m ->
                                hour = h
                                minute = m
                            },
                            withTime = true,
                            label = "${date.toString().split("-").reversed().joinToString("/")}\nAt $hour:${minute.toString().padStart(2, '0')}"
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // SUBMIT BUTTON
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                if (name.isNotEmpty()) {
                                    onSubmit(
                                        name,
                                        description,
                                        selectedTask?.id,
                                        selectedEvent?.id,
                                        date,
                                        hour,
                                        minute
                                    )
                                }
                            },
                            enabled = !isLoading && name.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimary,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Set Reminder")
                            }
                        }
                    }
                }
            }
        }
    }
}