package com.example.on_track_app.viewModels.raw

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.abstractions.repositories.TaskRepository
import com.example.on_track_app.model.MockTask
import com.example.on_track_app.model.toDate
import com.example.on_track_app.viewModels.main.ItemStatus
import com.example.on_track_app.viewModels.utils.asItemStatus
import com.example.on_track_app.viewModels.utils.filterItemStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate



class TasksViewModel(
    private val repo: TaskRepository,
) : ViewModel() {


    private val _text = MutableStateFlow("Your task list is empty")
    private val tasks: StateFlow<ItemStatus<List<MockTask>>> = this.repo.getAll()
        .asItemStatus(viewModelScope, SharingStarted.Eagerly)



    val text: StateFlow<String> = _text

    val allTasks: StateFlow<ItemStatus<List<MockTask>>> = tasks

    fun byProject(id: String): StateFlow<ItemStatus<List<MockTask>>> = this.repo.byProject(id)
        .asItemStatus(viewModelScope, SharingStarted.Eagerly)

    //mock
    fun tasksFor(date: LocalDate): StateFlow<ItemStatus<List<MockTask>>> {
        return tasks.filterItemStatus(viewModelScope, SharingStarted.Eagerly){
                t->
                t.filter { task ->
                    task.due.toDate() == date
                }

        }
    }
}