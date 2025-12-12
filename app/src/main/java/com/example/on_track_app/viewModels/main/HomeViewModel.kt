package com.example.on_track_app.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.FirestoreRepository
import com.example.on_track_app.data.auth.GoogleAuthClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.on_track_app.model.Task
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val taskRepository: FirestoreRepository<Task>,
    private val googleAuthClient: GoogleAuthClient
) : ViewModel() {

    private val userId: String? = googleAuthClient.getUserId()
    private val _text = MutableStateFlow("This is home screen")
    val text: StateFlow<String> = _text

    val tasks: StateFlow<ItemStatus> = if (userId != null) {
            taskRepository.getElements(userId)
        } else {
            flowOf(emptyList())
        }
        .map { taskList ->
        ItemStatus.Success(taskList) as ItemStatus
    }
        .onStart {
            emit(ItemStatus.Loading)
        }
        .catch {
            emit(ItemStatus.Error)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ItemStatus.Loading
        )
}