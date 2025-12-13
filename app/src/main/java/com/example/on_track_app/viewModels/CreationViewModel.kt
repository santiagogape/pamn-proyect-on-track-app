package com.example.on_track_app.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.abstractions.repositories.EventRepository
import com.example.on_track_app.data.abstractions.repositories.ProjectRepository
import com.example.on_track_app.data.abstractions.repositories.TaskRepository
import com.example.on_track_app.data.realm.utils.toInstant
import com.example.on_track_app.model.MockTimeField
import com.example.on_track_app.model.OwnerType
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime


class CreationViewModel(val projectRepository: ProjectRepository, val eventRepository: EventRepository, val taskRepository: TaskRepository
): ViewModel() {
    fun addNewProject(name: String, description: String, owner: String ) {
        viewModelScope.launch {
            projectRepository.addProject(
                name,
                description,
                null,
                owner,
                OwnerType.USER
            )
        }
    }

    fun addNewEvent(name: String, description: String, project: String, owner:String, type: OwnerType, startDateTime: LocalDateTime, endDateTime: LocalDateTime) {
        viewModelScope.launch {
            eventRepository.addEvent(
                name = name,
                description = description,
                start = MockTimeField(startDateTime.toInstant(), true),
                end = MockTimeField(endDateTime.toInstant(), true),
                projectId = project,
                cloudId = null,
                ownerId = owner,
                ownerType = type
            )

        }
    }

    fun addNewTask(name: String, description: String?, project: String,owner:String, type: OwnerType, date: LocalDate, hour: Int?, minute: Int?) {
        viewModelScope.launch {
            taskRepository.addTask(
                name,
                description ?: "",
                MockTimeField(date.toInstant(hour, minute), hour != null && minute != null),
                project,
                null,
                ownerId = owner,
                ownerType = type
            )
        }
    }



}