package com.example.on_track_app.data.abstractions.repositories

import com.example.on_track_app.model.MockTask
import com.example.on_track_app.model.MockTimeField

interface TaskRepository: BasicById<MockTask>, IndexedByProject<MockTask> {

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
}