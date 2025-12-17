package com.example.on_track_app.data.realm.repositories.decorated

import com.example.on_track_app.data.abstractions.repositories.BasicById
import com.example.on_track_app.data.abstractions.repositories.EventRepository
import com.example.on_track_app.data.abstractions.repositories.GroupRepository
import com.example.on_track_app.data.abstractions.repositories.IndexedByLink
import com.example.on_track_app.data.abstractions.repositories.IndexedByOwner
import com.example.on_track_app.data.abstractions.repositories.IndexedByProject
import com.example.on_track_app.data.abstractions.repositories.ProjectRepository
import com.example.on_track_app.data.abstractions.repositories.ReminderRepository
import com.example.on_track_app.data.abstractions.repositories.TaskRepository
import com.example.on_track_app.data.abstractions.repositories.UserRepository
import com.example.on_track_app.data.realm.entities.Entity
import com.example.on_track_app.data.realm.entities.EventRealmEntity
import com.example.on_track_app.data.realm.entities.GroupRealmEntity
import com.example.on_track_app.data.realm.entities.LocalConfig
import com.example.on_track_app.data.realm.entities.ProjectRealmEntity
import com.example.on_track_app.data.realm.entities.ReminderRealmEntity
import com.example.on_track_app.data.realm.entities.SynchronizableEntity
import com.example.on_track_app.data.realm.entities.SynchronizableLinkedEntity
import com.example.on_track_app.data.realm.entities.SynchronizableOwnedEntity
import com.example.on_track_app.data.realm.entities.SynchronizableProjectOwnershipEntity
import com.example.on_track_app.data.realm.entities.TaskRealmEntity
import com.example.on_track_app.data.realm.entities.UserRealmEntity
import com.example.on_track_app.data.realm.entities.delete
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.data.realm.entities.update
import com.example.on_track_app.data.realm.repositories.Filter
import com.example.on_track_app.data.realm.repositories.SynchronizableRepository
import com.example.on_track_app.data.realm.repositories.SynchronizedReference
import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.data.realm.utils.toRealmInstant
import com.example.on_track_app.data.synchronization.EventDTO
import com.example.on_track_app.data.synchronization.GroupDTO
import com.example.on_track_app.data.synchronization.ProjectDTO
import com.example.on_track_app.data.synchronization.ReferenceIntegrityManager
import com.example.on_track_app.data.synchronization.ReminderDTO
import com.example.on_track_app.data.synchronization.SyncEngine
import com.example.on_track_app.data.synchronization.SynchronizableDTO
import com.example.on_track_app.data.synchronization.TaskDTO
import com.example.on_track_app.data.synchronization.UserDTO
import com.example.on_track_app.data.synchronization.toObjectId
import com.example.on_track_app.model.Identifiable
import com.example.on_track_app.model.Link
import com.example.on_track_app.model.Linked
import com.example.on_track_app.model.MockEvent
import com.example.on_track_app.model.MockGroup
import com.example.on_track_app.model.MockProject
import com.example.on_track_app.model.MockReminder
import com.example.on_track_app.model.MockTask
import com.example.on_track_app.model.MockTimeField
import com.example.on_track_app.model.MockUser
import com.example.on_track_app.model.Owned
import com.example.on_track_app.model.OwnerType
import com.example.on_track_app.model.ProjectOwned
import com.example.on_track_app.utils.DebugLogcatLogger
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.TypedRealmObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId
import java.time.Instant
import kotlin.reflect.KClass

interface SyncMapper<RE, DTO, DOM> :
    DomainMapper<RE, DOM>,
    LocalMapper<RE, DTO>,
    DTOMapper<RE, DTO>
        where RE : SynchronizableEntity,
              DOM : Identifiable,
              DTO : SynchronizableDTO

@FunctionalInterface
fun interface DomainMapper<RE, DOM> {
    fun toDomain(entity: RE): DOM
}
@FunctionalInterface
fun interface LocalMapper<RE, DTO> {
    fun toLocal(dto: DTO, entity: RE)
}
@FunctionalInterface
fun interface DTOMapper<RE, DTO> {
    fun toDTO(entity: RE): DTO
}

