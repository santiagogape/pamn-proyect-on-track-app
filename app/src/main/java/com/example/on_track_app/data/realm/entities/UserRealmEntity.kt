package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.toRealmList
import com.example.on_track_app.model.MockUser
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class UserRealmEntity : RealmObject {
    @PrimaryKey
    var id: ObjectId = ObjectId()

    var username: String = ""
    var email: String = ""
    var groups: RealmList<String> = realmListOf()
    var cloudId: String? = null
    var defaultProjectId: String = ""
    var projectsId: RealmList<String> = realmListOf()
}

fun UserRealmEntity.toDomain(): MockUser {
    return MockUser(
        id = id.toHexString(),
        username = username,
        email = email,
        groups = groups,
        cloudId = cloudId,
        defaultProjectId = defaultProjectId,
        projectsId = projectsId
    )
}

fun MockUser.toEntity(): UserRealmEntity {
    return UserRealmEntity().apply {
        id = ObjectId(this@toEntity.id)
        username = this@toEntity.username
        email = this@toEntity.email
        groups = this@toEntity.groups.toRealmList()
        cloudId = this@toEntity.cloudId
        defaultProjectId = this@toEntity.defaultProjectId
        projectsId = this@toEntity.projectsId.toRealmList()
    }
}
