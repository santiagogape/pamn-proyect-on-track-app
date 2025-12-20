package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.abstractions.repositories.BasicById
import com.example.on_track_app.data.abstractions.repositories.EventRepository
import com.example.on_track_app.data.abstractions.repositories.IndexedByLink
import com.example.on_track_app.data.abstractions.repositories.IndexedByOwner
import com.example.on_track_app.data.abstractions.repositories.IndexedByProject
import com.example.on_track_app.data.realm.entities.Entity
import com.example.on_track_app.data.realm.entities.EventRealmEntity
import com.example.on_track_app.data.realm.entities.GroupRealmEntity
import com.example.on_track_app.data.realm.entities.LinkReference
import com.example.on_track_app.data.realm.entities.MembershipRealmEntity
import com.example.on_track_app.data.realm.entities.MembershipReference
import com.example.on_track_app.data.realm.entities.OwnerReference
import com.example.on_track_app.data.realm.entities.ProjectRealmEntity
import com.example.on_track_app.data.realm.entities.ProjectReference
import com.example.on_track_app.data.realm.entities.RealmLinkable
import com.example.on_track_app.data.realm.entities.RealmMembership
import com.example.on_track_app.data.realm.entities.RealmOwner
import com.example.on_track_app.data.realm.entities.ReminderRealmEntity
import com.example.on_track_app.data.realm.entities.SynchronizableEntity
import com.example.on_track_app.data.realm.entities.SynchronizationEntity
import com.example.on_track_app.data.realm.entities.TaskRealmEntity
import com.example.on_track_app.data.realm.entities.TimeRealmEmbeddedObject
import com.example.on_track_app.data.realm.entities.UserRealmEntity
import com.example.on_track_app.data.realm.entities.delete
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.data.realm.entities.toObjectId
import com.example.on_track_app.data.realm.entities.upToDate
import com.example.on_track_app.data.realm.utils.toRealmInstant
import com.example.on_track_app.data.synchronization.EventDTO
import com.example.on_track_app.data.synchronization.GroupDTO
import com.example.on_track_app.data.synchronization.MembershipDTO
import com.example.on_track_app.data.synchronization.ProjectDTO
import com.example.on_track_app.data.synchronization.ReminderDTO
import com.example.on_track_app.data.synchronization.SyncEngine
import com.example.on_track_app.data.synchronization.SynchronizableDTO
import com.example.on_track_app.data.synchronization.TaskDTO
import com.example.on_track_app.data.synchronization.UserDTO
import com.example.on_track_app.data.synchronization.toDTO
import com.example.on_track_app.data.synchronization.toRealm
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Group
import com.example.on_track_app.model.Identifiable
import com.example.on_track_app.model.Linkable
import com.example.on_track_app.model.Linked
import com.example.on_track_app.model.LinkedType
import com.example.on_track_app.model.Membership
import com.example.on_track_app.model.MembershipType
import com.example.on_track_app.model.Owned
import com.example.on_track_app.model.OwnerType
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.ProjectOwned
import com.example.on_track_app.model.Reminder
import com.example.on_track_app.model.Task
import com.example.on_track_app.model.User
import com.example.on_track_app.model.UserMembership
import com.example.on_track_app.utils.DebugLogcatLogger
import com.example.on_track_app.viewModels.GroupOwnerContext
import com.example.on_track_app.viewModels.OwnerContext
import com.example.on_track_app.viewModels.UserOwnerContext
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.TypedRealmObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId
import kotlin.jvm.java
import kotlin.reflect.KClass

enum class Filter(val query: String) {

    // ---------- LOCAL ID ----------
    ENTITY("id == $0"),

    // ---------- OWNER ----------
    OWNER("owner.id == $0"),
    REMOTE_OWNER("owner.identity.cloudId == $0"),

    // ---------- PROJECT ----------
    PROJECT("project.id == $0"),
    REMOTE_PROJECT("project.identity.cloudId == $0"),

    // ---------- LINK ----------
    LINK("linkedTo.id == $0"),
    LINKS("linkedTo.id IN $0"),
    REMOTE_LINK("linkedTo.identity.cloudId == $0"),

