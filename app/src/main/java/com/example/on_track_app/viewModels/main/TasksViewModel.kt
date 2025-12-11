package com.example.on_track_app.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.FirestoreRepository
import com.example.on_track_app.model.Task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class TasksViewModel(
    private val taskRepository: FirestoreRepository<Task>
) : ViewModel() {

    private val _text = MutableStateFlow("Your task list is empty")
    val text: StateFlow<String> = _text

    private val _currentProjectId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<List<Task>> = _currentProjectId
        .flatMapLatest { projectId ->
            if (projectId == null) {
                taskRepository.getElements()
            } else {
                taskRepository.getTasksByProjectId(projectId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setProjectId(id: String?) {
        _currentProjectId.value = id
    }

}