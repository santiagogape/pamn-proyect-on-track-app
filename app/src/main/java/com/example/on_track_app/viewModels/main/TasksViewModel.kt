package com.example.on_track_app.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.abstractions.repositories.TaskRepository
import com.example.on_track_app.model.MockTask
import com.example.on_track_app.model.toDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class TasksViewModel(private val repo: TaskRepository) : ViewModel() {

    private val _text = MutableStateFlow("This is dashboard screen")
    val text: StateFlow<String> = _text

    val tasks: StateFlow<List<MockTask>> = this.repo.getAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun byProject(id: String): StateFlow<List<MockTask>> = this.repo.byProject(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    //mock
    val taskByDates: StateFlow<Map<LocalDate, List<MockTask>>> =
        tasks.map {
                list -> list.groupBy {  it.date.toDate() }}.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyMap()
        )

    fun tasksFor(date: LocalDate): StateFlow<List<MockTask>> {
        return taskByDates
            .map { map -> map[date].orEmpty() }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                emptyList()
            )
    }
}