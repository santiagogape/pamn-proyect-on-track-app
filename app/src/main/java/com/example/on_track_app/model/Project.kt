package com.example.on_track_app.model

import com.google.firebase.firestore.DocumentId

data class Project (
    @DocumentId
    override val id: String = "",
    val ownerId: String = "",
    override val name: String = "",
    override val description: String = "",
    val membersIds: List<String> = emptyList()
) : Expandable