    // ---------- TASK ----------
    TASK_DUE_IN("due.instant >= $0 AND due.instant <= $1"),
    GROUP_TASK_DUE_IN("owner.id == $0 AND due.instant >= $1 AND due.instant <= $2"),
    PROJECT_TASK_DUE_IN("project.id == $0 AND due.instant >= $1 AND due.instant <= $2"),

    // ---------- EVENT ----------
    EVENT_IN("start.instant >= $0 AND end.instant <= $1"),
    GROUP_EVENT_IN("owner.id == $0 AND start.instant >= $1 AND end.instant <= $2"),
    PROJECT_EVENT_IN("project.id == $0 AND start.instant >= $1 AND end.instant <= $2"),

    // ---------- REMINDER ----------
    REMINDER_IN("at.instant >= $0 AND at.instant <= $1"),
    LINKED_REMINDER_IN("linkedTo.id IN $0 AND at.instant >= $1 AND at.instant <= $2"),

    // ---------- MEMBERSHIP ----------
    MEMBERSHIP_ENTITY("membership.id == $0"),
    MEMBERSHIP_MEMBER("member.id == $0"),

    // ---------- REMOTE ENTITY ----------
    REMOTE("identity.cloudId == $0"),
    VERSION("identity.version > $0")


}


// =====================================================
// SYNCHRONIZATION – COMMON CONTRACTS
// =====================================================

interface SynchronizableRepositoryElimination<D : SynchronizableDTO> {
    suspend fun applyRemoteDelete(dto: D)
}


interface SynchronizableRepository<D : SynchronizableDTO> :
    SynchronizableRepositoryElimination<D> {

    suspend fun applyRemoteInsert(dto: D)

    suspend fun applyRemoteUpdate(dto: D)

    suspend fun applyCloudId(
        id: String,
        cloudId: String
    ): D

    fun attachToEngine(engine: SyncEngine)

    fun getId(dto: D): String
    suspend fun getDTO(id: String): D?
    suspend fun getDTOUpdatedAfter(version:Long): List<D>
    suspend fun onLocalChange(id: String)
}

interface ReferenceResolver {

    fun user(realm:MutableRealm,cloudId: String): UserRealmEntity
    fun user(realm:MutableRealm,user: User): UserRealmEntity

    fun owner(realm:MutableRealm,owner: OwnerContext): OwnerReference
    fun owner(realm:MutableRealm,type: OwnerType, cloudId: String): OwnerReference

    fun project(realm:MutableRealm,project: Project?): ProjectReference?
    fun project(realm:MutableRealm,cloudId: String?): ProjectReference?


    fun link(realm:MutableRealm,link: Linkable?): LinkReference?
    fun link(realm:MutableRealm,type: LinkedType, cloudId: String?): LinkReference?

    fun membership(realm:MutableRealm,membership: Membership): MembershipReference
    fun membership(realm:MutableRealm,type: MembershipType, cloudId: String): MembershipReference
}


