package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.ProjectRepository
import com.example.on_track_app.data.realm.RealmDatabase
import com.example.on_track_app.data.realm.entities.ProjectRealmEntity
import com.example.on_track_app.data.realm.entities.delete
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.data.realm.entities.update
import com.example.on_track_app.data.synchronization.toObjectId
import com.example.on_track_app.data.realm.utils.toRealmList
import com.example.on_track_app.model.MockProject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RealmProjectRepository: ProjectRepository {

    private val db = RealmDatabase.realm

    override fun getAll(): Flow<List<MockProject>> {
        return db.query(ProjectRealmEntity::class)
            .asFlow()
            .map { it.list.map { e -> e.toDomain() } }
    }

    override fun getById(id: String): MockProject? {
        return db.query(ProjectRealmEntity::class, "id == $0", id.toObjectId())
            .first()
            .find()
            ?.toDomain()
    }

    override suspend fun addProject(
        name: String,
        membersId: List<String>,
        cloudId: String?,
        ownerId: String,
        ownerType: String
    ): String {
        val project = ProjectRealmEntity().apply {
            this.name = name
            this.members = membersId.toRealmList()
            this.cloudId = cloudId
            this.ownerId = ownerId.toObjectId()
            this.ownerType = ownerType

        }

        return db.write {
            copyToRealm(project).id.toHexString()
        }
    }

    override suspend fun updateProject(id: String, newName: String) {
        db.write {
            val project: ProjectRealmEntity? = entity(id)
            project?.let { it.name = newName; it.update() }
        }
    }

    override suspend fun delete(id: String) {
        db.write {
            val project: ProjectRealmEntity? = entity(id)

            project?.let { delete(findLatest(it)!!) }
        }
    }

    override suspend fun markAsDeleted(id: String) {
        db.write {
            val project: ProjectRealmEntity? = entity(id)

            project?.delete()
        }
    }
}
