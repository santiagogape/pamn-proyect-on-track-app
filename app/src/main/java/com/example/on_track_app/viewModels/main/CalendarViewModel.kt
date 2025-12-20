package com.example.on_track_app.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.abstractions.repositories.EventRepository
import com.example.on_track_app.data.abstractions.repositories.TaskRepository
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Task
import com.example.on_track_app.model.toDate
import com.example.on_track_app.viewModels.utils.asItemStatus
import com.example.on_track_app.viewModels.utils.mapItemStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

class CalendarViewModel(private val repo: EventRepository, private val tasksRepo: TaskRepository) : ViewModel() {

    val events: StateFlow<ItemStatus<List<Event>>> = this.repo.getAll()
        .asItemStatus(viewModelScope, SharingStarted.Eagerly)

    val tasks: StateFlow<ItemStatus<List<Task>>> = this.tasksRepo.getAll()
        .asItemStatus(viewModelScope, SharingStarted.Eagerly)

    val eventsByDates: StateFlow<ItemStatus<Map<LocalDate, List<Event>>>> =
        events.mapItemStatus(viewModelScope,
            SharingStarted.Eagerly) {
                list -> ItemStatus.Success(list.elements.groupBy {  it.start.toDate() })
        }

    val tasksByDates: StateFlow<ItemStatus<Map<LocalDate, List<Task>>>> =
        tasks.mapItemStatus(viewModelScope,
            SharingStarted.Eagerly) {
                list -> ItemStatus.Success(list.elements.groupBy {  it.due.toDate() })
        }



    fun byProject(id: String): StateFlow<ItemStatus<Map<LocalDate, List<Event>>>> = this.repo.byProject(id)
        .asItemStatus(viewModelScope, SharingStarted.Eagerly).mapItemStatus(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000)
        ){ success ->
            ItemStatus.Success(success.elements.groupBy { it.start.toDate() })
        }

    fun tasksByProject(id: String): StateFlow<ItemStatus<Map<LocalDate, List<Task>>>> = this.tasksRepo.byProject(id)
        .asItemStatus(viewModelScope, SharingStarted.Eagerly).mapItemStatus(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000)
        ){ success ->
            ItemStatus.Success(success.elements.groupBy { it.due.toDate() })
        }

    fun byGroup(id: String): StateFlow<ItemStatus<Map<LocalDate, List<Event>>>> = this.repo.of(id)
        .asItemStatus(viewModelScope, SharingStarted.Eagerly).mapItemStatus(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000)
        ){ success ->
            ItemStatus.Success(success.elements.groupBy { it.start.toDate() })
        }

    fun tasksByGroup(id: String): StateFlow<ItemStatus<Map<LocalDate, List<Task>>>> = this.tasksRepo.of(id)
        .asItemStatus(viewModelScope, SharingStarted.Eagerly).mapItemStatus(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000)
        ){ success ->
            ItemStatus.Success(success.elements.groupBy { it.due.toDate() })
        }


}

