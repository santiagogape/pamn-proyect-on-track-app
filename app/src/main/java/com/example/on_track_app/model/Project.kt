package com.example.on_track_app.model

import com.google.firebase.firestore.DocumentId

data class Project (
    @DocumentId
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val description: String = "",
    val membersIds: List<String> = emptyList()
)