package com.example.on_track_app.ui.fragments.reusable.header

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Importante para el color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenRemindersIconButton(
    modifier: Modifier = Modifier, // Ahora acepta modificadores de posición/tamaño
    tint: Color = LocalContentColor.current // Ahora acepta un color personalizado (opcional)
) {

    var showPopup by remember { mutableStateOf(false) }

    // Pasamos el 'modifier' al IconButton para que el padre (Header) pueda controlarlo si es necesario
    IconButton(
        onClick = { showPopup = true },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Filled.AccessAlarm,
            contentDescription = "Open reminders",
            tint = tint // Usamos el color pasado como argumento
        )
    }

    if (showPopup) {
        ModalBottomSheet(
            onDismissRequest = { showPopup = false }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text = "Recordatorios",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val reminders = listOf(
                    "Buy bread",
                    "Finish Project1",
                    "Call grandma",
                    "Start Practice 9 IG"
                )

                LazyColumn {
                    items(reminders) { item ->
                        Text(
                            text = "• $item",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}