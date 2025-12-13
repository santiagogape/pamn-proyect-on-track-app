package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.TaskRepository
import com.example.on_track_app.data.realm.entities.SyncMapper
import com.example.on_track_app.data.realm.entities.TaskRealmEntity
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.data.realm.entities.update
import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.data.synchronization.toObjectId
import com.example.on_track_app.data.realm.utils.toRealmInstant
import com.example.on_track_app.data.synchronization.ReferenceIntegrityManager
import com.example.on_track_app.data.synchronization.TaskDTO
import com.example.on_track_app.model.MockTask
import com.example.on_track_app.model.MockTimeField
import com.example.on_track_app.model.OwnerType
import com.example.on_track_app.utils.DebugLogcatLogger
import io.realm.kotlin.Realm
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.collections.map
import kotlin.reflect.KClass

class RealmTaskRepository(
    db: Realm,
    mapper: SyncMapper<TaskRealmEntity, TaskDTO , MockTask>,
    maker: () ->TaskRealmEntity,
    klass: KClass<TaskRealmEntity> = TaskRealmEntity::class,
    dtoClass: KClass<TaskDTO> = TaskDTO::class,
    integrityManager: ReferenceIntegrityManager
) : TaskRepository,
    RealmSynchronizableRepository<TaskRealmEntity, TaskDTO , MockTask>(db,mapper,maker,klass,dtoClass,integrityManager),
    SynchronizedReference {

    override suspend fun addTask(
        name: String,
        description: String,
        date: MockTimeField,
        projectId: String?,
        cloudId: String?,
        ownerId: String,
        ownerType: OwnerType
    ): String {
        var id = ""

        db.write {
            val task = TaskRealmEntity().apply {
                this.name = name
                this.description = description
                this.projectId = projectId?.toObjectId()
                this.date = date.instant.toRealmInstant()
                this.withTime = date.timed
                this.cloudId = cloudId
                this.ownerId = ownerId.toObjectId()
                this.ownerType = ownerType.name
                //todo add status
                this.synchronizationStatus = SynchronizationState.CREATED.name
                this.version = RealmInstant.now()
            }

            val saved = copyToRealm(task)

            integrityManager.
            resolveReferenceOnCreate(transferClass, saved.ownerId.toHexString(), Filter.OWNER
            )?.let { saved.cloudOwnerId = it }

            saved.projectId?.let { integrityManager.
                resolveReferenceOnCreate(
                    transferClass,
                    it.toHexString(),
                    Filter.PROJECT
                )?.let { cloud -> saved.cloudProjectId = cloud } }

            id = saved.id.toHexString()


            DebugLogcatLogger.logRealmSaved(saved)

        }
        syncEngine?.onLocalChange(id, transferClass)

        return id
    }

    override suspend fun updateTask(
        id: String,
        newName: String,
        newDescription: String
    ) {
        db.write {
            val entity: TaskRealmEntity? = entity(id)
            entity?.let {
                it.name = newName
                it.description = newDescription
                it.update()
            }
        }

        syncEngine?.onLocalChange(id,transferClass)

    }

    override fun byProject(id: String): Flow<List<MockTask>> {
        return queryByProjectId(id)
            .asFlow()
            .map { results ->
                results.list.map { it.toDomain() }
            }
    }

    private fun queryByProjectId(id: String): RealmQuery<TaskRealmEntity> =
        db.query(localClass, Filter.PROJECT.query, id.toObjectId())


    override fun canSync(id: String): Boolean {
        val entity = db.entity(id) ?: return false

        if (entity.cloudOwnerId == null) return false

        if (entity.projectId != null && entity.cloudProjectId == null) return false

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
                Filter.PROJECT,
                id
            ).map { entity ->
                entity.cloudProjectId = cloudId
            }
        }
    }
}
