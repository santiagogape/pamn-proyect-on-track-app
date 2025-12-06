package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.data.realm.utils.toInstant
import com.example.on_track_app.model.MockReminder
import com.example.on_track_app.model.MockTimeField
import com.example.on_track_app.model.ReminderOwner
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class ReminderRealmEntity : RealmObject, SynchronizableEntity, Owned {
    @PrimaryKey
    var id: ObjectId = ObjectId()

    var date: RealmInstant = RealmInstant.now()
    var withTime: Boolean = false
    var label: String = ""

    @Index
    override var ownerId: ObjectId = ObjectId()
    override var ownerType: String = ""

    @Index
    override var cloudId: String? = null
    @Index
    override var version: RealmInstant = RealmInstant.now()
    @Index
    override var synchronizationStatus: String = SynchronizationState.CREATED.name
}


fun ReminderRealmEntity.toDomain(): MockReminder {
    return MockReminder(
        id = id.toHexString(),
        time = MockTimeField(
            date = this.date.toInstant(),
            timed = this.withTime
        ),
        ownerId = this.ownerId.toHexString(),
        cloudId = this.cloudId,
        ownerType = ReminderOwner.valueOf(this.ownerType),
        label = this.label
    )
}


