package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.Initializable
import com.example.on_track_app.data.abstractions.repositories.UniqueRepository
import com.example.on_track_app.data.realm.entities.LocalConfig
import com.example.on_track_app.data.realm.entities.UserRealmEntity
import com.example.on_track_app.data.synchronization.UserDTO
import com.example.on_track_app.data.synchronization.toDTO
import com.example.on_track_app.model.LocalConfigurations
import com.example.on_track_app.model.MockUser
import io.realm.kotlin.Realm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.reflect.KClass

class LocalConfigRepository(private val db: Realm) : UniqueRepository<LocalConfigurations>, Initializable, RealmRepository<LocalConfig>() {
    override val localClass: KClass<LocalConfig> = LocalConfig::class
    private val _config = MutableStateFlow<LocalConfigurations?>(null)
    override val config = _config.asStateFlow()

    override suspend fun init(remote: MockUser): UserDTO {
        var dto = UserDTO()
        if (db.config() == null) {
            db.write {
                val managedUser = copyToRealm(
                    UserRealmEntity().apply {
                        name = remote.name
                        email = remote.email
                        cloudId = remote.cloudId
                    }
                )
                _config.value = LocalConfigurations(managedUser.id.toHexString())


                copyToRealm(
                    LocalConfig().apply { user = managedUser }
                )

                dto = managedUser.toDTO()
            }
        } else {
            val userConf = db.config()!!.user!!
            _config.value = LocalConfigurations(userConf.id.toHexString())
            dto = userConf.toDTO()
        }
        return dto
    }

    override fun ready(): Boolean = _config.value != null

    override fun get(): LocalConfigurations =
        _config.value ?: error("LocalConfig not initialized yet")

}