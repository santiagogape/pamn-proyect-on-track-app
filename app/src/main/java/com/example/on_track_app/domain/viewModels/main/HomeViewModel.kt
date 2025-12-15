package com.example.on_track_app.domain.viewModels.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.on_track_app.data.FirestoreRepository
import com.example.on_track_app.data.auth.GoogleAuthClient
import com.example.on_track_app.domain.usecase.EventManager
import com.example.on_track_app.domain.usecase.TaskManager
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.on_track_app.model.Task
import com.example.on_track_app.ui.fragments.dialogs.CreationStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class HomeViewModel(
    private val eventRepository: FirestoreRepository<Event>,
    private val taskRepository: FirestoreRepository<Task>,
    private val projectRepository: FirestoreRepository<Project>,
    private val taskManager: TaskManager,
    private val eventManager: EventManager,
    private val googleAuthClient: GoogleAuthClient
) : ViewModel() {

    private val userId: String? = googleAuthClient.getUserId()
    private val _text = MutableStateFlow("This is the home screen")
    val text: StateFlow<String> = _text

    val events: StateFlow<List<Event>> = if (userId != null) {
            eventRepository.getElements(userId)
        } else {
            flowOf(emptyList())
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun updateEvent(
        eventId: String,
        name: String,
        description: String,
        projectId: String?,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ) {
        viewModelScope.launch {
            eventManager.updateEvent(
                eventId,
                name,
                description,
                projectId,
                startDate,
                endDate
            )
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch{
            eventManager.deleteEvent(eventId)
        }
    }

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

    private val _creationStatus  = MutableStateFlow<CreationStatus>(CreationStatus.Idle)
    val creationStatus = _creationStatus.asStateFlow()

    val availableProjects: StateFlow<ItemStatus> = if (userId != null) {
        projectRepository.getElements(userId)
    } else {
        flowOf(emptyList())
    }
        .map { projectList ->
            ItemStatus.Success(projectList) as ItemStatus
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

    fun updateTask(
        taskId: String,
        newName: String,
        newDescription: String,
        newDate: LocalDate,
        newHour: Int?,
        newMinute: Int?,
        newProjectId: String?
    ) {
        viewModelScope.launch {
            taskManager.updateTask(taskId, newName, newDescription, newDate, newHour, newMinute, newProjectId)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            taskManager.deleteTask(taskId)
        }
    }

    fun isLoading(): Boolean {
        return _creationStatus.value == CreationStatus.Loading
    }

    fun resetStatus() {
        _creationStatus.value = CreationStatus.Idle
    }
}