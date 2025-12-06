package com.example.on_track_app.data.abstractions.repositories

import com.example.on_track_app.model.MockProject

interface ProjectRepository: BasicById<MockProject> {
    suspend fun addProject(
        name: String,
        membersId: List<String>,
        cloudId: String?,
        ownerId: String,
        ownerType: String
    ): String
    suspend fun updateProject(id: String, newName: String)
}