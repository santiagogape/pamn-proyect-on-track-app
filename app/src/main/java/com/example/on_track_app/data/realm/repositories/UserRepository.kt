package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.UserRepository
import com.example.on_track_app.data.realm.entities.SyncMapper
import com.example.on_track_app.data.realm.entities.UserRealmEntity
import com.example.on_track_app.data.realm.entities.update
import com.example.on_track_app.data.realm.utils.toRealmList
import com.example.on_track_app.data.synchronization.UserDTO
import com.example.on_track_app.model.MockUser
import com.example.on_track_app.utils.DebugLogcatLogger
import io.realm.kotlin.Realm
import kotlin.reflect.KClass

class RealmUserRepository(
    db: Realm,
    mapper: SyncMapper<UserRealmEntity, UserDTO, MockUser>,
    maker: () -> UserRealmEntity,
    klass: KClass<UserRealmEntity> = UserRealmEntity::class
) : UserRepository, RealmSynchronizableRepository<UserRealmEntity, UserDTO, MockUser>(db,mapper,maker,klass) {

    override suspend fun addUser(
        username: String,
        email: String,
        groupsId: List<String>,
        defaultProjectId: String,
        projectsId: List<String>,
        cloudId: String?
    ): String {
        var dto: UserDTO? = null
        var id = ""

        db.write {
            val entity = UserRealmEntity().apply {
                this.username = username
                this.email = email
                this.groups = groupsId.toRealmList()
                this.defaultProjectId = defaultProjectId
                this.projectsId = projectsId.toRealmList()
                this.cloudId = cloudId
            }
            val saved = copyToRealm(entity)
            id = saved.id.toHexString()
            dto = mapper.toDTO(saved)
            DebugLogcatLogger.logRealmSaved(saved)
        }

        dto?.let { syncEngine?.onLocalChange(id, it) }

        return id

    }

    override suspend fun updateUser(id: String, newEmail: String) {
        var dto: UserDTO? = null
        db.write {
            val user: UserRealmEntity? = entity(id)

            user?.let { it.email = newEmail; it.update() }
            dto = user?.let{mapper.toDTO(it)}
        }

        dto?.let { syncEngine?.onLocalChange(id,it)}
    }
}




