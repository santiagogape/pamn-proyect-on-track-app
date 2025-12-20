package com.example.on_track_app.data.synchronization

import com.example.on_track_app.data.abstractions.repositories.SyncRepository
import com.example.on_track_app.data.realm.repositories.SynchronizableRepository
import com.example.on_track_app.utils.DebugLogcatLogger
import com.example.on_track_app.utils.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

/*
data class SyncRepositoryEntry<D : SynchronizableDTO>(
    val dtoClass: KClass<D>,
    val local: SynchronizableRepository<D>,
    val remote: SyncRepository<D>
)

class PendingSyncQueue {
    private val pending: MutableMap<KClass<out SynchronizableDTO>, MutableSet<String>> =
        mutableMapOf()

    fun add(clazz: KClass<out SynchronizableDTO>, id: String) {
        pending.getOrPut(clazz) { mutableSetOf() }.add(id)
    }

    fun remove(clazz: KClass<out SynchronizableDTO>, id: String) {
        pending[clazz]?.remove(id)
    }

    fun contains(clazz: KClass<out SynchronizableDTO>, id: String) =
        pending[clazz]?.contains(id) ?: false


    fun has(clazz: KClass<out SynchronizableDTO>): Boolean =
        pending[clazz]?.isNotEmpty() == true

    fun idsFor(clazz: KClass<out SynchronizableDTO>): Set<String> =
        pending[clazz].orEmpty()
}

interface ConnectivityProvider {
    val isOnline: StateFlow<Boolean>
}



//todo after recovering connection
//todo sign out -> delete local -> warn if anything isn't synchronized
//todo ensure order for sync
class SyncRepositoryFactory(
    entries: List<SyncRepositoryEntry<out SynchronizableDTO>>,
    val order: List<KClass<out SynchronizableDTO>> = listOf(UserDTO::class,GroupDTO::class,ProjectDTO::class,
        MembershipDTO::class,
        TaskDTO::class,EventDTO::class,ReminderDTO::class
    )
) {
    private val registry: Map<KClass<out SynchronizableDTO>, SyncRepositoryEntry<out SynchronizableDTO>> =
        entries.associateBy { it.dtoClass }

    fun allEntries(): Collection<SyncRepositoryEntry<out SynchronizableDTO>> {
        DebugLogcatLogger.log(registry.values.map { it.dtoClass.simpleName }.toString())
        return registry.values
    }

    fun orderedEntries(): List<SyncRepositoryEntry<out SynchronizableDTO>> =
        order.mapNotNull { clazz -> registry[clazz] }



    @Suppress("UNCHECKED_CAST")
    fun <D : SynchronizableDTO> getForClass(clazz: KClass<D>): SyncRepositoryEntry<D>? {
        return registry[clazz] as? SyncRepositoryEntry<D>
    }
}

data class SyncState(
    var lastSuccessfulPush: Long,
    var lastSuccessfulPull: Long
)
@Suppress("UNCHECKED_CAST")
class SyncEngine(
    private val factory: SyncRepositoryFactory,
    private val scope: CoroutineScope,
    private val connectivity: ConnectivityProvider,
    private val settings: SettingsDataStore
) {

    private val pendingQueue = PendingSyncQueue()
    private lateinit var syncState: SyncState

    // ----------------------------------------------------
    // INITIALIZATION
    // ----------------------------------------------------

    suspend fun loadState() {
        syncState = settings.syncStateFlow.first()
    }

    fun start() {

        // --- Remote listeners (hot sync) ---
        factory.allEntries().forEach { rawEntry ->
            scope.launch {
                DebugLogcatLogger.log("Starting sync for ${rawEntry.dtoClass.simpleName}")
                rawEntry.local.attachToEngine(this@SyncEngine)

                rawEntry.remote.observeRemoteChanges()
                    .collect { dto ->
                        DebugLogcatLogger.log("Received remote change for ${rawEntry.dtoClass.simpleName}")
                        onRemoteChange(dto)
                    }
            }
        }

        // --- Connectivity observer ---
        scope.launch {
            connectivity.isOnline.collect { online ->
                if (online) {
                    DebugLogcatLogger.log("🟢 Connectivity restored")
                    coldSyncAndFlush()
                }
            }
        }
    }

    // ----------------------------------------------------
    // COLD SYNC
    // ----------------------------------------------------

    suspend fun coldSyncAndFlush() {
        if (!connectivity.isOnline.value) return

        flushPending()

        coldSyncIfOnline()

        flushLocalChanges()
    }

    private suspend fun coldSyncIfOnline() {
        var maxPulledVersion = syncState.lastSuccessfulPull

        for (clazz in factory.order) {
            val entry = factory.getForClass(clazz) ?: continue

            val remoteChanges =
                entry.remote.getUpdatedAfter(syncState.lastSuccessfulPull)

            for (dto in remoteChanges) {
                onRemoteChange(dto)
                maxPulledVersion = maxOf(maxPulledVersion, dto.version)
            }
        }

        syncState = syncState.copy(lastSuccessfulPull = maxPulledVersion)
        settings.updateLastPull(maxPulledVersion)
    }

    // ----------------------------------------------------
    // LOCAL FLUSH
    // ----------------------------------------------------

    private suspend fun flushLocalChanges() {
        val last = syncState.lastSuccessfulPush

        for (entry in factory.orderedEntries()) {
            flushEntry(entry, last)
        }

        val now = now().toEpochMilli()
        syncState = syncState.copy(lastSuccessfulPush = now)
        settings.updateLastPush(now)
    }

    private suspend fun <D : SynchronizableDTO> flushEntry(
        entry: SyncRepositoryEntry<D>,
        last: Long
    ) {
        val changed = entry.local.getDTOUpdatedAfter(last)

        for (dto in changed.sortedBy { it.version }) {
            when {
                dto.deleted -> {
                    entry.remote.delete(dto)
                }

                dto.cloudId == null -> {
                    val cloudId = entry.remote.generateCloudId()
                    val id = entry.local.getId(dto)
                    val updated = entry.local.applyCloudId(id, cloudId)
                    entry.remote.push(cloudId, updated)
                }

                else -> {
                    entry.remote.push(dto.cloudId!!, dto)
                }
            }
        }
    }

    // ----------------------------------------------------
    // PENDING QUEUE
    // ----------------------------------------------------

    private suspend fun flushPending() {
        for (clazz in factory.order) {
            val ids = pendingQueue.idsFor(clazz)
            for (id in ids) {
                onLocalChange(id, clazz)
                pendingQueue.remove(clazz, id)
            }
        }
    }

    // ----------------------------------------------------
    // LOCAL CHANGE
    // ----------------------------------------------------

    suspend fun <D : SynchronizableDTO> onLocalChange(
        id: String,
        clazz: KClass<D>
    ) {
        val entry = factory.getForClass(clazz) ?: return
        val dto: D = entry.local.getDTO(id) ?: return

        if (!connectivity.isOnline.value) {
            if (dto.deleted && pendingQueue.contains(clazz, id))
                pendingQueue.remove(clazz, id)
            else
                pendingQueue.add(clazz, id)
            return
        }

        when {
            dto.deleted -> {
                entry.remote.delete(dto)
            }

            dto.cloudId == null -> {
                val cloudId = entry.remote.generateCloudId()
                val updated = entry.local.applyCloudId(id, cloudId)
                entry.remote.push(cloudId, updated)
            }

            else -> {
                entry.remote.push(dto.cloudId!!, dto)
            }
        }

        pendingQueue.remove(clazz, id)
    }

    // ----------------------------------------------------
    // REMOTE CHANGE
    // ----------------------------------------------------

    suspend fun <D : SynchronizableDTO> onRemoteChange(dto: D) {
        val entry =
            factory.getForClass(dto::class) as? SyncRepositoryEntry<D> ?: return

        if (dto.deleted) {
            entry.local.applyRemoteDelete(dto)
        } else {
            entry.local.applyRemoteUpdate(dto)
        }
    }
}


 */

