package com.example.on_track_app.data.realm.repositories

import com.example.on_track_app.data.SoftDeleteByLinkPort
import com.example.on_track_app.data.SoftDeleteByMembershipPort
import com.example.on_track_app.data.SoftDeleteByOwnerPort
import com.example.on_track_app.data.SoftDeleteByProjectPort
import com.example.on_track_app.data.abstractions.repositories.BasicById
import com.example.on_track_app.data.abstractions.repositories.EventRepository
import com.example.on_track_app.data.abstractions.repositories.GroupRepository
import com.example.on_track_app.data.abstractions.repositories.IndexedByLink
import com.example.on_track_app.data.abstractions.repositories.IndexedByOwner
import com.example.on_track_app.data.abstractions.repositories.IndexedByProject
import com.example.on_track_app.data.abstractions.repositories.MembershipRepository
import com.example.on_track_app.data.abstractions.repositories.ProjectRepository
import com.example.on_track_app.data.abstractions.repositories.ReminderRepository
import com.example.on_track_app.data.abstractions.repositories.TaskRepository
import com.example.on_track_app.data.abstractions.repositories.UserRepository
import com.example.on_track_app.data.realm.entities.EventRealmEntity
import com.example.on_track_app.data.realm.entities.GroupRealmEntity
import com.example.on_track_app.data.realm.entities.MembershipRealmEntity
import com.example.on_track_app.data.realm.entities.ProjectRealmEntity
import com.example.on_track_app.data.realm.entities.ReminderRealmEntity
import com.example.on_track_app.data.realm.entities.SynchronizationEntity
import com.example.on_track_app.data.realm.entities.TaskRealmEntity
import com.example.on_track_app.data.realm.entities.TimeRealmEmbeddedObject
import com.example.on_track_app.data.realm.entities.UserRealmEntity
import com.example.on_track_app.data.realm.entities.toObjectId
import com.example.on_track_app.data.realm.entities.update
import com.example.on_track_app.data.realm.utils.SynchronizationState
import com.example.on_track_app.data.realm.utils.toRealmInstant
import com.example.on_track_app.data.synchronization.EventDTO
import com.example.on_track_app.data.synchronization.GroupDTO
import com.example.on_track_app.data.synchronization.MembershipDTO
import com.example.on_track_app.data.synchronization.ProjectDTO
import com.example.on_track_app.data.synchronization.ReminderDTO
import com.example.on_track_app.data.synchronization.TaskDTO
import com.example.on_track_app.data.synchronization.UserDTO
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Group
import com.example.on_track_app.model.Linkable
import com.example.on_track_app.model.Membership
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.Reminder
import com.example.on_track_app.model.Task
import com.example.on_track_app.model.TimeField
import com.example.on_track_app.model.User
import com.example.on_track_app.model.UserMembership
import com.example.on_track_app.utils.DebugLogcatLogger
import com.example.on_track_app.viewModels.OwnerContext
import io.realm.kotlin.Realm
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant


class RealmUserRepository(
    private val db: Realm,
    private val core: RealmSynchronizableRepository<
            UserRealmEntity,
            UserDTO,
            User
            >
) : UserRepository,
    BasicById<User> by core,
    SynchronizableRepository<UserDTO> by core {

    override suspend fun addUser(
        username: String,
        email: String
    ): String {
        lateinit var id: String

        db.write {
            val entity = UserRealmEntity().apply {
                identity = SynchronizationEntity()
                name = username
                this.email = email
                identity!!.synchronizationStatus = SynchronizationState.CREATED.name
                identity!!.version = RealmInstant.now()
            }
            val copy = copyToRealm(entity)
            id = copy.id.toHexString()
        }

        core.onLocalChange(id)
        return id
    }

    override suspend fun updateUser(id: String, newEmail: String) {
        db.write {
            core.entity(this, id)?.apply {
                email = newEmail
                update()
            }
        }
        core.onLocalChange(id)
    }
}

