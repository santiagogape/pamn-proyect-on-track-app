package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.ProjectRepository
import com.example.on_track_app.data.realm.entities.ProjectRealmEntity
import com.example.on_track_app.data.realm.entities.SyncMapper
import com.example.on_track_app.data.realm.entities.update
import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.data.synchronization.toObjectId
import com.example.on_track_app.data.realm.utils.toRealmList
import com.example.on_track_app.data.synchronization.ProjectDTO
import com.example.on_track_app.data.synchronization.toDTO
import com.example.on_track_app.model.MockProject
import com.example.on_track_app.utils.DebugLogcatLogger
import io.realm.kotlin.Realm
import io.realm.kotlin.types.RealmInstant
import kotlin.reflect.KClass

class RealmProjectRepository(
    db: Realm,
    mapper: SyncMapper<ProjectRealmEntity, ProjectDTO, MockProject>,
    maker: () -> ProjectRealmEntity,
    klass: KClass<ProjectRealmEntity> = ProjectRealmEntity::class
) : ProjectRepository, RealmSynchronizableRepository<ProjectRealmEntity, ProjectDTO, MockProject>(db,mapper,maker,klass) {

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
}




