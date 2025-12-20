package com.example.on_track_app.data.synchronization

/*
import com.example.on_track_app.data.realm.entities.refactor.*
import com.example.on_track_app.data.realm.utils.*
import com.example.on_track_app.data.synchronization.*
 */
import com.example.on_track_app.data.realm.entities.EventRealmEntity
import com.example.on_track_app.data.realm.entities.GroupRealmEntity
import com.example.on_track_app.data.realm.entities.MembershipRealmEntity
import com.example.on_track_app.data.realm.entities.ProjectRealmEntity
import com.example.on_track_app.data.realm.entities.ReminderRealmEntity
import com.example.on_track_app.data.realm.entities.TaskRealmEntity
import com.example.on_track_app.data.realm.entities.UserRealmEntity
import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.data.realm.utils.toMillis
import com.example.on_track_app.data.realm.utils.toRealmInstant
import com.example.on_track_app.model.LinkedType
import com.example.on_track_app.model.MembershipType
import com.example.on_track_app.model.OwnerType

fun UserRealmEntity.toDTO(): UserDTO =
    UserDTO(
        cloudId = identity!!.cloudId,
        name = name,
        email = email,
        version = identity!!.version.toMillis(),
        deleted = identity!!.synchronizationStatus == SynchronizationState.DELETED.name
    )

fun UserDTO.toRealm(entity: UserRealmEntity) {
    entity.name = name
    entity.email = email
    entity.identity!!.cloudId = cloudId ?: entity.identity!!.cloudId
    entity.identity!!.version = version.toRealmInstant()
    entity.identity!!.synchronizationStatus =
        if (deleted) SynchronizationState.DELETED.name
        else SynchronizationState.CURRENT.name
}


fun GroupRealmEntity.toDTO(): GroupDTO =
    GroupDTO(
        cloudId = identity!!.cloudId,
        name = name,
        description = description,
        cloudOwnerId = owner!!.identity!!.cloudId,
        version = identity!!.version.toMillis(),
        deleted = identity!!.synchronizationStatus == SynchronizationState.DELETED.name
    )

fun GroupDTO.toRealm(entity: GroupRealmEntity) {
    entity.name = name
    entity.description = description
    entity.identity!!.cloudId = cloudId ?: entity.identity!!.cloudId
    entity.identity!!.version = version.toRealmInstant()
    entity.identity!!.synchronizationStatus =
        if (deleted) SynchronizationState.DELETED.name
        else SynchronizationState.CURRENT.name
}

fun ProjectRealmEntity.toDTO(): ProjectDTO =
    ProjectDTO(
        cloudId = identity!!.cloudId,
        name = name,
        description = description,
        cloudOwnerId = owner!!.identity!!.cloudId,
        ownerType = when {
            owner!!.user != null -> OwnerType.USER.name
            owner!!.group != null -> OwnerType.GROUP.name
            else -> error("Unresolved owner")
        },
        version = identity!!.version.toMillis(),
        deleted = identity!!.synchronizationStatus == SynchronizationState.DELETED.name
    )

fun ProjectDTO.toRealm(entity: ProjectRealmEntity) {
    entity.name = name
    entity.description = description
    entity.identity!!.cloudId = cloudId ?: entity.identity!!.cloudId
    entity.identity!!.version = version.toRealmInstant()
    entity.identity!!.synchronizationStatus =
        if (deleted) SynchronizationState.DELETED.name
        else SynchronizationState.CURRENT.name
}

fun TaskRealmEntity.toDTO(): TaskDTO =
    TaskDTO(
        cloudId = identity!!.cloudId,
        name = name,
        description = description,
        date = due!!.instant.toMillis(),
        withTime = due!!.timed,
        status = status,
        cloudProjectId = project?.identity?.cloudId,
        cloudOwnerId = owner!!.identity!!.cloudId,
        ownerType = when {
            owner!!.user != null -> OwnerType.USER.name
            owner!!.group != null -> OwnerType.GROUP.name
            else -> error("Unresolved owner")
        },
        version = identity!!.version.toMillis(),
        deleted = identity!!.synchronizationStatus == SynchronizationState.DELETED.name
    )

