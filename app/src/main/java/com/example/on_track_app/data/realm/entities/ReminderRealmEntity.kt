package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.toInstant
import com.example.on_track_app.data.realm.utils.toRealmInstant
import com.example.on_track_app.model.realmMocks.Reminder
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class ReminderRealmEntity( apply: TemporizedRealmEntity?) : RealmObject {
    @PrimaryKey
    val id: ObjectId = ObjectId()

    var temporalData: TemporizedRealmEntity = apply ?: TemporizedRealmEntity()
}

fun ReminderRealmEntity.toDomain(): Reminder {
    return Reminder(
        id = this.id.toString(),
        instant = this.temporalData.date.toInstant()
    )
}

fun Reminder.toEntity(): ReminderRealmEntity {
    val entity = this
    return ReminderRealmEntity( TemporizedRealmEntity().apply {
        this.date = entity.instant.toRealmInstant()
        this.withTime = true
    } )
}