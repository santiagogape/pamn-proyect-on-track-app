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
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.example.on_track_app.data.auth.GoogleAuthClient
import com.example.on_track_app.ui.fragments.dialogs.CreationStatus
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.time.LocalTime

class TasksViewModel(
    private val taskRepository: FirestoreRepository<Task>,
    private val googleAuthClient: GoogleAuthClient
) : ViewModel() {

    private val userId: String? = googleAuthClient.getUserId()
    private val _text = MutableStateFlow("Your task list is empty")
    val text: StateFlow<String> = _text

    private val _currentProjectId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<ItemStatus> = _currentProjectId
        .flatMapLatest { projectId ->
            if (projectId == null) {
                if (userId != null) {
                    taskRepository.getElements(userId)
                } else {
                    flowOf(emptyList())
                }
            } else {
                taskRepository.getTasksByProjectId(projectId)
            }
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

    fun setProjectId(id: String?) {
        _currentProjectId.value = id
    }

    private val _creationStatus  = MutableStateFlow<CreationStatus>(CreationStatus.Idle)
    val creationStatus = _creationStatus.asStateFlow()

    fun createTask(
        name: String,
        description: String,
        date: LocalDate,
        hour: Int?,
        minute: Int?,
        projectId: String?
    ) {
        viewModelScope.launch {
            val currentUserId =
                googleAuthClient.getUserId() ?: // Handle error: User is not logged in
                return@launch

            val timeString = if (hour != null && minute != null) {
                LocalTime.of(hour, minute).toString()
            } else {
                null
            }

            val newTask = Task(
                userId = currentUserId,
                name = name,
                description = description,
                dateIso = date.toString(),
                timeIso = timeString,
                projectId = projectId
            )

            // 3. Send to Repository
            val success = taskRepository.addElement(newTask)

            if (success) {
                _creationStatus.value = CreationStatus.Success
            } else {
                _creationStatus.value = CreationStatus.Error("Failed to save task to database")
            }
        }
    }

    fun resetStatus() {
        _creationStatus.value = CreationStatus.Idle
    }

}