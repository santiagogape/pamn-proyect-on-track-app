package com.example.on_track_app.viewModels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NotificationsViewModel : ViewModel() {

    private val _text = MutableStateFlow("This is notifications screen")
    val text: StateFlow<String> = _text

    private val _items = MutableStateFlow(listOf("project1", "project2", "project3"))
    val items: StateFlow<List<String>> = _items

    private val _projectItems = MutableStateFlow(listOf("notice1", "notice2"))

    fun project(id: String): StateFlow<List<String>>{
        return _projectItems
    }
}