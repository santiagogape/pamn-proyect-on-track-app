package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.data.realm.utils.toInstant
import com.example.on_track_app.model.Described
import com.example.on_track_app.model.MockEvent
import com.example.on_track_app.model.MockTimeField
import com.example.on_track_app.model.Named
import com.example.on_track_app.model.OwnerType
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class EventRealmEntity : RealmObject, SynchronizableEntity, SynchronizableOwnershipEntity, SynchronizableProjectOwnershipEntity,
    Named,
    Described {
    @PrimaryKey
    override var id: ObjectId = ObjectId()
    override var name: String = ""
    override var description: String = ""

    @Index
    override var ownerId: ObjectId = ObjectId()
    override var ownerType: String = OwnerType.USER.name

    @Index
    override var projectId: ObjectId? = null

    @Index
    var startDate: RealmInstant = RealmInstant.now()
    var startWithTime: Boolean = false
    var endDate: RealmInstant = RealmInstant.now()
    var endWithTime: Boolean = false

    @Index
    override var cloudId: String? = null
    @Index
    override var version: RealmInstant = RealmInstant.now()
    @Index
    override var synchronizationStatus: String = SynchronizationState.CREATED.name
    @Index
    override var cloudOwnerId: String? = null
    override var cloudProjectId: String? = null
}


fun EventRealmEntity.toDomain(): MockEvent {
    return MockEvent(
        id = id.toHexString(),
        name = name,
        description = description,
        projectId = projectId?.toHexString(),
        start = MockTimeField(startDate.toInstant(), startWithTime),
        end = MockTimeField(endDate.toInstant(), endWithTime),
        cloudId = cloudId,
        ownerId = ownerId.toHexString(),
        ownerType = OwnerType.valueOf(ownerType),
    )
}





