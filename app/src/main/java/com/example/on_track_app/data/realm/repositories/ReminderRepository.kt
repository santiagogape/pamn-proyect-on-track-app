package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.ReminderRepository
import com.example.on_track_app.data.realm.RealmDatabase
import com.example.on_track_app.data.realm.entities.ReminderRealmEntity
import com.example.on_track_app.data.realm.entities.delete
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.data.realm.entities.update
import com.example.on_track_app.data.synchronization.toObjectId
import com.example.on_track_app.data.realm.utils.toRealmInstant
import com.example.on_track_app.model.MockReminder
import com.example.on_track_app.model.ReminderOwner
import io.realm.kotlin.Realm
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.UpdatedResults
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime

class RealmReminderRepository(): ReminderRepository {
    private val db: Realm = RealmDatabase.realm

    /**
     * Obtiene un flujo de todos los recordatorios como objetos de dominio.
     */
    override fun getAll(): Flow<List<MockReminder>> {
        return db.query(ReminderRealmEntity::class).asFlow()
            .map { results ->
                when (results) {
                    is InitialResults, is UpdatedResults -> results.list.map { it.toDomain() }
                }
            }
    }

    /**
     * Obtiene un recordatorio específico por su ID y lo devuelve como objeto de dominio.
     */
    override fun getById(id: String): MockReminder? {
        val entity = db.query(
            ReminderRealmEntity::class,
            "id == $0",
            id.toObjectId()
        ).first().find()
        // Devuelve el objeto de dominio o null si la entidad no fue encontrada
        return entity?.toDomain()
    }

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
        val newReminder = ReminderRealmEntity().apply {
            this.date = date.toRealmInstant(time)
            this.withTime = withTime
            this.ownerId = ownerId.toObjectId()
            this.cloudId = cloudId
            this.ownerType = type.name
            this.label = label
        }

        return db.write {
            copyToRealm(newReminder).id.toHexString()
        }
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
        db.write {
            val entity: ReminderRealmEntity? = entity(id)
            entity?.let {
                it.date = newDate.toRealmInstant(newTime)
                it.withTime = withTime
                it.update()
            }
        }
    }

    /**
     * Elimina un recordatorio de la base de datos por su ID.
     */
    override suspend fun delete(id: String) {
        db.write {
            val entity: ReminderRealmEntity? = entity(id)
            entity?.let { it -> findLatest(it)?.let { delete(it) } }
        }
    }

    override suspend fun markAsDeleted(id: String) {
        db.write {
            val entity: ReminderRealmEntity? = entity(id)
            entity?.delete()
        }
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
