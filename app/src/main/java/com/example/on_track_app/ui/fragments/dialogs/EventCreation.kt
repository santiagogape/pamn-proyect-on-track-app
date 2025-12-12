package com.example.on_track_app.ui.fragments.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.on_track_app.ui.fragments.reusable.calendar.Calendar
import com.example.on_track_app.ui.fragments.reusable.time.DateTimeField
import com.example.on_track_app.ui.theme.ButtonColors
import com.example.on_track_app.ui.theme.OutlinedTextFieldColors
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.LocalDate.now as today
import java.time.LocalTime.now as currently

enum class DateType {
    START,END
}

@Composable
fun EventCreation(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String?, LocalDateTime, LocalDateTime) -> Unit
) {
    var deadlineOpen: DateType? by remember {mutableStateOf(null)}
    var oneDayEvent by remember { mutableStateOf(false) }

    var startDate: LocalDate by remember { mutableStateOf(today()) }
    var endDate: LocalDate by remember { mutableStateOf(today().plusDays(1)) }

    var startTime by remember { mutableStateOf(currently()) }
    var endTime by remember { mutableStateOf(currently()) }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var project by remember { mutableStateOf("") }
    val start: LocalDateTime by remember { derivedStateOf {
        if (oneDayEvent) LocalDateTime.of(startDate, LocalTime.of(0,0))
        else LocalDateTime.of(startDate,startTime)
    }}
    val end: LocalDateTime by remember { derivedStateOf {
        if (oneDayEvent) LocalDateTime.of(endDate, LocalTime.of(23,59))
        else LocalDateTime.of(endDate,endTime)
    }}


    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
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
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ){
                    IconButton(
                        modifier = Modifier.align(Alignment.CenterStart),
                        onClick = onDismiss) {
                        Icon(Icons.Filled.Close,null)
                    }
                    // TITLE
                    Text(
                        text = "New Task",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                when (deadlineOpen){
                    DateType.START -> 
                        Calendar(mapOf()) { chosen ->
                            startDate = chosen
                            deadlineOpen = null
                        }
                    DateType.END -> 
                        Calendar(mapOf()) { chosen ->
                            endDate = chosen
                            deadlineOpen = null
                        }
                    null -> 
                        {
                            OutlinedTextFieldColors {
                                    colors ->
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

                                OutlinedTextField(
                                    value = project,
                                    onValueChange = { project = it },
                                    label = { Text("Project (optional)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = colors
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    if (oneDayEvent) Arrangement.Center
                                    else  Arrangement.SpaceBetween
                            ){

                                DateTimeField(
                                    onOpenCalendar = { deadlineOpen = DateType.START },
                                    onTime = { h, m -> startTime = LocalTime.of(h, m) },
                                    withTime = !oneDayEvent,
                                    label = "Start\n" + startDate.toString().split("-").reversed().joinToString("/")
                                )

                                if (!oneDayEvent) {
                                    DateTimeField(
                                        onOpenCalendar = { deadlineOpen = DateType.END },
                                        onTime = { h, m -> endTime = LocalTime.of(h, m) },
                                        withTime = true,
                                        label = "End\n" + endDate.toString().split("-").reversed().joinToString("/")
                                    )
                                }
                            }



                            Spacer(modifier = Modifier.height(8.dp))


                            // BUTTON ROW
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            )
                            {
                                ButtonColors {
                                        colors ->
                                    Button(
                                        onClick = {oneDayEvent = !oneDayEvent},
                                        colors = colors,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("one day")
                                        if (oneDayEvent) Icon(Icons.Filled.Check,null)
                                        else Icon(Icons.Filled.Close,null)
                                    }
                                }
                                // SUBMIT
                                Button(
                                    onClick = {
                                        onSubmit(
                                            name, description, project.ifBlank { null },
                                            start,end
                                        )
                                    },
                                    enabled = name.isNotEmpty() && description.isNotEmpty(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.onPrimary,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isLoading) {
                                        androidx.compose.material3.CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("Submit")
                                    }
                                }
                            }
                        }
                }
                
            }


        }
    }
}

