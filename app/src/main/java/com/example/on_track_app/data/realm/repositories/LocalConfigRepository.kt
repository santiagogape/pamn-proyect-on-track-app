package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.Initializable
import com.example.on_track_app.data.abstractions.repositories.UniqueRepository
import com.example.on_track_app.data.realm.entities.LocalConfig
import com.example.on_track_app.data.realm.entities.SynchronizationEntity
import com.example.on_track_app.data.realm.entities.UserRealmEntity
import com.example.on_track_app.data.realm.entities.toDTO
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.data.synchronization.UserDTO
import com.example.on_track_app.data.synchronization.toDTO
import com.example.on_track_app.model.LocalConfigurations
import com.example.on_track_app.model.User
import io.realm.kotlin.Realm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.reflect.KClass

class LocalConfigRepository(override val db: Realm) :
    UniqueRepository<LocalConfigurations>,
    Initializable,
    RealmRepository<LocalConfig> {
    override val localClass: KClass<LocalConfig> = LocalConfig::class
    private val _config = MutableStateFlow<LocalConfigurations?>(null)
    override val config = _config.asStateFlow()

    private fun config(): LocalConfig?{
        return db.query(localClass).first().find()
    }

    override suspend fun init(remote: User): UserDTO {
        var dto = UserDTO()
        if (config() == null) {
            db.write {
                val managedUser = copyToRealm(
                    UserRealmEntity().apply {
                        identity = SynchronizationEntity()
                        name = remote.name
                        email = remote.email
                        identity!!.cloudId = remote.cloudId
                    }
                )
                _config.value = LocalConfigurations(managedUser.toDomain())


                copyToRealm(
                    LocalConfig().apply { user = managedUser }
                )

                dto = managedUser.toDTO()
            }
        } else {
            val conf = config()!!
            _config.value = conf.toDomain()
            dto = conf.toDTO()
        }
        return dto
    }

    override fun ready(): Boolean = _config.value != null

    override fun get(): LocalConfigurations =
        _config.value ?: error("LocalConfig not initialized yet")

}