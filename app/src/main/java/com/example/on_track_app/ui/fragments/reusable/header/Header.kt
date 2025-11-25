package com.example.on_track_app.ui.fragments.reusable.header

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    pfpUrl: String?
) {
    Header(
        startContent = {  OpenRemindersIconButton {  } },
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
fun ProjectsHeader(
    label: String,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    pfpUrl: String? = null
) {
    Header(
        startContent = { BackButton(); OpenRemindersIconButton {  } },
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
    pfpUrl: String?
) {
    Header(
        startContent = { BackButton(); OpenRemindersIconButton {  } },
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
