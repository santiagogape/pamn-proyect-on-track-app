package com.example.on_track_app.data.abstractions.repositories

import com.example.on_track_app.model.MockEvent
import com.example.on_track_app.model.MockTimeField
import com.example.on_track_app.model.OwnerType

interface EventRepository: BasicById<MockEvent>,
    IndexedByProject<MockEvent>,
    IndexedByOwner<MockEvent>,
    Update<MockEvent>, InTimeInterval<MockEvent>, GroupAndInterval<MockEvent>, ProjectAndInterval<MockEvent> {
    suspend fun addEvent(
        name: String,
        description: String,
        start: MockTimeField,
        end: MockTimeField,
        projectId: String?,
        cloudId: String?,
        ownerId: String,
        ownerType: OwnerType
    ): String

}