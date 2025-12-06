package com.example.on_track_app.data.abstractions.repositories

import com.example.on_track_app.model.MockReminder
import com.example.on_track_app.model.ReminderOwner
import java.time.LocalDate
import java.time.LocalTime

interface ReminderRepository: BasicById<MockReminder>, IndexedByOwner<MockReminder> {
    suspend fun addReminder(
        ownerId: String,
        label: String,
        date: LocalDate,
        time: LocalTime,
        withTime: Boolean,
        cloudId: String?,
        type: ReminderOwner
    ): String
    suspend fun updateReminder(
        id: String,
        newDate: LocalDate,
        newTime: LocalTime,
        withTime: Boolean
    )
}