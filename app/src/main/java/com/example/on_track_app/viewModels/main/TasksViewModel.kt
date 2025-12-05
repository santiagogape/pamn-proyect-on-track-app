package com.example.on_track_app.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.FirestoreRepository
import com.example.on_track_app.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class TasksViewModel(
    private val taskRepository: FirestoreRepository<Task>
) : ViewModel() {

    private val _text = MutableStateFlow("This is dashboard screen")
    val text: StateFlow<String> = _text

    val items: StateFlow<List<Task>> = taskRepository.getElements()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun project(id: String): StateFlow<List<Task>>{
        return taskRepository.getTasksByProjectId(id)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )
    }




}