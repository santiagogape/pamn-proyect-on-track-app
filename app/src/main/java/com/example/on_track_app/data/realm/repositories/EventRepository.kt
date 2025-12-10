package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.EventRepository
import com.example.on_track_app.data.realm.RealmDatabase
import com.example.on_track_app.data.realm.entities.EventRealmEntity
import com.example.on_track_app.data.realm.entities.SyncMapper
import com.example.on_track_app.data.realm.entities.delete
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.data.realm.entities.update
import com.example.on_track_app.data.synchronization.toObjectId
import com.example.on_track_app.data.realm.utils.toRealmInstant
import com.example.on_track_app.data.synchronization.EventDTO
import com.example.on_track_app.model.MockEvent
import com.example.on_track_app.model.MockTimeField
import io.realm.kotlin.Realm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.reflect.KClass


class RealmEventRepository (
    db: Realm,
    mapper: SyncMapper<EventRealmEntity, EventDTO, MockEvent>,
    maker: () -> EventRealmEntity,
    klass: KClass<EventRealmEntity> = EventRealmEntity::class
) : EventRepository, RealmSynchronizableRepository<EventRealmEntity, EventDTO, MockEvent>(db,mapper,maker,klass) {

    override suspend fun addEvent(
        name: String,
        description: String,
        projectId: String,
        start: MockTimeField,
        end: MockTimeField,
        cloudId: String?
    ): String {
        var dto: EventDTO? = null
        var newId = ""


        db.write {
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

            val saved = copyToRealm(event)
            newId = saved.id.toHexString()
            dto = mapper.toDTO(saved)
        }
        dto?.let { syncEngine?.onLocalChange(newId, it) }
        return newId
    }

    override suspend fun updateEvent(
        id: String,
        newName: String,
        newDescription: String
    ) {
        var dto: EventDTO? = null
        db.write {
            val entity: EventRealmEntity? = entity(id)
            entity?.let {
                it.name = newName
                it.description = newDescription
                it.update()
            }
            entity?.let { dto = mapper.toDTO(it) }
        }
        dto?.let { syncEngine?.onLocalChange(id,it) }
    }

    override fun byProject(id: String): Flow<List<MockEvent>> {
        return db.query(EventRealmEntity::class, "projectId == $0", id.toObjectId())
            .asFlow()
            .map { results ->
                results.list.map { it.toDomain() }
            }
    }

}
