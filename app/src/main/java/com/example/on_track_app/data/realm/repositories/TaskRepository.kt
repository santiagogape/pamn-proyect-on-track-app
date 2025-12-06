package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.TaskRepository
import com.example.on_track_app.data.realm.RealmDatabase
import com.example.on_track_app.data.realm.entities.TaskRealmEntity
import com.example.on_track_app.data.realm.entities.delete
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.data.realm.entities.update
import com.example.on_track_app.data.synchronization.toObjectId
import com.example.on_track_app.data.realm.utils.toRealmInstant
import com.example.on_track_app.data.realm.utils.toRealmList
import com.example.on_track_app.model.MockTask
import com.example.on_track_app.model.MockTimeField
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.collections.map

class RealmTaskRepository: TaskRepository {

    private val db = RealmDatabase.realm

    override fun getAll(): Flow<List<MockTask>> {
        return db.query(TaskRealmEntity::class)
            .asFlow()
            .map { it.list.map { e -> e.toDomain() } }
    }

    override fun getById(id: String): MockTask? {
        return db.query(TaskRealmEntity::class, "id == $0", id.toObjectId())
            .first()
            .find()
            ?.toDomain()
    }

    override suspend fun addTask(
        name: String,
        description: String,
        date: MockTimeField,
        remindersId: List<String>,
        projectId: String,
        cloudId: String?
    ): String {
        val task = TaskRealmEntity().apply {
            this.name = name
            this.description = description
            this.projectId = projectId.toObjectId()
            this.date = date.date.toRealmInstant()
            this.withTime = date.timed
            this.reminders = remindersId.toRealmList()
            this.cloudId = cloudId
        }

        return db.write {
            copyToRealm(task).id.toHexString()
        }
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
    }

    override suspend fun delete(id: String) {
        db.write {
            val entity: TaskRealmEntity? = entity(id)
            entity?.let { delete(findLatest(it)!!) }
        }
    }

    override suspend fun markAsDeleted(id: String) {
        db.write {
            val entity: TaskRealmEntity? = entity(id)
            entity?.delete()
        }
    }

    override fun byProject(id: String): Flow<List<MockTask>> {
        return db.query(TaskRealmEntity::class, "projectId == $0", id.toObjectId())
            .asFlow()
            .map { results ->
                results.list.map { it.toDomain() }
            }
    }
}
