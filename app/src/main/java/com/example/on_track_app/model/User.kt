package com.example.on_track_app.model

data class User (
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val taskIds: List<String> = emptyList(),
    val projectIds: List<String> = emptyList(),
    val groupIds: List<String>? = null
)