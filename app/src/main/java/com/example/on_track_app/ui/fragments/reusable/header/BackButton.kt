package com.example.on_track_app.ui.fragments.reusable.header

import androidx.activity.compose.LocalActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.on_track_app.R

@Composable
fun BackButton() {
    val activity = LocalActivity.current
    IconButton(onClick = { activity?.finish() }) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.get_back)
        )
    }
}
