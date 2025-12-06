package com.example.on_track_app.data.abstractions.repositories

import com.example.on_track_app.model.MockTask
import com.example.on_track_app.model.MockTimeField
import kotlinx.coroutines.flow.Flow

interface TaskRepository {

    fun getAllTasks(): Flow<List<MockTask>>
    fun getTaskById(id: String): MockTask?
    suspend fun addTask(
        name: String,
        description: String,
        date: MockTimeField,
        remindersId: List<String>,
        projectId: String,
        cloudId: String?
    ): String
    suspend fun updateTask(
        id: String,
        newName: String,
        newDescription: String
    )
    suspend fun deleteTask(id: String)
}