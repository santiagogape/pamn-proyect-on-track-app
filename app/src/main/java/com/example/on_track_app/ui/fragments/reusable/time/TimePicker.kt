package com.example.on_track_app.ui.fragments.reusable.time

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.on_track_app.ui.theme.ButtonColors
import java.time.LocalTime.now

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    onDismiss: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val now = now()
    val timeState = rememberTimePickerState(
        initialHour = now.hour,
        initialMinute = now.minute,
        is24Hour = true
    )

    ButtonColors {
        colors ->
            Button(
                onClick = { showDialog = true },
                colors = colors,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("%02d:%02d".format(timeState.hour, timeState.minute))
            }
            if (showDialog) {
                TimePickerDialog(
                    onDismissRequest = {  onDismiss(); showDialog = false },

                    title = {
                        Text(
                            "Select hour",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    },

                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDialog = false
                                onTimeSelected(timeState.hour, timeState.minute)
                            },
                            colors = colors
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }, colors = colors) {
                            Text("Cancel")
                        }
                    }
                ) {
                    TimePicker(state = timeState)
                }
            }
    }
}


