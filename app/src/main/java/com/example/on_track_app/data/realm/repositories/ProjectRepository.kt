package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.ProjectRepository
import com.example.on_track_app.data.realm.entities.ProjectRealmEntity
import com.example.on_track_app.data.realm.entities.delete
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.data.realm.entities.update
import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.data.synchronization.toObjectId
import com.example.on_track_app.data.realm.utils.toRealmList
import com.example.on_track_app.data.synchronization.ProjectDTO
import com.example.on_track_app.data.synchronization.SyncEngine
import com.example.on_track_app.data.synchronization.toDTO
import com.example.on_track_app.data.synchronization.toRealm
import com.example.on_track_app.model.MockProject
import com.example.on_track_app.utils.DebugLogcatLogger
import io.realm.kotlin.Realm
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RealmProjectRepository(
    val db: Realm
) : ProjectRepository, SynchronizableRepository<ProjectDTO>, RealmRepository<ProjectRealmEntity>() {

    private var syncEngine: SyncEngine? = null
    override val klass = ProjectRealmEntity::class


    override fun getAll(): Flow<List<MockProject>> {
        return db.query(ProjectRealmEntity::class)
            .asFlow()
            .map { it.list.map { e -> e.toDomain() } }
    }

    override fun getById(id: String): MockProject? {
        return db.entity(id)?.toDomain()
    }

    override suspend fun addProject(
        name: String,
        membersId: List<String>,
        cloudId: String?,
        ownerId: String,
        ownerType: String
    ): String {
        var dto: ProjectDTO? = null
        var newId = ""

        db.write {
            val project = ProjectRealmEntity().apply {
                this.name = name
                this.members = membersId.toRealmList()
                this.cloudId = cloudId
                this.ownerId = ownerId.toObjectId()
                this.ownerType = ownerType
                this.synchronizationStatus = SynchronizationState.CREATED.name
                this.version = RealmInstant.now()
            }
            val saved = copyToRealm(project)
            newId = saved.id.toHexString()

            dto = saved.toDTO()

            DebugLogcatLogger.logRealmSaved(saved)
        }

        dto?.let { syncEngine?.onLocalChange(newId, it)}

        return newId

    }

    override suspend fun updateProject(id: String, newName: String) {
        var dto: ProjectDTO? = null
        db.write {
            val project: ProjectRealmEntity? = entity(id)
            project?.let {
                it.name = newName
                it.update()
            }
            dto = project?.toDTO()
        }

        dto?.let { syncEngine?.onLocalChange(id,it) }
    }

    override suspend fun markAsDeleted(id: String) {
        var dto: ProjectDTO? = null

        db.write {
            val project: ProjectRealmEntity? = entity(id)
            project?.delete()
            dto = project?.toDTO()
        }

        dto?.let { syncEngine?.onLocalChange(id,it) }
    }

    override suspend fun delete(id: String) {
        db.write {
            val entity = entity<ProjectRealmEntity>(id)
            entity?.let { delete(findLatest(it)!!) }
        }
    }


    override suspend fun applyRemoteInsert(dto: ProjectDTO) {
        db.write {
            val entity = ProjectRealmEntity().apply {
                dto.toRealm(this)
            }
            DebugLogcatLogger.logDTOFromRemote(DebugLogcatLogger.event.insert,dto)
            DebugLogcatLogger.logRealmSaved(entity)
            copyToRealm(entity)
        }
    }

    override suspend fun applyRemoteUpdate(dto: ProjectDTO) {
        val local = db.entityByCloudId(dto.cloudId!!)

        if (local == null) {
            applyRemoteInsert(dto)
            return
        }

        db.write {
            val latest = findLatest(local) ?: return@write
            dto.toRealm(latest)
            latest.synchronizationStatus = SynchronizationState.CURRENT.name
            DebugLogcatLogger.logDTOFromRemote(DebugLogcatLogger.event.update,dto)
            DebugLogcatLogger.logRealmSaved(latest)
        }
    }

    override suspend fun applyCloudId(id:String,dto: ProjectDTO): ProjectDTO {
        lateinit var result: ProjectDTO

        db.entity(id)?.let { local ->
            db.write {
                val latest = findLatest(local) ?: return@write
                dto.toRealm(latest)
                latest.synchronizationStatus = SynchronizationState.CURRENT.name
                result = latest.toDTO()
                DebugLogcatLogger.logRealmSaved(latest)
            }
        }
        return result
    }

    override fun attachToEngine(engine: SyncEngine) {
        syncEngine = engine
    }


    override suspend fun applyRemoteDelete(dto: ProjectDTO) {
        db.write {

            dto.cloudId?.let { cloudId ->
                val local = entityByCloudId<ProjectRealmEntity>(cloudId)

                local?.let { delete(findLatest(it)!!) }
            }


        }
    }

}




