package com.example.on_track_app.data.dependencyInjections.builders

import com.example.on_track_app.data.abstractions.repositories.BasicById
import com.example.on_track_app.data.abstractions.repositories.EventRepository
import com.example.on_track_app.data.abstractions.repositories.GroupRepository
import com.example.on_track_app.data.abstractions.repositories.ProjectRepository
import com.example.on_track_app.data.abstractions.repositories.ReminderRepository
import com.example.on_track_app.data.abstractions.repositories.TaskRepository
import com.example.on_track_app.data.abstractions.repositories.UserRepository
import com.example.on_track_app.data.realm.entities.EventRealmEntity
import com.example.on_track_app.data.realm.entities.GroupRealmEntity
import com.example.on_track_app.data.realm.entities.ProjectRealmEntity
import com.example.on_track_app.data.realm.entities.ReminderRealmEntity
import com.example.on_track_app.data.realm.entities.TaskRealmEntity
import com.example.on_track_app.data.realm.entities.UserRealmEntity
import com.example.on_track_app.data.realm.repositories.SynchronizableRepository
import com.example.on_track_app.data.realm.repositories.SynchronizedReference
import com.example.on_track_app.data.realm.repositories.decorated.LinkedRepository
import com.example.on_track_app.data.realm.repositories.decorated.OwnershipRepository
import com.example.on_track_app.data.realm.repositories.decorated.ProjectOwnershipRepository
import com.example.on_track_app.data.realm.repositories.decorated.RealmEventRepository
import com.example.on_track_app.data.realm.repositories.decorated.RealmGroupRepository
import com.example.on_track_app.data.realm.repositories.decorated.RealmProjectRepository
import com.example.on_track_app.data.realm.repositories.decorated.RealmReminderRepository
import com.example.on_track_app.data.realm.repositories.decorated.RealmTaskRepository
import com.example.on_track_app.data.realm.repositories.decorated.RealmUserRepository
import com.example.on_track_app.data.realm.repositories.decorated.SyncMapper
import com.example.on_track_app.data.synchronization.EventDTO
import com.example.on_track_app.data.synchronization.GroupDTO
import com.example.on_track_app.data.synchronization.ProjectDTO
import com.example.on_track_app.data.synchronization.ReferenceIntegrityManager
import com.example.on_track_app.data.synchronization.ReminderDTO
import com.example.on_track_app.data.synchronization.SynchronizableDTO
import com.example.on_track_app.data.synchronization.TaskDTO
import com.example.on_track_app.data.synchronization.UserDTO
import com.example.on_track_app.model.Identifiable
import com.example.on_track_app.model.MockEvent
import com.example.on_track_app.model.MockGroup
import com.example.on_track_app.model.MockProject
import com.example.on_track_app.model.MockReminder
import com.example.on_track_app.model.MockTask
import com.example.on_track_app.model.MockUser
import io.realm.kotlin.Realm


data class RepositoryHandle<R: BasicById<T>,T: Identifiable,K: SynchronizableDTO>(
    val repo: R,
    val sync: SynchronizableRepository<K>? = null,
    val reference: SynchronizedReference? = null
)


class UserRepositoryBuilder(
    realm: Realm,
    mapper: SyncMapper<UserRealmEntity, UserDTO, MockUser>,
    integrityManager: ReferenceIntegrityManager
) : RealmRepoBuilder<UserRealmEntity, UserDTO, MockUser>(
    realm,
    mapper,
    { UserRealmEntity() },
    UserRealmEntity::class,
    UserDTO::class,
    integrityManager
) {

    fun build(): RepositoryHandle<UserRepository,MockUser,UserDTO> {
        val base = base()
        val repo = RealmUserRepository(
            core = base,
            mapper = base,
            sync = base,
            byId = base
        )
        return RepositoryHandle(
            repo = repo,
            sync = repo,
            reference = null
        )
    }
}

class GroupRepositoryBuilder(
    realm: Realm,
    mapper: SyncMapper<GroupRealmEntity, GroupDTO, MockGroup>,
    integrityManager: ReferenceIntegrityManager
) : RealmRepoBuilder<GroupRealmEntity, GroupDTO, MockGroup>(
    realm,
    mapper,
    { GroupRealmEntity() },
    GroupRealmEntity::class,
    GroupDTO::class,
    integrityManager
) {

    fun build(): RepositoryHandle<GroupRepository, MockGroup,GroupDTO> {
        val base = base()
        val owned = OwnershipRepository(core = base, mapper = base)

        val repo =  RealmGroupRepository(
            core = base,
            mapper = base,
            sync = base,
            byOwner = owned,
            integrityManager = integrityManager,
            byId = base,
        )
        return RepositoryHandle(
            repo = repo,
            sync = repo,
            reference = repo
        )
    }
}

