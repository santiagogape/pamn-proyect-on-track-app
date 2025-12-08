package com.example.on_track_app.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import java.time.LocalDate
import java.time.LocalTime

data class Reminder(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val taskId: String? = null,
    val eventId: String? = null,
    @get:PropertyName("date")
    val dateIso: String = LocalDate.now().toString(),
    @get:PropertyName("time")
    val timeIso: String = LocalTime.now().toString(),
) {
    @get:Exclude
    val date: LocalDate
        get() = LocalDate.parse(dateIso)
    @get:Exclude
    val time: LocalTime
        get() = LocalTime.parse(timeIso)
}
