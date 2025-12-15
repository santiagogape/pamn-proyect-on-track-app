package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.Initializable
import com.example.on_track_app.data.abstractions.repositories.UniqueRepository
import com.example.on_track_app.data.realm.entities.LocalConfig
import com.example.on_track_app.data.realm.entities.UserRealmEntity
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.model.LocalConfigurations
import com.example.on_track_app.model.MockUser
import io.realm.kotlin.Realm
import kotlin.reflect.KClass

class LocalConfigRepository(private val db: Realm) : UniqueRepository<LocalConfigurations>, Initializable, RealmRepository<LocalConfig>() {
    override val localClass: KClass<LocalConfig> = LocalConfig::class
    private var user: MockUser? = null

    override suspend fun init(remote: MockUser) {
        if (db.config() == null) {
            db.write {
                val user = UserRealmEntity().apply {
                    name = remote.name
                    email = remote.email
                    cloudId = remote.cloudId
                }
                val managedUser = copyToRealm(user)
                this@LocalConfigRepository.user = managedUser.toDomain()

                val config = LocalConfig().apply {
                    this.user = managedUser
                }

                copyToRealm(config)
            }

        } else {
            user = db.config()!!.user!!.toDomain()
        }
    }

    override fun get(): LocalConfigurations {
        return LocalConfigurations(user!!.id)
    }

    override fun ready(): Boolean {
        return user != null
    }
}