package com.example.on_track_app.model

data class Event(
    val id: String = "",
    override val name: String = "",
    override val description: String = "",
    val projectId: String? = null,
    val startDate: String = "",
    val startTime: String? = null,
    val endTime: String? = null,
    val endDate: String? = null
) : Expandable