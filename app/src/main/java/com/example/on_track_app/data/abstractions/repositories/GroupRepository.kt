package com.example.on_track_app.data.abstractions.repositories

import com.example.on_track_app.model.MockGroup

interface GroupRepository: BasicById<MockGroup> {
    suspend fun addGroup(
        name: String,
        description:String,
        ownerId:String,
        cloudId: String?
    ): String

    suspend fun updateGroup(id: String, newName: String)
}