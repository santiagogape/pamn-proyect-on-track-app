package com.example.on_track_app.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.LocalDate.now

class CalendarViewModel : ViewModel() {

    private val _text = MutableStateFlow("This is calendar screen")
    val text: StateFlow<String> = _text

    private val _items = MutableStateFlow(listOf("project1", "project2", "project3"))
    val items: StateFlow<List<String>> = _items
    private val _projectItems = MutableStateFlow(listOf("event1", "event2"))

    fun project(id: String): StateFlow<List<String>>{
        return _projectItems
    }

    val tasks: StateFlow<List<String>> = _items

    val taskByDates: StateFlow<Map<LocalDate, List<String>>> =
        tasks.map {
                list -> list.groupBy {  now().plusDays(list.indexOf(it).toLong()) }}.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyMap()
        )

    fun tasksFor(date: LocalDate): StateFlow<List<String>> {
        return taskByDates
            .map { map -> map[date].orEmpty() }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                emptyList()
            )
    }


}