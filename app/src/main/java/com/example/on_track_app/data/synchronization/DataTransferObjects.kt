package com.example.on_track_app.data.synchronization

import com.example.on_track_app.model.Described
import com.example.on_track_app.model.Labeled
import com.example.on_track_app.model.Named


interface SynchronizableUserOwnership {
    val cloudOwnerId: String
}

interface SynchronizableOwnership: SynchronizableUserOwnership {
    val ownerType: String
}

interface SynchronizableProjectOwnership {
    val cloudProjectId: String
}

interface SynchronizableMembership {
    val cloudEntityId: String
    val cloudMemberId: String
    val type: String
}

interface SynchronizableLink {
    val cloudLinkTo: String?
    val linkType: String?
}

interface SynchronizableDTO {
    val cloudId: String?
    val version: Long
    val deleted: Boolean
    fun copyDTO(cloudId: String? = null): SynchronizableDTO
}

data class MembershipDTO (
    override val cloudId: String?,
    override val version: Long,
    override val deleted: Boolean,
    override val cloudEntityId: String,
    override val cloudMemberId: String,
    override val type: String,
    ) : SynchronizableDTO, SynchronizableMembership {
    override fun copyDTO(cloudId: String?): SynchronizableDTO {
        return copy(cloudId = cloudId)
    }
}

data class EventDTO(
    override val cloudId: String? = null,
    override val name: String = "",
    override val description: String = "",
    override val cloudProjectId: String = "",
    val startDate: Long = 0L,     // millis
    val startWithTime: Boolean = false,
    val endDate: Long = 0L,       // millis
    val endWithTime: Boolean = false,
    override val version: Long = 0L,
    override val deleted: Boolean = false,
    override val ownerType: String = "",
    override val cloudOwnerId: String = "",
    ): SynchronizableDTO,
    SynchronizableProjectOwnership,
    SynchronizableOwnership,
    Named, Described {
    override fun copyDTO(cloudId: String?): EventDTO = copy(cloudId = cloudId)
}

data class GroupDTO(
    override val cloudId: String? = null,
    override val name: String = "",
    override val version: Long = 0L,
    override val deleted: Boolean = false,
    override val description: String = "",
    override val cloudOwnerId: String = "",
): SynchronizableDTO,
    Named,
    Described,
    SynchronizableUserOwnership {
    override fun copyDTO(cloudId: String?): GroupDTO = copy(cloudId = cloudId)
}

data class ProjectDTO(
    override val cloudId: String? = null,
    override val name: String = "",

    override val ownerType: String = "",
    override val cloudOwnerId: String = "",

    override val version: Long = 0L,
    override val deleted: Boolean = false,
    override val description: String = "",
): SynchronizableDTO,
    SynchronizableOwnership,
    Named,
    Described {
    override fun copyDTO(cloudId: String?): ProjectDTO = copy(cloudId = cloudId)
}

data class ReminderDTO(
    override val cloudId: String? = null,

    val date: Long = 0L,
    val withTime: Boolean = false,
    override val label: String = "",

    override val cloudOwnerId: String = "",
    override val ownerType: String = "",

    override val version: Long = 0L,
    override val deleted: Boolean = false,
    override val cloudLinkTo: String? = null,
    override val linkType: String? = null,
): SynchronizableDTO,
    SynchronizableOwnership,
    Labeled,
    SynchronizableLink {
    override fun copyDTO(cloudId: String?): ReminderDTO = copy(cloudId = cloudId)
}

data class TaskDTO(
    override val cloudId: String? = null,
    override val name: String = "",
    override val description: String = "",
    val date: Long = 0L,
    val withTime: Boolean = false,
    val status: String = "",
    override val cloudProjectId: String = "",
    override val version: Long = 0L,
    override val deleted: Boolean = false,
    override val ownerType: String = "",
    override val cloudOwnerId: String = "",
): SynchronizableDTO,
    SynchronizableProjectOwnership,
    SynchronizableOwnership,
    Named,
    Described {
    override fun copyDTO(cloudId: String?): TaskDTO = copy(cloudId = cloudId)
}


data class UserDTO(
    override val cloudId: String? = null,
    override val name: String = "",
    val email: String = "",
    override val version: Long = 0L,
    override val deleted: Boolean = false,
): SynchronizableDTO, Named {
    override fun copyDTO(cloudId: String?): UserDTO = copy(cloudId = cloudId)
}
