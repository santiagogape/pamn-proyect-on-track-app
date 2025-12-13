package com.example.on_track_app.model

data class Task (
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val date: String = "",
    val projectId: String? = null
)