class RealmReferenceResolver(
) : ReferenceResolver {


    override fun owner(realm:MutableRealm,owner: OwnerContext): OwnerReference {

        val found = when(owner){

            is GroupOwnerContext -> realm.query(GroupRealmEntity::class,  Filter.ENTITY.query, owner.ownerId.toObjectId())
                .first().find()?.let { group ->
                    OwnerReference().apply {
                        id = group.id
                        identity = group.identity
                        this.group = group
                    }
                }
            is UserOwnerContext -> realm.query(UserRealmEntity::class,  Filter.ENTITY.query, owner.ownerId.toObjectId())
                .first().find()?.let { user ->
                    OwnerReference().apply {
                        id = user.id
                        identity = user.identity
                        this.user = user
                    }
                }
        }

        return found ?: error("Owner not found: ${owner.ownerId}")
    }

    override fun project(realm:MutableRealm,project: Project?): ProjectReference? {
        if (project == null) return null
        DebugLogcatLogger.log("  project $project")

        val entity = realm.query(
            ProjectRealmEntity::class,
            Filter.ENTITY.query,
            project.id.toObjectId()
        ).first().find() ?: error("Project not found: ${project.id}")

        return ProjectReference().apply {
            id = entity.id
            identity = entity.identity
            this.project = entity
        }
    }

    override fun link(realm:MutableRealm,link: Linkable?): LinkReference? {
        if (link == null) return null

        val found = when(link){
            is Task -> realm.query(TaskRealmEntity::class, Filter.ENTITY.query, link.id.toObjectId())
                .first().find()?.let { task ->
                    LinkReference().apply {
                        id = task.id
                        identity = task.identity
                        this.task = task
                    }
                }
            is Event -> realm.query(EventRealmEntity::class, Filter.ENTITY.query, link.id.toObjectId())
                .first().find()?.let { event ->
                    LinkReference().apply {
                        id = event.id
                        identity = event.identity
                        this.event = event
                    }
                }
        }
        return found ?: error("Linkable not found: ${link.id}")
    }

    override fun membership(realm:MutableRealm,membership: Membership): MembershipReference {
        val found = when(membership){
            is Group -> realm.query(GroupRealmEntity::class, Filter.ENTITY.query, membership.id.toObjectId())
                .first().find()?.let { group ->
                    MembershipReference().apply {
                        id = group.id
                        identity = group.identity
                        this.group = group
                    }
                }
            is Project -> realm.query(ProjectRealmEntity::class, Filter.ENTITY.query, membership.id.toObjectId())
                .first().find()?.let { project ->
                    MembershipReference().apply {
                        id = project.id
                        identity = project.identity
                        this.project = project
                    }
                }

        }
        return found ?: error("Membership not found: ${membership.id}")
    }


    override fun owner(realm:MutableRealm,
        type: OwnerType,
        cloudId: String
    ): OwnerReference {
        when(type){
            OwnerType.GROUP -> realm.query(GroupRealmEntity::class, Filter.REMOTE.query, cloudId)
                .first().find()?.let { group ->
                    return OwnerReference().apply {
                        id = group.id
                        identity = group.identity
                        this.group = group
                    }
                }
            OwnerType.USER -> realm.query(UserRealmEntity::class, Filter.REMOTE.query, cloudId)
                .first().find()?.let { user ->
                    return OwnerReference().apply {
                        id = user.id
                        identity = user.identity
                        this.user = user
                    }
                }
        }
        error("Owner not found for cloudId=$cloudId")
    }

    override fun project(realm:MutableRealm,cloudId: String?): ProjectReference? =
        cloudId?.let {
            realm.query(ProjectRealmEntity::class, Filter.REMOTE.query, it)
                .first().find()?.let { entity ->
                    ProjectReference().apply {
                        id = entity.id
                        identity = entity.identity
                        this.project = entity
                    }
                } ?: error("Project not found for cloudId=$cloudId")
        }

    override fun link(realm:MutableRealm,
        type: LinkedType,
        cloudId: String?
    ): LinkReference? =
        cloudId?.let {
            when(type){
                LinkedType.TASK -> realm.query(TaskRealmEntity::class, Filter.REMOTE.query, it)
                    .first().find()?.let { task ->
                        return LinkReference().apply {
                            id = task.id
                            identity = task.identity
                            this.task = task
                        }
                    }
                LinkedType.EVENT -> realm.query(EventRealmEntity::class, Filter.REMOTE.query, it)
                    .first().find()?.let { event ->
                        return LinkReference().apply {
                            id = event.id
                            identity = event.identity
                            this.event = event
                        }
                    }
            }
            error("Linkable not found for cloudId=$cloudId")
        }

    override fun membership(realm:MutableRealm,
        type: MembershipType,
        cloudId: String
    ): MembershipReference {
        when (type){
            MembershipType.PROJECT -> realm.query(ProjectRealmEntity::class, Filter.REMOTE.query, cloudId)
                .first().find()?.let { project ->
                    return MembershipReference().apply {
                        id = project.id
                        identity = project.identity
                        this.project = project
                    }
                }
            MembershipType.GROUP -> realm.query(GroupRealmEntity::class, Filter.REMOTE.query, cloudId)
                .first().find()?.let { group ->
                    return MembershipReference().apply {
                        id = group.id
                        identity = group.identity
                        this.group = group
                    }
                }
        }
        error("Membership not found for cloudId=$cloudId")
    }

    override fun user(realm:MutableRealm,cloudId: String) = realm.query(
        UserRealmEntity::class,
        Filter.REMOTE.query,cloudId
    ).first().find()
        ?: error("User not found for cloudId=$cloudId")

    override fun user(realm:MutableRealm,user: User) = realm.query(
        UserRealmEntity::class,
        Filter.ENTITY.query,
        user.id.toObjectId()
    ).first().find() ?: error("Project not found: ${user.id}")
}




