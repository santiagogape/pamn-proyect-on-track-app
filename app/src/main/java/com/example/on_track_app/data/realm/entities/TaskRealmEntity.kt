package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.data.realm.utils.toInstant
import com.example.on_track_app.model.Described
import com.example.on_track_app.model.MockTask
import com.example.on_track_app.model.MockTimeField
import com.example.on_track_app.model.Named
import com.example.on_track_app.model.OwnerType
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class TaskRealmEntity : Named, Described, RealmObject, SynchronizableEntity, SynchronizableOwnershipEntity, SynchronizableProjectOwnershipEntity {
    @PrimaryKey
    override var id: ObjectId = ObjectId()
    override var name: String = ""
    var date: RealmInstant = RealmInstant.now()
    var withTime: Boolean = false
    override var description: String = ""
    @Index
    var status: String = ""

    @Index
    override var projectId: ObjectId? = null

    @Index
    override var cloudId: String? = null
    @Index
    override var version: RealmInstant = RealmInstant.now()
    @Index
    override var synchronizationStatus: String = SynchronizationState.CREATED.name
    override var ownerType: String = OwnerType.USER.name
    override var ownerId: ObjectId = ObjectId()
    override var cloudOwnerId: String? = null
    override var cloudProjectId: String? = null

}

fun TaskRealmEntity.toDomain(): MockTask {
    return MockTask(
        id = id.toHexString(),
        name = name,
        date = MockTimeField(
            instant = date.toInstant(),
            timed = withTime
        ),
        description = description,
        projectId = projectId?.toHexString(),
        cloudId = cloudId,
        ownerId = ownerId.toHexString(),
        ownerType = OwnerType.valueOf(ownerType),
    )
}


