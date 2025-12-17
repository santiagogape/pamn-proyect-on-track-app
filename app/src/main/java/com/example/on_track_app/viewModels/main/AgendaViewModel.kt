package com.example.on_track_app.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.abstractions.repositories.EventRepository
import com.example.on_track_app.data.abstractions.repositories.ProjectRepository
import com.example.on_track_app.data.abstractions.repositories.TaskRepository
import com.example.on_track_app.model.MockEvent
import com.example.on_track_app.model.MockProject
import com.example.on_track_app.model.MockTask
import com.example.on_track_app.model.toDate
import com.example.on_track_app.ui.DelegateConsultProject
import com.example.on_track_app.ui.DelegateModifyEvent
import com.example.on_track_app.ui.DelegateModifyTask
import com.example.on_track_app.ui.ModifyEvent
import com.example.on_track_app.ui.ModifyTask
import com.example.on_track_app.ui.ProjectsConsult
import com.example.on_track_app.viewModels.utils.asItemStatus
import com.example.on_track_app.viewModels.utils.mapItemStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

class AgendaViewModel(
    private val eventRepo: EventRepository,
    private val tasksRepo: TaskRepository,
    projectRepo: ProjectRepository,
) : ViewModel(), ProjectsConsult, ModifyTask, ModifyEvent  {

    private val projects = DelegateConsultProject(projectRepo)
    private val updateTask = DelegateModifyTask(tasksRepo, viewModelScope)
    private val updateEvent = DelegateModifyEvent(eventRepo, viewModelScope)

    override fun projects(group: String?): StateFlow<ItemStatus<List<MockProject>>> =
        projects.projects(group).asItemStatus(viewModelScope, SharingStarted.Eagerly)
    override fun project(id: String): MockProject? = projects.project(id)
    override fun update(task: MockTask) = updateTask.update(task)
    override fun update(event: MockEvent) = updateEvent.update(event)
    override fun delete(task: MockTask) = updateTask.delete(task)
    override fun delete(event: MockEvent) = updateEvent.delete(event)


    val events: StateFlow<ItemStatus<List<MockEvent>>> = this.eventRepo.getAll()
        .asItemStatus(viewModelScope, SharingStarted.Eagerly)

    val tasks: StateFlow<ItemStatus<List<MockTask>>> = this.tasksRepo.getAll()
        .asItemStatus(viewModelScope, SharingStarted.Eagerly)

    val eventsByDates: StateFlow<ItemStatus<Map<LocalDate, List<MockEvent>>>> =
        events.mapItemStatus(viewModelScope,
            SharingStarted.Eagerly) {
                list -> ItemStatus.Success(list.elements.groupBy {  it.start.toDate() })
        }

    val tasksByDates: StateFlow<ItemStatus<Map<LocalDate, List<MockTask>>>> =
        tasks.mapItemStatus(viewModelScope,
            SharingStarted.Eagerly) {
                list -> ItemStatus.Success(list.elements.groupBy {  it.due.toDate() })
        }


    fun byProject(id: String): StateFlow<ItemStatus<Map<LocalDate, List<MockEvent>>>> = this.eventRepo.byProject(id)
        .asItemStatus(viewModelScope, SharingStarted.Eagerly).mapItemStatus(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000)
        ){ success ->
            ItemStatus.Success(success.elements.groupBy { it.start.toDate() })
        }

    fun tasksByProject(id: String): StateFlow<ItemStatus<Map<LocalDate, List<MockTask>>>> = this.tasksRepo.byProject(id)
        .asItemStatus(viewModelScope, SharingStarted.Eagerly).mapItemStatus(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000)
        ){ success ->
            ItemStatus.Success(success.elements.groupBy { it.due.toDate() })
        }

    fun byGroup(id: String): StateFlow<ItemStatus<Map<LocalDate, List<MockEvent>>>> = this.eventRepo.of(id)
        .asItemStatus(viewModelScope, SharingStarted.Eagerly).mapItemStatus(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000)
        ){ success ->
            ItemStatus.Success(success.elements.groupBy { it.start.toDate() })
        }

    fun tasksByGroup(id: String): StateFlow<ItemStatus<Map<LocalDate, List<MockTask>>>> = this.tasksRepo.of(id)
        .asItemStatus(viewModelScope, SharingStarted.Eagerly).mapItemStatus(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000)
        ){ success ->
            ItemStatus.Success(success.elements.groupBy { it.due.toDate() })
        }

}

