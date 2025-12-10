package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.ReminderRepository
import com.example.on_track_app.data.realm.entities.ReminderRealmEntity
import com.example.on_track_app.data.realm.entities.SyncMapper
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.data.realm.entities.update
import com.example.on_track_app.data.synchronization.toObjectId
import com.example.on_track_app.data.realm.utils.toRealmInstant
import com.example.on_track_app.data.synchronization.ReminderDTO
import com.example.on_track_app.model.MockReminder
import com.example.on_track_app.model.ReminderOwner
import com.example.on_track_app.utils.DebugLogcatLogger
import io.realm.kotlin.Realm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import kotlin.reflect.KClass

class RealmReminderRepository (
    db: Realm,
    mapper: SyncMapper<ReminderRealmEntity, ReminderDTO, MockReminder>,
    maker: () -> ReminderRealmEntity,
    klass: KClass<ReminderRealmEntity> = ReminderRealmEntity::class
) : ReminderRepository, RealmSynchronizableRepository<ReminderRealmEntity, ReminderDTO,MockReminder >(db,mapper,maker,klass) {

  /**
     * Añade un nuevo recordatorio a la base de datos.
     */
    override suspend fun addReminder(
        ownerId: String,
        label: String,
        date: LocalDate,
        time: LocalTime,
        withTime: Boolean,
        cloudId: String?,
        type: ReminderOwner
    ): String {

        var dto: ReminderDTO? = null
        var newId = ""


        db.write {
            val newReminder = ReminderRealmEntity().apply {
                this.date = date.toRealmInstant(time)
                this.withTime = withTime
                this.ownerId = ownerId.toObjectId()
                this.cloudId = cloudId
                this.ownerType = type.name
                this.label = label
            }

            val saved = copyToRealm(newReminder)
            newId = saved.id.toHexString()
            dto = mapper.toDTO(saved)
            DebugLogcatLogger.logRealmSaved(saved)
        }

        dto?.let { syncEngine?.onLocalChange(newId, it) }

        return newId
    }

    /**
     * Actualiza un recordatorio existente.
     */
    override suspend fun updateReminder(
        id: String,
        newDate: LocalDate,
        newTime: LocalTime,
        withTime: Boolean
    ) {
        var dto: ReminderDTO? = null
        db.write {
            val entity: ReminderRealmEntity? = entity(id)
            entity?.let {
                it.date = newDate.toRealmInstant(newTime)
                it.withTime = withTime
                it.update()
            }
            dto = entity?.let{mapper.toDTO(it)}
        }
        dto?.let { syncEngine?.onLocalChange(id,it) }

    }

    override fun ownedBy(id: String): Flow<List<MockReminder>> {
        return db.query(
            ReminderRealmEntity::class,
            "ownerId == $0",
            id.toObjectId()
        )
            .asFlow()
            .map { results ->
                results.list.map { it.toDomain() }
            }
    }
}
