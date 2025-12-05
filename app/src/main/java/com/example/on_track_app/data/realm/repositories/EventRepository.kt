package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.realm.RealmDatabase
import com.example.on_track_app.data.realm.entities.CloudIdField
import com.example.on_track_app.data.realm.entities.EventRealmEntity
import com.example.on_track_app.data.realm.entities.TemporalDataField
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.data.realm.utils.toRealmInstant
import com.example.on_track_app.model.MockEvent
import com.example.on_track_app.model.MockTimeField
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId

class EventRepository {

    private val db = RealmDatabase.realm

    fun getAllEvents(): Flow<List<MockEvent>> {
        return db.query(EventRealmEntity::class)
            .asFlow()
            .map { results ->
                results.list.map { it.toDomain() }
            }
    }

    fun getEventById(id: String): MockEvent? {
        val entity = db.query(EventRealmEntity::class, "id == $0", ObjectId(id))
            .first()
            .find()
        return entity?.toDomain()
    }

    suspend fun addEvent(
        name: String,
        description: String,
        projectId: String,
        start: MockTimeField,
        end: MockTimeField,
        cloudId: String?
    ): String {
        val event = EventRealmEntity().apply {
            this.name = name
            this.description = description
            this.project = projectId
            this.start = TemporalDataField(start.date.toRealmInstant(), start.timed)
            this.end = TemporalDataField(end.date.toRealmInstant(), end.timed)
            this.cloudIdField = CloudIdField(cloudId)
        }

        return db.write {
            copyToRealm(event).id.toHexString()
        }
    }

    suspend fun updateEvent(
        id: String,
        newName: String,
        newDescription: String
    ) {
        db.write {
            val entity = query(EventRealmEntity::class, "id == $0", org.mongodb.kbson.ObjectId(id))
                .first()
                .find()

            entity?.let {
                it.name = newName
                it.description = newDescription
            }
        }
    }

    suspend fun deleteEvent(id: String) {
        db.write {
            val entity = query(EventRealmEntity::class, "id == $0", ObjectId(id))
                .first()
                .find()

            entity?.let {
                delete(findLatest(it)!!)
            }
        }
    }
}
