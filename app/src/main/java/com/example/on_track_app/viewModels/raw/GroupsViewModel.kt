package com.example.on_track_app.viewModels.raw

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.abstractions.repositories.GroupRepository
import com.example.on_track_app.model.MockGroup
import com.example.on_track_app.viewModels.main.ItemStatus
import com.example.on_track_app.viewModels.utils.asItemStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow

class GroupsViewModel(private val repo: GroupRepository) : ViewModel() {

    private val _text = MutableStateFlow("This is notifications screen")
    val text: StateFlow<String> = _text

    val groups: StateFlow<ItemStatus<List<MockGroup>>> = this.repo.getAll()
        .asItemStatus(viewModelScope, SharingStarted.Eagerly)
}