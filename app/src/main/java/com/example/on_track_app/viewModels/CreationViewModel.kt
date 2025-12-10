package com.example.on_track_app.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.abstractions.repositories.EventRepository
import com.example.on_track_app.data.abstractions.repositories.ProjectRepository
import com.example.on_track_app.data.abstractions.repositories.TaskRepository
import com.example.on_track_app.data.realm.utils.toInstant
import com.example.on_track_app.model.MockTimeField
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId
import java.time.LocalDate
import java.time.LocalDateTime


class CreationViewModel(val projectRepository: ProjectRepository, val eventRepository: EventRepository, val taskRepository: TaskRepository
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

    fun addNewEvent(name: String, description: String, project: String, startDateTime: LocalDateTime, endDateTime: LocalDateTime) {
        viewModelScope.launch {
            eventRepository.addEvent(
                name = name,
                description = description,
                projectId = project,
                start = MockTimeField(startDateTime.toInstant(),true),
                end = MockTimeField(endDateTime.toInstant(),true),
                cloudId = null
            )

        }
    }

    fun addNewTask(name: String, description: String?, project: String, date: LocalDate, hour: Int?, minute: Int?) {
        viewModelScope.launch {
            taskRepository.addTask(
                name,
                description ?: "",
                MockTimeField(date.toInstant(hour,minute), hour != null && minute != null),
                listOf(),
                project,
                null,
            )
        }
    }



}