fun TaskDTO.toRealm(entity: TaskRealmEntity) {
    entity.name = name
    entity.description = description
    entity.status = status
    entity.due!!.instant = date.toRealmInstant()
    entity.due!!.timed = withTime

    entity.identity!!.cloudId = cloudId ?: entity.identity!!.cloudId
    entity.identity!!.version = version.toRealmInstant()
    entity.identity!!.synchronizationStatus =
        if (deleted) SynchronizationState.DELETED.name
        else SynchronizationState.CURRENT.name
}

fun EventRealmEntity.toDTO(): EventDTO =
    EventDTO(
        cloudId = identity!!.cloudId,
        name = name,
        description = description,
        cloudProjectId = project?.identity?.cloudId,
        startDate = start!!.instant.toMillis(),
        startWithTime = start!!.timed,
        endDate = end!!.instant.toMillis(),
        endWithTime = end!!.timed,
        cloudOwnerId = owner!!.identity!!.cloudId,
        ownerType = when {
            owner!!.user != null -> OwnerType.USER.name
            owner!!.group != null -> OwnerType.GROUP.name
            else -> error("Unresolved owner")
        },
        version = identity!!.version.toMillis(),
        deleted = identity!!.synchronizationStatus == SynchronizationState.DELETED.name
    )

fun EventDTO.toRealm(entity: EventRealmEntity) {
    entity.name = name
    entity.description = description
    entity.start!!.instant = startDate.toRealmInstant()
    entity.start!!.timed = startWithTime
    entity.end!!.instant = endDate.toRealmInstant()
    entity.end!!.timed = endWithTime

    entity.identity!!.cloudId = cloudId ?: entity.identity!!.cloudId
    entity.identity!!.version = version.toRealmInstant()
    entity.identity!!.synchronizationStatus =
        if (deleted) SynchronizationState.DELETED.name
        else SynchronizationState.CURRENT.name
}

fun ReminderRealmEntity.toDTO(): ReminderDTO =
    ReminderDTO(
        cloudId = identity!!.cloudId,
        name = name,
        description = description,
        date = at!!.instant.toMillis(),
        withTime = at!!.timed,
        cloudOwnerId = owner!!.identity!!.cloudId,
        ownerType = when {
            owner!!.user != null -> OwnerType.USER.name
            owner!!.group != null -> OwnerType.GROUP.name
            else -> error("Unresolved owner")
        },
        cloudLinkTo = linkedTo?.identity?.cloudId,
        linkType = when {
            linkedTo?.task != null -> LinkedType.TASK.name
            linkedTo?.event != null -> LinkedType.EVENT.name
            else -> null
        },
        version = identity!!.version.toMillis(),
        deleted = identity!!.synchronizationStatus == SynchronizationState.DELETED.name
    )

fun ReminderDTO.toRealm(entity: ReminderRealmEntity) {
    entity.name = name
    entity.description = description
    entity.at!!.instant = date.toRealmInstant()
    entity.at!!.timed = withTime

    entity.identity!!.cloudId = cloudId ?: entity.identity!!.cloudId
    entity.identity!!.version = version.toRealmInstant()
    entity.identity!!.synchronizationStatus =
        if (deleted) SynchronizationState.DELETED.name
        else SynchronizationState.CURRENT.name
}

fun MembershipRealmEntity.toDTO(): MembershipDTO =
    MembershipDTO(
        cloudId = identity!!.cloudId,
        cloudEntityId = membership!!.identity!!.cloudId,
        cloudMemberId = member!!.identity!!.cloudId,
        type = when {
            membership!!.group != null -> MembershipType.GROUP.name
            membership!!.project != null -> MembershipType.PROJECT.name
            else -> error("Unresolved membership")
        },
        version = identity!!.version.toMillis(),
        deleted = identity!!.synchronizationStatus == SynchronizationState.DELETED.name
    )

fun MembershipDTO.toRealm(entity: MembershipRealmEntity) {
    entity.identity!!.cloudId = cloudId ?: entity.identity!!.cloudId
    entity.identity!!.version = version.toRealmInstant()
    entity.identity!!.synchronizationStatus =
        if (deleted) SynchronizationState.DELETED.name
        else SynchronizationState.CURRENT.name
}
