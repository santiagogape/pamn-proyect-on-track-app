package com.example.on_track_app.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.abstractions.repositories.EventRepository
import com.example.on_track_app.model.MockEvent
import com.example.on_track_app.model.toDate
import com.example.on_track_app.viewModels.utils.asItemStatus
import com.example.on_track_app.viewModels.utils.mapItemStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

class CalendarViewModel(private val repo: EventRepository) : ViewModel() {

    private val _text = MutableStateFlow("This is calendar screen")
    val text: StateFlow<String> = _text

    val events: StateFlow<ItemStatus<List<MockEvent>>> = this.repo.getAll()
        .asItemStatus(viewModelScope, SharingStarted.Eagerly)

    val eventsByDates: StateFlow<ItemStatus<Map<LocalDate, List<MockEvent>>>> =
        events.mapItemStatus(viewModelScope,
            SharingStarted.Eagerly) {
                list -> ItemStatus.Success(list.elements.groupBy {  it.start.toDate() })
        }

    fun eventsFor(date: LocalDate): StateFlow<ItemStatus<List<MockEvent>>>{
        return eventsByDates.mapItemStatus(
            scope = viewModelScope,
            started = SharingStarted.Eagerly
        ) { success ->
            ItemStatus.Success(
                success.elements[date].orEmpty()
            )
        }
    }

    fun eventsByDatesAndProject(id: String): StateFlow<ItemStatus<Map<LocalDate, List<MockEvent>>>> {
        return events.mapItemStatus(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000)
        ){ success ->
            ItemStatus.Success(success.elements.filter { it.projectId == id }
                .groupBy { it.start.toDate() })
        }

    }



}