package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.toRealmList
import com.example.on_track_app.model.MockProject
import com.example.on_track_app.model.ProjectOwner
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId
class ProjectRealmEntity : RealmObject {
    @PrimaryKey
    var id: ObjectId = ObjectId()

    var name: String = ""

    @Index
    var ownerType: String = ""
    @Index
    var ownerId: String = ""
    var members: RealmList<String> = realmListOf()
    var cloudId: String? = null
    var tasksId: RealmList<String> = realmListOf()
    var eventsId: RealmList<String> = realmListOf()
}

fun ProjectRealmEntity.toDomain(): MockProject {
    return MockProject(
        id = id.toHexString(),
        name = name,
        membersId = members,
        cloudId = cloudId,
        ownerType = ProjectOwner.valueOf(ownerType),
        ownerId = ownerId,
        tasksId = tasksId,
        eventsId = eventsId
    )
}

fun MockProject.toEntity(): ProjectRealmEntity {
    return ProjectRealmEntity().apply {
        id = ObjectId(this@toEntity.id)
        name = this@toEntity.name
        members = this@toEntity.membersId.toRealmList()
        cloudId = this@toEntity.cloudId
        ownerType = this@toEntity.ownerType.name
        ownerId = this@toEntity.ownerId
        tasksId = this@toEntity.tasksId.toRealmList()
        eventsId = this@toEntity.eventsId.toRealmList()
    }
}

