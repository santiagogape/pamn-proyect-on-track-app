package com.example.on_track_app.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.abstractions.repositories.EventRepository
import com.example.on_track_app.data.abstractions.repositories.ProjectRepository
import com.example.on_track_app.data.abstractions.repositories.TaskRepository
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.Task
import com.example.on_track_app.viewModels.main.ItemStatus
import com.example.on_track_app.viewModels.utils.asItemStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow

class CreationSourcesViewModel(
    private val projectRepository: ProjectRepository,
    private val eventRepository: EventRepository,
    private val taskRepository: TaskRepository,
) : ViewModel() {

    // -------------------------------
    // CACHES
    // -------------------------------

    private val projectsCache =
        mutableMapOf<OwnerContext, StateFlow<ItemStatus<List<Project>>>>()

    private val tasksCache =
        mutableMapOf<CreationContext, StateFlow<ItemStatus<List<Task>>>>()

    private val eventsCache =
        mutableMapOf<CreationContext, StateFlow<ItemStatus<List<Event>>>>()

    // -------------------------------
    // PROJECTS (for Project creation)
    // -------------------------------

    fun projects(context: OwnerContext): StateFlow<ItemStatus<List<Project>>> =
        projectsCache.getOrPut(context) {
            val source = when (context) {
                is UserOwnerContext ->
                    projectRepository.getAll()

                is GroupOwnerContext ->
                    projectRepository.of(context.ownerId)
            }

            source.asItemStatus(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000)
            )
        }

    fun project(id: String): Project? = projectRepository.getById(id)

    // -------------------------------
    // TASKS (for Task / Reminder creation)
    // -------------------------------

    fun tasks(context: CreationContext): StateFlow<ItemStatus<List<Task>>> =
        tasksCache.getOrPut(context) {
            val source = when (context) {
                is UserCreationContext ->
                    taskRepository.getAll()

                is GroupCreationContext ->
                    taskRepository.of(context.ownerId)

                is ProjectCreationContext ->
                    taskRepository.byProject(context.projectId)
            }

            source.asItemStatus(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000)
            )
        }

    // -------------------------------
    // EVENTS (for Event / Reminder creation)
    // -------------------------------

    fun events(context: CreationContext): StateFlow<ItemStatus<List<Event>>> =
        eventsCache.getOrPut(context) {
            val source = when (context) {
                is UserCreationContext ->
                    eventRepository.getAll()

                is GroupCreationContext ->
                    eventRepository.of(context.ownerId)

                is ProjectCreationContext ->
                    eventRepository.byProject(context.projectId)
            }

            source.asItemStatus(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000)
            )
        }
}
