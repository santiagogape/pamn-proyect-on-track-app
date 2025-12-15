package com.example.on_track_app.data.synchronization

import com.example.on_track_app.data.realm.entities.EventRealmEntity
import com.example.on_track_app.data.realm.entities.GroupRealmEntity
import com.example.on_track_app.data.realm.entities.ProjectRealmEntity
import com.example.on_track_app.data.realm.entities.RealmMembershipEntity
import com.example.on_track_app.data.realm.entities.ReminderRealmEntity
import com.example.on_track_app.data.realm.entities.TaskRealmEntity
import com.example.on_track_app.data.realm.entities.UserRealmEntity
import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.data.realm.utils.toMillis
import com.example.on_track_app.data.realm.utils.toRealmInstant
import org.mongodb.kbson.ObjectId

fun String.toObjectId(): ObjectId = ObjectId(this)


fun EventRealmEntity.toDTO(): EventDTO =
    EventDTO(
        cloudId = cloudId,
        name = name,
        description = description,

        cloudProjectId = cloudProjectId,
        startDate = startDate.toMillis(),
        startWithTime = startWithTime,
        endDate = endDate.toMillis(),
        endWithTime = endWithTime,

        version = version.toMillis(),
        deleted = synchronizationStatus == SynchronizationState.DELETED.name,
        ownerType = ownerType,
        cloudOwnerId = cloudOwnerId!!,
    )


fun EventDTO.toRealm(entity: EventRealmEntity) {
    entity.name = name
    entity.description = description

    entity.cloudProjectId = cloudProjectId
    entity.cloudOwnerId = cloudOwnerId
    entity.ownerType = ownerType

    entity.startDate = startDate.toRealmInstant()
    entity.startWithTime = startWithTime
    entity.endDate = endDate.toRealmInstant()
    entity.endWithTime = endWithTime

    entity.cloudId = cloudId
    entity.version = version.toRealmInstant()
    entity.synchronizationStatus =
        if (deleted) SynchronizationState.DELETED.name
        else SynchronizationState.CURRENT.name
}


fun GroupRealmEntity.toDTO(): GroupDTO =
    GroupDTO(
        cloudId = cloudId,
        name = name,
        cloudOwnerId = cloudOwnerId!!,
        version = version.toMillis(),
        deleted = synchronizationStatus == SynchronizationState.DELETED.name,
        description = description,
    )


fun GroupDTO.toRealm(entity: GroupRealmEntity) {
    entity.name = name
    entity.description = description
    entity.cloudOwnerId = cloudOwnerId
    entity.cloudId = cloudId
    entity.version = version.toRealmInstant()
    entity.synchronizationStatus =
        if (deleted) SynchronizationState.DELETED.name
        else SynchronizationState.CURRENT.name
}


fun ProjectRealmEntity.toDTO(): ProjectDTO =
    ProjectDTO(
        cloudId = cloudId,
        name = name,
        ownerType = ownerType,
        cloudOwnerId = cloudOwnerId!!,
        version = version.toMillis(),
        deleted = synchronizationStatus == SynchronizationState.DELETED.name,
        description = description,
    )

fun ProjectDTO.toRealm(entity: ProjectRealmEntity) {
    entity.name = name
    entity.description = description
    entity.cloudOwnerId = cloudOwnerId
    entity.ownerType = ownerType
    entity.cloudId = cloudId
    entity.version = version.toRealmInstant()
    entity.synchronizationStatus =
        if (deleted) SynchronizationState.DELETED.name
        else SynchronizationState.CURRENT.name
}

fun ReminderRealmEntity.toDTO(): ReminderDTO =
    ReminderDTO(
        cloudId = cloudId,

        date = at.toMillis(),
        withTime = withTime,
        name = this.name,
        description = this.description,

        cloudOwnerId = cloudOwnerId!!,
        ownerType = ownerType,

        version = version.toMillis(),
        deleted = synchronizationStatus == SynchronizationState.DELETED.name,
        cloudLinkTo = cloudLinkedTo!!,
        linkType = linkType!!,
    )

fun ReminderDTO.toRealm(entity: ReminderRealmEntity) {
    entity.at = date.toRealmInstant()
    entity.withTime = withTime
    entity.name = name
    entity.description = description
    entity.cloudOwnerId = cloudOwnerId
    entity.ownerType = ownerType

    entity.cloudId = cloudId
    entity.version = version.toRealmInstant()
    entity.cloudLinkedTo = cloudLinkTo
    entity.linkType = linkType
    entity.synchronizationStatus =
        if (deleted) SynchronizationState.DELETED.name
        else SynchronizationState.CURRENT.name
}

fun TaskRealmEntity.toDTO(): TaskDTO =
    TaskDTO(
        cloudId = cloudId,

        name = name,
        date = date.toMillis(),
        withTime = withTime,
        description = description,
        status = status,
        cloudProjectId = cloudProjectId,
        version = version.toMillis(),
        deleted = synchronizationStatus == SynchronizationState.DELETED.name,
        ownerType = ownerType,
        cloudOwnerId = cloudOwnerId!!,
    )

fun TaskDTO.toRealm(entity: TaskRealmEntity) {
    entity.name = name
    entity.date = date.toRealmInstant()
    entity.withTime = withTime
    entity.description = description
    entity.status = status

    entity.cloudProjectId = cloudProjectId

    entity.cloudId = cloudId
    entity.version = version.toRealmInstant()
    entity.ownerType = ownerType
    entity.cloudOwnerId = cloudOwnerId
    entity.synchronizationStatus =
        if (deleted) SynchronizationState.DELETED.name
        else SynchronizationState.CURRENT.name
}

fun UserRealmEntity.toDTO(): UserDTO =
    UserDTO(
        cloudId = cloudId,
        email = email,
        version = version.toMillis(),
        deleted = synchronizationStatus == SynchronizationState.DELETED.name,
        name = name
    )

fun UserDTO.toRealm(entity: UserRealmEntity) {
    entity.email = email
    entity.name = name
    entity.cloudId = cloudId
    entity.version = version.toRealmInstant()
    
    entity.synchronizationStatus =
        if (deleted) SynchronizationState.DELETED.name
        else SynchronizationState.CURRENT.name
}

fun RealmMembershipEntity.toDTO(): MembershipDTO =
    MembershipDTO(
        cloudId = cloudId,
        version = version.toMillis(),
        deleted = synchronizationStatus == SynchronizationState.DELETED.name,
        cloudEntityId = cloudEntityId!!,
        cloudMemberId = cloudMemberId!!,
        type = membershipType
    )



fun MembershipDTO.toRealm(entity: RealmMembershipEntity){
    entity.cloudId = cloudId
    entity.version = version.toRealmInstant()
    entity.synchronizationStatus =
        if (deleted) SynchronizationState.DELETED.name
        else SynchronizationState.CURRENT.name
    entity.cloudEntityId = cloudEntityId
    entity.cloudMemberId = cloudMemberId
    entity.membershipType = type
}