// -----------------------------
// Core contracts (as you already have)
// -----------------------------
// interface SynchronizableDTO { val cloudId: String?; val version: Long; val deleted: Boolean; fun copyDTO(cloudId: String? = null): SynchronizableDTO }
// interface SyncRepository<D: SynchronizableDTO> { fun observeRemoteChanges(): Flow<D>; suspend fun push(cloudId:String, dto: D); fun generateCloudId(): String; suspend fun delete(dto: D); suspend fun getUpdatedAfter(version: Long): List<D>; suspend fun getAll(): List<D> }
// interface SynchronizableRepository<D: SynchronizableDTO> { ... attachToEngine(engine: SyncEngine) ... getDTO(id:String): D? ... getDTOUpdatedAfter(version:Long): List<D> ... applyCloudId(id:String, cloudId:String): D ... getId(dto:D): String ... applyRemoteUpdate(dto:D) ... applyRemoteDelete(dto:D) ... }

// =====================================================
// SyncRepositoryEntry (generic, but we’ll allow building it heterogeneously)
// =====================================================

data class SyncRepositoryEntry<D : SynchronizableDTO>(
    val dtoClass: KClass<D>,
    val local: SynchronizableRepository<D>,
    val remote: SyncRepository<D>
)

// =====================================================
// Pending queue (unchanged semantics)
// =====================================================

class PendingSyncQueue {
    private val pending: MutableMap<KClass<out SynchronizableDTO>, MutableSet<String>> = mutableMapOf()

    fun add(clazz: KClass<out SynchronizableDTO>, id: String) {
        pending.getOrPut(clazz) { mutableSetOf() }.add(id)
    }

