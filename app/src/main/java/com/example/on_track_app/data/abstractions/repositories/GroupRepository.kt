package com.example.on_track_app.data.abstractions.repositories

import com.example.on_track_app.model.MockGroup
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun getAllGroups(): Flow<List<MockGroup>>
    fun getGroupById(id: String): MockGroup?
    suspend fun addGroup(
        name: String,
        membersId: List<String>,
        defaultProjectId: String,
        projectsId: List<String>,
        cloudId: String?
    ): String

    suspend fun updateGroup(id: String, newName: String)
    suspend fun deleteGroup(id: String)
}