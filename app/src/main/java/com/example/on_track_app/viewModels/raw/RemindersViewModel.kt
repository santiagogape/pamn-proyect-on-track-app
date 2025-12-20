package com.example.on_track_app.viewModels.raw

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.abstractions.repositories.EventRepository
import com.example.on_track_app.data.abstractions.repositories.ReminderRepository
import com.example.on_track_app.data.abstractions.repositories.TaskRepository
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Identifiable
import com.example.on_track_app.model.Reminder
import com.example.on_track_app.model.Task
import com.example.on_track_app.viewModels.main.ItemStatus
import com.example.on_track_app.viewModels.utils.asItemStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RemindersViewModel(
    private val repo: ReminderRepository,
    private val tasksRepo: TaskRepository,
    private val eventRepository: EventRepository
    ): ViewModel() {


    private fun mergeFlow(
        tasks: Flow<List<Task>>,
        events: Flow<List<Event>>
    ): Flow<ItemStatus<List<Identifiable>>> {
        val combined: Flow<List<Identifiable>> =
            combine(tasks, events) { a, b ->
                (a + b).distinctBy { it.id }
            }
        return combined.asItemStatus()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun byProject(project:String) = mergeFlow(tasksRepo.byProject(project),
            eventRepository.byProject(project))
            .toReminders()

    fun byGroup(group:String)= repo.of(group).asItemStatus(viewModelScope)

    fun all() = repo.getAll().asItemStatus(viewModelScope)


    @OptIn(ExperimentalCoroutinesApi::class)
    private fun Flow<ItemStatus<List<Identifiable>>>.toReminders(
    ): StateFlow<ItemStatus<List<Reminder>>> {

        return this
            .flatMapLatest { referencesStatus ->
                when (referencesStatus) {

                    is ItemStatus.Loading ->
                        flowOf(ItemStatus.Loading)

                    is ItemStatus.Error ->
                        flowOf(ItemStatus.Error)

                    is ItemStatus.Success -> {
                        val ids = referencesStatus.elements.map { it.id }

                        repo.linkedTo(ids)
                            .asItemStatus(viewModelScope)
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ItemStatus.Loading
            )
    }

    fun update(task: Reminder){
        viewModelScope.launch {
            repo.update(task)
        }
    }

}

