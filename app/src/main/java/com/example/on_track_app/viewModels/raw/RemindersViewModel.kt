package com.example.on_track_app.viewModels.raw

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.abstractions.repositories.ReminderRepository
import com.example.on_track_app.model.MockReminder
import com.example.on_track_app.viewModels.main.ItemStatus
import com.example.on_track_app.viewModels.utils.asItemStatus
import kotlinx.coroutines.flow.StateFlow

class RemindersViewModel(private val repo: ReminderRepository): ViewModel() {
    val all: StateFlow<ItemStatus<List<MockReminder>>> = repo.getAll().asItemStatus(viewModelScope)
}