package com.example.on_track_app.model

data class Reminder(
    val id: String,
    val date: String,
    val time: String,
    val taskIds: List<String>? = null
)