class ProjectRepositoryBuilder(
    realm: Realm,
    mapper: SyncMapper<ProjectRealmEntity, ProjectDTO, MockProject>,
    integrityManager: ReferenceIntegrityManager
) : RealmRepoBuilder<ProjectRealmEntity, ProjectDTO, MockProject>(
    realm,
    mapper,
    { ProjectRealmEntity() },
    ProjectRealmEntity::class,
    ProjectDTO::class,
    integrityManager
) {

    fun build(): RepositoryHandle<ProjectRepository, MockProject, ProjectDTO> {
        val base = base()
        val owned = OwnershipRepository(core = base, mapper = base)

        val repo =  RealmProjectRepository(
            core = base,
            mapper = base,
            sync = base,
            byOwner = owned,
            integrityManager = integrityManager,
            byId = base
        )
        return RepositoryHandle(
            repo = repo,
            sync = repo,
            reference = repo
        )
    }
}

class TaskRepositoryBuilder(
    realm: Realm,
    mapper: SyncMapper<TaskRealmEntity, TaskDTO, MockTask>,
    integrityManager: ReferenceIntegrityManager
) : RealmRepoBuilder<TaskRealmEntity, TaskDTO, MockTask>(
    realm,
    mapper,
    { TaskRealmEntity() },
    TaskRealmEntity::class,
    TaskDTO::class,
    integrityManager
) {

    fun build(): RepositoryHandle<TaskRepository,MockTask, TaskDTO> {
        val base = base()
        val owned = OwnershipRepository(core = base, mapper = base)
        val projected = ProjectOwnershipRepository(core = base, mapper = base)

        val repo =  RealmTaskRepository(
            core = base,
            mapper = base,
            sync = base,
            byOwner = owned,
            byProject = projected,
            integrityManager = integrityManager,
            byId = base
        )
        return RepositoryHandle(
            repo = repo,
            sync = repo,
            reference = repo
        )
    }
}

class ReminderRepositoryBuilder(
    realm: Realm,
    mapper: SyncMapper<ReminderRealmEntity, ReminderDTO, MockReminder>,
    integrityManager: ReferenceIntegrityManager
) : RealmRepoBuilder<ReminderRealmEntity, ReminderDTO, MockReminder>(
    realm,
    mapper,
    { ReminderRealmEntity() },
    ReminderRealmEntity::class,
    ReminderDTO::class,
    integrityManager
) {

    fun build(): RepositoryHandle<ReminderRepository,MockReminder, ReminderDTO> {
        val base = base()
        val owned = OwnershipRepository(core = base, mapper = base)
        val linked = LinkedRepository(core = base, mapper = base)

        val repo =  RealmReminderRepository(
            core = base,
            mapper = base,
            sync = base,
            byOwner = owned,
            byLink = linked,
            integrityManager = integrityManager,
            byId = base
        )

        return RepositoryHandle(
            repo = repo,
            sync = repo,
            reference = repo
        )
    }
}


class EventRepositoryBuilder(
    realm: Realm,
    mapper: SyncMapper<EventRealmEntity, EventDTO, MockEvent>,
    integrityManager: ReferenceIntegrityManager
) : RealmRepoBuilder<EventRealmEntity, EventDTO, MockEvent>(
    realm,
    mapper,
    { EventRealmEntity() },
    EventRealmEntity::class,
    EventDTO::class,
    integrityManager
) {

    fun build(): RepositoryHandle<EventRepository,MockEvent,EventDTO>{
        val base = base()
        val owned = OwnershipRepository(core = base, mapper = base)
        val projected = ProjectOwnershipRepository(core = base, mapper = base)

        val repo =  RealmEventRepository(
            core = base,
            mapper = base,
            sync = base,
            byOwner = owned,
            byProject = projected,
            integrityManager = integrityManager,
            transferClass = EventDTO::class,
            byId = base
        )
        return RepositoryHandle(
            repo = repo,
            sync = repo,
            reference = repo
        )
    }
}

