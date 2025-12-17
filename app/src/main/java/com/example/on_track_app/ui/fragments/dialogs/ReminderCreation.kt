package com.example.on_track_app.ui.fragments.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.on_track_app.model.LinkedType
import com.example.on_track_app.model.Selectable
import com.example.on_track_app.ui.fragments.reusable.Selector
import com.example.on_track_app.ui.fragments.reusable.calendar.Calendar
import com.example.on_track_app.ui.fragments.reusable.time.DateTimeField
import com.example.on_track_app.ui.theme.OutlinedTextFieldColors
import com.example.on_track_app.viewModels.main.ItemStatus
import java.time.LocalDate
import java.time.LocalTime


data class ReminderCreationBuilder(
    val sources: Map<LinkedType, ItemStatus<List<Selectable>>>,
    val reminderDefaultSource: Selectable? = null,
    val defaultType: LinkedType? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderCreation(
    builder: ReminderCreationBuilder,
    onDismiss: () -> Unit,
    // onSubmit passes: Name, Desc, linkTo, type, Date, Hour, Minute
    onSubmit: (String, String, String?, LinkedType?, LocalDate, Int, Int) -> Unit
) {
    var calendarOpen by remember { mutableStateOf(false) }

    // Input State
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var selectedType by remember { mutableStateOf<Selectable?>(null) }
    var type by remember { mutableStateOf<LinkedType?>(null) }
    var selected by remember { mutableStateOf<Selectable?>(null) }


    // Date/Time State (Default to now + 1 hour)
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
                    Calendar(
                        mapOf(),
                        { chosen ->
                            date = chosen
                            calendarOpen = false
                        },mapOf()
                    )
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

                        if (builder.reminderDefaultSource != null && builder.defaultType != null) {
                            selected = builder.reminderDefaultSource; type = builder.defaultType
                        } else {
                            val types = builder.sources.keys
                                .map { type -> object:Selectable{
                                    override val id: String = type.name
                                    override val name: String = type.name
                                } }

                            Selector(
                                colors,
                                ItemStatus.Success(types),
                                default = null,
                                label = "Select a type to link",
                                placeholder = "tasks, events, ...",
                                noSelectionLabel = "no link selected",
                                select = { selection ->
                                    selectedType = selection
                                }
                            )

                            selectedType?.let { selection ->
                                Selector(
                                    colors,
                                    builder.sources[LinkedType.valueOf(selection.name)]
                                        ?: ItemStatus.Loading,
                                    null,
                                    label = "select",
                                    placeholder = "link",
                                    noSelectionLabel = "no link selected",
                                ) { selection ->
                                    selected = selection
                                    type = LinkedType.valueOf(selectedType!!.name)
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
                            label = "${
                                date
                                    .toString()
                                    .split("-")
                                    .reversed()
                                    .joinToString("/")
                            }\nAt $hour:${
                                minute
                                    .toString()
                                    .padStart(2, '0')
                            }"
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
                                    selected?.id?.let {
                                        onSubmit(
                                            name,
                                            description,
                                            selected?.id,
                                            type,
                                            date,
                                            hour,
                                            minute
                                        )
                                    }
                                }
                            },
                            enabled = name.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimary,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {

                            Text("Set Reminder")

                        }
                    }
                }
            }
        }
    }
}