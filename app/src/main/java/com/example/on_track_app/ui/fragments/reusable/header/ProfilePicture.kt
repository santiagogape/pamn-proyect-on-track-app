package com.example.on_track_app.ui.fragments.reusable.header

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun ProfilePicture(url: String, modifier: Modifier = Modifier) {
    AsyncImage(
        model = url,
        contentDescription = "User",
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
    )
}
