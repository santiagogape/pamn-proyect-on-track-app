package com.example.on_track_app.model

data class Reminder(
    val id: String = "",
    val userId: String = "",
    val ownerId: String = "",
    val ownerType: String = "",
    val date: String,
    val time: String,
)
