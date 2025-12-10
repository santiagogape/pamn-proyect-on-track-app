package com.example.on_track_app.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.abstractions.repositories.EventRepository
import com.example.on_track_app.model.MockEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class NotificationsViewModel(private val repo: EventRepository) : ViewModel() {

    private val _text = MutableStateFlow("This is notifications screen")
    val text: StateFlow<String> = _text

    val events: StateFlow<List<MockEvent>> = this.repo.getAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun byProject(id: String): StateFlow<List<MockEvent>> = this.repo.byProject(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

}