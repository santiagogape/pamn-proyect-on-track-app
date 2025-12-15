package com.example.on_track_app.ui.fragments.reusable.header

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.on_track_app.model.Expandable
import com.example.on_track_app.model.Reminder
import java.time.LocalDate

@Composable
private fun Header(
    startContent: (@Composable RowScope.() -> Unit)? = null,
    centerContent: @Composable () -> Unit,
    endContent: (@Composable RowScope.() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // LEFT
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            startContent?.invoke(this)
        }

        // CENTER
        Box(modifier = Modifier.align(Alignment.Center)) {
            centerContent()
        }

        // RIGHT
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            endContent?.invoke(this)
        }
    }
}


@Composable
fun MainHeader(
    label: String,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    reminders: List<Reminder>,
    pfpUrl: String?,
    onLogout: () -> Unit // <--- 1. New Parameter
) {
    Header(
        startContent = { OpenRemindersIconButton(reminders) },
        centerContent = {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        endContent = {
            ThemeToggleIconButton(darkTheme, onToggleTheme)

            if (pfpUrl != null) {
                // 2. State for the menu
                var menuExpanded by remember { mutableStateOf(false) }

                // 3. Wrap in Box to anchor the DropdownMenu
                Box {
                    ProfilePicture(
                        url = pfpUrl,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clickable { menuExpanded = true } // Open menu on click
                    )

                    // 4. The Dropdown Menu
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Log out") },
                            onClick = {
                                menuExpanded = false
                                onLogout() // <--- Trigger the logout
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Log out"
                                )
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun ProjectsHeader(
    label: String,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    projectReminders: List<Expandable>,
    pfpUrl: String? = null
) {
    Header(
        startContent = {
            BackButton()
            OpenRemindersIconButton(projectReminders as List<Reminder>)
        },
        centerContent = {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        endContent = {
            ThemeToggleIconButton(darkTheme, onToggleTheme)
            if (pfpUrl != null) ProfilePicture(pfpUrl)
        }
    )
}


@Composable
fun AgendaHeader(
    date: LocalDate,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    reminders: List<Reminder>,
    pfpUrl: String?
) {
    Header(
        startContent = {
            BackButton()
            OpenRemindersIconButton(reminders)
        },
        centerContent = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = date.dayOfWeek.name.lowercase().replaceFirstChar { it.titlecase() },
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = date.toString().split("-").reversed().joinToString("/"),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        endContent = {
            ThemeToggleIconButton(darkTheme, onToggleTheme)
            if (pfpUrl != null) ProfilePicture(pfpUrl)
        }
    )
}