package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.toInstant
import com.example.on_track_app.model.MockTask
import com.example.on_track_app.model.MockTimeField
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class TaskRealmEntity : RealmObject {
    @PrimaryKey
    var id: ObjectId = ObjectId()

    var name: String = ""
    var date: RealmInstant = RealmInstant.now()
    var withTime: Boolean = false
    var description: String = ""
    @Index
    var status: String = ""
    var reminders: RealmList<String> = realmListOf()
    @Index
    var projectId: String = ""
    // cloud
    @Index
    var cloudId: String? = null
    // versions
    @Index
    var version: RealmInstant = RealmInstant.now()
    @Index
    var synchronized: Boolean = false

}

fun TaskRealmEntity.toDomain(): MockTask {
    return MockTask(
        id = id.toHexString(),
        name = name,
        date = MockTimeField(
            date = date.toInstant(),
            timed = withTime
        ),
        description = description,
        remindersId = reminders.toList(),
        projectId = projectId,
        cloudId = cloudId
    )
}


