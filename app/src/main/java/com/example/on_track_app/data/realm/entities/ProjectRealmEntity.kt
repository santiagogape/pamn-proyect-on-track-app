package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.model.MockProject
import com.example.on_track_app.model.ProjectOwner
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmInstant
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
    // cloud
    @Index
    var cloudId: String? = null
    // versions
    @Index
    var version: RealmInstant = RealmInstant.now()
    @Index
    var synchronized: Boolean = false
}

fun ProjectRealmEntity.toDomain(): MockProject {
    return MockProject(
        id = id.toHexString(),
        name = name,
        membersId = members.toList(),
        cloudId = cloudId,
        ownerType = ProjectOwner.valueOf(ownerType),
        ownerId = ownerId
    )
}



