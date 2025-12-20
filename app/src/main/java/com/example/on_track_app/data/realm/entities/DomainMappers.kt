package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.toInstant
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Group
import com.example.on_track_app.model.Linkable
import com.example.on_track_app.model.Membership
import com.example.on_track_app.model.Owner
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.Reminder
import com.example.on_track_app.model.Task
import com.example.on_track_app.model.TimeField
import com.example.on_track_app.model.User
import com.example.on_track_app.model.UserMembership
import org.mongodb.kbson.ObjectId

fun ObjectId.hex(): String = toHexString()
fun String.toObjectId() = ObjectId(this)

fun OwnerReference.toDomain(): Owner =
    when {
        user != null -> user!!.toDomain()
        group != null -> group!!.toDomain()
        else -> error(
            "OwnerReference not resolved. " +
                    "identity=${identity?.cloudId}, id=${id}"
        )
    }

fun LinkReference.toDomain(): Linkable =
    when {
        task != null -> task!!.toDomain()
        event != null -> event!!.toDomain()
        else -> error(
            "LinkReference not resolved. " +
                    "identity=${identity?.cloudId}, id=${id}"
        )
    }

fun MembershipReference.toDomain(): Membership =
    when {
        group != null -> group!!.toDomain()
        project != null -> project!!.toDomain()
        else -> error(
            "MembershipReference not resolved. " +
                    "identity=${identity?.cloudId}, id=${id}"
        )
    }

fun ProjectReference.toDomain(): Project? {
    return project?.toDomain()
}

fun TimeRealmEmbeddedObject.toDomain(): TimeField =
    TimeField(
        instant = instant.toInstant(),
        timed = timed
    )


fun UserRealmEntity.toDomain(): User =
    User(
        id = id.hex(),
        name = name,
        email = email,
        cloudId = identity!!.cloudId
    )

fun GroupRealmEntity.toDomain(): Group =
    Group(
        id = id.hex(),
        name = name,
        description = description,
        cloudId = identity!!.cloudId,
        owner = owner!!.toDomain()
    )


fun ProjectRealmEntity.toDomain(): Project =
    Project(
        id = id.hex(),
        name = name,
        description = description,
        cloudId = identity!!.cloudId,
        owner = owner!!.toDomain()
    )

fun TaskRealmEntity.toDomain(): Task =
    Task(
        id = id.hex(),
        name = name,
        description = description,
        due = due!!.toDomain(),
        cloudId = identity!!.cloudId,
        owner = owner!!.toDomain(),
        project = project?.toDomain()
    )

fun EventRealmEntity.toDomain(): Event =
    Event(
        id = id.hex(),
        name = name,
        description = description,
        start = start!!.toDomain(),
        end = end!!.toDomain(),
        cloudId = identity!!.cloudId,
        owner = owner!!.toDomain(),
        project = project?.toDomain()
    )

fun ReminderRealmEntity.toDomain(): Reminder =
    Reminder(
        id = id.hex(),
        name = name,
        description = description,
        at = at!!.toDomain(),
        cloudId = identity!!.cloudId,
        owner = owner!!.toDomain(),
        linkedTo = linkedTo?.toDomain()
    )

fun MembershipRealmEntity.toDomain(): UserMembership =
    UserMembership(
        id = id.hex(),
        cloudId = identity!!.cloudId,
        user = member!!.toDomain(),
        membership = membership!!.toDomain()
    )
