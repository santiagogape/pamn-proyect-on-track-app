package com.example.on_track_app.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.abstractions.repositories.EventRepository
import com.example.on_track_app.model.MockEvent
import com.example.on_track_app.model.toDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class CalendarViewModel(private val repo: EventRepository) : ViewModel() {

    private val _text = MutableStateFlow("This is calendar screen")
    val text: StateFlow<String> = _text

    val events: StateFlow<List<MockEvent>> = this.repo.getAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val eventsByDates: StateFlow<Map<LocalDate, List<MockEvent>>> =
        events.map {
                list -> list.groupBy {  it.start.toDate() }}.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyMap()
        )

    fun eventsFor(date: LocalDate): StateFlow<List<MockEvent>> {
        return eventsByDates
            .map { map -> map[date].orEmpty() }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                emptyList()
            )
    }

    fun eventsByDatesAndProject(id:String): StateFlow<Map<LocalDate, List<MockEvent>>>{
        return events.map { it.filter { e->e.projectId == id }  }.map {
                list -> list.groupBy {  it.start.toDate() }}.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyMap()
        )
    }


}