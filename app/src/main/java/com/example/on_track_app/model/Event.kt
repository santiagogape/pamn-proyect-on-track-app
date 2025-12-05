package com.example.on_track_app.model

data class Event(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val project: Project? = null,
    val startDate: String = "",
    val startTime: String? = null,
    val endTime: String? = null,
    val endDate: String? = null
)