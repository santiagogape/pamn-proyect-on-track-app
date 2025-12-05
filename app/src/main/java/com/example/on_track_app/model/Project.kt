package com.example.on_track_app.model

data class Project (
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val taskIds: List<String> = emptyList(),
    val eventIds: List<String> = emptyList(),
    val reminderIds: List<String> = emptyList(),
    val members: List<User> = emptyList()
)