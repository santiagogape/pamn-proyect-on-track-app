package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.realm.RealmDatabase
import com.example.on_track_app.data.realm.entities.CloudIdField
import com.example.on_track_app.data.realm.entities.UserRealmEntity
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.model.MockUser
import com.example.on_track_app.data.realm.utils.toRealmList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId

class UserRepository {

    private val db = RealmDatabase.realm

    fun getAllUsers(): Flow<List<MockUser>> {
        return db.query(UserRealmEntity::class)
            .asFlow()
            .map { it.list.map { e -> e.toDomain() } }
    }

    fun getUserById(id: String): MockUser? {
        return db.query(UserRealmEntity::class, "id == $0", ObjectId(id))
            .first()
            .find()
            ?.toDomain()
    }

    suspend fun addUser(
        username: String,
        email: String,
        groupsId: List<String>,
        defaultProjectId: String,
        projectsId: List<String>,
        cloudId: String?
    ): String {
        val entity = UserRealmEntity().apply {
            this.username = username
            this.email = email
            this.groups = groupsId.toRealmList()
            this.defaultProjectId = defaultProjectId
            this.projectsId = projectsId.toRealmList()
            this.cloudIdField = CloudIdField(cloudId)
        }

        return db.write {
            copyToRealm(entity).id.toHexString()
        }
    }

    suspend fun updateUser(id: String, newEmail: String) {
        db.write {
            val user = query(UserRealmEntity::class, "id == $0", ObjectId(id))
                .first()
                .find()

            user?.let { it.email = newEmail }
        }
    }

    suspend fun deleteUser(id: String) {
        db.write {
            val user = query(UserRealmEntity::class, "id == $0", ObjectId(id))
                .first()
                .find()

            user?.let { delete(findLatest(it)!!) }
        }
    }
}
