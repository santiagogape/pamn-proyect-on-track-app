package com.example.on_track_app.ui.fragments.reusable.header

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.on_track_app.R
import com.example.on_track_app.utils.Language

@Composable
fun ProfilePicture(
    url: String?,
    currentLanguage: Language,
    onSettingsClick: (Language) -> Unit,
    onLogoutClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }


    Box(
        contentAlignment = Alignment.Center
    ) {
        val imageModifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .clickable { showMenu = true }

        if (url.isNullOrBlank()) {
            Box(modifier = imageModifier, contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Default Profile",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = "User Profile",
                contentScale = ContentScale.Crop,
                modifier = imageModifier,
                error = null
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.language)) },
                onClick = { showLanguageMenu = true },
                leadingIcon = {
                    Icon(
                        painter = painterResource(
                            if (currentLanguage == Language.ES) R.drawable.es
                            else R.drawable.uk
                        ),
                        contentDescription = null,
                        modifier = imageModifier
                    )
                }
            )

            DropdownMenuItem(
                text = { Text("Sign out") },
                onClick = {
                    showMenu = false
                    onLogoutClick()
                },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) }
            )
        }

        DropdownMenu(
            expanded = showLanguageMenu,
            onDismissRequest = { showLanguageMenu = false }
        ) {

            DropdownMenuItem(
                text = { Text(stringResource(R.string.english)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.uk),
                        contentDescription = null,
                        modifier = imageModifier
                    )
                },
                onClick = {

                    onSettingsClick(Language.ENG)
                }
            )

            DropdownMenuItem(
                text = { Text(stringResource(R.string.spanish)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.es),
                        contentDescription = null,
                        modifier = imageModifier
                    )
                },
                onClick = {
                    onSettingsClick(Language.ES)
                }
            )
        }



    }
}