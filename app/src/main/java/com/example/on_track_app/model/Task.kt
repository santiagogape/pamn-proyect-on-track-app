package com.example.on_track_app.model

import com.google.firebase.firestore.DocumentId

data class Task (
    @DocumentId
    val id: String = "",
    override val name: String = "",
    override val description: String = "",
    val date: String = "",
    val reminders: List<Reminder>? = null,
    val projectId: String? = null
) : Expandable