package com.example.on_track_app.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import java.time.LocalDate

data class Task (
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    @get:PropertyName("date")
    val dateIso: String  = LocalDate.now().toString(),
    val projectId: String? = null
) {
    @get:Exclude
    val date: LocalDate
        get() = LocalDate.parse(dateIso)
}
