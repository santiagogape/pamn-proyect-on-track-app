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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.on_track_app.R
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.TimeField
import com.example.on_track_app.model.toDate
import com.example.on_track_app.model.toTime
import com.example.on_track_app.ui.fragments.reusable.Selector
import com.example.on_track_app.ui.fragments.reusable.calendar.Calendar
import com.example.on_track_app.ui.fragments.reusable.time.DateTimeField
import com.example.on_track_app.ui.theme.ButtonColors
import com.example.on_track_app.ui.theme.OutlinedTextFieldColors
import com.example.on_track_app.utils.DebugLogcatLogger
import com.example.on_track_app.viewModels.ConsultProject
import com.example.on_track_app.viewModels.ModifyEvent
import com.example.on_track_app.viewModels.main.ItemStatus
import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDate.now as today
import java.time.LocalTime.now as currently

enum class DateType {
    START,END
}

@Composable
fun EventCreation(
    defaultProject: Project? = null,
    availableProjects: ItemStatus<List<Project>>,
    onDismiss: () -> Unit,
    onSubmit: (String, String, Project?, TimeField, TimeField) -> Unit,
    existingEvent: Event? = null,
    currentDate: LocalDate? = null
) {
    var deadlineOpen: DateType? by remember {mutableStateOf(null)}
    var oneDayEvent by remember { mutableStateOf(existingEvent?.onDayEvent ?: false) }

    var startDate: LocalDate by remember { mutableStateOf(existingEvent?.start?.toDate() ?: currentDate ?: today()) }
    var endDate: LocalDate by remember { mutableStateOf(existingEvent?.end?.toDate() ?: currentDate?.plusDays(1) ?: today().plusDays(1)) }

    var startTime by remember { mutableStateOf(existingEvent?.start?.toTime() ?: currently()) }
    var endTime by remember { mutableStateOf(existingEvent?.end?.toTime() ?:currently()) }

    var name by remember { mutableStateOf(existingEvent?.name ?:"") }
    var description by remember { mutableStateOf(existingEvent?.description?:"") }
    var project by remember { mutableStateOf(defaultProject) }
    val start: TimeField by remember { derivedStateOf {
        if (oneDayEvent) TimeField(startDate)
        else TimeField(startDate,startTime.hour, startTime.minute)
    }}
    val end: TimeField by remember { derivedStateOf {
        if (oneDayEvent) TimeField(startDate)
        else TimeField(endDate,endTime.hour, endTime.minute)
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
                        text = "New Event",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                when (deadlineOpen){
                    DateType.START -> 
                        Calendar(
                            mapOf(),
                            { chosen ->
                                startDate = chosen
                                deadlineOpen = null
                            },mapOf()
                        )

                    DateType.END ->
                        Calendar(
                            mapOf(),
                            { chosen ->
                                endDate = chosen
                                deadlineOpen = null
                            },mapOf()
                        )

                    null ->
                        {
                            OutlinedTextFieldColors {
                                    colors ->
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text(stringResource(R.string.TEXT_FIELD_LABEL_NAME)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    isError = name.isEmpty(),
                                    colors = colors
                                )

                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    label = { Text(stringResource(R.string.TEXT_FIELD_LABEL_DESCRIPTION)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 80.dp, max = 250.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    isError = description.isEmpty(),
                                    colors = colors
                                )

                                Selector(
                                    colors,
                                    availableProjects,
                                    defaultProject,
                                    stringResource(R.string.select_a_project_optional),
                                    stringResource(R.string.selection),
                                    stringResource(R.string.no_project_selected)
                                ){  DebugLogcatLogger.log("received selected $it")
                                    project = it }
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
                                    label = stringResource(R.string.start) +"\n" + startDate.toString().split("-").reversed().joinToString("/")
                                )

                                if (!oneDayEvent) {
                                    DateTimeField(
                                        onOpenCalendar = { deadlineOpen = DateType.END },
                                        onTime = { h, m -> endTime = LocalTime.of(h, m) },
                                        withTime = true,
                                        label = stringResource(R.string.end) + "\n" + endDate.toString().split("-").reversed().joinToString("/")
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
                                        Text( if (!oneDayEvent) stringResource(R.string.just_one_whole_day) else stringResource(
                                            R.string.choose_both_limits_instead
                                        ))
                                        if (oneDayEvent) Icon(Icons.Filled.Check,null)
                                        else Icon(Icons.Filled.Close,null)
                                    }
                                }
                                // SUBMIT
                                Button(
                                    onClick = {
                                        DebugLogcatLogger.log("project $project")
                                        onSubmit(
                                            name, description, project,
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
                                    Text(stringResource(R.string.submit))

                                }
                            }
                        }
                }
                
            }


        }
    }
}

@Composable
fun <T> EditEvent(
    eventToEdit: Event?,
    state: ItemStatus<List<Project>>,
    viewModel: T,
    dismiss: () -> Unit,
) where T: ModifyEvent, T: ConsultProject {
    if (eventToEdit != null){
        EventCreation(
            defaultProject = eventToEdit.project,
            availableProjects = state,
            onDismiss = { dismiss() },
            onSubmit = { name, desc, projId, start, end ->
                viewModel.update(
                    eventToEdit.update(
                        name, desc, start, end, projId
                    )
                )
                dismiss()
            },
            existingEvent = eventToEdit,
        )
    }
}