    fun remove(clazz: KClass<out SynchronizableDTO>, id: String) {
        pending[clazz]?.remove(id)
    }

    fun contains(clazz: KClass<out SynchronizableDTO>, id: String): Boolean =
        pending[clazz]?.contains(id) ?: false

    fun has(clazz: KClass<out SynchronizableDTO>): Boolean =
        pending[clazz]?.isNotEmpty() == true

    fun idsFor(clazz: KClass<out SynchronizableDTO>): Set<String> =
        pending[clazz].orEmpty()
}

// =====================================================
// Connectivity
// =====================================================

interface ConnectivityProvider {
    val isOnline: StateFlow<Boolean>
}

// =====================================================
// Factory (stores heterogeneous entries; retrieval is typed via cast)
// =====================================================

class SyncRepositoryFactory(
    entries: List<SyncRepositoryEntry<out SynchronizableDTO>>,
    val order: List<KClass<out SynchronizableDTO>> = listOf(
        UserDTO::class,
        GroupDTO::class,
        ProjectDTO::class,
        MembershipDTO::class,
        TaskDTO::class,
        EventDTO::class,
        ReminderDTO::class
    )
) {
    private val registry: Map<KClass<out SynchronizableDTO>, SyncRepositoryEntry<out SynchronizableDTO>> =
        entries.associateBy { it.dtoClass }

    fun allEntries(): Collection<SyncRepositoryEntry<out SynchronizableDTO>> {
        DebugLogcatLogger.log(registry.values.map { it.dtoClass.simpleName }.toString())
        return registry.values
    }

    fun orderedEntries(): List<SyncRepositoryEntry<out SynchronizableDTO>> =
        order.mapNotNull { clazz -> registry[clazz] }

    @Suppress("UNCHECKED_CAST")
    fun <D : SynchronizableDTO> getForClass(clazz: KClass<D>): SyncRepositoryEntry<D>? {
        return registry[clazz] as? SyncRepositoryEntry<D>
    }

    // "Builder-friendly" access: no generic required at callsite.
    // You can hand it any KClass<out SynchronizableDTO> and we return the stored entry.
    fun getAny(clazz: KClass<out SynchronizableDTO>): SyncRepositoryEntry<out SynchronizableDTO>? = registry[clazz]
}

// =====================================================
// SyncState
// =====================================================

data class SyncState(
    var lastSuccessfulPush: Long,
    var lastSuccessfulPull: Long
)

// =====================================================
// SyncEngine
//
// Key change for “not restrictive with the builder”:
// - Engine works over heterogeneous entries and does a SINGLE unsafe cast at the edge,
//   then uses only the SynchronizableDTO contract.
// - This avoids the “CapturedType(*)#1 vs #2” problems when you build entries from a catalog/binding list.
// =====================================================

