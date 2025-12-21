package com.example.on_track_app.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.abstractions.repositories.EventRepository
import com.example.on_track_app.data.abstractions.repositories.ProjectRepository
import com.example.on_track_app.data.abstractions.repositories.TaskRepository
import com.example.on_track_app.data.realm.utils.toInstant
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.Task
import com.example.on_track_app.viewModels.DelegateConsultProject
import com.example.on_track_app.viewModels.DelegateModifyEvent
import com.example.on_track_app.viewModels.DelegateModifyTask
import com.example.on_track_app.viewModels.ModifyEvent
import com.example.on_track_app.viewModels.ModifyTask
import com.example.on_track_app.viewModels.ProjectsConsult
import com.example.on_track_app.viewModels.utils.asItemStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.LocalDateTime

class HomeViewModel(
    private val taskRepository: TaskRepository,
    private val eventRepository: EventRepository,
    private val projectRepository: ProjectRepository
    ) : ViewModel(),ProjectsConsult, ModifyTask, ModifyEvent {

    val today: LocalDateTime = LocalDate.now().atStartOfDay()
    val start = today.toInstant()
    val nextWeek: LocalDateTime = today.plusDays(7)
    val end = nextWeek.toInstant()

    private val projects = DelegateConsultProject(this.projectRepository)
    private val updateTask = DelegateModifyTask(this.taskRepository, viewModelScope)
    private val updateEvent = DelegateModifyEvent(this.eventRepository, viewModelScope)

    override fun projects(group: String?): StateFlow<ItemStatus<List<Project>>> =
        projects.projects(group).asItemStatus(viewModelScope)
    override fun project(id: String): Project? = projects.project(id)
    override fun update(task: Task) = updateTask.update(task)
    override fun update(event: Event) = updateEvent.update(event)
    override fun delete(task: Task) = updateTask.delete(task)
    override fun delete(event: Event) = updateEvent.delete(event)

    private val _text = MutableStateFlow("This is home screen")
    val text: StateFlow<String> = _text


    val tasks = this.taskRepository.between(start,end).asItemStatus(viewModelScope)
    val events = this.eventRepository.between(start,end).asItemStatus(viewModelScope)

}


