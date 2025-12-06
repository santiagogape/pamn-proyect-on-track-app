package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.ReminderRepository
import com.example.on_track_app.data.realm.RealmDatabase
import com.example.on_track_app.data.realm.entities.ReminderRealmEntity
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.data.realm.utils.toRealmInstant
import com.example.on_track_app.model.MockReminder
import com.example.on_track_app.model.ReminderOwner
import io.realm.kotlin.Realm
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.UpdatedResults
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId
import java.time.LocalDate
import java.time.LocalTime

class RealmReminderRepository(): ReminderRepository {
    private val db: Realm = RealmDatabase.realm

    /**
     * Obtiene un flujo de todos los recordatorios como objetos de dominio.
     */
    override fun getAllReminders(): Flow<List<MockReminder>> {
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
    override fun getReminderById(id: String): MockReminder? {
        val entity = db.query(ReminderRealmEntity::class, "id == $0", ObjectId(id)).first().find()
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
            owner = ownerId
            this.cloudId = cloudId
            this.type = type.name
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
            val reminderToUpdate = query(ReminderRealmEntity::class, "id == $0", ObjectId(id)).first().find()
            reminderToUpdate?.let {
                it.date = newDate.toRealmInstant(newTime)
                it.withTime = withTime
            }
        }
    }

    /**
     * Elimina un recordatorio de la base de datos por su ID.
     */
    override suspend fun deleteReminder(id: String) {
        db.write {
            val reminderToDelete = query(ReminderRealmEntity::class, "id == $0", ObjectId(id)).first().find()
            reminderToDelete?.let { it -> findLatest(it)?.let { delete(it) } }
        }
    }
}
