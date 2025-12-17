package com.example.on_track_app.ui.fragments.reusable

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.on_track_app.model.Selectable
import com.example.on_track_app.utils.DebugLogcatLogger
import com.example.on_track_app.viewModels.main.ItemStatus


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> Selector(
    colors: TextFieldColors,
    sources: ItemStatus<List<T>>,
    default: T?,
    label: String,
    placeholder: String,
    noSelectionLabel:String,
    select: (T?) -> Unit
) where T: Selectable {
    var selectedProject by remember { mutableStateOf(default) }
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedProject?.name ?: default?.name ?: noSelectionLabel,
            onValueChange = {}, // Read-only, handled by menu
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            shape = RoundedCornerShape(12.dp),
            colors = colors,
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable,true) // Important for M3 dropdown positioning
                .fillMaxWidth()
        )
        when(sources){
            ItemStatus.Error -> {}
            ItemStatus.Loading ->
                Row {
                    Text("Loading sources")
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                }

            is ItemStatus.Success ->
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(noSelectionLabel) },
                        onClick = {
                            select(null)
                            expanded = false
                            DebugLogcatLogger.log("No selection")
                        }
                    )

                    sources.elements.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.name) },
                            onClick = {
                                select(item)
                                selectedProject = item
                                expanded = false
                                DebugLogcatLogger.log("Selection: ${item.name}, ${item.id}")
                            }
                        )
                    }
                }
        }

    }
}