class SyncEngine(
    private val factory: SyncRepositoryFactory,
    private val scope: CoroutineScope,
    private val connectivity: ConnectivityProvider,
    private val settings: SettingsDataStore
) {

    private val pendingQueue = PendingSyncQueue()
    private lateinit var syncState: SyncState

    // ---- Helpers to unify types at the edge (builder-friendly) ----

    @Suppress("UNCHECKED_CAST")
    private fun anyEntryToBase(
        entry: SyncRepositoryEntry<out SynchronizableDTO>
    ): SyncRepositoryEntry<SynchronizableDTO> = entry as SyncRepositoryEntry<SynchronizableDTO>

    @Suppress("UNCHECKED_CAST")
    private fun dtoKClassToBase(
        clazz: KClass<out SynchronizableDTO>
    ): KClass<SynchronizableDTO> = clazz as KClass<SynchronizableDTO>

    // ----------------------------------------------------
    // INITIALIZATION
    // ----------------------------------------------------

    suspend fun loadState() {
        syncState = settings.syncStateFlow.first()
    }

    fun propagateId(id:String){
        factory.allEntries().forEach({it.remote.setUserId(id)})
    }

    fun start() {

        // --- Remote listeners (hot sync) ---
        factory.allEntries().forEach { rawEntry ->
            scope.launch {
                DebugLogcatLogger.log("Starting sync for ${rawEntry.dtoClass.simpleName}")

                // attach engine (no generic needed here)
                rawEntry.local.attachToEngine(this@SyncEngine)

                // collect remote changes
                rawEntry.remote.observeRemoteChanges()
                    .collect { dto ->
                        DebugLogcatLogger.log("Received remote change for ${rawEntry.dtoClass.simpleName}")
                        onRemoteChangeAny(dto)
                    }
            }
        }

        // --- Connectivity observer ---
        scope.launch {
            connectivity.isOnline.collect { online ->
                if (online) {
                    DebugLogcatLogger.log("🟢 Connectivity restored")
                    coldSyncAndFlush()
                }
            }
        }
    }

    // ----------------------------------------------------
    // COLD SYNC
    // ----------------------------------------------------

    suspend fun coldSyncAndFlush() {
        if (!connectivity.isOnline.value) return

        flushPending()

        coldSyncIfOnline()

        flushLocalChanges()
    }

    private suspend fun coldSyncIfOnline() {
        var maxPulledVersion = syncState.lastSuccessfulPull

        for (clazzAny in factory.order) {
            val entryAny = factory.getAny(clazzAny) ?: continue
            val entry = anyEntryToBase(entryAny)

            val remoteChanges = entry.remote.getUpdatedAfter(syncState.lastSuccessfulPull)

            for (dto in remoteChanges) {
                onRemoteChangeAny(dto)
                maxPulledVersion = maxOf(maxPulledVersion, dto.version)
            }
        }

        syncState = syncState.copy(lastSuccessfulPull = maxPulledVersion)
        settings.updateLastPull(maxPulledVersion)
    }

    // ----------------------------------------------------
    // LOCAL FLUSH
    // ----------------------------------------------------

    private suspend fun flushLocalChanges() {
        val last = syncState.lastSuccessfulPush

        for (rawEntry in factory.orderedEntries()) {
            val entry = anyEntryToBase(rawEntry)
            flushEntry(entry, last)
        }

        val now = java.time.Instant.now().toEpochMilli()
        syncState = syncState.copy(lastSuccessfulPush = now)
        settings.updateLastPush(now)
    }

    private suspend fun flushEntry(
        entry: SyncRepositoryEntry<SynchronizableDTO>,
        last: Long
    ) {
        val changed = entry.local.getDTOUpdatedAfter(last)

        for (dto in changed.sortedBy { it.version }) {
            when {
                dto.deleted -> {
                    entry.remote.delete(dto)
                }

                dto.cloudId?.isBlank() ?:true -> {
                    val cloudId = entry.remote.generateCloudId()
                    val id = entry.local.getId(dto)
                    val updated = entry.local.applyCloudId(id, cloudId)
                    entry.remote.push(cloudId, updated)
                }

                else -> {
                    entry.remote.push(dto.cloudId!!, dto)
                }
            }
        }
    }

    // ----------------------------------------------------
    // PENDING QUEUE
    // ----------------------------------------------------

    private suspend fun flushPending() {
        for (clazzAny in factory.order) {
            val ids = pendingQueue.idsFor(clazzAny)
            for (id in ids) {
                onLocalChangeAny(id, clazzAny)
                pendingQueue.remove(clazzAny, id)
            }
        }
    }

    // ----------------------------------------------------
    // LOCAL CHANGE (builder-friendly signature)
    // ----------------------------------------------------

    suspend fun onLocalChangeAny(
        id: String,
        clazzAny: KClass<out SynchronizableDTO>
    ) {
        val entryAny = factory.getAny(clazzAny) ?: return
        val entry = anyEntryToBase(entryAny)

        val dto = entry.local.getDTO(id) ?: return
        DebugLogcatLogger.log("got $dto by $id")

        if (!connectivity.isOnline.value) {
            if (dto.deleted && pendingQueue.contains(clazzAny, id)) {
                pendingQueue.remove(clazzAny, id)
            } else {
                pendingQueue.add(clazzAny, id)
            }
            return
        }

        when {
            dto.deleted -> {
                entry.remote.delete(dto)
            }

            dto.cloudId?.isBlank() ?:true  -> {
                val cloudId = entry.remote.generateCloudId()
                DebugLogcatLogger.log("got $cloudId for $dto by $id")
                val updated = entry.local.applyCloudId(id, cloudId)
                DebugLogcatLogger.log("updated $updated by $id")
                entry.remote.push(cloudId, updated)
            }

            else -> {
                entry.remote.push(dto.cloudId!!, dto)
            }
        }

        pendingQueue.remove(clazzAny, id)
    }

    // Keep your old typed API if you still want it from repositories:
    suspend fun <D : SynchronizableDTO> onLocalChange(
        id: String,
        clazz: KClass<D>
    ) = onLocalChangeAny(id, clazz)

    // ----------------------------------------------------
    // REMOTE CHANGE (builder-friendly signature)
    // ----------------------------------------------------

    suspend fun onRemoteChangeAny(dto: SynchronizableDTO) {
        val clazzAny = dto::class
        val entryAny = factory.getAny(clazzAny) ?: return
        val entry = anyEntryToBase(entryAny)

        if (dto.deleted) {
            entry.local.applyRemoteDelete(dto)
        } else {
            entry.local.applyRemoteUpdate(dto)
        }
    }

    // Keep your old typed API if you still want it:
    suspend fun <D : SynchronizableDTO> onRemoteChange(dto: D) = onRemoteChangeAny(dto)

}
