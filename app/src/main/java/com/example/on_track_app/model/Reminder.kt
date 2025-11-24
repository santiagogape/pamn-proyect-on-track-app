package com.example.on_track_app.model

data class Reminder(
    val id: String,
    val date: String,
    val time: String,
    val tasks: List<Task>? = null
)
