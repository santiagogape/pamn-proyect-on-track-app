package com.example.on_track_app.viewModels.raw

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.abstractions.repositories.ProjectRepository
import com.example.on_track_app.data.abstractions.repositories.TaskRepository
import com.example.on_track_app.model.MockProject
import com.example.on_track_app.model.MockTask
import com.example.on_track_app.ui.DelegateConsultProject
import com.example.on_track_app.ui.DelegateModifyTask
import com.example.on_track_app.ui.ModifyTask
import com.example.on_track_app.ui.ProjectsConsult
import com.example.on_track_app.viewModels.main.ItemStatus
import com.example.on_track_app.viewModels.utils.asItemStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow


class TasksViewModel(
    private val repo: TaskRepository,
    projectRepo: ProjectRepository
) : ViewModel(), ProjectsConsult, ModifyTask {

    private val projects = DelegateConsultProject(projectRepo)
    private val updateTask = DelegateModifyTask(repo, viewModelScope)

    override fun projects(group: String?): StateFlow<ItemStatus<List<MockProject>>> =
        projects.projects(group).asItemStatus(viewModelScope, SharingStarted.Eagerly)
    override fun project(id: String): MockProject? = projects.project(id)
    override fun update(task: MockTask) = updateTask.update(task)
    override fun delete(task: MockTask) = updateTask.delete(task)

    private val _text = MutableStateFlow("Your task list is empty")
    private val tasks: StateFlow<ItemStatus<List<MockTask>>> = this.repo.getAll()
        .asItemStatus(viewModelScope, SharingStarted.Eagerly)


    val text: StateFlow<String> = _text

    val allTasks: StateFlow<ItemStatus<List<MockTask>>> = tasks

    fun byProject(id: String): StateFlow<ItemStatus<List<MockTask>>> = this.repo.byProject(id)
        .asItemStatus(viewModelScope, SharingStarted.Eagerly)

    fun byGroup(id: String): StateFlow<ItemStatus<List<MockTask>>> = this.repo.of(id)
        .asItemStatus(viewModelScope, SharingStarted.Eagerly)




}