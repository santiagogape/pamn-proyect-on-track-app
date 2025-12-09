package com.example.on_track_app.data.synchronization

import com.example.on_track_app.data.firebase.FirestoreSyncRepository
import com.example.on_track_app.data.realm.repositories.SynchronizableRepository
import com.example.on_track_app.utils.DebugLogcatLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

data class SyncRepositoryEntry<D : SynchronizableDTO>(
    val dtoClass: KClass<D>,
    val local: SynchronizableRepository<D>,
    val remote: FirestoreSyncRepository<D>
)


/**
 * use:
 * val projectEntry = SyncRepositoryEntry(
 *     dtoClass = ProjectDTO::class,
 *     local = realmProjectRepository,              // implements SynchronizableRepository<ProjectDTO>
 *     remote = FirestoreSyncRepository(
 *         collection = FirestoreService.firestore.collection("projects"),
 *         clazz = ProjectDTO::class.java
 *     )
 * )
 *
 * val factory = SyncRepositoryFactory(
 *     entries = listOf(projectEntry /*, taskEntry, eventEntry, etc.*/)
 * )
 *
 */
class SyncRepositoryFactory(
    entries: List<SyncRepositoryEntry<out SynchronizableDTO>>
) {
    // KClass<DTO> -> entry correspondiente
    private val registry: Map<KClass<out SynchronizableDTO>, SyncRepositoryEntry<out SynchronizableDTO>> =
        entries.associateBy { it.dtoClass }

    fun allEntries(): Collection<SyncRepositoryEntry<out SynchronizableDTO>> =
        registry.values

    @Suppress("UNCHECKED_CAST")
    fun <D : SynchronizableDTO> getForClass(clazz: KClass<D>): SyncRepositoryEntry<D>? {
        return registry[clazz] as? SyncRepositoryEntry<D>
    }
}

@Suppress("UNCHECKED_CAST")
class SyncEngine(
    private val factory: SyncRepositoryFactory,
    private val scope: CoroutineScope
) {

    fun start() {
        factory.allEntries().forEach { rawEntry ->
            scope.launch {
                rawEntry.local.attachToEngine(this@SyncEngine) // -> this in OnTrackApp: Application
                rawEntry.remote.observeRemoteChanges()
                    .collect { dto ->
                        onRemoteChange(dto)
                    }
            }
        }
    }

    suspend fun <D : SynchronizableDTO> onLocalChange(id:String, dto: D) {
        val entry = factory.getForClass(dto::class) as? SyncRepositoryEntry<D> ?: return

        if (dto.deleted) {
            entry.remote.delete(dto)
        //todo -> algorithm for elimination and synchronization between shared objects

        } else if (dto.cloudId == null){
            val generateCloudId = entry.remote.generateCloudId()
            val updated = entry.local.applyCloudId( id,dto.copyDTO(generateCloudId) as D)
            (updated as? ProjectDTO)?.let { DebugLogcatLogger.logDTOToRemote(it) }
            entry.remote.push(generateCloudId, updated)
        } else {
            dto.cloudId?.let { entry.remote.push(it, dto)}

        }


    }

    suspend fun <D : SynchronizableDTO> onRemoteChange(dto: D) {
        val entry = factory.getForClass(dto::class) as? SyncRepositoryEntry<D> ?: return

        if (dto.deleted) {
            entry.local.applyRemoteDelete(dto)
        } else {
            entry.local.applyRemoteUpdate(dto)
        }
    }
}
