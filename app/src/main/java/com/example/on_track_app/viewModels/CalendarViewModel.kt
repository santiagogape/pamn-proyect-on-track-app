package com.example.on_track_app.viewModels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CalendarViewModel : ViewModel() {

    private val _text = MutableStateFlow("This is calendar screen")
    val text: StateFlow<String> = _text
    private val _projectItems = MutableStateFlow(listOf("event1", "event2"))

    fun project(id: String): StateFlow<List<String>>{
        return _projectItems
    }
}