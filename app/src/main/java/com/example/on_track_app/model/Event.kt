package com.example.on_track_app.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class Event(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val projectId: String? = null,
    val startDate: LocalDate = LocalDate.now(),
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val endDate: LocalDate? = null
)