package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.toInstant
import com.example.on_track_app.data.realm.utils.toRealmInstant
import com.example.on_track_app.model.MockReminder
import com.example.on_track_app.model.MockTimeField
import com.example.on_track_app.model.ReminderOwner
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class ReminderRealmEntity : RealmObject {
    @PrimaryKey
    var id: ObjectId = ObjectId()

    var temporalData: TemporalDataField = TemporalDataField()
    var type: String = ""
    @Index
    var owner: String = ""
    var cloudId: String? = null
    var label: String = ""
}


fun ReminderRealmEntity.toDomain(): MockReminder {
    return MockReminder(
        id = this.id.toHexString(),
        time = MockTimeField(
            date = this.temporalData.date.toInstant(),
            timed = this.temporalData.withTime
        ),
        ownerId = this.owner,
        cloudId = this.cloudId,
        ownerType = ReminderOwner.valueOf(this.type),
        label = this.label
    )
}


fun MockReminder.toEntity(): ReminderRealmEntity {
    return ReminderRealmEntity().apply {
        id = ObjectId(this@toEntity.id)
        temporalData = TemporalDataField(
            date = this@toEntity.time.date.toRealmInstant(),
            withTime = this@toEntity.time.timed
        )
        type = ownerType.name
        owner = ownerId
        cloudId = this@toEntity.cloudId
        label = this@toEntity.label
    }
}
