package com.example.on_track_app.data.synchronization


interface Owned {
    val ownerId: String
    val ownerType: String
}

interface ProjectOwnership {
    val projectId: String
}

interface SynchronizableDTO {
    val cloudId: String?
    val version: Long
    val deleted: Boolean
}

data class EventDTO(
    override val cloudId: String? = null,
    val name: String = "",
    val description: String = "",
    override val projectId: String = "",
    val startDate: Long = 0L,     // millis
    val startWithTime: Boolean = false,
    val endDate: Long = 0L,       // millis
    val endWithTime: Boolean = false,
    val remindersId: List<String> = emptyList(),

    override val version: Long = 0L,
    override val deleted: Boolean = false
): SynchronizableDTO, ProjectOwnership

data class GroupDTO(
    override val cloudId: String? = null,
    val name: String = "",
    val members: List<String> = emptyList(),
    val defaultProjectId: String = "",
    val projectsId: List<String> = emptyList(),
    val ownerId: String = "",

    override val version: Long = 0L,
    override val deleted: Boolean = false
): SynchronizableDTO

data class ProjectDTO(
    override val cloudId: String? = null,
    val name: String = "",
    val members: List<String> = emptyList(),

    override val ownerType: String = "",
    override val ownerId: String = "",

    override val version: Long = 0L,
    override val deleted: Boolean = false
): SynchronizableDTO, Owned

data class ReminderDTO(
    override val cloudId: String? = null,

    val date: Long = 0L,
    val withTime: Boolean = false,
    val label: String = "",

    override val ownerId: String = "",
    override val ownerType: String = "",

    override val version: Long = 0L,
    override val deleted: Boolean = false
): SynchronizableDTO, Owned


data class TaskDTO(
    override val cloudId: String? = null,
    val name: String = "",
    val date: Long = 0L,
    val withTime: Boolean = false,
    val description: String = "",
    val status: String = "",
    val reminders: List<String> = emptyList(),
    override val projectId: String = "",

    override val version: Long = 0L,
    override val deleted: Boolean = false
): SynchronizableDTO, ProjectOwnership


data class UserDTO(
    override val cloudId: String? = null,
    val username: String = "",
    val email: String = "",
    val groups: List<String> = emptyList(),
    val defaultProjectId: String = "",
    val projectsId: List<String> = emptyList(),

    override val version: Long = 0L,
    override val deleted: Boolean = false
): SynchronizableDTO
