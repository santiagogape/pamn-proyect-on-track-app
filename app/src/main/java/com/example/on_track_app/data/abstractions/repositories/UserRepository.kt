package com.example.on_track_app.data.abstractions.repositories

import com.example.on_track_app.model.MockUser

interface UserRepository: BasicById<MockUser> {
    suspend fun addUser(
        username: String,
        email: String,
        cloudId: String?
    ): String
    suspend fun updateUser(id: String, newEmail: String)

}