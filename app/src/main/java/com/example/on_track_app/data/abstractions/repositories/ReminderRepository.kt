package com.example.on_track_app.data.abstractions.repositories

import com.example.on_track_app.model.Link
import com.example.on_track_app.model.MockReminder
import com.example.on_track_app.model.MockTimeField
import com.example.on_track_app.model.OwnerType
import java.time.LocalDate
import java.time.LocalTime

interface ReminderRepository: BasicById<MockReminder>, IndexedByOwner<MockReminder> {
    suspend fun addReminder(
        ownerId: String,
        ownerType: OwnerType,
        label: String,
        at: MockTimeField,
        linked: Link?,
        cloudId: String?
    ): String
    suspend fun updateReminder(
        id: String,
        newDate: LocalDate,
        newTime: LocalTime,
        withTime: Boolean
    )
}