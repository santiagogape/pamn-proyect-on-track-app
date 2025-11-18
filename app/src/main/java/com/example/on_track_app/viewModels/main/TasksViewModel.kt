package com.example.on_track_app.viewModels.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TasksViewModel : ViewModel() {

    private val _text = MutableStateFlow("This is dashboard screen")
    val text: StateFlow<String> = _text

    private val _items = MutableStateFlow(listOf("task1", "task2", "task3"))
    val items: StateFlow<List<String>> = _items

    private val _projectItems = MutableStateFlow(listOf("task1", "task2"))

    fun project(id: String): StateFlow<List<String>>{
        return _projectItems
    }




}