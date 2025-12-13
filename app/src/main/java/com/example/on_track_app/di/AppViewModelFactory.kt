package com.example.on_track_app.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.on_track_app.viewModels.login.LoginViewModel
import com.example.on_track_app.viewModels.main.CalendarViewModel
import com.example.on_track_app.viewModels.main.HomeViewModel
import com.example.on_track_app.viewModels.main.RemindersViewModel
import com.example.on_track_app.viewModels.main.ProjectsViewModel
import com.example.on_track_app.viewModels.main.TasksViewModel

class AppViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                HomeViewModel(container.taskRepository, container.googleAuthClient) as T
            }
            modelClass.isAssignableFrom(CalendarViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                CalendarViewModel(container.projectRepository, container.taskRepository, container.eventRepository, container.googleAuthClient) as T
            }
            modelClass.isAssignableFrom(RemindersViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                RemindersViewModel(container.reminderRepository, container.taskRepository, container.googleAuthClient) as T
            }
            modelClass.isAssignableFrom(TasksViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                TasksViewModel(container.taskRepository, container.googleAuthClient) as T
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


