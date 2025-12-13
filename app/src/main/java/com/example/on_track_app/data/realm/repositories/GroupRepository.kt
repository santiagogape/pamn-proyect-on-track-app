package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.GroupRepository
import com.example.on_track_app.data.realm.entities.GroupRealmEntity
import com.example.on_track_app.data.realm.entities.SyncMapper
import com.example.on_track_app.data.realm.entities.update
import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.data.synchronization.GroupDTO
import com.example.on_track_app.data.synchronization.ReferenceIntegrityManager
import com.example.on_track_app.data.synchronization.toObjectId
import com.example.on_track_app.model.MockGroup
import io.realm.kotlin.Realm
import io.realm.kotlin.types.RealmInstant
import kotlin.reflect.KClass

class RealmGroupRepository (
    db: Realm,
    mapper: SyncMapper<GroupRealmEntity, GroupDTO , MockGroup>,
    maker: () -> GroupRealmEntity,
    klass: KClass<GroupRealmEntity> = GroupRealmEntity::class,
    dtoClass: KClass<GroupDTO> = GroupDTO::class,
    integrityManager: ReferenceIntegrityManager
) : GroupRepository , RealmSynchronizableRepository<GroupRealmEntity,GroupDTO , MockGroup>(db,mapper,maker,klass,dtoClass,integrityManager), SynchronizedReference {

    override suspend fun addGroup(
        name: String,
        description: String,
        ownerId: String,
        cloudId: String?
    ): String {
        var newId = ""

        db.write {
            val entity = GroupRealmEntity().apply {
                this.name = name
                this.ownerId = ownerId.toObjectId()
                this.description = description
                this.cloudId = cloudId

                this.synchronizationStatus = SynchronizationState.CREATED.name
                this.version = RealmInstant.now()
            }

            val saved = copyToRealm(entity)
            integrityManager.
                resolveReferenceOnCreate(transferClass, saved.ownerId.toHexString(),
                    Filter.OWNER
                )?.let { saved.cloudOwnerId = it }
            newId = saved.id.toHexString()
        }
        syncEngine?.onLocalChange(newId, transferClass)

        return newId
    }

    override suspend fun updateGroup(id: String, newName: String) {
        db.write {
            val group:GroupRealmEntity? = entity(id)
            group?.let { it.name = newName; it.update() }
        }
        syncEngine?.onLocalChange(id,transferClass)

    }

    override fun canSync(id: String): Boolean {
        val entity = db.entity(id) ?: return false

        return entity.cloudOwnerId != null
    }

    override suspend fun synchronizeReferences(id: String, cloudId: String) {
        db.write {
            filter(
                Filter.OWNER,
                id
            ).map { entity ->
                entity.cloudOwnerId = cloudId
            }
        }
    }
}
