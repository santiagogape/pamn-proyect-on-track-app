package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.data.realm.utils.toInstant
import com.example.on_track_app.model.Described
import com.example.on_track_app.model.MockReminder
import com.example.on_track_app.model.MockTimeField
import com.example.on_track_app.model.Named
import com.example.on_track_app.model.OwnerType
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class ReminderRealmEntity : RealmObject, Named, Described, SynchronizableEntity,  SynchronizableOwnershipEntity, SynchronizableLinkEntity {
    @PrimaryKey
    override var id: ObjectId = ObjectId()
    @Index
    override var ownerId: ObjectId = ObjectId()
    override var ownerType: String = OwnerType.USER.name

    override var name: String = ""
    override var description: String = ""
    var at: RealmInstant = RealmInstant.now()
    var withTime: Boolean = false

    @Index
    override var cloudId: String? = null
    @Index
    override var version: RealmInstant = RealmInstant.now()
    @Index
    override var synchronizationStatus: String = SynchronizationState.CREATED.name

    override var cloudOwnerId: String? = null

    @Index
    override var linkedTo: ObjectId? = null
    override var linkType: String? = null

    @Index
    override var cloudLinkedTo: String? = null
}


fun ReminderRealmEntity.toDomain(): MockReminder {
    return MockReminder(
        id = id.toHexString(),
        ownerId = this.ownerId.toHexString(),
        ownerType = OwnerType.valueOf(this.ownerType),
        at = MockTimeField(
            instant = this.at.toInstant(),
            timed = this.withTime
        ),
        linked = this.toLink(),
        cloudId = this.cloudId,
        name = this.name,
        description = this.description
    )
}


