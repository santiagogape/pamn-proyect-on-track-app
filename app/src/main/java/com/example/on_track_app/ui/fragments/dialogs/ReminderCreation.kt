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
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun ReminderCreation(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    // onSubmit passes: Date, Hour, Minute
    onSubmit: (LocalDate, Int, Int) -> Unit
) {
    var calendarOpen by remember { mutableStateOf(false) }

    // Default to today and current hour + 1 (so it's in the future)
    var date by remember { mutableStateOf(LocalDate.now()) }
    var hour by remember { mutableIntStateOf(LocalTime.now().hour + 1) }
    var minute by remember { mutableIntStateOf(0) }

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
                    // Reusing your Calendar composable
                    Calendar(mapOf()) { chosen ->
                        date = chosen
                        calendarOpen = false
                    }
                } else {
                    Text(
                        text = "When should we remind you?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // DATE & TIME PICKER
                    // Using "withTime = true" hardcoded because Reminder model requires time
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
                            withTime = true, // Reminders always need time
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
                                onSubmit(date, hour, minute)
                            },
                            enabled = !isLoading,
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