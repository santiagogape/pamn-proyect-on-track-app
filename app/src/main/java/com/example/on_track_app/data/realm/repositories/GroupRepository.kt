package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.GroupRepository
import com.example.on_track_app.data.realm.entities.GroupRealmEntity
import com.example.on_track_app.data.realm.entities.SyncMapper
import com.example.on_track_app.data.realm.entities.update
import com.example.on_track_app.data.realm.utils.toRealmList
import com.example.on_track_app.data.synchronization.GroupDTO
import com.example.on_track_app.model.MockGroup
import io.realm.kotlin.Realm
import kotlin.reflect.KClass

class RealmGroupRepository (
    db: Realm,
    mapper: SyncMapper<GroupRealmEntity, GroupDTO , MockGroup>,
    maker: () -> GroupRealmEntity,
    klass: KClass<GroupRealmEntity> = GroupRealmEntity::class
) : GroupRepository , RealmSynchronizableRepository<GroupRealmEntity,GroupDTO , MockGroup>(db,mapper,maker,klass) {

    override suspend fun addGroup(
        name: String,
        membersId: List<String>,
        defaultProjectId: String,
        projectsId: List<String>,
        cloudId: String?
    ): String {
        var dto: GroupDTO? = null
        var newId = ""

        db.write {
            val entity = GroupRealmEntity().apply {
                this.name = name
                this.members = membersId.toRealmList()
                this.defaultProjectId = defaultProjectId
                this.projectsId = projectsId.toRealmList()
                this.cloudId = cloudId
            }

            val saved = copyToRealm(entity)
            dto = mapper.toDTO(saved)
            newId = saved.id.toHexString()
        }
        dto?.let { syncEngine?.onLocalChange(newId, it) }

        return newId
    }

    override suspend fun updateGroup(id: String, newName: String) {
        var dto: GroupDTO? = null
        db.write {
            val group:GroupRealmEntity? = entity(id)
            group?.let { it.name = newName; it.update() }
            dto = group?.let{mapper.toDTO(it)}
        }
        dto?.let { syncEngine?.onLocalChange(id,it) }

    }
}
