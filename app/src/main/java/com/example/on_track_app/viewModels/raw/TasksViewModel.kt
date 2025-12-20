package com.example.on_track_app.viewModels.raw

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.abstractions.repositories.ProjectRepository
import com.example.on_track_app.data.abstractions.repositories.TaskRepository
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.Task
import com.example.on_track_app.viewModels.DelegateConsultProject
import com.example.on_track_app.viewModels.DelegateModifyTask
import com.example.on_track_app.viewModels.ModifyTask
import com.example.on_track_app.viewModels.ProjectsConsult
import com.example.on_track_app.viewModels.main.ItemStatus
import com.example.on_track_app.viewModels.utils.asItemStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class TasksViewModel(
    private val repo: TaskRepository,
    projectRepo: ProjectRepository
) : ViewModel(), ProjectsConsult, ModifyTask {

    private val projects = DelegateConsultProject(projectRepo)
    private val updateTask = DelegateModifyTask(repo, viewModelScope)

    override fun projects(group: String?): StateFlow<ItemStatus<List<Project>>> =
        projects.projects(group).asItemStatus(viewModelScope)
    override fun project(id: String): Project? = projects.project(id)
    override fun update(task: Task) = updateTask.update(task)
    override fun delete(task: Task) = updateTask.delete(task)

    private val _text = MutableStateFlow("Your task list is empty")
    private val tasks: StateFlow<ItemStatus<List<Task>>> = this.repo.getAll()
        .asItemStatus(viewModelScope)


    val text: StateFlow<String> = _text

    val allTasks: StateFlow<ItemStatus<List<Task>>> = tasks

    fun byProject(id: String): StateFlow<ItemStatus<List<Task>>> = this.repo.byProject(id)
        .asItemStatus(viewModelScope)

    fun byGroup(id: String): StateFlow<ItemStatus<List<Task>>> = this.repo.of(id)
        .asItemStatus(viewModelScope)




}