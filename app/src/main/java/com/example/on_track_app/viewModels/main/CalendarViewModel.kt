package com.example.on_track_app.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.FirestoreRepository
import com.example.on_track_app.data.auth.GoogleAuthClient
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class CalendarViewModel(
    private val projectRepository: FirestoreRepository<Project>,
    private val taskRepository: FirestoreRepository<Task>,
    private val eventRepository: FirestoreRepository<Event>,
    private val googleAuthClient: GoogleAuthClient
) : ViewModel() {

    private val userId: String? = googleAuthClient.getUserId()
    private val _text = MutableStateFlow("There are no tasks for today")
    val text: StateFlow<String> = _text

    val projects: StateFlow<List<Project>> = if (userId != null) {
            projectRepository.getElements(userId)
        } else {
            flowOf(emptyList())
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // TODO: Tasks and events from a specific project or not? Fix this
    private val _projectEvents = MutableStateFlow<List<Event>>(emptyList())
    private val _projectTasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _projectTasks

    val taskByDates: StateFlow<Map<LocalDate, List<Task>>> =
        tasks.map {
                list -> list.groupBy {  task -> task.date }.toSortedMap()}.stateIn(
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

    fun project(id: String): StateFlow<List<Event>>{
        return _projectEvents
    }

}