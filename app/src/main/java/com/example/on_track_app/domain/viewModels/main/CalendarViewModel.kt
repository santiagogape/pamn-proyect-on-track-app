package com.example.on_track_app.domain.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.FirestoreRepository
import com.example.on_track_app.data.auth.GoogleAuthClient
import com.example.on_track_app.domain.usecase.EventManager
import com.example.on_track_app.domain.usecase.TaskManager
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Expandable
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.Reminder
import com.example.on_track_app.model.Task
import com.example.on_track_app.ui.fragments.dialogs.CreationStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.collections.filterIsInstance

class CalendarViewModel(
    private val reminderRepository: FirestoreRepository<Reminder>,
    private val taskRepository: FirestoreRepository<Task>,
    private val eventRepository: FirestoreRepository<Event>,
    private val projectRepository: FirestoreRepository<Project>,
    private val taskManager: TaskManager,
    private val eventManager: EventManager,
    private val googleAuthClient: GoogleAuthClient
) : ViewModel() {

    private val userId: String? = googleAuthClient.getUserId()
    private val _text = MutableStateFlow("There are no tasks or events for today")
    val text: StateFlow<String> = _text

    val reminders: StateFlow<List<Reminder>> = if (userId != null) {
        reminderRepository.getElements(userId)
    } else {
        flowOf(emptyList())
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _currentProjectId = MutableStateFlow<String?>(null)

    val events: StateFlow<List<Expandable>> = _currentProjectId
        .flatMapLatest { projectId ->
            if (projectId == null) {
                if (userId != null) {
                    eventRepository.getElements(userId)
                } else {
                    flowOf(emptyList())
                }
            } else {
                eventRepository.getElementsByProjectId(projectId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        ) as StateFlow<List<Event>>

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
    val eventsByDates: StateFlow<Map<LocalDate, List<Event>>> = eventManager.getEventsByDate(viewModelScope, events)

    fun eventsFor(date: LocalDate): StateFlow<List<Event>> {
        return eventManager.getEventsForDate(
            sourceMap = eventsByDates,
            date = date,
            scope = viewModelScope
        )
    }

    private val _creationStatus  = MutableStateFlow<CreationStatus>(CreationStatus.Idle)
    val creationStatus = _creationStatus.asStateFlow()

    fun createEvent(
        name: String,
        description: String,
        projectId: String?,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ) {
        viewModelScope.launch {
            val currentUserId =
                googleAuthClient.getUserId() ?: // Handle error: User is not logged in
                return@launch

            val success = eventManager.createEvent(
                currentUserId, name, description, projectId, startDate, endDate
            )

            if (success) {
                _creationStatus.value = CreationStatus.Success
            } else {
                _creationStatus.value = CreationStatus.Error("Failed to save event to database")
            }
        }
    }



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

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<List<Expandable>> = _currentProjectId
        .flatMapLatest { projectId ->
            if (projectId == null) {
                if (userId != null) {
                    taskRepository.getElements(userId)
                } else {
                    flowOf(emptyList())
                }
            } else {
                // If ID exists, switch to specific query
                taskRepository.getElementsByProjectId(projectId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        ) as StateFlow<List<Task>>

    fun setProjectId(id: String?) {
        _currentProjectId.value = id
    }

    val taskByDates: StateFlow<Map<LocalDate, List<Task>>> = taskManager.getTasksByDate(viewModelScope, tasks)

    fun tasksFor(date: LocalDate): StateFlow<List<Task>> {
        return taskManager.getTasksForDate(
            sourceMap = taskByDates,
            date = date,
            scope = viewModelScope
        )
    }

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