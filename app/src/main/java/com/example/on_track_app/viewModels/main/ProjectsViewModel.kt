package com.example.on_track_app.viewModels.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProjectsViewModel : ViewModel() {

    private val _text = MutableStateFlow("This is projects screen")
    val text: StateFlow<String> = _text

    private val _items = MutableStateFlow(listOf("project1", "project2", "project3"))
    val items: StateFlow<List<String>> = _items


}