class RealmGroupRepository(
    private val db: Realm,
    private val core: RealmSynchronizableRepository<
            GroupRealmEntity,
            GroupDTO,
            Group
            >,
    private val resolver: ReferenceResolver
) : GroupRepository,
    BasicById<Group> by core,
    IndexedByOwner<Group> by OwnershipRepository(core, core),
    SoftDeleteByOwnerPort by OwnershipGarbageCollectorRepository(core),
    SynchronizableRepository<GroupDTO> by core {

    override suspend fun addGroup(
        name: String,
        description: String,
        owner: OwnerContext
    ): String {
        lateinit var id: String

        db.write {
            val entity = GroupRealmEntity().apply {
                identity = SynchronizationEntity()
                this.name = name
                this.description = description
                identity!!.synchronizationStatus = SynchronizationState.CREATED.name
                identity!!.version = RealmInstant.now()
            }
            val copy = copyToRealm(entity)
            copy.owner = resolver.owner(this@write,owner)
            id = copy.id.toHexString()
        }

        core.onLocalChange(id)
        return id
    }

    override suspend fun update(updated: Group) {
        db.write {
            core.entity(this, updated.id)?.apply {
                name = updated.name
                description = updated.description
                update()
            }
        }
        core.onLocalChange(updated.id)
    }
}

class RealmProjectRepository(
    private val db: Realm,
    private val core: RealmSynchronizableRepository<
            ProjectRealmEntity,
            ProjectDTO,
            Project
            >,
    private val resolver: ReferenceResolver
) : ProjectRepository,
    BasicById<Project> by core,
    IndexedByOwner<Project> by OwnershipRepository(core, core),
    SoftDeleteByOwnerPort by OwnershipGarbageCollectorRepository(core),
    SynchronizableRepository<ProjectDTO> by core {

    override suspend fun addProject(
        name: String,
        description: String,
        owner: OwnerContext
    ): String {
        lateinit var id: String

        db.write {
            val entity = ProjectRealmEntity().apply {
                identity = SynchronizationEntity()
                this.name = name
                this.description = description
                identity!!.synchronizationStatus = SynchronizationState.CREATED.name
                identity!!.version = RealmInstant.now()
            }
            val copy = copyToRealm(entity)
            copy.owner = resolver.owner(this@write,owner)
            id = copy.id.toHexString()
        }

        core.onLocalChange(id)
        return id
    }

    override suspend fun update(updated: Project) {
        db.write {
            core.entity(this, updated.id)?.apply {
                name = updated.name
                description = updated.description
                update()
            }
        }
        core.onLocalChange(updated.id)
    }
}

class RealmMembershipRepository(
    private val db: Realm,
    private val core: RealmSynchronizableRepository<
            MembershipRealmEntity,
            MembershipDTO,
            UserMembership
            >,
    private val resolver: ReferenceResolver
) : MembershipRepository,
    BasicById<UserMembership> by core,
    SoftDeleteByMembershipPort by MembershipsGarbageCollectorRepository(core),
    SynchronizableRepository<MembershipDTO> by core {

    // --------------------------------------------------
    // CREATE
    // --------------------------------------------------

    override suspend fun addMembership(
        user: User,
        membership: Membership
    ): String {

        lateinit var id: String

        db.write {
            val entity = MembershipRealmEntity().apply {
                identity = SynchronizationEntity()
                identity!!.synchronizationStatus = SynchronizationState.CREATED.name
                identity!!.version = RealmInstant.now()
            }

            val copy = copyToRealm(entity)
            copy.member = resolver.user(this@write,user)
            copy.membership = resolver.membership(this@write,membership)
            id = copy.id.toHexString()
        }

        core.onLocalChange(id)
        return id
    }

    // --------------------------------------------------
    // OPTIONAL: queries específicas del dominio
    // --------------------------------------------------

    override fun byMember(user: User): Flow<List<UserMembership>> =
        db.uiQuery(
            MembershipRealmEntity::class,
            Filter.MEMBERSHIP_MEMBER,
            user.id.toObjectId()
        )
            .find()
            .asFlow()
            .map { it.list.map(core::toDomain) }

    override fun byMembership(membership: Membership): Flow<List<UserMembership>> =
        db.uiQuery(
            MembershipRealmEntity::class,
            Filter.MEMBERSHIP_ENTITY,
            membership.id.toObjectId()
        )
            .find()
            .asFlow()
            .map { it.list.map(core::toDomain) }
}


