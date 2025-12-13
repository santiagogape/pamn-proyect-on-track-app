package com.example.on_track_app.data.abstractions.repositories

import com.example.on_track_app.model.MockTask
import com.example.on_track_app.model.MockTimeField
import com.example.on_track_app.model.OwnerType

interface TaskRepository: BasicById<MockTask>, IndexedByProject<MockTask> {

    suspend fun addTask(
        name: String,
        description: String,
        date: MockTimeField,
        projectId: String?,
        cloudId: String?,
        ownerId: String,
        ownerType: OwnerType
    ): String
    suspend fun updateTask(
        id: String,
        newName: String,
        newDescription: String
    )
}