interface RealmRepository<RE>
        where RE : TypedRealmObject, RE : Entity {

    val db: Realm
    val localClass: KClass<RE>

    fun config(realm: Realm): LocalConfig? =
        realm.query(LocalConfig::class).first().find()

    fun entity(realm: Realm, id: String): RE? =
        realm.query(localClass, Filter.ENTITY.query, ObjectId(id))
            .first().find()

    fun entity(realm: MutableRealm, id: String): RE? =
        realm.query(localClass, Filter.ENTITY.query, ObjectId(id))
            .first().find()

    fun entityByCloudId(realm: Realm, id: String): RE? =
        realm.query(localClass, Filter.REMOTE.query, id)
            .first().find()

    fun entityByCloudId(realm: MutableRealm, id: String): RE? =
        realm.query(localClass, Filter.REMOTE.query, id)
            .first().find()

    fun filter(
        realm: MutableRealm,
        filter: Filter,
        id: String
    ): RealmResults<RE> =
        realm.query(localClass, filter.query, ObjectId(id)).find()
}

open class RealmSynchronizableRepository<RE, DTO, DOM>(
    override val db: Realm,
    private val syncMapper: SyncMapper<RE, DTO, DOM>,
    val maker: () -> RE,
    override val localClass: KClass<RE>,
    val transferClass: KClass<DTO>,
    val integrityManager: ReferenceIntegrityManager
) : RealmRepository<RE>,
    BasicById<DOM>,
    SynchronizableRepository<DTO>,
    DomainMapper<RE, DOM>,
    LocalMapper<RE, DTO>,
    DTOMapper<RE, DTO>
        where RE : RealmObject,
              RE : SynchronizableEntity,
              DOM : Identifiable,
              DTO : SynchronizableDTO {

    var syncEngine: SyncEngine? = null

    // ---- mapper delegation ----
    override fun toDomain(entity: RE): DOM =
        syncMapper.toDomain(entity)

    override fun toLocal(dto: DTO, entity: RE) =
        syncMapper.toLocal(dto, entity)

    override fun toDTO(entity: RE): DTO =
        syncMapper.toDTO(entity)

    // ---- BASIC CRUD ----
    override fun getAll(): Flow<List<DOM>> =
        db.query(localClass)
            .asFlow()
            .map { it.list.map(::toDomain) }

    override fun getById(id: String): DOM? =
        entity(db, id)?.let(::toDomain)

    override fun liveById(id: String): Flow<DOM?> =
        db.query(localClass, Filter.ENTITY.query, ObjectId(id))
            .first().asFlow()
            .map { it.obj?.let(::toDomain) }

    override suspend fun markAsDeleted(id: String) {
        db.write { entity(this, id)?.delete() }
        syncEngine?.onLocalChange(id, transferClass)
    }

    override suspend fun delete(id: String) {
        db.write {
            entity(this, id)?.let { delete(findLatest(it)!!) }
        }
    }

    // ---------------------------
    // REMOTE → LOCAL
    // ---------------------------

    override suspend fun applyRemoteInsert(dto: DTO) {
        db.write {
            val entity = maker().apply { toLocal(dto, this) }
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
            toLocal(dto, latest)
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
            result = toDTO(local)
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
        return entity(db,id)?.let { toDTO(it) }
    }

    override fun getRemoteOf(id: String): String? {
        return entity(db,id)?.cloudId
    }

    override suspend fun onLocalChange(id: String) {
        syncEngine?.onLocalChange(id,transferClass)
    }
}


abstract class RealmRepositoryDecorator<RE>(
    protected val core: RealmRepository<RE>
) : RealmRepository<RE> by core
        where RE : TypedRealmObject, RE : Entity


class OwnershipRepository<RE, DOM>(
    core: RealmRepository<RE>,
    private val mapper: DomainMapper<RE, DOM>
) : RealmRepositoryDecorator<RE>(core),
    IndexedByOwner<DOM>
        where RE : TypedRealmObject, RE : Entity,
              RE : SynchronizableOwnedEntity,
              DOM : Identifiable,
              DOM : Owned {

    override fun of(id: String): Flow<List<DOM>> =
        core.db.query(core.localClass, Filter.OWNER.query, ObjectId(id))
            .find()
            .asFlow()
            .map { it.list.map(mapper::toDomain) }
}


class ProjectOwnershipRepository<RE, DOM>(
    core: RealmRepository<RE>,
    private val mapper: DomainMapper<RE, DOM>
) : RealmRepositoryDecorator<RE>(core),
    IndexedByProject<DOM>
        where RE : TypedRealmObject, RE : Entity,
              RE : SynchronizableProjectOwnershipEntity,
              DOM : Identifiable,
              DOM : ProjectOwned {

    override fun byProject(id: String): Flow<List<DOM>> =
        core.db.query(core.localClass, Filter.PROJECT.query, ObjectId(id))
            .find()
            .asFlow()
            .map { it.list.map(mapper::toDomain) }
}


class LinkedRepository<RE, DOM>(
    core: RealmRepository<RE>,
    private val mapper: DomainMapper<RE, DOM>
) : RealmRepositoryDecorator<RE>(core),
    IndexedByLink<DOM>
        where RE : TypedRealmObject, RE : Entity,
              RE : SynchronizableLinkedEntity,
              DOM : Identifiable,
              DOM : Linked {

    override fun linkedTo(id: String): Flow<List<DOM>> =
        core.db.query(core.localClass, Filter.LINK.query, ObjectId(id))
            .find()
            .asFlow()
            .map { it.list.map(mapper::toDomain) }

    override fun linkedTo(ids: List<String>): Flow<List<DOM>> =
        core.db.query(core.localClass, Filter.LINKS.query, ids.map {ObjectId(it)})
            .find()
            .asFlow()
            .map { it.list.map(mapper::toDomain) }
}


class RealmEventRepository(
    private val core: RealmRepository<EventRealmEntity>,
    private val mapper: DomainMapper<EventRealmEntity, MockEvent> = DomainMapper { it.toDomain()},
    private val sync: SynchronizableRepository<EventDTO>,
    private val byId: BasicById<MockEvent>,
    private val byOwner: IndexedByOwner<MockEvent>,
    private val byProject: IndexedByProject<MockEvent>,
    private val integrityManager: ReferenceIntegrityManager,
    private val transferClass: KClass<EventDTO> = EventDTO::class
) : EventRepository,
    SynchronizedReference,
    BasicById<MockEvent> by byId,
    IndexedByOwner<MockEvent> by byOwner,
    IndexedByProject<MockEvent> by byProject,
    SynchronizableRepository<EventDTO> by sync
{
    override suspend fun addEvent(
        name: String,
        description: String,
        start: MockTimeField,
        end: MockTimeField,
        projectId: String?,
        cloudId: String?,
        ownerId: String,
        ownerType: OwnerType
    ): String {


        var newId = ""

        core.db.write {
            val event = EventRealmEntity().apply {
                minValuesInit(name, description, start, end, projectId)
                this.cloudId = cloudId
                this.ownerId = ownerId.toObjectId()
                this.ownerType = ownerType.name
                this.synchronizationStatus = SynchronizationState.CREATED.name
                this.version = RealmInstant.now()
            }

            val saved = copyToRealm(event)

            integrityManager
                .resolveReferenceOnCreate(
                    transferClass,
                    saved.ownerId.toHexString(),
                    Filter.OWNER
                )
                ?.let { saved.cloudOwnerId = it }

            saved.projectId?.let {
                integrityManager
                    .resolveReferenceOnCreate(
                        transferClass,
                        it.toHexString(),
                        Filter.PROJECT
                    )
                    ?.let { cloud -> saved.cloudProjectId = cloud }
            }

            newId = saved.id.toHexString()
        }

        sync.onLocalChange(newId)

        return newId
    }

    private fun EventRealmEntity.minValuesInit(
        name: String,
        description: String,
        start: MockTimeField,
        end: MockTimeField,
        projectId: String?
    ) {
        this.name = name
        this.description = description
        this.start = start.instant.toRealmInstant()
        this.startWithTime = start.timed
        this.end = end.instant.toRealmInstant()
        this.endWithTime = end.timed
        this.projectId = projectId?.toObjectId()
    }

    override suspend fun update(
        updated:MockEvent
    ) {
        core.db.write {
            core.entity(this, updated.id)?.apply {
                minValuesInit(updated.name,updated.description,updated.start,updated.end,updated.projectId)
                update()
            }
        }
        sync.onLocalChange(updated.id)
    }

    override fun canSync(id: String): Boolean {
        val entity = core.entity(core.db, id) ?: return false

        if (entity.cloudOwnerId == null) return false
        if (entity.projectId != null && entity.cloudProjectId == null) return false

        return true
    }

    override suspend fun synchronizeReferences(id: String, cloudId: String) {
        core.db.write {
            core.filter(this, Filter.OWNER, id).forEach {
                it.cloudOwnerId = cloudId
            }

            core.filter(this, Filter.PROJECT, id).forEach {
                it.cloudProjectId = cloudId
            }
        }
    }

    override fun between(
        start: Instant,
        end: Instant
    ): Flow<List<MockEvent>> =
        core.db.query(core.localClass, Filter.EVENT_IN.query, start.toRealmInstant(), end.toRealmInstant())
            .find()
            .asFlow()
            .map { it.list.map(mapper::toDomain) }

    override fun byGroupAndInterval(
        group: String,
        start: Instant,
        end: Instant
    ): Flow<List<MockEvent>> =
        core.db.query(core.localClass, Filter.GROUP_EVENT_IN.query,group.toObjectId(), start.toRealmInstant(), end.toRealmInstant())
            .find()
            .asFlow()
            .map { it.list.map(mapper::toDomain) }

    override fun byProjectAndInterval(
        project: String,
        start: Instant,
        end: Instant
    ): Flow<List<MockEvent>> =
        core.db.query(core.localClass, Filter.PROJECT_EVENT_IN.query, project.toObjectId(),start.toRealmInstant(), end.toRealmInstant())
            .find()
            .asFlow()
            .map { it.list.map(mapper::toDomain) }
}


/*
val base =
    RealmSynchronizableRepository(
        db,
        syncMapper,
        maker,
        GroupRealmEntity::class,
        GroupDTO::class,
        integrityManager
    )

val owned =
    OwnershipRepository(
        core = base,
        mapper = base
    )

val groupRepo =
    RealmGroupRepository(
        core = base,
        mapper = base,
        sync = base,
        byOwner = owned,
        integrityManager = integrityManager
    )

 */
class RealmGroupRepository(
    private val core: RealmRepository<GroupRealmEntity>,
    private val mapper: DomainMapper<GroupRealmEntity, MockGroup>,
    private val sync: SynchronizableRepository<GroupDTO>,
    private val byId: BasicById<MockGroup>,
    private val byOwner: IndexedByOwner<MockGroup>,
    private val integrityManager: ReferenceIntegrityManager,
    private val transferClass: KClass<GroupDTO> = GroupDTO::class
) : GroupRepository,
    SynchronizedReference,
    BasicById<MockGroup> by byId,
    IndexedByOwner<MockGroup> by byOwner,
    SynchronizableRepository<GroupDTO> by sync
{
    override suspend fun addGroup(
        name: String,
        description: String,
        ownerId: String,
        cloudId: String?
    ): String {

        var newId = ""

        core.db.write {
            val entity = GroupRealmEntity().apply {
                this.name = name
                this.description = description
                this.ownerId = ownerId.toObjectId()
                this.cloudId = cloudId

                this.synchronizationStatus = SynchronizationState.CREATED.name
                this.version = RealmInstant.now()
            }

            val saved = copyToRealm(entity)

            integrityManager
                .resolveReferenceOnCreate(
                    /* transfer class ya la conoce sync internamente */
                    transferClass,
                    saved.ownerId.toHexString(),Filter.OWNER
                )
                ?.let { saved.cloudOwnerId = it }

            newId = saved.id.toHexString()
        }

        sync.onLocalChange(newId)
        return newId
    }

    override suspend fun update(updated: MockGroup) {
        core.db.write {
            core.entity(this, updated.id)?.apply {
                name = updated.name
                description = updated.description
                update()
            }
        }
        sync.onLocalChange(updated.id)
    }

    override fun canSync(id: String): Boolean {
        val entity = core.entity(core.db, id) ?: return false
        return entity.cloudOwnerId != null
    }

    override suspend fun synchronizeReferences(
        id: String,
        cloudId: String
    ) {
        core.db.write {
            core.filter(this, Filter.OWNER, id).forEach { entity ->
                entity.cloudOwnerId = cloudId
            }
        }
    }

}

/*
val base =
    RealmSynchronizableRepository(
        db,
        syncMapper,
        maker,
        ProjectRealmEntity::class,
        ProjectDTO::class,
        integrityManager
    )

val owned =
    OwnershipRepository(
        core = base,
        mapper = base
    )

val projectRepo =
    RealmProjectRepository(
        core = base,
        mapper = base,
        sync = base,
        byOwner = owned,
        integrityManager = integrityManager
    )

 */

class RealmProjectRepository(
    private val core: RealmRepository<ProjectRealmEntity>,
    private val mapper: DomainMapper<ProjectRealmEntity, MockProject>,
    private val sync: SynchronizableRepository<ProjectDTO>,
    private val byId: BasicById<MockProject>,
    private val byOwner: IndexedByOwner<MockProject>,
    private val integrityManager: ReferenceIntegrityManager,
    private val transferClass: KClass<ProjectDTO> = ProjectDTO::class
) : ProjectRepository,
    SynchronizedReference,
    BasicById<MockProject> by byId,
    IndexedByOwner<MockProject> by byOwner,
    SynchronizableRepository<ProjectDTO> by sync
{
    override suspend fun addProject(
        name: String,
        description: String,
        cloudId: String?,
        ownerId: String,
        ownerType: OwnerType
    ): String {

        var newId = ""

        core.db.write {
            val project = ProjectRealmEntity().apply {
                this.name = name
                this.description = description
                this.cloudId = cloudId
                this.ownerId = ownerId.toObjectId()
                this.ownerType = ownerType.name
                this.synchronizationStatus = SynchronizationState.CREATED.name
                this.version = RealmInstant.now()
            }

            val saved = copyToRealm(project)

            integrityManager
                .resolveReferenceOnCreate(
                    transferClass,saved.ownerId.toHexString(),Filter.OWNER
                )
                ?.let { saved.cloudOwnerId = it }

            DebugLogcatLogger.logRealmSaved(saved)
            newId = saved.id.toHexString()
        }

        sync.onLocalChange(newId)
        return newId
    }

    override suspend fun update(updated: MockProject) {
        core.db.write {
            core.entity(this, updated.id)?.apply {
                name = updated.name
                description = updated.description
                update()
            }
        }
        sync.onLocalChange(updated.id)
    }


    override fun canSync(id: String): Boolean {
        val entity = core.entity(core.db, id) ?: return false
        return entity.cloudOwnerId != null
    }

    override suspend fun synchronizeReferences(
        id: String,
        cloudId: String
    ) {
        core.db.write {
            core.filter(this, Filter.OWNER, id).forEach { entity ->
                entity.cloudOwnerId = cloudId
            }
        }
    }
}

/*
val base =
    RealmSynchronizableRepository(
        db,
        syncMapper,
        maker,
        ReminderRealmEntity::class,
        ReminderDTO::class,
        integrityManager
    )

val owned =
    OwnershipRepository(
        core = base,
        mapper = base
    )

val linked =
    LinkedRepository(
        core = base,
        mapper = base
    )

val reminderRepo =
    RealmReminderRepository(
        core = base,
        mapper = base,
        sync = base,
        byOwner = owned,
        byLink = linked,
        integrityManager = integrityManager
    )

 */

class RealmReminderRepository(
    private val core: RealmRepository<ReminderRealmEntity>,
    private val mapper: DomainMapper<ReminderRealmEntity, MockReminder>,
    private val sync: SynchronizableRepository<ReminderDTO>,
    private val byOwner: IndexedByOwner<MockReminder>,
    private val byId: BasicById<MockReminder>,
    private val byLink: IndexedByLink<MockReminder>,
    private val integrityManager: ReferenceIntegrityManager,
    private val transferClass: KClass<ReminderDTO> = ReminderDTO::class
) : ReminderRepository,
    SynchronizedReference,
    BasicById<MockReminder> by byId,
    IndexedByOwner<MockReminder> by byOwner,
    IndexedByLink<MockReminder> by byLink,
    SynchronizableRepository<ReminderDTO> by sync
{
    override suspend fun addReminder(
        ownerId: String,
        ownerType: OwnerType,
        name: String,
        description: String,
        at: MockTimeField,
        linked: Link?,
        cloudId: String?
    ): String {

        var newId = ""

        core.db.write {
            val newReminder = ReminderRealmEntity().apply {

                minValuesInit(name, description, at, linked)

                this.ownerId = ownerId.toObjectId()
                this.ownerType = ownerType.name
                this.cloudId = cloudId

                this.synchronizationStatus = SynchronizationState.CREATED.name
                this.version = RealmInstant.now()
            }

            val saved = copyToRealm(newReminder)

            integrityManager
                .resolveReferenceOnCreate(transferClass,
                    saved.ownerId.toHexString(),
                    Filter.OWNER
                )
                ?.let { saved.cloudOwnerId = it }

            saved.linkedTo?.let {
                integrityManager
                    .resolveReferenceOnCreate(transferClass,
                        it.toHexString(),
                        Filter.LINK
                    )
                    ?.let { cloud -> saved.cloudLinkedTo = cloud }
            }

            DebugLogcatLogger.logRealmSaved(saved)
            newId = saved.id.toHexString()
        }

        sync.onLocalChange(newId)
        return newId
    }

    private fun ReminderRealmEntity.minValuesInit(
        name: String,
        description: String,
        at: MockTimeField,
        linked: Link?
    ) {
        this.name = name
        this.description = description
        this.at = at.instant.toRealmInstant()
        this.withTime = at.timed
        this.linkedTo = linked?.to?.toObjectId()
        this.linkType = linked?.ofType?.name
    }

    override suspend fun update(updated: MockReminder) {
        core.db.write {
            core.entity(this, updated.id)?.apply {
                minValuesInit(updated.name,updated.description,updated.at,updated.linked)
                update()
            }
        }
        sync.onLocalChange(updated.id)
    }

    override fun canSync(id: String): Boolean {
        val entity = core.entity(core.db, id) ?: return false

        if (entity.cloudOwnerId == null) return false
        if (entity.linkedTo != null && entity.cloudLinkedTo == null) return false

        return true
    }

    override suspend fun synchronizeReferences(
        id: String,
        cloudId: String
    ) {
        core.db.write {
            core.filter(this, Filter.OWNER, id).forEach {
                it.cloudOwnerId = cloudId
            }

            core.filter(this, Filter.LINK, id).forEach {
                it.cloudLinkedTo = cloudId
            }
        }
    }

    override fun between(
        start: Instant,
        end: Instant
    ): Flow<List<MockReminder>> =
        core.db.query(core.localClass, Filter.REMINDER_IN.query, start.toRealmInstant(), end.toRealmInstant())
        .find()
        .asFlow()
        .map { it.list.map(mapper::toDomain) }

    override fun byLinkAndInterval(
        links: List<String>,
        start: Instant,
        end: Instant
    ): Flow<List<MockReminder>> =
        core.db.query(core.localClass, Filter.LINKED_REMINDER_IN.query,links, start.toRealmInstant(), end.toRealmInstant())
            .find()
            .asFlow()
            .map { it.list.map(mapper::toDomain) }
}

/*
val base =
    RealmSynchronizableRepository(
        db,
        syncMapper,
        maker,
        TaskRealmEntity::class,
        TaskDTO::class,
        integrityManager
    )

val owned =
    OwnershipRepository(
        core = base,
        mapper = base
    )

val projected =
    ProjectOwnershipRepository(
        core = base,
        mapper = base
    )

val taskRepo =
    RealmTaskRepository(
        core = base,
        mapper = base,
        sync = base,
        byOwner = owned,
        byProject = projected,
        integrityManager = integrityManager
    )

 */

class RealmTaskRepository(
    private val core: RealmRepository<TaskRealmEntity>,
    private val mapper: DomainMapper<TaskRealmEntity, MockTask>,
    private val sync: SynchronizableRepository<TaskDTO>,
    private val byId: BasicById<MockTask>,
    private val byOwner: IndexedByOwner<MockTask>,
    private val byProject: IndexedByProject<MockTask>,
    private val integrityManager: ReferenceIntegrityManager,
    private val transferClass: KClass<TaskDTO> = TaskDTO::class
) : TaskRepository,
    SynchronizedReference,
    BasicById<MockTask> by byId,
    IndexedByOwner<MockTask> by byOwner,
    IndexedByProject<MockTask> by byProject,
    SynchronizableRepository<TaskDTO> by sync {
    override suspend fun addTask(
        name: String,
        description: String,
        date: MockTimeField,
        projectId: String?,
        cloudId: String?,
        ownerId: String,
        ownerType: OwnerType
    ): String {

        var id = ""

        core.db.write {
            val task = TaskRealmEntity().apply {
                minValuesInit(name, description, projectId, date)
                this.cloudId = cloudId
                this.ownerId = ownerId.toObjectId()
                this.ownerType = ownerType.name
                this.synchronizationStatus = SynchronizationState.CREATED.name
                this.version = RealmInstant.now()
            }

            val saved = copyToRealm(task)

            integrityManager
                .resolveReferenceOnCreate(transferClass,
                    saved.ownerId.toHexString(),
                    Filter.OWNER
                )
                ?.let { saved.cloudOwnerId = it }

            saved.projectId?.let {
                integrityManager
                    .resolveReferenceOnCreate(transferClass,
                        it.toHexString(),
                        Filter.PROJECT
                    )
                    ?.let { cloud -> saved.cloudProjectId = cloud }
            }

            DebugLogcatLogger.logRealmSaved(saved)
            id = saved.id.toHexString()
        }

        sync.onLocalChange(id)
        return id
    }

    private fun TaskRealmEntity.minValuesInit(
        name: String,
        description: String,
        projectId: String?,
        date: MockTimeField
    ) {
        this.name = name
        this.description = description
        this.projectId = projectId?.toObjectId()
        this.due = date.instant.toRealmInstant()
        this.withTime = date.timed
    }

    override suspend fun update(updated: MockTask) {
        core.db.write {
            core.entity(this, updated.id)?.apply {
                minValuesInit(updated.name,updated.description,updated.projectId,updated.due)
                update()
            }
        }
        sync.onLocalChange(updated.id)
    }

    override fun canSync(id: String): Boolean {
        val entity = core.entity(core.db, id) ?: return false

        if (entity.cloudOwnerId == null) return false
        if (entity.projectId != null && entity.cloudProjectId == null) return false

        return true
    }

    override suspend fun synchronizeReferences(
        id: String,
        cloudId: String
    ) {
        core.db.write {
            core.filter(this, Filter.OWNER, id).forEach {
                it.cloudOwnerId = cloudId
            }

            core.filter(this, Filter.PROJECT, id).forEach {
                it.cloudProjectId = cloudId
            }
        }
    }

    override fun between(
        start: Instant,
        end: Instant
    ): Flow<List<MockTask>> =
        core.db.query(core.localClass, Filter.TASK_DUE_IN.query, start.toRealmInstant(), end.toRealmInstant())
            .find()
            .asFlow()
            .map { it.list.map(mapper::toDomain) }

    override fun byGroupAndInterval(
        group: String,
        start: Instant,
        end: Instant
    ): Flow<List<MockTask>> =
        core.db.query(
            core.localClass,
            Filter.GROUP_TASK_DUE_IN.query,
            group,
            start.toRealmInstant(),
            end.toRealmInstant()
        )
            .find()
            .asFlow()
            .map { it.list.map(mapper::toDomain) }

    override fun byProjectAndInterval(
        project: String,
        start: Instant,
        end: Instant
    ): Flow<List<MockTask>> =
        core.db.query(core.localClass, Filter.PROJECT_TASK_DUE_IN.query, start.toRealmInstant(), end.toRealmInstant())
            .find()
            .asFlow()
            .map { it.list.map(mapper::toDomain) }
}

/*
val base =
    RealmSynchronizableRepository(
        db,
        syncMapper,
        maker,
        UserRealmEntity::class,
        UserDTO::class,
        integrityManager
    )

val userRepo =
    RealmUserRepository(
        core = base,
        mapper = base,
        sync = base
    )

 */
class RealmUserRepository(
    private val core: RealmRepository<UserRealmEntity>,
    private val mapper: DomainMapper<UserRealmEntity, MockUser>,
    private val sync: SynchronizableRepository<UserDTO>,
    private val byId: BasicById<MockUser>
) : UserRepository,
    BasicById<MockUser> by byId,
    SynchronizableRepository<UserDTO> by sync {

    override suspend fun addUser(
        username: String,
        email: String,
        cloudId: String?
    ): String {

        var id = ""

        core.db.write {
            val entity = UserRealmEntity().apply {
                this.name = username
                this.email = email
                this.cloudId = cloudId
                this.synchronizationStatus = SynchronizationState.CREATED.name
                this.version = RealmInstant.now()
            }

            val saved = copyToRealm(entity)
            DebugLogcatLogger.logRealmSaved(saved)
            id = saved.id.toHexString()
        }

        sync.onLocalChange(id)
        return id
    }


    override suspend fun updateUser(
        id: String,
        newEmail: String
    ) {
        core.db.write {
            core.entity(this, id)?.let {
                it.email = newEmail
                it.update()
            }
        }

        sync.onLocalChange(id)
    }

    override fun canSync(id: String): Boolean {
        return core.entity(core.db, id) != null
    }

}