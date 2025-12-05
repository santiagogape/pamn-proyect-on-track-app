package com.example.on_track_app.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.FirestoreRepository
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDate.now

class CalendarViewModel(
    private val projectRepository: FirestoreRepository<Project>,
    private val taskRepository: FirestoreRepository<Task>,
    private val eventRepository: FirestoreRepository<Event>
) : ViewModel() {

    private val _text = MutableStateFlow("This is calendar screen")
    val text: StateFlow<String> = _text

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects

    // Las tareas y eventos son de un proyecto específico o no?
    private val _projectEvents = MutableStateFlow<List<Event>>(emptyList())
    private val _projectTasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _projectTasks

    val taskByDates: StateFlow<Map<String, List<Task>>> =
        tasks.map {
                list -> list.groupBy {  task -> task.date }.toSortedMap()}.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyMap()
        )

    fun tasksFor(date: LocalDate): StateFlow<List<Task>> {
        return taskByDates
            .map { map -> map[date].orEmpty() }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                emptyList()
            )
    }

    fun project(id: String): StateFlow<List<Event>>{
        return _projectEvents
    }

    init {
        loadProjectInfo()
    }

    fun loadProjectInfo() {
        viewModelScope.launch {
            projectRepository.getElements { list ->
                _projects.value = list
            }
            taskRepository.getElements { list ->
                _projectTasks.value = list
            }
            eventRepository.getElements { list ->
                _projectEvents.value = list
            }
        }
    }


}