package com.example.on_track_app.data.abstractions.repositories

import com.example.on_track_app.model.MockEvent
import com.example.on_track_app.model.MockTimeField

interface EventRepository: BasicById<MockEvent>, IndexedByProject<MockEvent> {
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

}