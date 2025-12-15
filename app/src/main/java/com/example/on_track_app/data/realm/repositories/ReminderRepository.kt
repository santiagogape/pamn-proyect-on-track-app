package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.ReminderRepository
import com.example.on_track_app.data.realm.entities.ReminderRealmEntity
import com.example.on_track_app.data.realm.entities.SyncMapper
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.data.realm.entities.update
import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.data.realm.utils.toRealmInstant
import com.example.on_track_app.data.synchronization.ReferenceIntegrityManager
import com.example.on_track_app.data.synchronization.ReminderDTO
import com.example.on_track_app.data.synchronization.toObjectId
import com.example.on_track_app.model.Link
import com.example.on_track_app.model.MockReminder
import com.example.on_track_app.model.MockTimeField
import com.example.on_track_app.model.OwnerType
import com.example.on_track_app.utils.DebugLogcatLogger
import io.realm.kotlin.Realm
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import kotlin.reflect.KClass

class RealmReminderRepository (
    db: Realm,
    mapper: SyncMapper<ReminderRealmEntity, ReminderDTO, MockReminder>,
    maker: () -> ReminderRealmEntity,
    klass: KClass<ReminderRealmEntity> = ReminderRealmEntity::class,
    dtoClass: KClass<ReminderDTO> = ReminderDTO::class,
    integrityManager: ReferenceIntegrityManager
) : ReminderRepository, RealmSynchronizableRepository<ReminderRealmEntity, ReminderDTO,MockReminder >(db,mapper,maker,klass,dtoClass,integrityManager), SynchronizedReference {

  /**
     * Añade un nuevo recordatorio a la base de datos.
     */
    override suspend fun addReminder(
      ownerId: String,
      ownerType: OwnerType,
      name: String,
      description: String,
      at: MockTimeField,
      linked: Link?,
      cloudId: String?
  ): String {

        var newId = ""


        db.write {
            val newReminder = ReminderRealmEntity().apply {
                this.ownerId = ownerId.toObjectId()
                this.ownerType = ownerType.name
                this.name = name
                this.description = description
                this.at = at.instant.toRealmInstant()
                this.withTime = at.timed
                this.cloudId = cloudId
                this.linkedTo = linked?.to?.toObjectId()
                this.linkType = linked?.ofType?.name

                this.synchronizationStatus = SynchronizationState.CREATED.name
                this.version = RealmInstant.now()
            }

            val saved = copyToRealm(newReminder)
            integrityManager.
            resolveReferenceOnCreate(transferClass, saved.ownerId.toHexString(), Filter.OWNER
            )?.let { saved.cloudOwnerId = it }

            saved.linkedTo?.let  {
                integrityManager.
                resolveReferenceOnCreate(transferClass, it.toHexString(), Filter.LINK
                )?.let { cloud -> saved.cloudLinkedTo = cloud }
            }

            newId = saved.id.toHexString()
            DebugLogcatLogger.logRealmSaved(saved)
        }

        syncEngine?.onLocalChange(newId, transferClass)

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
        db.write {
            val entity: ReminderRealmEntity? = entity(id)
            entity?.let {
                it.at = newDate.toRealmInstant(newTime)
                it.withTime = withTime
                it.update()
            }
        }
        syncEngine?.onLocalChange(id,transferClass)

    }

    override fun ownedBy(id: String): Flow<List<MockReminder>> {
        return db.query(
            localClass,
            Filter.OWNER.query,
            id.toObjectId()
        )
            .asFlow()
            .map { results ->
                results.list.map { it.toDomain() }
            }
    }

    override fun canSync(id: String): Boolean {
        val entity = db.entity(id) ?: return false

        if (entity.cloudOwnerId == null) return false
        if (entity.linkedTo != null && entity.cloudLinkedTo == null)  return false
        return true
    }

    override suspend fun synchronizeReferences(id: String, cloudId: String) {
        db.write {
            filter(
                Filter.OWNER,
                id
            ).map { entity ->
                entity.cloudOwnerId = cloudId
            }

            filter(
                Filter.LINK,
                id
            ).map { entity ->
                entity.cloudLinkedTo = cloudId
            }
        }
    }
}
