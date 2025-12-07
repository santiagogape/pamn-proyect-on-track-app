package com.example.on_track_app.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.on_track_app.data.FirestoreRepository
import com.example.on_track_app.model.Task
import com.example.on_track_app.ui.fragments.navigable.notifications.NotificationsScreen
import com.example.on_track_app.viewModels.main.CalendarViewModel
import com.example.on_track_app.viewModels.main.HomeViewModel
import com.example.on_track_app.viewModels.main.NotificationsViewModel
import com.example.on_track_app.viewModels.main.ProjectsViewModel
import com.example.on_track_app.viewModels.main.TasksViewModel

class AppViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                HomeViewModel(container.taskRepository) as T
            }
            modelClass.isAssignableFrom(CalendarViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                CalendarViewModel(container.projectRepository, container.taskRepository, container.eventRepository) as T
            }
            modelClass.isAssignableFrom(NotificationsViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                NotificationsViewModel(container.projectRepository, container.taskRepository) as T
            }
            modelClass.isAssignableFrom(TasksViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                TasksViewModel(container.taskRepository) as T
            }
            modelClass.isAssignableFrom(ProjectsViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                ProjectsViewModel(container.projectRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}


