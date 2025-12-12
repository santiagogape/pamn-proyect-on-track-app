package com.example.on_track_app.model

import com.google.firebase.firestore.DocumentId
import java.util.UUID

data class Project (
    @DocumentId
    override val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    override val name: String = "",
    override val description: String = "",
    val membersIds: List<String> = emptyList()
) : Expandable