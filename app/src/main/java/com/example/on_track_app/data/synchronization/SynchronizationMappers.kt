package com.example.on_track_app.data.synchronization

import com.example.on_track_app.data.realm.entities.EventRealmEntity
import com.example.on_track_app.data.realm.entities.GroupRealmEntity
import com.example.on_track_app.data.realm.entities.ProjectRealmEntity
import com.example.on_track_app.data.realm.entities.ReminderRealmEntity
import com.example.on_track_app.data.realm.entities.TaskRealmEntity
import com.example.on_track_app.data.realm.entities.UserRealmEntity
import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.data.realm.utils.toMillis
import com.example.on_track_app.data.realm.utils.toRealmInstant
import com.example.on_track_app.data.realm.utils.toRealmList
import org.mongodb.kbson.ObjectId

fun String.toObjectId(): ObjectId = ObjectId(this)


fun EventRealmEntity.toDTO(): EventDTO =
    EventDTO(
        cloudId = cloudId,
        name = name,
        description = description,

        projectId = projectId.toHexString(),

        startDate = startDate.toMillis(),
        startWithTime = startWithTime,
        endDate = endDate.toMillis(),
        endWithTime = endWithTime,

        remindersId = remindersId.toList(),

        version = version.toMillis(),
        deleted = synchronizationStatus == SynchronizationState.DELETED.name,
    )


fun EventDTO.toRealm(entity: EventRealmEntity) {
    entity.name = name
    entity.description = description

    entity.projectId = projectId.toObjectId()

    entity.startDate = startDate.toRealmInstant()
    entity.startWithTime = startWithTime
    entity.endDate = endDate.toRealmInstant()
    entity.endWithTime = endWithTime

    entity.remindersId = remindersId.toRealmList()

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
        members = members.toList(),
        defaultProjectId = defaultProjectId,
        projectsId = projectsId.toList(),
        ownerId = ownerId,

        version = version.toMillis(),
        deleted = synchronizationStatus == SynchronizationState.DELETED.name,
    )


fun GroupDTO.toRealm(entity: GroupRealmEntity) {
    entity.name = name
    entity.members = members.toRealmList()
    entity.defaultProjectId = defaultProjectId
    entity.projectsId = projectsId.toRealmList()
    entity.ownerId = ownerId

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
        members = members.toList(),

        ownerType = ownerType,
        ownerId = ownerId.toHexString(),

        version = version.toMillis(),
        deleted = synchronizationStatus == SynchronizationState.DELETED.name,
        
    )

fun ProjectDTO.toRealm(entity: ProjectRealmEntity) {
    entity.name = name
    entity.members = members.toRealmList()

    entity.ownerType = ownerType
    entity.ownerId = ownerId.toObjectId()

    entity.cloudId = cloudId
    entity.version = version.toRealmInstant()
    
    entity.synchronizationStatus =
        if (deleted) SynchronizationState.DELETED.name
        else SynchronizationState.CURRENT.name
}

fun ReminderRealmEntity.toDTO(): ReminderDTO =
    ReminderDTO(
        cloudId = cloudId,

        date = date.toMillis(),
        withTime = withTime,
        label = label,

        ownerId = ownerId.toHexString(),
        ownerType = ownerType,

        version = version.toMillis(),
        deleted = synchronizationStatus == SynchronizationState.DELETED.name,
        
    )

fun ReminderDTO.toRealm(entity: ReminderRealmEntity) {
    entity.date = date.toRealmInstant()
    entity.withTime = withTime
    entity.label = label

    entity.ownerId = ownerId.toObjectId()
    entity.ownerType = ownerType

    entity.cloudId = cloudId
    entity.version = version.toRealmInstant()
    
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

        reminders = reminders.toList(),
        projectId = projectId.toHexString(),

        version = version.toMillis(),
        deleted = synchronizationStatus == SynchronizationState.DELETED.name,
        
    )

fun TaskDTO.toRealm(entity: TaskRealmEntity) {
    entity.name = name
    entity.date = date.toRealmInstant()
    entity.withTime = withTime
    entity.description = description
    entity.status = status

    entity.reminders = reminders.toRealmList()
    entity.projectId = projectId.toObjectId()

    entity.cloudId = cloudId
    entity.version = version.toRealmInstant()
    
    entity.synchronizationStatus =
        if (deleted) SynchronizationState.DELETED.name
        else SynchronizationState.CURRENT.name
}

fun UserRealmEntity.toDTO(): UserDTO =
    UserDTO(
        cloudId = cloudId,
        username = username,
        email = email,
        groups = groups.toList(),
        defaultProjectId = defaultProjectId.toHexString(),
        projectsId = projectsId.map { it.toHexString() }.toList(),

        version = version.toMillis(),
        deleted = synchronizationStatus == SynchronizationState.DELETED.name,
        
    )

fun UserDTO.toRealm(entity: UserRealmEntity) {
    entity.username = username
    entity.email = email
    entity.groups = groups.toRealmList()
    entity.defaultProjectId = defaultProjectId.toObjectId()
    entity.projectsId = projectsId.map { it.toObjectId() }.toRealmList()

    entity.cloudId = cloudId
    entity.version = version.toRealmInstant()
    
    entity.synchronizationStatus =
        if (deleted) SynchronizationState.DELETED.name
        else SynchronizationState.CURRENT.name
}
