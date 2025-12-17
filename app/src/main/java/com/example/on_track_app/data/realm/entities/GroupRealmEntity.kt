package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.model.Described
import com.example.on_track_app.model.MockGroup
import com.example.on_track_app.model.Named
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class GroupRealmEntity : RealmObject, Named, Described,SynchronizableEntity,
    SynchronizableOwnedEntity  {
    @PrimaryKey
    override var id: ObjectId = ObjectId()

    override var name: String = ""
    override var ownerId: ObjectId = ObjectId()


    @Index
    override var cloudId: String? = null
    @Index
    override var version: RealmInstant = RealmInstant.now()
    @Index
    override var synchronizationStatus: String = SynchronizationState.CREATED.name
    override var description: String = ""
    override var cloudOwnerId: String? = null

}

fun GroupRealmEntity.toDomain(): MockGroup {
    return MockGroup(
        id = id.toHexString(),
        name = name,
        cloudId = cloudId,
        ownerId = ownerId.toHexString(),
        description = description
    )
}

