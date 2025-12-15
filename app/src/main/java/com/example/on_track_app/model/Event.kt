package com.example.on_track_app.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class Event(
    @DocumentId
    override val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    override val name: String = "",
    override val description: String = "",
    val projectId: String? = null,
    var startDate: String = LocalDate.now().toString(),
    var startTime: String? = null,
    var endTime: String? = null,
    var endDate: String? = null
) : Expandable {
    @get:Exclude
    val startDateObj: LocalDate
        get() = LocalDate.parse(startDate)

    @get:Exclude
    val startTimeObj: LocalTime?
        get() = startTime?.let { LocalTime.parse(it) }

    @get:Exclude
    val endTimeObj: LocalTime?
        get() = endTime?.let { LocalTime.parse(it) }

    @get:Exclude
    val endDateObj: LocalDate?
        get() = endDate?.let { LocalDate.parse(it) }

}