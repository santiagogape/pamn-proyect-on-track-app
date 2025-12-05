package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.realm.RealmDatabase
import com.example.on_track_app.data.realm.entities.ProjectRealmEntity
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.data.realm.utils.toRealmList
import com.example.on_track_app.model.MockProject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId

class ProjectRepository {

    private val db = RealmDatabase.realm

    fun getAllProjects(): Flow<List<MockProject>> {
        return db.query(ProjectRealmEntity::class)
            .asFlow()
            .map { it.list.map { e -> e.toDomain() } }
    }

    fun getProjectById(id: String): MockProject? {
        return db.query(ProjectRealmEntity::class, "id == $0", ObjectId(id))
            .first()
            .find()
            ?.toDomain()
    }

    suspend fun addProject(
        name: String,
        membersId: List<String>,
        cloudId: String?
    ): String {
        val project = ProjectRealmEntity().apply {
            this.name = name
            this.members = membersId.toRealmList()
            this.cloudId = cloudId
        }

        return db.write {
            copyToRealm(project).id.toHexString()
        }
    }

    suspend fun updateProject(id: String, newName: String) {
        db.write {
            val project = query(ProjectRealmEntity::class, "id == $0", ObjectId(id))
                .first()
                .find()

            project?.let { it.name = newName }
        }
    }

    suspend fun deleteProject(id: String) {
        db.write {
            val project = query(ProjectRealmEntity::class, "id == $0", ObjectId(id))
                .first()
                .find()

            project?.let { delete(findLatest(it)!!) }
        }
    }
}
