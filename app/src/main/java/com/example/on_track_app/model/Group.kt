package com.example.on_track_app.model

import com.google.firebase.firestore.DocumentId

data class Group (
    @DocumentId
    val id: String = "",
    val name: String = "",
    val members: List<User> = emptyList()
)