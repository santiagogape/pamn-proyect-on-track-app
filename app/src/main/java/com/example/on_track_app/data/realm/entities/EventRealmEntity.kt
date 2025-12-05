package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.toInstant
import com.example.on_track_app.data.realm.utils.toRealmInstant
import com.example.on_track_app.data.realm.utils.toRealmList
import com.example.on_track_app.model.MockEvent
import com.example.on_track_app.model.MockTimeField
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class EventRealmEntity : RealmObject {
    @PrimaryKey
    var id: ObjectId = ObjectId()

    var name: String = ""
    var description: String = ""

    @Index
    var project: String = ""
    var start: TemporalDataField = TemporalDataField()
    var end: TemporalDataField = TemporalDataField()
    var cloudId: String? = null
    var remindersId: RealmList<String> = realmListOf()
}


fun EventRealmEntity.toDomain(): MockEvent {
    return MockEvent(
        id = id.toHexString(),
        name = name,
        description = description,
        projectId = project,
        start = MockTimeField(start.date.toInstant(), start.withTime),
        end = MockTimeField(end.date.toInstant(), end.withTime),
        cloudId = cloudId,
        remindersId = remindersId
    )
}


fun MockEvent.toEntity(): EventRealmEntity {
    return EventRealmEntity().apply {
        id = ObjectId(this@toEntity.id)
        name = this@toEntity.name
        description = this@toEntity.description
        project = this@toEntity.projectId
        start = TemporalDataField(
            date = this@toEntity.start.date.toRealmInstant(),
            withTime = this@toEntity.start.timed
        )
        end = TemporalDataField(
            date = this@toEntity.end.date.toRealmInstant(),
            withTime = this@toEntity.end.timed
        )
        cloudId = this@toEntity.cloudId
        remindersId = this@toEntity.remindersId.toRealmList()
    }
}




