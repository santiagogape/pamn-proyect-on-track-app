package com.example.on_track_app.viewModels.raw

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.abstractions.repositories.ProjectRepository
import com.example.on_track_app.model.MockProject
import com.example.on_track_app.viewModels.main.ItemStatus
import com.example.on_track_app.viewModels.utils.asItemStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ProjectsViewModel(private val repo: ProjectRepository
) : ViewModel() {


    private val _text = MutableStateFlow("This is projects screen")
    val text: StateFlow<String> = _text

    private val _items = MutableStateFlow(listOf("project1", "project2", "project3"))
    val items: StateFlow<List<String>> = _items

    fun project(id:String): MockProject? {
        return this.repo.getById(id)
    }

    fun liveProject(id:String): StateFlow<MockProject?> =
        this.repo.liveById(id)
            .stateIn(viewModelScope, SharingStarted.Eagerly,null)

    fun projects(): StateFlow<ItemStatus<List<MockProject>>> =
        this.repo.getAll()
            .asItemStatus(viewModelScope, SharingStarted.Eagerly)
}