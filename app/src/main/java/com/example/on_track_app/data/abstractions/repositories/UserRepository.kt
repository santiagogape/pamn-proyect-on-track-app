package com.example.on_track_app.data.abstractions.repositories

import com.example.on_track_app.model.MockUser
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getAllUsers(): Flow<List<MockUser>>
    fun getUserById(id: String): MockUser?
    suspend fun addUser(
        username: String,
        email: String,
        groupsId: List<String>,
        defaultProjectId: String,
        projectsId: List<String>,
        cloudId: String?
    ): String
    suspend fun updateUser(id: String, newEmail: String)
    suspend fun deleteUser(id: String)

}