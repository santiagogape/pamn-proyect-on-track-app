package com.example.on_track_app.ui.fragments.navigable.dashboard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DashboardViewModel : ViewModel() {

    private val _text = MutableStateFlow("This is dashboard screen")
    val text: StateFlow<String> = _text
}
