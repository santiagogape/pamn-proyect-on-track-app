package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.UserRepository
import com.example.on_track_app.data.realm.RealmDatabase
import com.example.on_track_app.data.realm.entities.UserRealmEntity
import com.example.on_track_app.data.realm.entities.delete
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.data.realm.entities.update
import com.example.on_track_app.data.synchronization.toObjectId
import com.example.on_track_app.model.MockUser
import com.example.on_track_app.data.realm.utils.toRealmList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RealmUserRepository: UserRepository {

    private val db = RealmDatabase.realm


    override fun getAll(): Flow<List<MockUser>> {
        return db.query(UserRealmEntity::class)
            .asFlow()
            .map { it.list.map { e -> e.toDomain() } }
    }

    override fun getById(id: String): MockUser? {
        return db.query(UserRealmEntity::class, "id == $0", id.toObjectId())
            .first()
            .find()
            ?.toDomain()
    }

    override suspend fun addUser(
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
            this.cloudId = cloudId
        }

        return db.write {
            copyToRealm(entity).id.toHexString()
        }
    }

    override suspend fun updateUser(id: String, newEmail: String) {
        db.write {
            val user: UserRealmEntity? = entity(id)

            user?.let { it.email = newEmail; it.update() }
        }
    }

    override suspend fun delete(id: String) {
        db.write {
            val user: UserRealmEntity? = entity(id)

            user?.let { delete(findLatest(it)!!) }
        }
    }

    override suspend fun markAsDeleted(id: String) {
        db.write {
            val user: UserRealmEntity? = entity(id)
            user?.delete()
        }
    }
}