// ---------------- MAPPERS ----------------

interface SyncMapper<RE, DTO, DOM> :
    DomainMapper<RE, DOM>,
    LocalMapper<RE, DTO>,
    DTOMapper<RE, DTO>
        where RE : SynchronizableEntity,
              DOM : Identifiable,
              DTO : SynchronizableDTO

fun interface DomainMapper<RE, DOM> {
    fun toDomain(entity: RE): DOM
}

fun interface LocalMapper<RE, DTO> {
    fun toLocal(realm:MutableRealm,dto: DTO, entity: RE, isNew: Boolean)
}

fun interface DTOMapper<RE, DTO> {
    fun toDTO(entity: RE): DTO
}


class RealmSyncMapperFactory(
    private val resolver : RealmReferenceResolver
) {


    val user = object : SyncMapper<UserRealmEntity, UserDTO, User> {
        override fun toDomain(entity: UserRealmEntity) = entity.toDomain()
        override fun toDTO(entity: UserRealmEntity) = entity.toDTO()
        override fun toLocal(realm:MutableRealm,dto: UserDTO, entity: UserRealmEntity, isNew: Boolean) {
            if (isNew) entity.identity = SynchronizationEntity()
            dto.toRealm(entity)
        }
    }

    val group = object : SyncMapper<GroupRealmEntity, GroupDTO, Group> {
        override fun toDomain(entity: GroupRealmEntity) = entity.toDomain()
        override fun toDTO(entity: GroupRealmEntity) = entity.toDTO()
        override fun toLocal(realm:MutableRealm,dto: GroupDTO, entity: GroupRealmEntity, isNew: Boolean) {
            if(isNew) {
                entity.identity = SynchronizationEntity()
                entity.owner = resolver.owner(realm,OwnerType.USER,dto.cloudOwnerId)
            }
            dto.toRealm(entity)
        }
    }

    val project = object : SyncMapper<ProjectRealmEntity, ProjectDTO, Project> {
        override fun toDomain(entity: ProjectRealmEntity) = entity.toDomain()
        override fun toDTO(entity: ProjectRealmEntity) = entity.toDTO()
        override fun toLocal(realm:MutableRealm,dto: ProjectDTO, entity: ProjectRealmEntity, isNew: Boolean) {
            if(isNew) {
                entity.identity = SynchronizationEntity()
                entity.owner = resolver.owner(realm,OwnerType.valueOf(dto.ownerType),dto.cloudOwnerId)
            }
            dto.toRealm(entity)
        }
    }

    val task = object : SyncMapper<TaskRealmEntity, TaskDTO, Task> {
        override fun toDomain(entity: TaskRealmEntity) = entity.toDomain()
        override fun toDTO(entity: TaskRealmEntity) = entity.toDTO()
        override fun toLocal(realm:MutableRealm,dto: TaskDTO, entity: TaskRealmEntity, isNew: Boolean) {
            if (isNew) {
                entity.identity = SynchronizationEntity()
                entity.due = TimeRealmEmbeddedObject()
                entity.owner = resolver.owner(realm,OwnerType.valueOf(dto.ownerType),dto.cloudOwnerId)
                entity.project = dto.cloudProjectId?.let {resolver.project(realm,it)}
            }
            dto.toRealm(entity)
        }
    }

    val event = object : SyncMapper<EventRealmEntity, EventDTO, Event> {
        override fun toDomain(entity: EventRealmEntity) = entity.toDomain()
        override fun toDTO(entity: EventRealmEntity) = entity.toDTO()
        override fun toLocal(realm:MutableRealm,dto: EventDTO, entity: EventRealmEntity, isNew: Boolean) {
            if (isNew) {
                entity.identity = SynchronizationEntity()
                entity.start = TimeRealmEmbeddedObject()
                entity.end = TimeRealmEmbeddedObject()
                entity.owner = resolver.owner(realm,OwnerType.valueOf(dto.ownerType),dto.cloudOwnerId)
                entity.project = dto.cloudProjectId?.let {resolver.project(realm,it)}
            }
            dto.toRealm(entity)
        }
    }

    val reminder = object : SyncMapper<ReminderRealmEntity, ReminderDTO, Reminder> {
        override fun toDomain(entity: ReminderRealmEntity) = entity.toDomain()
        override fun toDTO(entity: ReminderRealmEntity) = entity.toDTO()
        override fun toLocal(realm:MutableRealm,dto: ReminderDTO, entity: ReminderRealmEntity, isNew: Boolean) {
            if (isNew) {
                entity.identity = SynchronizationEntity()
                entity.at = TimeRealmEmbeddedObject()
                entity.owner = resolver.owner(realm,OwnerType.valueOf(dto.ownerType),dto.cloudOwnerId)
                entity.linkedTo = when {
                    dto.linkType != null && dto.cloudLinkTo != null ->
                        resolver.link(realm,LinkedType.valueOf(dto.linkType),dto.cloudLinkTo)
                    else -> null
                }
            }
            dto.toRealm(entity)

        }
    }

    val membership = object : SyncMapper<MembershipRealmEntity, MembershipDTO, UserMembership> {
        override fun toDomain(entity: MembershipRealmEntity) = entity.toDomain()
        override fun toDTO(entity: MembershipRealmEntity) = entity.toDTO()
        override fun toLocal(realm:MutableRealm,dto: MembershipDTO, entity: MembershipRealmEntity, isNew: Boolean) {
            if (isNew) {
                entity.identity = SynchronizationEntity()
                entity.membership = resolver.membership(realm,MembershipType.valueOf(dto.type),dto.cloudEntityId)
                entity.member = resolver.user(realm,dto.cloudMemberId)
            }
            dto.toRealm(entity)

        }
    }
}


