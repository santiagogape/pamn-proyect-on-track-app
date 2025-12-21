package com.example.on_track_app.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.on_track_app.domain.viewModels.login.LoginViewModel
import com.example.on_track_app.domain.viewModels.main.CalendarViewModel
import com.example.on_track_app.domain.viewModels.main.HomeViewModel
import com.example.on_track_app.domain.viewModels.main.RemindersViewModel
import com.example.on_track_app.domain.viewModels.main.ProjectsViewModel
import com.example.on_track_app.domain.viewModels.main.TasksViewModel

class AppViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                HomeViewModel(container.eventRepository,container.taskRepository, container.projectRepository, container.taskManager,container.eventManager, container.googleAuthClient) as T
            }
            modelClass.isAssignableFrom(CalendarViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                CalendarViewModel(container.reminderRepository, container.taskRepository, container.eventRepository, container.projectRepository, container.taskManager, container.eventManager, container.googleAuthClient) as T
            }
            modelClass.isAssignableFrom(RemindersViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                RemindersViewModel(container.reminderRepository, container.taskRepository, container.eventRepository, container.googleAuthClient) as T
            }
            modelClass.isAssignableFrom(TasksViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                TasksViewModel(container.taskRepository, container.projectRepository, container.taskManager, container.googleAuthClient) as T
            }
            modelClass.isAssignableFrom(ProjectsViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                ProjectsViewModel(container.projectRepository, container.googleAuthClient) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                LoginViewModel(container.googleAuthClient) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}


