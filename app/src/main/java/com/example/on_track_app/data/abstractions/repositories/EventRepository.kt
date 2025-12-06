package com.example.on_track_app.data.abstractions.repositories

import com.example.on_track_app.model.MockEvent
import com.example.on_track_app.model.MockTimeField
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun getAllEvents(): Flow<List<MockEvent>>
    fun getEventById(id: String): MockEvent?
    suspend fun addEvent(
        name: String,
        description: String,
        projectId: String,
        start: MockTimeField,
        end: MockTimeField,
        cloudId: String?
    ): String
    suspend fun updateEvent(
        id: String,
        newName: String,
        newDescription: String
    )
    suspend fun deleteEvent(id: String)

}