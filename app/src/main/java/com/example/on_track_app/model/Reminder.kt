package com.example.on_track_app.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class Reminder(
    @DocumentId
    override val id: String = UUID.randomUUID().toString(),
    override val name: String = "",
    override val description: String = "",
    val userId: String = "",
    val taskId: String? = null,
    val eventId: String? = null,
    @get:PropertyName("date")
    @set:PropertyName("date")
    var dateIso: String = LocalDate.now().toString(),
    @get:PropertyName("time")
    @set:PropertyName("time")
    var timeIso: String = LocalTime.now().toString(),
) : Expandable {
    @get:Exclude
    val date: LocalDate
        get() = LocalDate.parse(dateIso)
    @get:Exclude
    val time: LocalTime
        get() = LocalTime.parse(timeIso)
}
