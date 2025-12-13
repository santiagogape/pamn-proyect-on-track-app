package com.example.on_track_app.data.synchronization

import com.example.on_track_app.data.realm.repositories.Filter
import kotlin.reflect.KClass


data class ReferenceIntegrityManagerEntry<D : SynchronizableDTO>(
    val dtoClass: KClass<D>,
    val propagate: suspend (String, String) -> Unit,
    val resolve: Map<Filter,(String)->String?>
)

class ReferenceIntegrityManager(
    private val mapped: MutableMap<KClass<out SynchronizableDTO>, ReferenceIntegrityManagerEntry<out SynchronizableDTO>>
) {
    suspend fun propagateOnCloudIdAssigned(
        dtoClass: KClass<out SynchronizableDTO>,
        localId: String,
        cloudId: String
    ) {
        mapped[dtoClass]?.propagate(localId, cloudId)
    }

    fun resolveReferenceOnCreate(
        dtoClass: KClass<out SynchronizableDTO>,
        id: String, filter:Filter): String? {
        return mapped[dtoClass]?.resolve[filter]?.invoke(id)
    }

    fun addClassAndEntry(dtoClass: KClass<out SynchronizableDTO>, entry: ReferenceIntegrityManagerEntry<out SynchronizableDTO>) {
        mapped[dtoClass] = entry
    }
}
