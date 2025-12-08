package com.example.on_track_app.model

import com.google.firebase.firestore.DocumentId
import java.time.LocalDate
import java.time.LocalTime

data class Reminder(
    @DocumentId
    val id: String = "",
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now(),
    val taskIds: List<String>? = null
)
