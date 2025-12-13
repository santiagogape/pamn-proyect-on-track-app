package com.example.on_track_app.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.FirestoreRepository
import com.example.on_track_app.data.auth.GoogleAuthClient
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Expandable
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.Task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class CalendarViewModel(
    private val projectRepository: FirestoreRepository<Project>,
    private val taskRepository: FirestoreRepository<Task>,
    private val eventRepository: FirestoreRepository<Event>,
    private val googleAuthClient: GoogleAuthClient
) : ViewModel() {

    private val userId: String? = googleAuthClient.getUserId()
    private val _text = MutableStateFlow("There are no tasks or events for today")
    val text: StateFlow<String> = _text

    // TODO: Tasks and events from a specific project or not? Fix this
    private val _events = MutableStateFlow<List<Event>>(emptyList())

    private val _currentProjectId = MutableStateFlow<String?>(null)

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

    val taskByDates: StateFlow<Map<LocalDate, List<Task>>> =
        tasks.map { list ->
            list
                .filterIsInstance<Task>()
                .groupBy {  task -> task.date }
                .toSortedMap()
        }.stateIn(
            viewModelScope,
            // Stop calculation 5s after UI disappears to save battery
            SharingStarted.WhileSubscribed(5000),
            emptyMap()
        )

    fun tasksFor(date: LocalDate): StateFlow<List<Task>> {
        return taskByDates
            .map { map -> map[date].orEmpty() }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )
    }

}