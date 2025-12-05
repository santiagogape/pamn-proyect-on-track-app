package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.realm.RealmDatabase
import com.example.on_track_app.data.realm.entities.CloudIdField
import com.example.on_track_app.data.realm.entities.GroupRealmEntity
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.data.realm.utils.toRealmList
import com.example.on_track_app.model.MockGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId

class GroupRepository {

    private val db = RealmDatabase.realm

    fun getAllGroups(): Flow<List<MockGroup>> {
        return db.query(GroupRealmEntity::class)
            .asFlow()
            .map { it.list.map { e -> e.toDomain() } }
    }

    fun getGroupById(id: String): MockGroup? {
        return db.query(GroupRealmEntity::class, "id == $0", ObjectId(id))
            .first()
            .find()
            ?.toDomain()
    }

    suspend fun addGroup(
        name: String,
        membersId: List<String>,
        defaultProjectId: String,
        projectsId: List<String>,
        cloudId: String?
    ): String {
        val entity = GroupRealmEntity().apply {
            this.name = name
            this.members = membersId.toRealmList()
            this.defaultProjectId = defaultProjectId
            this.projectsId = projectsId.toRealmList()
            this.cloudIdField = CloudIdField(cloudId)
        }

        return db.write {
            copyToRealm(entity).id.toHexString()
        }
    }

    suspend fun updateGroup(id: String, newName: String) {
        db.write {
            val group = query(GroupRealmEntity::class, "id == $0", ObjectId(id))
                .first()
                .find()

            group?.let { it.name = newName }
        }
    }

    suspend fun deleteGroup(id: String) {
        db.write {
            val group = query(GroupRealmEntity::class, "id == $0", ObjectId(id))
                .first()
                .find()

            group?.let { delete(findLatest(it)!!) }
        }
    }
}
