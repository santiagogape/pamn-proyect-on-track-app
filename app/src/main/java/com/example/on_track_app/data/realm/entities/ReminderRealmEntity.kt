package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.toInstant
import com.example.on_track_app.model.MockReminder
import com.example.on_track_app.model.MockTimeField
import com.example.on_track_app.model.ReminderOwner
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class ReminderRealmEntity : RealmObject {
    @PrimaryKey
    var id: ObjectId = ObjectId()

    var date: RealmInstant = RealmInstant.now()
    var withTime: Boolean = false
    var type: String = ""
    @Index
    var owner: String = ""
    var label: String = ""
    // cloud
    @Index
    var cloudId: String? = null
    // versions
    @Index
    var version: RealmInstant = RealmInstant.now()
    @Index
    var synchronized: Boolean = false
}


fun ReminderRealmEntity.toDomain(): MockReminder {
    return MockReminder(
        id = id.toHexString(),
        time = MockTimeField(
            date = this.date.toInstant(),
            timed = this.withTime
        ),
        ownerId = this.owner,
        cloudId = this.cloudId,
        ownerType = ReminderOwner.valueOf(this.type),
        label = this.label
    )
}


