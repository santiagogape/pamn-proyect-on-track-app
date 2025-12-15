package com.example.on_track_app.ui.fragments.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.Task
import com.example.on_track_app.ui.fragments.reusable.calendar.Calendar
import com.example.on_track_app.ui.fragments.reusable.time.DateTimeField
import com.example.on_track_app.ui.theme.ButtonColors
import com.example.on_track_app.ui.theme.OutlinedTextFieldColors
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCreation(
    isLoading: Boolean,
    availableProjects: List<Project>,
    existingTask: Task? = null,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String?, LocalDate, Int?, Int?) -> Unit
) {
    var deadlineOpen by remember { mutableStateOf(false) }

    var name by remember(existingTask) { mutableStateOf(existingTask?.name ?: "") }

    var description by remember(existingTask) { mutableStateOf(existingTask?.description ?: "") }

    var selectedProject by remember(existingTask) {
        mutableStateOf(availableProjects.find { it.id == existingTask?.projectId })
    }
    var projectDropdownExpanded by remember { mutableStateOf(false) }

    var date by remember(existingTask) { mutableStateOf(existingTask?.date ?: LocalDate.now()) }

    var hour by remember(existingTask) { mutableIntStateOf(existingTask?.time?.hour ?: -1) }
    var minute by remember(existingTask) { mutableIntStateOf(existingTask?.time?.minute ?: -1) }

    var pickHour by remember(existingTask) { mutableStateOf(existingTask?.time != null) }

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
            )
            {
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
                        text = if (existingTask != null) "Edit Task" else "New Task",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (deadlineOpen) {
                    // CALENDAR VIEW
                    Calendar(
                        eventsByDate = emptyMap(), // Pass empty map
                        tasksByDate = emptyMap(),  // Pass empty map
                    ) { chosen ->
                        date = chosen
                        deadlineOpen = false
                    }
                } else {
                    OutlinedTextFieldColors { colors ->
                        // Name Input
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

                        // Description Input
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 80.dp, max = 250.dp),
                            shape = RoundedCornerShape(12.dp),
                            isError = description.isEmpty(),
                            colors = colors
                        )

                        // Project Dropdown
                        ExposedDropdownMenuBox(
                            expanded = projectDropdownExpanded,
                            onExpandedChange = { projectDropdownExpanded = !projectDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedProject?.name ?: "",
                                onValueChange = {}, // Read-only
                                readOnly = true,
                                label = { Text("Project (optional)") },
                                placeholder = { Text("Select a project") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = projectDropdownExpanded)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = colors,
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = projectDropdownExpanded,
                                onDismissRequest = { projectDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("None") },
                                    onClick = {
                                        selectedProject = null
                                        projectDropdownExpanded = false
                                    }
                                )

                                availableProjects.forEach { project ->
                                    DropdownMenuItem(
                                        text = { Text(project.name) },
                                        onClick = {
                                            selectedProject = project
                                            projectDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Date & Time Selectors
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        DateTimeField(
                            onOpenCalendar = { deadlineOpen = true },
                            onTime = { h, m -> hour = h; minute = m },
                            withTime = pickHour,
                            label = "Deadline\n" + date.toString().split("-").reversed()
                                .joinToString("/")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ACTIONS ROW
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    )
                    {
                        // Toggle Time Button
                        ButtonColors { colors ->
                            Button(
                                onClick = { pickHour = !pickHour },
                                colors = colors,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("with time")
                                if (pickHour) Icon(Icons.Filled.Check, null)
                                else Icon(Icons.Filled.Close, null)
                            }
                        }

                        // Submit / Save Button
                        Button(
                            onClick = {
                                if (name.isNotEmpty() && description.isNotEmpty()) {
                                    onSubmit(
                                        name,
                                        description,
                                        selectedProject?.id,
                                        date,
                                        // Only pass time if the user checked "with time" and selected a valid time
                                        if (pickHour && hour != -1) hour else null,
                                        if (pickHour && minute != -1) minute else null
                                    )
                                }
                            },
                            enabled = !isLoading && name.isNotEmpty() && description.isNotEmpty(),
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
                                // Text changes based on mode
                                Text(if (existingTask != null) "Save" else "Submit")
                            }
                        }
                    }
                }
            }
        }
    }
}