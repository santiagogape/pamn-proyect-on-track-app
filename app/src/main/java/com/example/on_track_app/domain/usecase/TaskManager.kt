package com.example.on_track_app.domain.usecase

import android.util.Log
import com.example.on_track_app.data.FirestoreRepository
import com.example.on_track_app.model.Expandable
import com.example.on_track_app.model.Task
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TaskManager(
    private val taskRepository: FirestoreRepository<Task>
) {
    suspend fun createTask(
        userId: String,
        name: String,
        description: String,
        date: LocalDate,
        hour: Int?,
        minute: Int?,
        projectId: String?
    ) : Boolean {

        val timeString = if (hour != null && minute != null) {
            LocalTime.of(hour, minute).toString()
        } else {
            null
        }

        val newTask = Task(
            userId = userId,
            name = name,
            description = description,
            dateIso = date.toString(),
            timeIso = timeString,
            projectId = projectId
        )

        return taskRepository.addElement(newTask)
    }

    suspend fun updateTask(
        taskId: String,
        newName: String,
        newDescription: String,
        newDate: LocalDate,
        newHour: Int?,
        newMinute: Int?,
        newProjectId: String?
    ) {
        // 2. Format the time (Handle the logic for converting Ints to LocalTime)
        val newTimeIso = if (newHour != null && newMinute != null) {
            LocalTime.of(newHour, newMinute).toString()
        } else {
            null // or preserve originalTask.timeIso if you prefer logic implies "clearing" time
        }

        val updates = mutableMapOf<String, Any?>(
            "name" to newName,
            "description" to newDescription,
            "projectId" to newProjectId,
            "date" to newDate.toString(),
            "time" to newTimeIso
        )

        // 4. Send the complete, updated object to the repository
        taskRepository.updateElement(taskId, updates)
    }

    suspend fun deleteTask(taskId: String) {
        try {
            taskRepository.deleteTaskWithReminders(taskId)
        } catch (e: Exception) {
            Log.e("TasksViewModel", "Failed to delete task with ID=$taskId", e)
        }
    }

    fun getTasksByDate(scope: CoroutineScope, tasks: StateFlow<List<Expandable>>): StateFlow<Map<LocalDate, List<Task>>> {
        return tasks
            .map { list ->
                list
                    .filterIsInstance<Task>()
                    .groupBy { it.date }
                    .toSortedMap()
            }
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyMap()
            )
    }

    fun getTasksForDate(
        sourceMap: StateFlow<Map<LocalDate, List<Task>>>, // Pass the source flow
        date: LocalDate,
        scope: CoroutineScope
    ): StateFlow<List<Task>> {
        return sourceMap
            .map { map -> map[date].orEmpty() }
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }
}