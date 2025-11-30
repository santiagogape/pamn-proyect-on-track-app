package com.example.on_track_app.model

data class Task (
    val id: String = "",
    val name: String = "",
    val date: String = "",
    val description: String = "",
    val reminders: List<Reminder>? = null,
    val project: Project? = null
)