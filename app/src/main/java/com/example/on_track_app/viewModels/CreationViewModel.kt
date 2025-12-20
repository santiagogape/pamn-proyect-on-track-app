package com.example.on_track_app.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.abstractions.repositories.EventRepository
import com.example.on_track_app.data.abstractions.repositories.ProjectRepository
import com.example.on_track_app.data.abstractions.repositories.ReminderRepository
import com.example.on_track_app.data.abstractions.repositories.TaskRepository
import com.example.on_track_app.data.realm.utils.toInstant
import com.example.on_track_app.model.Linkable
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.TimeField
import kotlinx.coroutines.launch
import java.time.LocalDate


class CreationViewModel(
    val projectRepository: ProjectRepository,
    val eventRepository: EventRepository,
    val taskRepository: TaskRepository,
    val reminderRepository: ReminderRepository
): ViewModel() {
    fun addNewProject(name: String, description: String, creationContext:OwnerContext) {
        viewModelScope.launch {
            projectRepository.addProject(
                name,
                description,
                creationContext
            )
        }
    }

    fun addNewEvent(name: String, description: String, project: Project?, ownerContext: OwnerContext, start: TimeField, end: TimeField) {
        viewModelScope.launch {
            eventRepository.addEvent(
                name = name,
                description = description,
                start = start,
                end = end,
                project = project,
                owner = ownerContext
            )

        }
    }

    fun addNewTask(name: String, description: String?,project: Project?, ownerContext: OwnerContext, date: LocalDate, hour: Int?, minute: Int?) {
        viewModelScope.launch {
            taskRepository.addTask(
                name,
                description ?: "",
                TimeField(date.toInstant(hour, minute), hour != null && minute != null),
                project = project,
                owner = ownerContext
            )
        }
    }

    fun addReminder(
        name: String,
        description: String,
        date: LocalDate,
        hour: Int,
        minute: Int,
        ownerContext: OwnerContext,
        linkedTo: Linkable?
    ) {
        viewModelScope.launch {
            reminderRepository.addReminder(
                owner = ownerContext,
                at = TimeField(date.toInstant(hour, minute), true),
                name = name,
                description = description,
                linkedTo = linkedTo
            )
        }
    }


}