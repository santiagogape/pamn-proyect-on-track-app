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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import java.time.LocalDate.now

@Composable
fun TaskCreation(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String?, LocalDate, Int?, Int?) -> Unit
) {
    var deadlineOpen by remember {mutableStateOf(false)}

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var project by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(now()) }
    var hour by remember { mutableIntStateOf(-1) }
    var minute by remember { mutableIntStateOf(-1) }
    var pickHour by remember {mutableStateOf(false)}

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

                if (deadlineOpen){
                    Calendar(mapOf()) { chosen ->
                        date = chosen
                        deadlineOpen = false
                    }
                } else {
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


                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        DateTimeField(
                            onOpenCalendar = { deadlineOpen = true },
                            onTime = { h, m -> hour=h; minute = m },
                            withTime = pickHour,
                            label = "Deadline\n" + date.toString().split("-").reversed().joinToString("/")
                        )
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
                                onClick = {pickHour = !pickHour},
                                colors = colors,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("with time")
                                if (pickHour) Icon(Icons.Filled.Check,null)
                                else Icon(Icons.Filled.Close,null)
                            }
                        }

                        // SUBMIT
                        Button(
                            onClick = {
                                if (name.isNotEmpty() && description.isNotEmpty()) {
                                    onSubmit(
                                        name,
                                        description,
                                        project.ifBlank { null },
                                        date,
                                        if (hour != -1) hour else null,
                                        if (minute != -1) hour else null
                                    )
                                }
                            },
                            enabled = name.isNotEmpty() && description.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimary,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Submit")
                        }
                    }
                }
            }


        }
    }
}