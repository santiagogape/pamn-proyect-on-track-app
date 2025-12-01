package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.realm.RealmDatabase
import com.example.on_track_app.data.realm.entities.ReminderRealmEntity
import com.example.on_track_app.data.realm.entities.TemporizedRealmEntity
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.data.realm.utils.toRealmInstant
import com.example.on_track_app.model.realmMocks.Reminder
import io.realm.kotlin.Realm
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.UpdatedResults
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId
import java.time.LocalDate
import java.time.LocalTime

class ReminderRepository() {
    private val db: Realm = RealmDatabase.realm

    /**
     * Obtiene un flujo de todos los recordatorios como objetos de dominio.
     */
    fun getAllReminders(): Flow<List<Reminder>> {
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
    fun getReminderById(id: String): Reminder? {
        val entity = db.query(ReminderRealmEntity::class, "id == $0", ObjectId(id)).first().find()
        // Devuelve el objeto de dominio o null si la entidad no fue encontrada
        return entity?.toDomain()
    }

    /**
     * Añade un nuevo recordatorio a la base de datos.
     */
    suspend fun addReminder(date: LocalDate, time: LocalTime) {
        db.write {
            val newReminder = ReminderRealmEntity(TemporizedRealmEntity().apply {
                this.date = date.toRealmInstant(time)
                this.withTime = true
            })
            copyToRealm(newReminder)
        }
    }

    /**
     * Actualiza un recordatorio existente.
     */
    suspend fun updateReminder(id: String, newDate: LocalDate, newTime: LocalTime) {
        db.write {
            val reminderToUpdate = query(ReminderRealmEntity::class, "id == $0", ObjectId(id)).first().find()
            reminderToUpdate?.let {
                it.temporalData.date = newDate.toRealmInstant(newTime)
                it.temporalData.withTime = true
            }
        }
    }

    /**
     * Elimina un recordatorio de la base de datos por su ID.
     */
    suspend fun deleteReminder(id: String) {
        db.write {
            val reminderToDelete = query(ReminderRealmEntity::class, "id == $0", ObjectId(id)).first().find()
            reminderToDelete?.let { it -> findLatest(it)?.let { delete(it) } }
        }
    }
}
