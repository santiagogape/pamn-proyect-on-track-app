package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.data.realm.utils.toInstant
import com.example.on_track_app.model.MockEvent
import com.example.on_track_app.model.MockTimeField
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class EventRealmEntity : RealmObject, SynchronizableEntity, ProjectOwnership, Entity {
    @PrimaryKey
    override var id: ObjectId = ObjectId()
    var name: String = ""
    var description: String = ""

    @Index
    var startDate: RealmInstant = RealmInstant.now()
    var startWithTime: Boolean = false
    var endDate: RealmInstant = RealmInstant.now()
    var endWithTime: Boolean = false
    var remindersId: RealmList<String> = realmListOf()

    @Index
    override var projectId: ObjectId = ObjectId()

    @Index
    override var cloudId: String? = null
    @Index
    override var version: RealmInstant = RealmInstant.now()
    @Index
    override var synchronizationStatus: String = SynchronizationState.CREATED.name
}


fun EventRealmEntity.toDomain(): MockEvent {
    return MockEvent(
        id = id.toHexString(),
        name = name,
        description = description,
        projectId = projectId.toHexString(),
        start = MockTimeField(startDate.toInstant(), startWithTime),
        end = MockTimeField(endDate.toInstant(), endWithTime),
        cloudId = cloudId,
        remindersId = remindersId.toList()
    )
}





