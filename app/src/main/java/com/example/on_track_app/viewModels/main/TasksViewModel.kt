package com.example.on_track_app.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.abstractions.repositories.TaskRepository
import com.example.on_track_app.model.MockTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class TasksViewModel(private val repo: TaskRepository) : ViewModel() {

    private val _text = MutableStateFlow("This is dashboard screen")
    val text: StateFlow<String> = _text


    val tasks: StateFlow<List<MockTask>> = this.repo.getAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun byProject(id: String): StateFlow<List<MockTask>> = this.repo.byProject(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())





}