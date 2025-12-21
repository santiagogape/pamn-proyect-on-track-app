package com.example.on_track_app.viewModels.raw

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.abstractions.repositories.ProjectRepository
import com.example.on_track_app.model.Project
import com.example.on_track_app.viewModels.main.ItemStatus
import com.example.on_track_app.viewModels.utils.asItemStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProjectsViewModel(private val repo: ProjectRepository
) : ViewModel() {




    fun project(id:String): Project? {
        return this.repo.getById(id)
    }

    fun liveProject(id:String): StateFlow<Project?> =
        this.repo.liveById(id)
            .stateIn(viewModelScope,SharingStarted.WhileSubscribed(5_000),null)

    fun projects(): StateFlow<ItemStatus<List<Project>>> =
        this.repo.getAll()
            .asItemStatus(viewModelScope)

    fun projectsOf(id:String):StateFlow<ItemStatus<List<Project>>> {
        return this.repo.of(id).asItemStatus(viewModelScope)
    }

    fun update(task: Project){
        viewModelScope.launch {
            repo.update(task)
        }
    }

    fun delete(project: Project) {
        viewModelScope.launch {
            repo.markAsDeleted(project.id)
        }
    }
}