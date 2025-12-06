package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.model.MockGroup
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId
class GroupRealmEntity : RealmObject, SynchronizableEntity {
    @PrimaryKey
    var id: ObjectId = ObjectId()

    var name: String = ""
    var members: RealmList<String> = realmListOf()
    var defaultProjectId: String = ""
    var projectsId: RealmList<String> = realmListOf()
    var ownerId: String = ""

    @Index
    override var cloudId: String? = null
    @Index
    override var version: RealmInstant = RealmInstant.now()
    @Index
    override var synchronizationStatus: String = SynchronizationState.CREATED.name
}

fun GroupRealmEntity.toDomain(): MockGroup {
    return MockGroup(
        id = id.toHexString(),
        name = name,
        membersId = members.toList(),
        cloudId = cloudId,
        defaultProjectId = defaultProjectId,
        projectsId = projectsId.toList(),
        ownerId = ownerId
    )
}

