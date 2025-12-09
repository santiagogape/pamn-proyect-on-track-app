package com.example.on_track_app.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.abstractions.repositories.ProjectRepository
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId



class CreationViewModel(val projectRepository: ProjectRepository
): ViewModel() {
    //todo -> clean this, use local User id
    fun addNewProject(name: String) {
        viewModelScope.launch {
            projectRepository.addProject(
                name,
                emptyList(),
                null,
                ObjectId().toHexString(),
                "USER"
            )
        }
    }
}