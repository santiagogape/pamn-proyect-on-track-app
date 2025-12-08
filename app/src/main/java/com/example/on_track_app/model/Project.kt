package com.example.on_track_app.model

import com.google.firebase.firestore.DocumentId

data class Project (
    @DocumentId
    val id: String = "",
    override val name: String = "",
    override val description: String = "",
    val taskIds: List<String> = emptyList(),
    val eventIds: List<String> = emptyList(),
    val reminderIds: List<String> = emptyList(),
    val members: List<User> = emptyList()
) : Expandable