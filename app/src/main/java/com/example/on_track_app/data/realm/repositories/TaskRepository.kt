package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.TaskRepository
import com.example.on_track_app.data.realm.RealmDatabase
import com.example.on_track_app.data.realm.entities.TaskRealmEntity
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.data.realm.utils.toRealmInstant
import com.example.on_track_app.data.realm.utils.toRealmList
import com.example.on_track_app.model.MockTask
import com.example.on_track_app.model.MockTimeField
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId

class RealmTaskRepository: TaskRepository {

    private val db = RealmDatabase.realm

    override fun getAllTasks(): Flow<List<MockTask>> {
        return db.query(TaskRealmEntity::class)
            .asFlow()
            .map { it.list.map { e -> e.toDomain() } }
    }

    override fun getTaskById(id: String): MockTask? {
        return db.query(TaskRealmEntity::class, "id == $0", ObjectId(id))
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
            this.projectId = projectId
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
            val task = query(TaskRealmEntity::class, "id == $0", ObjectId(id))
                .first()
                .find()

            task?.let {
                it.name = newName
                it.description = newDescription
            }
        }
    }

    override suspend fun deleteTask(id: String) {
        db.write {
            val task = query(TaskRealmEntity::class, "id == $0", ObjectId(id))
                .first()
                .find()

            task?.let { delete(findLatest(it)!!) }
        }
    }
}
