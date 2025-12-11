package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.BasicById
import com.example.on_track_app.data.realm.entities.Entity
import com.example.on_track_app.data.realm.entities.LocalConfig
import com.example.on_track_app.data.realm.entities.SyncMapper
import com.example.on_track_app.data.realm.entities.SynchronizableEntity
import com.example.on_track_app.data.realm.entities.delete
import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.data.synchronization.SyncEngine
import com.example.on_track_app.data.synchronization.SynchronizableDTO
import com.example.on_track_app.model.Identifiable
import com.example.on_track_app.utils.DebugLogcatLogger
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.TypedRealmObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId
import kotlin.reflect.KClass

abstract class RealmRepository<K> where K : TypedRealmObject, K : Entity {

    abstract val klass: KClass<K>

    protected fun Realm.config(): LocalConfig? = query(LocalConfig::class,"name == $0", "LOCAL_CONFIG").first().find()

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
}


interface SynchronizableRepositoryElimination<D : SynchronizableDTO> {
    suspend fun applyRemoteDelete(dto: D)
}




interface SynchronizableRepository<D : SynchronizableDTO>: SynchronizableRepositoryElimination<D> {

    suspend fun applyRemoteInsert(dto: D)

    suspend fun applyRemoteUpdate(dto: D)


    suspend fun applyCloudId(id:String,dto: D): D

    fun attachToEngine(engine: SyncEngine)
}

open class RealmSynchronizableRepository<
        RE,
        DTO,
        DOM
        >(
    protected val db: Realm,
    protected val mapper: SyncMapper<RE, DTO, DOM>,
    protected val maker: ()->RE,
    override val klass: KClass<RE>
) : RealmRepository<RE>(),
    BasicById<DOM>,
    SynchronizableRepository<DTO> where RE: RealmObject, RE : SynchronizableEntity, RE: Entity, RE: TypedRealmObject, DOM : Identifiable,
                                        DTO : SynchronizableDTO {

    protected var syncEngine: SyncEngine? = null


    // ---------------------------
    // BASIC CRUD (GENERIC)
    // ---------------------------

    override fun getAll(): Flow<List<DOM>> =
        db.query(klass)
            .asFlow()
            .map { result -> result.list.map { mapper.toDomain(it)  } }

    override fun getById(id: String): DOM? =
        db.entity(id)?.let { mapper.toDomain(it) }

    override suspend fun markAsDeleted(id: String) {
        var dto: DTO? = null

        db.write {
            val local = entity(id) ?: return@write
            local.delete()
            dto = mapper.toDTO(local)
        }
        dto?.let { syncEngine?.onLocalChange(id, it) }
    }

    override suspend fun delete(id: String) {
        db.write {
            val local:RE? = entity(id)
            local?.let { delete(findLatest(it)!!) }
        }
    }

    // ---------------------------
    // REMOTE → LOCAL
    // ---------------------------

    override suspend fun applyRemoteInsert(dto: DTO) {
        db.write {
            val entity = maker().apply { mapper.toLocal(dto, this) }
            DebugLogcatLogger.logDTOFromRemote(DebugLogcatLogger.EventType.INSERT,dto)
            DebugLogcatLogger.logRealmSaved(entity)
            copyToRealm(entity)
        }
    }

    override suspend fun applyRemoteUpdate(dto: DTO) {
        val local = db.entityByCloudId(dto.cloudId!!)
        if (local == null) {
            applyRemoteInsert(dto)
            return
        }

        db.write {
            val latest = findLatest(local) ?: return@write
            mapper.toLocal(dto, latest)
            latest.synchronizationStatus = SynchronizationState.CURRENT.name
            DebugLogcatLogger.logDTOFromRemote(DebugLogcatLogger.EventType.UPDATE,dto)
            DebugLogcatLogger.logRealmSaved(latest)
        }
    }

    override suspend fun applyRemoteDelete(dto: DTO) {
        db.write {
            db.entityByCloudId(dto.cloudId!!)?.let {
                delete(findLatest(it)!!)
            }
        }
    }

    // ---------------------------
    // CLOUD ID ASSIGNATION
    // ---------------------------

    override suspend fun applyCloudId(id: String, dto: DTO): DTO {
        lateinit var result: DTO

        db.write {
            val local = entity(id) ?: return@write
            local.cloudId = dto.cloudId
            local.synchronizationStatus = SynchronizationState.CURRENT.name
            result = mapper.toDTO(local)
            DebugLogcatLogger.logRealmSaved(local)
        }
        return result
    }

    override fun attachToEngine(engine: SyncEngine) {
        this.syncEngine = engine
    }
}


