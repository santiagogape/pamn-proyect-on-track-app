package com.example.on_track_app.data.abstractions.repositories

import com.example.on_track_app.model.MockProject
import com.example.on_track_app.model.OwnerType

interface ProjectRepository: BasicById<MockProject>, IndexedByOwner<MockProject>, Update<MockProject> {
    suspend fun addProject(
        name: String,
        description:String,
        cloudId: String?,
        ownerId: String,
        ownerType: OwnerType
    ): String
}