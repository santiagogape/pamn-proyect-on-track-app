package com.example.on_track_app.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Date

data class Event(
    @DocumentId
    override val id: String = "",
    override val name: String = "",
    override val description: String = "",
    val projectId: String? = null,
    @get:PropertyName("startDate")
    val startDateIso: String = LocalDate.now().toString(),
    @get:PropertyName("startTime")
    val startTimeIso: String? = null,
    @get:PropertyName("endTime")
    val endTimeIso: String? = null,
    @get:PropertyName("endDate")
    val endDateIso: String? = null
) : Expandable {
    @get:Exclude
    val startDate: LocalDate
        get() = LocalDate.parse(startDateIso)

    @get:Exclude
    val startTime: LocalTime?
        get() = startTimeIso?.let { LocalTime.parse(it) }

    @get:Exclude
    val endTime: LocalTime?
        get() = endTimeIso?.let { LocalTime.parse(it) }

    @get:Exclude
    val endDate: LocalDate?
        get() = endDateIso.let { LocalDate.parse(it) }

}