class RealmTaskRepository(
    private val db: Realm,
    private val core: RealmSynchronizableRepository<
            TaskRealmEntity,
            TaskDTO,
            Task
            >,
    private val resolver: ReferenceResolver
) : TaskRepository,
    BasicById<Task> by core,
    IndexedByOwner<Task> by OwnershipRepository(core, core),
    IndexedByProject<Task> by ProjectOwnershipRepository(core, core),
    SoftDeleteByOwnerPort by OwnershipGarbageCollectorRepository(core),
    SoftDeleteByProjectPort by ProjectGarbageCollectorRepository(core),
    SynchronizableRepository<TaskDTO> by core {

    override suspend fun addTask(
        name: String,
        description: String,
        date: TimeField,
        owner: OwnerContext,
        project: Project?
    ): String {
        lateinit var id: String

        db.write {
            val entity = TaskRealmEntity().apply {
                identity = SynchronizationEntity()
                due = TimeRealmEmbeddedObject()
                this.name = name
                this.description = description
                due!!.instant = date.instant.toRealmInstant()
                due!!.timed = date.timed
                identity!!.synchronizationStatus = SynchronizationState.CREATED.name
                identity!!.version = RealmInstant.now()
            }
            DebugLogcatLogger.log(" created ")

            val copy = copyToRealm(entity)
            copy.owner = resolver.owner(this@write,owner)
            DebugLogcatLogger.log(" resolved project ${copy.owner?.user?.name}.")
            copy.project = resolver.project(this@write,project)
            DebugLogcatLogger.log(" resolved project ${copy.project?.project?.name}.")

            id = copy.id.toHexString()
        }

        core.onLocalChange(id)
        return id
    }

    override suspend fun update(updated: Task) {
        db.write {
            core.entity(this, updated.id)?.apply {
                name = updated.name
                description = updated.description
                project = resolver.project(this@write,updated.project)
                due!!.instant = updated.due.instant.toRealmInstant()
                due!!.timed = updated.due.timed
                update()
            }
        }
        core.onLocalChange(updated.id)
    }


    override fun between(
        start: Instant,
        end: Instant
    ): Flow<List<Task>> =
        core.db.uiQuery(core.localClass, Filter.TASK_DUE_IN, start.toRealmInstant(), end.toRealmInstant())
            .find()
            .asFlow()
            .map { it.list.map(core::toDomain) }

    override fun byGroupAndInterval(
        group: String,
        start: Instant,
        end: Instant
    ): Flow<List<Task>> =
        core.db.uiQuery(
            core.localClass,
            Filter.GROUP_TASK_DUE_IN,
            group,
            start.toRealmInstant(),
            end.toRealmInstant()
        )
            .find()
            .asFlow()
            .map { it.list.map(core::toDomain) }

    override fun byProjectAndInterval(
        project: String,
        start: Instant,
        end: Instant
    ): Flow<List<Task>> =
        core.db.uiQuery(core.localClass, Filter.PROJECT_TASK_DUE_IN, start.toRealmInstant(), end.toRealmInstant())
            .find()
            .asFlow()
            .map { it.list.map(core::toDomain) }
}

