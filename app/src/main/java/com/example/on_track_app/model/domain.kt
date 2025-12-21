package com.example.on_track_app.model

import com.example.on_track_app.data.realm.utils.toInstant
import com.example.on_track_app.data.realm.utils.toLocalDate
import com.example.on_track_app.data.realm.utils.toLocalTime
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime


// ---- DOM/UI ----

interface Identifiable { val id: String }
interface Named { val name: String }
interface Described { val description: String }
interface CloudIdentifiable { val cloudId: String }
interface Selectable: Identifiable,Named
interface Expandable: Selectable, Described
interface TimedExpandable: Expandable, Timed
fun List<TimedExpandable>.sort(): List<TimedExpandable> =
    this.sortedWith(compareBy { it.toTime() ?: LocalTime.MIN })
fun <T> List<T>.sortByTime(): List<T> where T: Timed =
    this.sortedWith(compareBy { it.toTime() ?: LocalTime.MIN })

// ---- RELATIONS ----
sealed interface Owner : Selectable
sealed interface Linkable : Selectable
sealed interface Membership : Selectable


interface Owned { val owner: Owner }
interface ProjectOwned { val project: Project? }
interface Linked { val linkedTo: Linkable? }

// ---- Time ----

interface Timed{
    fun toTime(): LocalTime?
    fun isDatedAt(day:LocalDate): Boolean
}
data class TimeField(val instant: Instant, val timed: Boolean = false){
    constructor(date:LocalDate,hour:Int?=null,min:Int?=null):
            this(date.toInstant(hour,min), hour != null && min != null)
}
fun TimeField.toDate(): LocalDate {
    return this.instant.toLocalDate()
}

fun TimeField.toTime(): LocalTime? {
    return if (this.timed) {
        instant.toLocalTime()
    } else {
        null
    }
}

// ---- DOM ----
data class User(
    override val id: String,
    override val name: String,
    val email: String,
    override val cloudId: String
) : CloudIdentifiable, Owner

data class Group(
    override val id: String,
    override val name: String,
    override val description: String,
    override val cloudId: String,
    override val owner: Owner
) : Expandable, CloudIdentifiable, Owned, Owner,Membership {
    fun update(name: String,
               description: String) = copy(name = name,
        description = description)
}

data class Project(
    override val id: String,
    override val name: String,
    override val description: String,
    override val cloudId: String,
    override val owner: Owner
) : Expandable, CloudIdentifiable, Owned, Membership {
    fun update(name: String,
               description: String) = copy(name = name,
        description = description)
}

data class UserMembership(
    override val id: String,
    override val cloudId: String,
    val user: User,
    val membership: Membership
): Identifiable, CloudIdentifiable


data class Task(
    override val id: String,
    override val name: String,
    override val description: String,
    val due: TimeField,
    override val cloudId: String,
    override val owner: Owner,
    override val project: Project?
) : Expandable, CloudIdentifiable, Owned, ProjectOwned, Linkable, TimedExpandable {
    override fun toTime(): LocalTime? = due.toTime()
    override fun isDatedAt(day: LocalDate): Boolean = due.toDate() == day
    fun update(name: String,
               description: String,
               due: TimeField,
               project: Project?) = copy(name = name,
        description = description, due = due, project = project)
}

data class Event(
    override val id: String,
    override val name: String,
    override val description: String,
    val start: TimeField,
    val end: TimeField,
    override val cloudId: String,
    override val owner: Owner,
    override val project: Project?
) : Expandable, CloudIdentifiable, Owned, ProjectOwned, Linkable, TimedExpandable {
    override fun toTime(): LocalTime? = start.toTime()

    override fun isDatedAt(day: LocalDate): Boolean =
        start.toDate() <= day && end.toDate() >= day

    fun update(name: String,
               description: String,
               start: TimeField,
               end: TimeField,
               project: Project?) = copy(name = name,
        description = description, start = start, end = end, project = project)

    val onDayEvent = start.toDate() == end.toDate() && start.toTime() == null && end.toTime() == null

}

data class Reminder(
    override val id: String,
    override val name: String,
    override val description: String,
    val at: TimeField,
    override val cloudId: String,
    override val owner: Owner,
    override val linkedTo: Linkable?
) : Expandable, CloudIdentifiable, Owned, Linked, TimedExpandable {
    override fun toTime(): LocalTime? = at.toTime()
    override fun isDatedAt(day: LocalDate): Boolean = at.toDate() == day
    fun update(name: String,
               description: String,
               at: TimeField,
               linked: Linkable?) = copy(name = name,
        description = description, at = at, linkedTo = linked)
}
