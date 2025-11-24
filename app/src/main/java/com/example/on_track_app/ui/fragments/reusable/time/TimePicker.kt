package com.example.on_track_app.ui.fragments.reusable.time

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.on_track_app.ui.theme.OutlinedTextFieldColors
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

    // Campo visible
    OutlinedTextFieldColors {
        colors ->
            OutlinedTextField(
                value = "%02d:%02d".format(timeState.hour, timeState.minute),
                onValueChange = {},
                readOnly = true,
                label = { Text("Hora") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.AccessTime, contentDescription = null)
                    }
                },
                colors = colors
            )
    }


    if (showDialog) {

        ButtonColors {
            colors ->
                TimePickerDialog(
                    onDismissRequest = {  onDismiss(); showDialog = false },

                    // ⬅️ Nuevo parámetro requerido
                    title = {
                        Text(
                            "Selecciona hora",
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
                            Text("Cancelar")
                        }
                    }
                ) {
                    TimePicker(state = timeState)
                }
        }

    }
}


