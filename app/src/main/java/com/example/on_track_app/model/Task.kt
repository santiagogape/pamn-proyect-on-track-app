package com.example.on_track_app.model

import com.google.firebase.firestore.DocumentId
import java.time.LocalDate

data class Task (
    @DocumentId
    val id: String = "",
    override val name: String = "",
    override val description: String = "",
    val date: LocalDate  = LocalDate.now(),
    val reminders: List<Reminder>? = null,
    val projectId: String? = null
) : Expandable