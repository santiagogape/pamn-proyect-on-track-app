package com.example.on_track_app.model

import com.google.firebase.firestore.DocumentId

data class User (
    @DocumentId
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val taskIds: List<String> = emptyList(),
    val projectIds: List<String> = emptyList(),
    val groupIds: List<String>? = null
)