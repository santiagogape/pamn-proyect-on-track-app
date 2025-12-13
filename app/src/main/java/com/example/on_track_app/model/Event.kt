package com.example.on_track_app.model

import java.time.LocalDate

data class Event(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val projectId: String? = null,
    val startDateIso: String = LocalDate.now().toString(),
    val startTimeIso: String? = null,
    val endTimeIso: String? = null,
    val endDateIso: String? = null
)