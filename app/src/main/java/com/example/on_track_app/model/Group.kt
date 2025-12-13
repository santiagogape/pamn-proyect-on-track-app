package com.example.on_track_app.model

data class Group (
    val id: String = "",
    val name: String = "",
    val membersIds: List<String> = emptyList()
)