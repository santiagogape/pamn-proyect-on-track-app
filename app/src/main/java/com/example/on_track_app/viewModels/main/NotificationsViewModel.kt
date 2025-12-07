package com.example.on_track_app.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.FirestoreRepository
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class NotificationsViewModel(
    private val projectRepository: FirestoreRepository<Project>,
    private val taskRepository: FirestoreRepository<Task>
) : ViewModel() {

    private val _text = MutableStateFlow("This is notifications screen")
    val text: StateFlow<String> = _text

    val items: StateFlow<List<Project>> = projectRepository.getElements()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // TODO: Clarify what type of object this will be (now as Task as placeholder), create notification model?
    fun project(id: String): StateFlow<List<Task>>{
        return taskRepository.getTasksByProjectId(id)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )
    }
}