// ---------------- REALM BASE REPOSITORY ----------------

interface RealmRepository<RE>
        where RE : TypedRealmObject, RE : Entity {

    val db: Realm
    val localClass: KClass<RE>

    fun entity(realm: Realm, id: String): RE? =
        realm.query(localClass, Filter.ENTITY.query, ObjectId(id))
            .first().find()

    fun entity(realm: MutableRealm, id: String): RE? =
        realm.query(localClass, Filter.ENTITY.query, ObjectId(id))
            .first().find()

    fun entityByCloudId(realm: Realm, cloudId: String): RE? =
        realm.query(localClass, Filter.REMOTE.query, cloudId)
            .first().find()

    fun entityByCloudId(realm: MutableRealm, cloudId: String): RE? =
        realm.query(localClass, Filter.REMOTE.query, cloudId)
            .first().find()

}


// ---------------- SYNCHRONIZABLE BASE ----------------

open class RealmSynchronizableRepository<RE, DTO, DOM>(
    override val db: Realm,
    private val mapper: SyncMapper<RE, DTO, DOM>,
    private val maker: () -> RE,
    override val localClass: KClass<RE>,
    private val transferClass: KClass<DTO>
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

    private var engine: SyncEngine? = null

    override fun toDomain(entity: RE): DOM = mapper.toDomain(entity)
    override fun toLocal(realm:MutableRealm,dto: DTO, entity: RE, isNew: Boolean) = mapper.toLocal(realm,dto, entity,isNew)
    override fun toDTO(entity: RE): DTO = mapper.toDTO(entity)

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
        db.write {
            entity(this, id)?.apply { delete() }
        }
        engine?.onLocalChange(id, transferClass)
    }

    override suspend fun delete(id: String) {
        db.write {
            entity(this, id)?.let { delete(findLatest(it)!!) }
        }
    }
    override suspend fun applyRemoteInsert(dto: DTO) {
        db.write {
            val entity = maker().apply { toLocal(this@write,dto, this,true) }
            copyToRealm(entity)
        }
    }

    override suspend fun applyRemoteUpdate(dto: DTO) {
        val local = entityByCloudId(db, dto.cloudId ?: return)
        if (local == null) {
            applyRemoteInsert(dto)
            return
        }

        db.write {
            val latest = findLatest(local) ?: return@write
            toLocal(this,dto, latest,false)
            latest.upToDate()
        }
    }

    override suspend fun applyRemoteDelete(dto: DTO) {
        db.write {
            entityByCloudId(this, dto.cloudId ?: return@write)?.let {
                delete(findLatest(it)!!)
            }
        }
    }

    override suspend fun applyCloudId(id: String, cloudId: String): DTO {
        lateinit var result: DTO
        db.write {
            DebugLogcatLogger.log("applying cloudId $cloudId => ${entity(this, id)?.id}")
            val local = entity(this, id) ?: return@write
            DebugLogcatLogger.log("apply cloudId $id => $cloudId")
            local.identity!!.cloudId = cloudId
            local.upToDate()
            result = toDTO(local)
        }
        return result
    }

    override fun attachToEngine(engine: SyncEngine) {
        this.engine = engine
    }

    override suspend fun getDTO(id: String): DTO? =
        entity(db, id)?.let(::toDTO)

    override suspend fun onLocalChange(id: String) {
        engine?.onLocalChange(id, transferClass)
    }

    override suspend fun getDTOUpdatedAfter(version: Long) =
        db.query(localClass,Filter.VERSION.query, version.toRealmInstant()).find()
            .map { toDTO(it) }

    override fun getId(dto: DTO): String =
        db.query(localClass,Filter.REMOTE.query,dto.cloudId)
            .first().find()!!.id.toHexString()

}


