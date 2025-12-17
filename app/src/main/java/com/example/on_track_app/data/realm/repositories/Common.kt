package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.BasicById
import com.example.on_track_app.data.realm.entities.Entity
import com.example.on_track_app.data.realm.entities.LocalConfig
import com.example.on_track_app.data.realm.entities.SynchronizableEntity
import com.example.on_track_app.data.realm.entities.delete
import com.example.on_track_app.data.realm.repositories.decorated.SyncMapper
import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.data.synchronization.ReferenceIntegrityManager
import com.example.on_track_app.data.synchronization.SyncEngine
import com.example.on_track_app.data.synchronization.SynchronizableDTO
import com.example.on_track_app.model.Identifiable
import com.example.on_track_app.utils.DebugLogcatLogger
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.TypedRealmObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId
import kotlin.reflect.KClass

enum class Filter{
    ENTITY("id == $0"),
    OWNER("ownerId == $0"),
    PROJECT("projectId == $0"),
    LINK("linkedTo == $0"),
    LINKS("linkedTo IN $0"),
    TASK_DUE_IN("due >= $0 AND due <= $1"),
    GROUP_TASK_DUE_IN("ownerId == $0 AND due >= $1 AND due <= $2"),
    PROJECT_TASK_DUE_IN("projectId == $0 AND due >= $1 AND due <= $2"),
    EVENT_IN("start >= $0 AND end <= $1"),
    GROUP_EVENT_IN("ownerId == $0 AND start >= $1 AND end <= $2"),
    PROJECT_EVENT_IN("projectId == $0 AND start >= $1 AND end <= $2"),
    REMINDER_IN("at >= $0 AND at <= $1"),
    LINKED_REMINDER_IN("linkedTo IN $0 AND at >= $1 AND at <= $2"),
    MEMBERSHIP_ENTITY("entityId == $0 "),
    MEMBERSHIP_MEMBER("memberId == $0"),

    REMOTE("cloudId == $0"),
    REMOTE_OWNER("cloudOwnerId == $0"),
    REMOTE_PROJECT("cloudProjectId == $0"),
    REMOTE_LINK("cloudLinkedTo == $0"),
    REMOTE_MEMBERSHIP_ENTITY("cloudEntityId == $0"),
    REMOTE_MEMBERSHIP_MEMBER("cloudMemberId == $0");

    val query: String
    constructor(query:String){
        this.query = query
    }

}

abstract class RealmRepository<K> where K : TypedRealmObject, K : Entity {

    abstract val localClass: KClass<K>

    fun config(realm: Realm): LocalConfig? = realm.query(LocalConfig::class).first().find()

    fun entity(realm: Realm,id:String): K? =
        realm.query(localClass, Filter.ENTITY.query, ObjectId(id))
            .first()
            .find()
    fun entityByCloudId(realm: Realm,id:String): K? =
        realm.query(localClass, Filter.REMOTE.query, id)
            .first()
            .find()

    fun entityByCloudId(realm: MutableRealm,id:String): K? =
        realm.query(localClass, Filter.REMOTE.query, id)
            .first()
            .find()

    fun entity(realm: MutableRealm,id: String): K? {
        return realm.query(localClass, Filter.ENTITY.query, ObjectId(id))
            .first()
            .find()
    }

    fun filter(realm: MutableRealm,filter: Filter, id: String): RealmResults<K> {
        return realm.query(localClass, filter.query, ObjectId(id))
            .find()
    }
}


interface SynchronizableRepositoryElimination<D : SynchronizableDTO> {
    suspend fun applyRemoteDelete(dto: D)
}

interface SynchronizedReference {
    suspend fun synchronizeReferences(id: String, cloudId: String)
}


interface SynchronizableRepository<D : SynchronizableDTO>: SynchronizableRepositoryElimination<D> {

    suspend fun applyRemoteInsert(dto: D)

    suspend fun applyRemoteUpdate(dto: D)


    suspend fun applyCloudId(id:String,cloudId: String): D

    fun attachToEngine(engine: SyncEngine)

    fun canSync(id:String): Boolean
    suspend fun getDTO(id:String): D?
    fun getRemoteOf(id:String): String?

    suspend fun onLocalChange(id:String)

}

open class RealmSynchronizableRepository<
        RE,
        DTO,
        DOM
        >(
    val db: Realm,
    val mapper: SyncMapper<RE, DTO, DOM>,
    val maker: ()->RE,
    override val localClass: KClass<RE>,
    val transferClass: KClass<DTO>,
    val integrityManager: ReferenceIntegrityManager
) : RealmRepository<RE>(),
    BasicById<DOM>,
    SynchronizableRepository<DTO>
        where RE: RealmObject,
              RE : SynchronizableEntity,
              DOM : Identifiable,
              DTO : SynchronizableDTO {

    var syncEngine: SyncEngine? = null


    // ---------------------------
    // BASIC CRUD (GENERIC)
    // ---------------------------

    override fun getAll(): Flow<List<DOM>> =
        db.query(localClass)
            .asFlow()
            .map { result -> result.list.map { mapper.toDomain(it)  } }

    override fun getById(id: String): DOM? =
        entity(db,id)?.let { mapper.toDomain(it) }

    override fun liveById(id: String): Flow<DOM?> {
        return db.query(localClass, Filter.ENTITY.query, ObjectId(id)).first()
            .asFlow().map { it.obj?.let { obj -> mapper.toDomain(obj) } }
    }

    override suspend fun markAsDeleted(id: String) {

        db.write {
            val local = entity(this,id) ?: return@write
            local.delete()
        }
        syncEngine?.onLocalChange(id, transferClass)
    }

    override suspend fun delete(id: String) {
        db.write {
            val local:RE? = entity(this,id)
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
        val local = entityByCloudId(db,dto.cloudId!!)
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
            entityByCloudId(this,dto.cloudId!!)?.let {
                delete(findLatest(it)!!)
            }
        }
    }


    /**
     * @see canSync(id:String) at inheritors of
     * @see RealmSynchronizableRepository
     * @see SyncEngine.onLocalChange(id:String,clazz:KClass<D>) where D::SynchronizableDTO
     * @exception NullPointerException might be thrown if you don check with
     *      canSync(id) before calling this
     */
    override suspend fun applyCloudId(id: String, cloudId: String): DTO {
        lateinit var result: DTO

        db.write {
            val local = entity(this,id) ?: return@write
            local.cloudId = cloudId
            local.synchronizationStatus = SynchronizationState.CURRENT.name
            result = mapper.toDTO(local)
            DebugLogcatLogger.logRealmSaved(local)
        }
        integrityManager.propagateOnCloudIdAssigned(result::class,id,cloudId)
        return result
    }

    override fun attachToEngine(engine: SyncEngine) {
        this.syncEngine = engine
    }

    override fun canSync(id: String): Boolean {
        return false // mock --> see specifications of this class
    }

    override suspend fun getDTO(id: String): DTO? {
        return entity(db,id)?.let { mapper.toDTO(it) }
    }

    override fun getRemoteOf(id: String): String? {
        return entity(db,id)?.cloudId
    }

    override suspend fun onLocalChange(id: String) {
        syncEngine?.onLocalChange(id, transferClass)
    }
}


