package com.example.on_track_app.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.on_track_app.model.Task
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val taskRepository: FirestoreRepository<Task>
) : ViewModel() {
    private val _text = MutableStateFlow("This is home screen")
    val text: StateFlow<String> = _text

    val tasks: StateFlow<List<Task>> = taskRepository.getElements()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
}