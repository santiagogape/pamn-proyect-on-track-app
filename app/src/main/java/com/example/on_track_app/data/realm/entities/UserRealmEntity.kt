package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.model.MockUser
import com.example.on_track_app.model.Named
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class UserRealmEntity : RealmObject, SynchronizableEntity, Named {
    @PrimaryKey
    override var id: ObjectId = ObjectId()
    override var name: String = ""
    var email: String = ""

    @Index
    override var cloudId: String? = null
    @Index
    override var version: RealmInstant = RealmInstant.now()
    @Index
    override var synchronizationStatus: String = SynchronizationState.CREATED.name
}

fun UserRealmEntity.toDomain(): MockUser {
    return MockUser(
        id = id.toHexString(),
        name = name,
        email = email,
        cloudId = cloudId
    )
}

