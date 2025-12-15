package com.example.on_track_app.domain.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.FirestoreRepository
import com.example.on_track_app.data.auth.GoogleAuthClient
import com.example.on_track_app.model.Project
import com.example.on_track_app.ui.fragments.dialogs.CreationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProjectsViewModel(
    private val projectRepository: FirestoreRepository<Project>,
    private val googleAuthClient: GoogleAuthClient
) : ViewModel() {
    private val userId: String? = googleAuthClient.getUserId()
    private val _text = MutableStateFlow("You have no projects")
    val text: StateFlow<String> = _text

    val projects: StateFlow<ItemStatus> = if (userId != null) {
            projectRepository.getElements(userId)
        } else {
            flowOf(emptyList())
        }
        .map { projectList ->
            ItemStatus.Success(projectList) as ItemStatus
        }
        .onStart {
            emit(ItemStatus.Loading)
        }
        .catch {
            emit(ItemStatus.Error)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ItemStatus.Loading
        )

    private val _creationStatus  = MutableStateFlow<CreationStatus>(CreationStatus.Idle)
    val creationStatus = _creationStatus.asStateFlow()

    fun createProject(
        name: String,
        description: String
    ) {
        viewModelScope.launch {
            val currentUserId =
                googleAuthClient.getUserId() ?: // Handle error: User is not logged in
                return@launch

            val newProject = Project(
                userId = currentUserId,
                name = name,
                description = description,
                membersIds = listOf(currentUserId)
            )

            val success = projectRepository.addElement(newProject)

            if (success) {
                _creationStatus.value = CreationStatus.Success
            } else {
                _creationStatus.value = CreationStatus.Error("Failed to save project to database")
            }
        }
    }

    fun resetStatus() {
        _creationStatus.value = CreationStatus.Idle
    }

}