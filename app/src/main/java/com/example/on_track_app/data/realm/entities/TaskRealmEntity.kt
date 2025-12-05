package com.example.on_track_app.data.realm.entities

import com.example.on_track_app.data.realm.utils.toInstant
import com.example.on_track_app.data.realm.utils.toRealmInstant
import com.example.on_track_app.data.realm.utils.toRealmList
import com.example.on_track_app.model.MockTask
import com.example.on_track_app.model.MockTimeField
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class TaskRealmEntity : RealmObject {
    @PrimaryKey
    var id: ObjectId = ObjectId()

    var name: String = ""
    var temporalData: TemporalDataField = TemporalDataField()
    var description: String = ""
    var status: String = ""
    var reminders: RealmList<String> = realmListOf()
    var project: String = ""
    var cloudIdField: CloudIdField = CloudIdField()
}

fun TaskRealmEntity.toDomain(): MockTask {
    return MockTask(
        id = id.toHexString(),
        name = name,
        date = MockTimeField(
            date = temporalData.date.toInstant(),
            timed = temporalData.withTime
        ),
        description = description,
        remindersId = reminders.toList(),
        projectId = project,
        cloudId = cloudIdField.cloudId
    )
}

fun MockTask.toEntity(): TaskRealmEntity {
    return TaskRealmEntity().apply {
        id = ObjectId(this@toEntity.id)
        name = this@toEntity.name
        description = this@toEntity.description
        reminders = this@toEntity.remindersId.toRealmList()
        project = this@toEntity.projectId
        temporalData = TemporalDataField(
            date = this@toEntity.date.date.toRealmInstant(),
            withTime = this@toEntity.date.timed
        )
        cloudIdField = CloudIdField(this@toEntity.cloudId)
    }
}
