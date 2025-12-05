package com.example.on_track_app.model

import java.time.LocalDate
import java.time.LocalTime

data class Reminder(
    val id: String,
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now(),
    val taskIds: List<String>? = null
)
