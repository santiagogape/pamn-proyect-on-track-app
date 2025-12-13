package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.model.Described
import com.example.on_track_app.model.MockProject
import com.example.on_track_app.model.Named
import com.example.on_track_app.model.OwnerType
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class ProjectRealmEntity : RealmObject, Named, Described, SynchronizableEntity, SynchronizableOwnershipEntity {
    @PrimaryKey
    override var id: ObjectId = ObjectId()
    override var name: String = ""
    override var description: String = ""
    override var ownerType: String = OwnerType.USER.name
    @Index
    override var cloudOwnerId: String? = null

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
        cloudId = cloudId,
        ownerType = OwnerType.valueOf(ownerType),
        ownerId = ownerId.toHexString(),
        description = description,
    )
}



