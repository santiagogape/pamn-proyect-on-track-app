package com.example.on_track_app.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.on_track_app.data.FirestoreRepository
import com.example.on_track_app.data.auth.GoogleAuthClient
import com.example.on_track_app.model.Expandable
import com.example.on_track_app.model.Reminder
import com.example.on_track_app.model.Task
import com.example.on_track_app.ui.fragments.dialogs.CreationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class RemindersViewModel(
    private val reminderRepository: FirestoreRepository<Reminder>,
    private val taskRepository: FirestoreRepository<Task>,
    private val googleAuthClient: GoogleAuthClient
) : ViewModel() {

    private val userId: String? = googleAuthClient.getUserId()
    private val _text = MutableStateFlow("No active reminders")
    val text: StateFlow<String> = _text

    val reminders: StateFlow<List<Reminder>> = if (userId != null) {
            reminderRepository.getElements(userId)
        } else {
            flowOf(emptyList())
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun projectReminders(id: String): StateFlow<List<Expandable>>{
        val projectTasks = taskRepository.getElementsByProjectId(id) as Flow<List<Task>>
        val projectReminders: StateFlow<List<Reminder>> = if (userId != null) {
                reminderRepository.getProjectReminders(userId, projectTasks )
            } else {
                flowOf(emptyList())
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )
        return projectReminders
    }

    private val _creationStatus  = MutableStateFlow<CreationStatus>(CreationStatus.Idle)
    val creationStatus = _creationStatus.asStateFlow()

    fun createReminder(
        name: String,
        description: String,
        taskId: String?,
        eventId: String?,
        date: LocalDate,
        hour: Int,
        minute: Int,
    ) {
        viewModelScope.launch {
            val currentUserId =
                googleAuthClient.getUserId() ?: // Handle error: User is not logged in
                return@launch

            val timeString = LocalTime.of(hour, minute).toString()

            val newReminder = Reminder(
                userId = currentUserId,
                name = name,
                description = description,
                dateIso = date.toString(),
                timeIso = timeString,
                taskId = taskId,
                eventId = eventId
            )

            val success = reminderRepository.addElement(newReminder)

            if (success) {
                _creationStatus.value = CreationStatus.Success
            } else {
                _creationStatus.value = CreationStatus.Error("Failed to save reminder to database")
            }
        }
    }

    fun resetStatus() {
        _creationStatus.value = CreationStatus.Idle
    }
}