package com.example.on_track_app.data.abstractions.repositories

import com.example.on_track_app.model.Link
import com.example.on_track_app.model.MockReminder
import com.example.on_track_app.model.MockTimeField
import com.example.on_track_app.model.OwnerType

interface ReminderRepository: 
    BasicById<MockReminder>,
    IndexedByLink<MockReminder>,
    Update<MockReminder>,
    InTimeInterval<MockReminder>,
        LinkAndInterval<MockReminder>
{
    suspend fun addReminder(
        ownerId: String,
        ownerType: OwnerType,
        name: String,
        description: String,
        at: MockTimeField,
        linked: Link?,
        cloudId: String?
    ): String
}