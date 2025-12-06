package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.model.MockUser
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class UserRealmEntity : RealmObject {
    @PrimaryKey
    var id: ObjectId = ObjectId()

    var username: String = ""
    var email: String = ""
    var groups: RealmList<String> = realmListOf()
    var defaultProjectId: String = ""
    var projectsId: RealmList<String> = realmListOf()
    // cloud
    @Index
    var cloudId: String? = null
    // versions
    @Index
    var version: RealmInstant = RealmInstant.now()
    @Index
    var synchronized: Boolean = false
}

fun UserRealmEntity.toDomain(): MockUser {
    return MockUser(
        id = id.toHexString(),
        username = username,
        email = email,
        groups = groups.toList(),
        cloudId = cloudId,
        defaultProjectId = defaultProjectId,
        projectsId = projectsId.toList()
    )
}

