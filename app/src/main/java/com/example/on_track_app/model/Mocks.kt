package com.example.on_track_app.model

import com.example.on_track_app.data.realm.utils.toLocalDate
import com.example.on_track_app.data.realm.utils.toLocalTime
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

data class MockTimeField(
    val date: Instant,
    val timed: Boolean = false
)

fun MockTimeField.toDate(): LocalDate {
    return this.date.toLocalDate()
}

fun MockTimeField.toTime(): LocalTime? {
    return if (this.timed) {
        date.toLocalTime()
    } else {
        null
    }
}


interface Identifiable {
    val id: String
    val name: String
}

data class MockObjectRemindersResume(
    val id: String,
    val remindersId: List<String> = listOf(),
)

data class MockReminder(
    override val id: String,
    val time: MockTimeField,
    val ownerId: String,
    val ownerType: ReminderOwner,
    override val name: String,
    override val cloudId: String? = null
): CloudIdentifiable, Identifiable

data class MockEvent(
    override val id: String = "",
    override val name: String = "",
    val description: String = "",
    val projectId: String,
    val start: MockTimeField,
    val end: MockTimeField,
    override val cloudId: String? = null,
    val remindersId: List<String> = emptyList()
): CloudIdentifiable, Identifiable

data class MockTask (
    override val id: String = "",
    override val name: String = "",
    val date: MockTimeField,
    val description: String = "",
    val remindersId: List<String> = listOf(),
    val projectId: String,
    override val cloudId: String? = null
): CloudIdentifiable, Identifiable

data class MockGroup (
    override val id: String = "",
    override val name: String = "",
    val ownerId: String = "",
    val membersId: List<String> = emptyList(),
    val defaultProjectId: String,
    val projectsId: List<String> = emptyList(),
    override val cloudId: String? = null
): CloudIdentifiable, Identifiable

data class MockProject (
    override val id: String = "",
    override val name: String = "",
    val membersId: List<String> = emptyList(),
    override val cloudId: String? = null,
    val ownerType: ProjectOwner = ProjectOwner.USER,
    val ownerId: String = ""
): CloudIdentifiable, Identifiable


data class MockUser (
    override val id: String = "",
    override val name: String = "",
    val email: String = "",
    val groups: List<String> = listOf(),
    val defaultProjectId: String,
    val projectsId: List<String> = emptyList(),
    override val cloudId: String? = null
): CloudIdentifiable, Identifiable