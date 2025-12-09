package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.synchronization.SyncEngine
import com.example.on_track_app.data.synchronization.SynchronizableDTO
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.types.TypedRealmObject
import org.mongodb.kbson.ObjectId
import kotlin.reflect.KClass

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

abstract class RealmRepository<K : TypedRealmObject> {

    abstract val klass: KClass<K>

    protected fun Realm.entity(id:String): K? =
        query(klass, "id == $0", ObjectId(id))
            .first()
            .find()

    protected fun Realm.entityByCloudId(id:String): K? =
        query(klass, "cloudId == $0", id)
            .first()
            .find()

    protected fun MutableRealm.entity(id: String): K? {
        return query(klass, "id == $0", ObjectId(id))
            .first()
            .find()
    }

    protected fun MutableRealm.entityByCloudId(id: String): K? {
        return query(klass, "cloudId == $0", id)
            .first()
            .find()
    }
}



interface SynchronizableRepository<D : SynchronizableDTO> {

    suspend fun applyRemoteInsert(dto: D)

    suspend fun applyRemoteUpdate(dto: D)

    suspend fun applyRemoteDelete(dto: D)
    suspend fun applyCloudId(id:String,dto: D): D

    fun attachToEngine(engine: SyncEngine)
}



