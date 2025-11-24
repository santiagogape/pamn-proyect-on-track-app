package com.example.on_track_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.FirestoreService
import com.example.on_track_app.data.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel: ViewModel() {
    private val repo = FirestoreRepository(FirestoreService.firestore)

    private val _tasks = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val tasks: StateFlow<List<Map<String, Any>>> = _tasks

    fun loadTasks() {

    }
}