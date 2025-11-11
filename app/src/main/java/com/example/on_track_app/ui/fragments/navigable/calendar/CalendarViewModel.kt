package com.example.on_track_app.ui.fragments.navigable.calendar

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CalendarViewModel : ViewModel() {

    private val _text = MutableStateFlow("This is calendar screen")
    val text: StateFlow<String> = _text
}