class RealmEventRepository(
    private val db: Realm,
    private val core: RealmSynchronizableRepository<
            EventRealmEntity,
            EventDTO,
            Event
            >,
    private val resolver: ReferenceResolver
) : EventRepository,
    BasicById<Event> by core,
    IndexedByOwner<Event> by OwnershipRepository(core, core),
    IndexedByProject<Event> by ProjectOwnershipRepository(core, core),
    SoftDeleteByOwnerPort by OwnershipGarbageCollectorRepository(core),
    SoftDeleteByProjectPort by ProjectGarbageCollectorRepository(core),
    SynchronizableRepository<EventDTO> by core {

    override suspend fun addEvent(
        name: String,
        description: String,
        start: TimeField,
        end: TimeField,
        owner: OwnerContext,
        project: Project?
    ): String {
        lateinit var id: String

        db.write {
            val entity = EventRealmEntity().apply {
                this.start = TimeRealmEmbeddedObject()
                this.end = TimeRealmEmbeddedObject()
                identity = SynchronizationEntity()
                this.name = name
                this.description = description
                this.start!!.instant = start.instant.toRealmInstant()
                this.start!!.timed = start.timed
                this.end!!.instant = end.instant.toRealmInstant()
                this.end!!.timed = end.timed
                identity!!.synchronizationStatus = SynchronizationState.CREATED.name
                identity!!.version = RealmInstant.now()
            }
            val copy = copyToRealm(entity)
            copy.owner = resolver.owner(this@write,owner)
            copy.project = resolver.project(this@write,project)
            id = copy.id.toHexString()
        }

        core.onLocalChange(id)
        return id
    }

    override suspend fun update(updated: Event) {
        db.write {
            core.entity(this, updated.id)?.apply {
                name = updated.name
                description = updated.description
                project = resolver.project(this@write,updated.project)
                start!!.instant = updated.start.instant.toRealmInstant()
                start!!.timed = updated.start.timed
                end!!.instant = updated.end.instant.toRealmInstant()
                end!!.timed = updated.end.timed
                update()
            }
        }
        core.onLocalChange(updated.id)
    }

    override fun between(
        start: Instant,
        end: Instant
    ): Flow<List<Event>> =
        core.db.uiQuery(core.localClass, Filter.EVENT_IN, start.toRealmInstant(), end.toRealmInstant())
            .find()
            .asFlow()
            .map { it.list.map(core::toDomain) }

    override fun byGroupAndInterval(
        group: String,
        start: Instant,
        end: Instant
    ): Flow<List<Event>> =
        core.db.uiQuery(core.localClass, Filter.GROUP_EVENT_IN,group.toObjectId(), start.toRealmInstant(), end.toRealmInstant())
            .find()
            .asFlow()
            .map { it.list.map(core::toDomain) }

    override fun byProjectAndInterval(
        project: String,
        start: Instant,
        end: Instant
    ): Flow<List<Event>> =
        core.db.uiQuery(core.localClass, Filter.PROJECT_EVENT_IN, project.toObjectId(),start.toRealmInstant(), end.toRealmInstant())
            .find()
            .asFlow()
            .map { it.list.map(core::toDomain) }
}

class RealmReminderRepository(
    private val db: Realm,
    private val core: RealmSynchronizableRepository<
            ReminderRealmEntity,
            ReminderDTO,
            Reminder
            >,
    private val resolver: ReferenceResolver
) : ReminderRepository,
    BasicById<Reminder> by core,
    IndexedByOwner<Reminder> by OwnershipRepository(core, core),
    IndexedByLink<Reminder> by LinkedRepository(core, core),
    SoftDeleteByOwnerPort by OwnershipGarbageCollectorRepository(core),
    SoftDeleteByLinkPort by LinkedGarbageCollectorRepository(core),
    SynchronizableRepository<ReminderDTO> by core {

    override suspend fun addReminder(
        name: String,
        description: String,
        at: TimeField,
        owner: OwnerContext,
        linkedTo: Linkable?
    ): String {
        lateinit var id: String

        db.write {
            val entity = ReminderRealmEntity().apply {
                identity = SynchronizationEntity()
                this.at = TimeRealmEmbeddedObject()
                this.name = name
                this.description = description
                this.at!!.instant = at.instant.toRealmInstant()
                this.at!!.timed = at.timed
                identity!!.synchronizationStatus = SynchronizationState.CREATED.name
                identity!!.version = RealmInstant.now()
            }
            val copy = copyToRealm(entity)
            copy.owner = resolver.owner(this@write,owner)
            copy.linkedTo = resolver.link(this@write,linkedTo)
            id = copy.id.toHexString()
        }

        core.onLocalChange(id)
        return id
    }

    override suspend fun update(updated: Reminder) {
        db.write {
            core.entity(this, updated.id)?.apply {
                name = updated.name
                description = updated.description
                linkedTo = resolver.link(this@write,updated.linkedTo)
                at!!.instant = updated.at.instant.toRealmInstant()
                at!!.timed = updated.at.timed
                update()
            }
        }
        core.onLocalChange(updated.id)
    }

    override fun between(
        start: Instant,
        end: Instant
    ): Flow<List<Reminder>> =
        core.db.uiQuery(core.localClass, Filter.REMINDER_IN, start.toRealmInstant(), end.toRealmInstant())
            .find()
            .asFlow()
            .map { it.list.map(core::toDomain) }

    override fun byLinkAndInterval(
        links: List<String>,
        start: Instant,
        end: Instant
    ): Flow<List<Reminder>> =
        core.db.uiQuery(core.localClass, Filter.LINKED_REMINDER_IN,links, start.toRealmInstant(), end.toRealmInstant())
            .find()
            .asFlow()
            .map { it.list.map(core::toDomain) }
}

