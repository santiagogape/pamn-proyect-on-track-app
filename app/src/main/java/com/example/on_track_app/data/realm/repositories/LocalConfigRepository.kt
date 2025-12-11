package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.Initializable
import com.example.on_track_app.data.abstractions.repositories.UniqueRepository
import com.example.on_track_app.data.realm.entities.LocalConfig
import com.example.on_track_app.data.realm.entities.ProjectRealmEntity
import com.example.on_track_app.data.realm.entities.UserRealmEntity
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.model.LocalConfigurations
import io.realm.kotlin.Realm
import kotlin.reflect.KClass

class LocalConfigRepository(private val db: Realm) : UniqueRepository<LocalConfigurations>, Initializable, RealmRepository<LocalConfig>() {
    override val klass: KClass<LocalConfig> = LocalConfig::class

    override suspend fun init() {
        if (db.config() == null) {
            db.write {
                val user = UserRealmEntity().apply {
                    username = "LOCAL"
                    email = "LOCAL"
                }

                val project = ProjectRealmEntity().apply {
                    name = "DEFAULT"
                }

                val managedUser = copyToRealm(user)
                val managedProject = copyToRealm(project)

                managedUser.defaultProjectId = managedProject.id
                managedProject.ownerId = managedUser.id

                val config = LocalConfig().apply {
                    this.user = managedUser
                    this.default = managedProject
                }

                copyToRealm(config)
            }

        }
    }

    override fun get(): LocalConfigurations? {
        return db.config()?.toDomain()
    }
}