package com.example.on_track_app.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.abstractions.repositories.ProjectRepository
import com.example.on_track_app.data.abstractions.repositories.UniqueRepository
import com.example.on_track_app.model.LocalConfigurations
import com.example.on_track_app.model.MockProject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ProjectsViewModel(private val repo: ProjectRepository, config: UniqueRepository<LocalConfigurations>
) : ViewModel(), Model<MockProject> {


    val default = config.get()?.defaultProjectID
    private val _text = MutableStateFlow("This is projects screen")
    val text: StateFlow<String> = _text

    private val _items = MutableStateFlow(listOf("project1", "project2", "project3"))
    val items: StateFlow<List<String>> = _items

    val projects: StateFlow<List<MockProject>> = this.repo.getAll().map { list ->
        list.filter { item -> item.id != default }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

}