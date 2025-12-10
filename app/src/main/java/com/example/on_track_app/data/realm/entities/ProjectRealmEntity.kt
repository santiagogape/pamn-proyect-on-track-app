package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.model.MockProject
import com.example.on_track_app.model.ProjectOwner
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId
class ProjectRealmEntity : RealmObject, SynchronizableEntity, Owned, Entity {
    @PrimaryKey
    override var id: ObjectId = ObjectId()
    var name: String = ""
    var members: RealmList<String> = realmListOf()

    @Index
    override var ownerType: String = ProjectOwner.USER.name
    @Index
    override var ownerId: ObjectId = ObjectId()


    @Index
    override var cloudId: String? = null
    @Index
    override var version: RealmInstant = RealmInstant.now()
    @Index
    override var synchronizationStatus: String = SynchronizationState.CREATED.name

}

fun ProjectRealmEntity.toDomain(): MockProject {
    return MockProject(
        id = id.toHexString(),
        name = name,
        membersId = members.toList(),
        cloudId = cloudId,
        ownerType = ProjectOwner.valueOf(ownerType),
        ownerId = ownerId.toHexString()
    )
}



