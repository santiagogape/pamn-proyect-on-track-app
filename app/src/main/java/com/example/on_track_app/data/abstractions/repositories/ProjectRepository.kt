package com.example.on_track_app.data.abstractions.repositories

import com.example.on_track_app.model.MockProject
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun getAllProjects(): Flow<List<MockProject>>
    fun getProjectById(id: String): MockProject?
    suspend fun addProject(
        name: String,
        membersId: List<String>,
        cloudId: String?
    ): String
    suspend fun updateProject(id: String, newName: String)
    suspend fun deleteProject(id: String)
}