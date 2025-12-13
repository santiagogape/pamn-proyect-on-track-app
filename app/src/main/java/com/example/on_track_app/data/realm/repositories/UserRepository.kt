package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.UserRepository
import com.example.on_track_app.data.realm.entities.SyncMapper
import com.example.on_track_app.data.realm.entities.UserRealmEntity
import com.example.on_track_app.data.realm.entities.update
import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.data.synchronization.ReferenceIntegrityManager
import com.example.on_track_app.data.synchronization.UserDTO
import com.example.on_track_app.model.MockUser
import com.example.on_track_app.utils.DebugLogcatLogger
import io.realm.kotlin.Realm
import io.realm.kotlin.types.RealmInstant
import kotlin.reflect.KClass

class RealmUserRepository(
    db: Realm,
    mapper: SyncMapper<UserRealmEntity, UserDTO, MockUser>,
    maker: () -> UserRealmEntity,
    klass: KClass<UserRealmEntity> = UserRealmEntity::class,
    dtoClass: KClass<UserDTO> = UserDTO::class,
    integrityManager: ReferenceIntegrityManager
) : UserRepository, RealmSynchronizableRepository<UserRealmEntity, UserDTO, MockUser>(db,mapper,maker,klass,dtoClass,integrityManager) {

    override suspend fun addUser(
        username: String,
        email: String,
        cloudId: String?
    ): String {
        var id = ""

        db.write {
            val entity = UserRealmEntity().apply {
                this.name = username
                this.email = email
                this.cloudId = cloudId

                this.synchronizationStatus = SynchronizationState.CREATED.name
                this.version = RealmInstant.now()
            }
            val saved = copyToRealm(entity)
            id = saved.id.toHexString()
            DebugLogcatLogger.logRealmSaved(saved)
        }

        syncEngine?.onLocalChange(id, transferClass)

        return id

    }

    override suspend fun updateUser(id: String, newEmail: String) {

        db.write {
            val user: UserRealmEntity? = entity(id)

            user?.let { it.email = newEmail; it.update() }
        }

        syncEngine?.onLocalChange(id,transferClass)
    }

    override fun canSync(id: String): Boolean {
        db.entity(id) ?: return false
        return true
    }

}




