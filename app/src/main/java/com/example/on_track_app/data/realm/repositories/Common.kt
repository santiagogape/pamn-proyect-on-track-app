package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.synchronization.SynchronizableDTO
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.types.TypedRealmObject
import org.mongodb.kbson.ObjectId

inline fun <reified T : TypedRealmObject> MutableRealm.entity(id: String): T? {
    return query(T::class, "id == $0", ObjectId(id))
        .first()
        .find()
}

inline fun <reified T : TypedRealmObject> MutableRealm.entityByCloudId(id: String): T? {
    return query(T::class, "cloudId == $0", ObjectId(id))
        .first()
        .find()
}



interface SynchronizableRepository<D : SynchronizableDTO> {

    suspend fun applyLocalChange(dto: D)

    suspend fun applyRemoteInsert(dto: D)

    suspend fun applyRemoteUpdate(dto: D)

    suspend fun applyRemoteDelete(dto: D)
}



