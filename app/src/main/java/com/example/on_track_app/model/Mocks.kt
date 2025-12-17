package com.example.on_track_app.model

import com.example.on_track_app.data.realm.utils.toInstant
import com.example.on_track_app.data.realm.utils.toLocalDate
import com.example.on_track_app.data.realm.utils.toLocalTime
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

interface Timed{
    fun toTime(): LocalTime?
    fun isDatedAt(day:LocalDate): Boolean
}

interface ExtendedTimed: Timed {
    fun toTimeAt(day: LocalDate): LocalTime?
}


data class MockTimeField(
    val instant: Instant,
    val timed: Boolean = false
){
    constructor(date: LocalDate, h: Int? = null, m: Int? = null) :
            this(
            date.toInstant(h, m),
            h != null && m != null
            )

}

fun MockTimeField.toDate(): LocalDate {
    return this.instant.toLocalDate()
}

fun MockTimeField.toTime(): LocalTime? {
    return if (this.timed) {
        instant.toLocalTime()
    } else {
        null
    }
}


interface Identifiable {
    val id: String
}


interface Owned {
    val ownerId: String
}

interface Ownership: Owned {
    val ownerType: OwnerType
}

interface ProjectOwned {
    val projectId: String?
}

interface Named {
    val name: String
}

interface Described {
    val description: String
}

interface Linked {
    val linked: Link?
}

data class Link(
    val to: String ,//id
    val ofType: LinkedType
)

data class Membership (
    val entityId: String = "",
    val memberId: String = "",
    val membershipType: MembershipType,
    override val id: String
): Identifiable

data class MockReminder(
    override val id: String,
    override val ownerId: String,
    override val ownerType: OwnerType,
    override val name: String,
    override val description: String,
    val at: MockTimeField,
    override val linked: Link?,
    override val cloudId: String?,
): Identifiable,
    Ownership,
    CloudIdentifiable, Named, Described, Linked, Selectable, TimedExpandable {
    override fun toTime(): LocalTime? = at.toTime()
    override fun isDatedAt(day: LocalDate): Boolean = at.toDate() == day
    fun update(name: String,
               description: String,
               at: MockTimeField,
               linked: Link?) = copy(name = name,
        description = description, at = at, linked = linked)
}

data class MockEvent(
    override val id: String,
    override val ownerId: String,
    override val ownerType: OwnerType,
    override val projectId: String?,
    override val name: String,
    override val description: String,
    val start: MockTimeField,
    val end: MockTimeField,
    override val cloudId: String?,
): Identifiable,
    Ownership,
    ProjectOwned,
    Named,
    Described,
    CloudIdentifiable, TimedExpandable, Selectable {
    override fun toTime(): LocalTime? = start.toTime()

    override fun isDatedAt(day: LocalDate): Boolean =
        start.toDate() <= day && end.toDate() >= day

    fun update(name: String,
               description: String,
               start: MockTimeField,
               end: MockTimeField,
               projectId: String?) = copy(name = name,
        description = description, start = start, end = end, projectId = projectId)

    val onDayEvent = start.toDate() == end.toDate() && start.toTime() == null && end.toTime() == null

}

data class MockTask (
    override val id: String,
    override val ownerId: String,
    override val ownerType: OwnerType,
    override val projectId: String?,
    override val name: String,
    override val description: String,
    val due: MockTimeField,
    override val cloudId: String? = null,
): Identifiable,
    Ownership,
    ProjectOwned,
    Named,
    Described,
    CloudIdentifiable, Selectable, TimedExpandable {
    override fun toTime(): LocalTime? = due.toTime()
    override fun isDatedAt(day: LocalDate): Boolean = due.toDate() == day
    fun update(name: String,
               description: String,
               due: MockTimeField,
               projectId: String?) = copy(name = name,
        description = description, due = due, projectId = projectId)
}

data class MockGroup (
    override val id: String,
    override val name: String,
    override val description: String,
    override val ownerId: String,
    override val cloudId: String?
): Identifiable,
    Named,
    Described,
    Owned,
    CloudIdentifiable, Expandable, Selectable {
    fun update(name: String,
               description: String) = copy(name = name,
        description = description)
}

data class MockProject (
    override val id: String,
    override val ownerId: String,
    override val ownerType: OwnerType,
    override val name: String,
    override val description: String,
    override val cloudId: String?
): Identifiable,
    Ownership,
    Named,
    Described,
    CloudIdentifiable, Expandable, Selectable {
    fun update(name: String,
               description: String) = copy(name = name,
        description = description)
}

data class MockUser (
    override val id: String = "",
    override val name: String = "",
    val email: String = "",
    override val cloudId: String? = null
):  Identifiable,
    Named,
    CloudIdentifiable, Selectable