package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.ProjectRepository
import com.example.on_track_app.data.realm.entities.ProjectRealmEntity
import com.example.on_track_app.data.realm.entities.SyncMapper
import com.example.on_track_app.data.realm.entities.update
import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.data.synchronization.toObjectId
import com.example.on_track_app.data.synchronization.ProjectDTO
import com.example.on_track_app.data.synchronization.ReferenceIntegrityManager
import com.example.on_track_app.model.MockProject
import com.example.on_track_app.model.OwnerType
import com.example.on_track_app.utils.DebugLogcatLogger
import io.realm.kotlin.Realm
import io.realm.kotlin.types.RealmInstant
import kotlin.reflect.KClass

class RealmProjectRepository(
    db: Realm,
    mapper: SyncMapper<ProjectRealmEntity, ProjectDTO, MockProject>,
    maker: () -> ProjectRealmEntity,
    klass: KClass<ProjectRealmEntity> = ProjectRealmEntity::class,
    dtoClass: KClass<ProjectDTO> = ProjectDTO::class,
    integrityManager: ReferenceIntegrityManager
) : ProjectRepository, RealmSynchronizableRepository<ProjectRealmEntity, ProjectDTO, MockProject>(db,mapper,maker,klass,dtoClass,integrityManager), SynchronizedReference {

    override suspend fun addProject(
        name: String,
        description:String,
        cloudId: String?,
        ownerId: String,
        ownerType: OwnerType
    ): String {
        var newId = ""

        db.write {
            val project = ProjectRealmEntity().apply {
                this.name = name
                this.cloudId = cloudId
                this.ownerId = ownerId.toObjectId()
                this.ownerType = ownerType.name
                this.synchronizationStatus = SynchronizationState.CREATED.name
                this.version = RealmInstant.now()
                this.description = description
            }
            val saved = copyToRealm(project)
            integrityManager
                .resolveReferenceOnCreate(
                    transferClass,
                    saved.ownerId.toHexString(),
                    Filter.OWNER
                )?.let { saved.cloudOwnerId = it }
            newId = saved.id.toHexString()


            DebugLogcatLogger.logRealmSaved(saved)
        }

        syncEngine?.onLocalChange(newId, transferClass)

        return newId

    }

    override suspend fun updateProject(id: String, newName: String) {
        db.write {
            val project: ProjectRealmEntity? = entity(id)
            project?.let {
                it.name = newName
                it.update()
            }
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




