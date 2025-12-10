package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.TaskRepository
import com.example.on_track_app.data.realm.RealmDatabase
import com.example.on_track_app.data.realm.entities.SyncMapper
import com.example.on_track_app.data.realm.entities.TaskRealmEntity
import com.example.on_track_app.data.realm.entities.delete
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.data.realm.entities.update
import com.example.on_track_app.data.synchronization.toObjectId
import com.example.on_track_app.data.realm.utils.toRealmInstant
import com.example.on_track_app.data.realm.utils.toRealmList
import com.example.on_track_app.data.synchronization.TaskDTO
import com.example.on_track_app.data.synchronization.toDTO
import com.example.on_track_app.model.MockTask
import com.example.on_track_app.model.MockTimeField
import com.example.on_track_app.utils.DebugLogcatLogger
import io.realm.kotlin.Realm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.collections.map
import kotlin.reflect.KClass

class RealmTaskRepository(
    db: Realm,
    mapper: SyncMapper<TaskRealmEntity, TaskDTO , MockTask>,
    maker: () ->TaskRealmEntity,
    klass: KClass<TaskRealmEntity> = TaskRealmEntity::class
) : TaskRepository, RealmSynchronizableRepository<TaskRealmEntity, TaskDTO , MockTask>(db,mapper,maker,klass) {

    override suspend fun addTask(
        name: String,
        description: String,
        date: MockTimeField,
        remindersId: List<String>,
        projectId: String,
        cloudId: String?
    ): String {
        var dto: TaskDTO? = null
        var id = ""

        db.write {
            val task = TaskRealmEntity().apply {
                this.name = name
                this.description = description
                this.projectId = projectId.toObjectId()
                this.date = date.date.toRealmInstant()
                this.withTime = date.timed
                this.reminders = remindersId.toRealmList()
                this.cloudId = cloudId
            }

            val saved = copyToRealm(task)
            id = saved.id.toHexString()

            dto = saved.toDTO()

            DebugLogcatLogger.logRealmSaved(saved)

        }
        dto?.let { syncEngine?.onLocalChange(id, it)}

        return id
    }

    override suspend fun updateTask(
        id: String,
        newName: String,
        newDescription: String
    ) {
        var dto: TaskDTO? = null
        db.write {
            val entity: TaskRealmEntity? = entity(id)
            entity?.let {
                it.name = newName
                it.description = newDescription
                it.update()
            }
            dto = entity?.toDTO()
        }

        dto?.let { syncEngine?.onLocalChange(id,it) }

    }

    override fun byProject(id: String): Flow<List<MockTask>> {
        return db.query(TaskRealmEntity::class, "projectId == $0", id.toObjectId())
            .asFlow()
            .map { results ->
                results.list.map { it.toDomain() }
            }
    }
}
