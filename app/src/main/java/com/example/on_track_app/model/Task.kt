package com.example.on_track_app.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class Task (
    @DocumentId
    override val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    override val name: String = "",
    override val description: String = "",
    val check: Boolean = false,
    @get:PropertyName("date")
    val dateIso: String  = LocalDate.now().toString(),
    @get:PropertyName("time")
    val timeIso: String? = null,
    val projectId: String? = null
) : Expandable {
    @get:Exclude
    val date: LocalDate
        get() = LocalDate.parse(dateIso)
    @get:Exclude
    val time: LocalTime?
        get() = timeIso?.let { LocalTime.parse(it) }
}
