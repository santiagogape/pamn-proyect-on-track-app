package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.toRealmList
import com.example.on_track_app.model.MockGroup
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId
class GroupRealmEntity : RealmObject {
    @PrimaryKey
    var id: ObjectId = ObjectId()

    var name: String = ""
    var members: RealmList<String> = realmListOf()
    var cloudId: String? = null
    var defaultProjectId: String = ""
    var projectsId: RealmList<String> = realmListOf()
    var ownerId: String = ""
}

fun GroupRealmEntity.toDomain(): MockGroup {
    return MockGroup(
        id = id.toHexString(),
        name = name,
        membersId = members,
        cloudId = cloudId,
        defaultProjectId = defaultProjectId,
        projectsId = projectsId,
        ownerId = ownerId
    )
}

fun MockGroup.toEntity(): GroupRealmEntity {
    return GroupRealmEntity().apply {
        id = ObjectId(this@toEntity.id)
        name = this@toEntity.name
        members = this@toEntity.membersId.toRealmList()
        cloudId = this@toEntity.cloudId
        defaultProjectId = this@toEntity.defaultProjectId
        projectsId = this@toEntity.projectsId.toRealmList()
        ownerId = this@toEntity.ownerId
    }
}