// ---------------- DECORATORS ----------------

abstract class RealmRepositoryDecorator<RE>(
    protected val core: RealmRepository<RE>
) : RealmRepository<RE> by core
        where RE : TypedRealmObject, RE : Entity


class OwnershipRepository<RE, DOM>(
    core: RealmRepository<RE>,
    private val mapper: DomainMapper<RE, DOM>
) : RealmRepositoryDecorator<RE>(core),
    IndexedByOwner<DOM>
        where RE : TypedRealmObject, RE : Entity, DOM : Owned,
              DOM : Identifiable {

    override fun of(id: String): Flow<List<DOM>> =
        core.db.query(core.localClass, Filter.OWNER.query, ObjectId(id))
            .find().asFlow()
            .map { it.list.map(mapper::toDomain) }
}


class ProjectOwnershipRepository<RE, DOM>(
    core: RealmRepository<RE>,
    private val mapper: DomainMapper<RE, DOM>
) : RealmRepositoryDecorator<RE>(core),
    IndexedByProject<DOM>
        where RE : TypedRealmObject, RE : Entity, DOM : ProjectOwned,
              DOM : Identifiable {

    override fun byProject(id: String): Flow<List<DOM>> =
        core.db.query(core.localClass, Filter.PROJECT.query, ObjectId(id))
            .find().asFlow()
            .map { it.list.map(mapper::toDomain) }
}


class LinkedRepository<RE, DOM>(
    core: RealmRepository<RE>,
    private val mapper: DomainMapper<RE, DOM>
) : RealmRepositoryDecorator<RE>(core),
    IndexedByLink<DOM>
        where RE : TypedRealmObject, RE : Entity, DOM : Linked,
              DOM : Identifiable {

    override fun linkedTo(id: String): Flow<List<DOM>> =
        core.db.query(core.localClass, Filter.LINK.query, ObjectId(id))
            .find().asFlow()
            .map { it.list.map(mapper::toDomain) }

    override fun linkedTo(ids: List<String>): Flow<List<DOM>> =
        core.db.query(core.localClass, Filter.LINKS.query, ids.map({it.toObjectId()}))
            .find().asFlow()
            .map { it.list.map(mapper::toDomain) }
}






