package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.EventRepository
import com.example.on_track_app.data.realm.RealmDatabase
import com.example.on_track_app.data.realm.entities.EventRealmEntity
import com.example.on_track_app.data.realm.entities.delete
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.data.realm.entities.update
import com.example.on_track_app.data.synchronization.toObjectId
import com.example.on_track_app.data.realm.utils.toRealmInstant
import com.example.on_track_app.model.MockEvent
import com.example.on_track_app.model.MockTimeField
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RealmEventRepository: EventRepository {

    private val db = RealmDatabase.realm

    override fun getAll(): Flow<List<MockEvent>> {
        return db.query(EventRealmEntity::class)
            .asFlow()
            .map { results ->
                results.list.map { it.toDomain() }
            }
    }

    override suspend fun markAsDeleted(id: String) {
        db.write {
            val entity: EventRealmEntity? = entity(id)
            entity?.delete()
        }
    }

    override fun getById(id: String): MockEvent? {
        val entity = db.query(EventRealmEntity::class, "id == $0", id.toObjectId())
            .first()
            .find()
        return entity?.toDomain()
    }

    override suspend fun addEvent(
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
            this.projectId = projectId.toObjectId()
            this.startDate = start.date.toRealmInstant()
            this.startWithTime = start.timed
            this.endDate = end.date.toRealmInstant()
            this.endWithTime = end.timed
            this.cloudId = cloudId
        }

        return db.write {
            copyToRealm(event).id.toHexString()
        }
    }

    override suspend fun updateEvent(
        id: String,
        newName: String,
        newDescription: String
    ) {
        db.write {
            val entity: EventRealmEntity? = entity(id)
            entity?.let {
                it.name = newName
                it.description = newDescription
                it.update()
            }
        }
    }

    override suspend fun delete(id: String) {
        db.write {
            val entity: EventRealmEntity? = entity(id)
            entity?.let {
                delete(findLatest(it)!!)
            }
        }
    }

    override fun byProject(id: String): Flow<List<MockEvent>> {
        return db.query(EventRealmEntity::class, "projectId == $0", id.toObjectId())
            .asFlow()
            .map { results ->
                results.list.map { it.toDomain() }
            }
    }

}
