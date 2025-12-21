package com.example.on_track_app.data.di

import com.example.on_track_app.data.GarbageCollector
import com.example.on_track_app.data.SoftDeleteByLinkPort
import com.example.on_track_app.data.SoftDeleteByOwnerPort
import com.example.on_track_app.data.SoftDeleteByProjectPort
import com.example.on_track_app.data.abstractions.repositories.BasicById
import com.example.on_track_app.data.abstractions.repositories.EventRepository
import com.example.on_track_app.data.abstractions.repositories.ProjectRepository
import com.example.on_track_app.data.abstractions.repositories.ReminderRepository
import com.example.on_track_app.data.abstractions.repositories.SyncRepository
import com.example.on_track_app.data.abstractions.repositories.TaskRepository
import com.example.on_track_app.data.auth.AuthClient
import com.example.on_track_app.data.firebase.FirestoreService
import com.example.on_track_app.data.firebase.FirestoreSyncRepository
import com.example.on_track_app.data.realm.RealmDatabase
import com.example.on_track_app.data.realm.entities.EventRealmEntity
import com.example.on_track_app.data.realm.entities.GroupRealmEntity
import com.example.on_track_app.data.realm.entities.MembershipRealmEntity
import com.example.on_track_app.data.realm.entities.ProjectRealmEntity
import com.example.on_track_app.data.realm.entities.ReminderRealmEntity
import com.example.on_track_app.data.realm.entities.SynchronizableEntity
import com.example.on_track_app.data.realm.entities.TaskRealmEntity
import com.example.on_track_app.data.realm.entities.UserRealmEntity
import com.example.on_track_app.data.realm.repositories.LocalConfigRepository
import com.example.on_track_app.data.realm.repositories.RealmEventRepository
import com.example.on_track_app.data.realm.repositories.RealmGroupRepository
import com.example.on_track_app.data.realm.repositories.RealmMembershipRepository
import com.example.on_track_app.data.realm.repositories.RealmProjectRepository
import com.example.on_track_app.data.realm.repositories.RealmReferenceResolver
import com.example.on_track_app.data.realm.repositories.RealmReminderRepository
import com.example.on_track_app.data.realm.repositories.RealmSyncMapperFactory
import com.example.on_track_app.data.realm.repositories.RealmTaskRepository
import com.example.on_track_app.data.realm.repositories.RealmUserRepository
import com.example.on_track_app.data.realm.repositories.SynchronizableRepository
import com.example.on_track_app.data.synchronization.ConnectivityProvider
import com.example.on_track_app.data.synchronization.EventDTO
import com.example.on_track_app.data.synchronization.GroupDTO
import com.example.on_track_app.data.synchronization.MembershipDTO
import com.example.on_track_app.data.synchronization.ProjectDTO
import com.example.on_track_app.data.synchronization.ReminderDTO
import com.example.on_track_app.data.synchronization.SyncEngine
import com.example.on_track_app.data.synchronization.SyncRepositoryEntry
import com.example.on_track_app.data.synchronization.SyncRepositoryFactory
import com.example.on_track_app.data.synchronization.SynchronizableDTO
import com.example.on_track_app.data.synchronization.TaskDTO
import com.example.on_track_app.data.synchronization.UserDTO
import com.example.on_track_app.model.CloudIdentifiable
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Group
import com.example.on_track_app.model.Identifiable
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.Reminder
import com.example.on_track_app.model.Task
import com.example.on_track_app.model.User
import com.example.on_track_app.model.UserMembership
import com.example.on_track_app.utils.DebugLogcatLogger
import com.example.on_track_app.utils.SettingsDataStore
import com.example.on_track_app.viewModels.CreationSourcesViewModel
import com.example.on_track_app.viewModels.CreationViewModel
import com.example.on_track_app.viewModels.factory.ViewModelsFactory
import com.example.on_track_app.viewModels.login.LoginViewModel
import com.example.on_track_app.viewModels.main.AgendaViewModel
import com.example.on_track_app.viewModels.main.CalendarViewModel
import com.example.on_track_app.viewModels.main.HomeViewModel
import com.example.on_track_app.viewModels.raw.ProjectsViewModel
import com.example.on_track_app.viewModels.raw.RemindersViewModel
import com.example.on_track_app.viewModels.raw.TasksViewModel
import io.realm.kotlin.Realm
import io.realm.kotlin.types.RealmObject
import kotlinx.coroutines.CoroutineScope
import kotlin.reflect.KClass

/* ============================================================
 *  SEMANTIC DI CORE — basado en Catalog + repetición explícita
 *
 *  Este archivo define:
 *   - RepositoryHandle
 *   - CatalogBinding
 *   - RemoteRepositoryFactory
 *   - CatalogBindingsBuilder 
 *   - ViewModelsFactoryBuilder
 *   - SyncEngineBuilder
 *
 *  NO incluye:
 *   - Catalogs (USER_CATALOG, GROUP_CATALOG, ...)
 *   - Mappers  (USER_MAPPER, GROUP_MAPPER, ...)
 *
 * ============================================================ */

/* ============================================================
 *  SEMANTIC DI CORE — FULL DOMAIN
 * ============================================================ */


/* ------------------------------------------------------------
 * RepositoryHandle
 * ------------------------------------------------------------ */

data class RepositoryHandle<R, DOM: Identifiable, DTO>(
    val repo: R,
    val sync: SynchronizableRepository<DTO>
) where
R : BasicById<DOM>,
DTO : SynchronizableDTO


/* ------------------------------------------------------------
 * CatalogBinding — núcleo semántico
 * ------------------------------------------------------------ */

data class CatalogBinding<DOM, LOCAL, DTO>(
    val catalog: Catalog<DOM, LOCAL, DTO>,
    val domainRepo: BasicById<DOM>,
    val syncRepo: SynchronizableRepository<DTO>,
    val remoteRepo: SyncRepository<DTO>
) where
DOM : Identifiable,
DOM : CloudIdentifiable,
LOCAL : RealmObject,
LOCAL : SynchronizableEntity,
DTO : SynchronizableDTO


/* ------------------------------------------------------------
 * RemoteRepositoryFactory
 * ------------------------------------------------------------ */

class RemoteRepositoryFactory {

    fun user(): SyncRepository<UserDTO> =
        FirestoreSyncRepository(
            db = FirestoreService.firestore,
            collectionName = "Users",
            clazz = UserDTO::class.java
        )

    fun group(): SyncRepository<GroupDTO> =
        FirestoreSyncRepository(
            db = FirestoreService.firestore,
            collectionName = "Groups",
            clazz = GroupDTO::class.java
        )

    fun project(): SyncRepository<ProjectDTO> =
        FirestoreSyncRepository(
            db = FirestoreService.firestore,
            collectionName = "Projects",
            clazz = ProjectDTO::class.java
        )

    fun membership(): SyncRepository<MembershipDTO> =
        FirestoreSyncRepository(
            db = FirestoreService.firestore,
            collectionName = "Memberships",
            clazz = MembershipDTO::class.java
        )

    fun task(): SyncRepository<TaskDTO> =
        FirestoreSyncRepository(
            db = FirestoreService.firestore,
            collectionName = "Tasks",
            clazz = TaskDTO::class.java
        )

    fun event(): SyncRepository<EventDTO> =
        FirestoreSyncRepository(
            db = FirestoreService.firestore,
            collectionName = "Events",
            clazz = EventDTO::class.java
        )

    fun reminder(): SyncRepository<ReminderDTO> =
        FirestoreSyncRepository(
            db = FirestoreService.firestore,
            collectionName = "Reminders",
            clazz = ReminderDTO::class.java
        )
}



/* ------------------------------------------------------------
 * CatalogBindingsBuilder — FULL DOMAIN
 * ------------------------------------------------------------ */

class CatalogBindingsFactory(
val gc: GarbageCollector
) {
    private val realm: Realm = RealmDatabase.realm
    private val referenceResolver: RealmReferenceResolver = RealmReferenceResolver()
    private val remoteFactory: RemoteRepositoryFactory = RemoteRepositoryFactory()

    private val mappers = RealmSyncMapperFactory(referenceResolver)


    val user: CatalogBinding<User, UserRealmEntity, UserDTO> by lazy {
        val handle =
            GenericRepositoryBuilder(
                realm,
                USER_CATALOG,
                mappers.user,
                { UserRealmEntity() },
                gc,
            ) { core ->
                gc.subscribe(User::class, core)
                RealmUserRepository(realm,core)
            }.build()

        CatalogBinding(USER_CATALOG, handle.repo, handle.sync, remoteFactory.user())
    }

    val group: CatalogBinding<Group, GroupRealmEntity, GroupDTO> by lazy {
        val handle =
            GenericRepositoryBuilder(
                realm,
                GROUP_CATALOG,
                mappers.group,
                { GroupRealmEntity() },
                gc,
            ) { core ->
                gc.subscribe(Group::class, core)
                val realmGroupRepository = RealmGroupRepository(realm, core, referenceResolver)
                gc.subscribe(Group::class, realmGroupRepository)
                realmGroupRepository
            }.build()

        CatalogBinding(GROUP_CATALOG, handle.repo, handle.sync, remoteFactory.group())
    }

    val project: CatalogBinding<Project, ProjectRealmEntity, ProjectDTO> by lazy {
        val handle =
            GenericRepositoryBuilder(
                realm,
                PROJECT_CATALOG,
                mappers.project,
                { ProjectRealmEntity() },
                gc,
            ) { core ->
                val realmProjectRepository = RealmProjectRepository(realm, core, referenceResolver)
                gc.subscribe(Project::class, realmProjectRepository)
                gc.subscribe(Project::class, core)
                realmProjectRepository
            }.build()

        CatalogBinding(PROJECT_CATALOG, handle.repo, handle.sync, remoteFactory.project())
    }

    val membership: CatalogBinding<UserMembership, MembershipRealmEntity, MembershipDTO> by lazy {
        val handle =
            GenericRepositoryBuilder(
                realm,
                MEMBERSHIP_CATALOG,
                mappers.membership,
                { MembershipRealmEntity() },
                gc,
            ) { core ->
                val realmMembershipRepository =
                    RealmMembershipRepository(realm, core, referenceResolver)
                gc.subscribe(realmMembershipRepository)
                gc.subscribe(UserMembership::class, core)
                realmMembershipRepository
            }.build()

        CatalogBinding(MEMBERSHIP_CATALOG, handle.repo, handle.sync, remoteFactory.membership())
    }

    val task: CatalogBinding<Task, TaskRealmEntity, TaskDTO> by lazy {
        val handle =
            GenericRepositoryBuilder(
                realm,
                TASK_CATALOG,
                mappers.task,
                { TaskRealmEntity() },
                gc,
            ) { core ->
                val realmTaskRepository = RealmTaskRepository(realm, core, referenceResolver)
                gc.subscribe(Task::class, realmTaskRepository as SoftDeleteByOwnerPort)
                gc.subscribe(Task::class, realmTaskRepository as SoftDeleteByProjectPort)
                gc.subscribe(Task::class, core)
                realmTaskRepository
            }.build()

        CatalogBinding(TASK_CATALOG, handle.repo, handle.sync, remoteFactory.task())
    }

    val event: CatalogBinding<Event, EventRealmEntity, EventDTO> by lazy {
        val handle =
            GenericRepositoryBuilder(
                realm,
                EVENT_CATALOG,
                mappers.event,
                { EventRealmEntity() },
                gc,
            ) { core ->
                val realmEventRepository = RealmEventRepository(realm, core, referenceResolver)
                gc.subscribe(Event::class, realmEventRepository as SoftDeleteByOwnerPort)
                gc.subscribe(Event::class, realmEventRepository as SoftDeleteByProjectPort)
                gc.subscribe(Event::class, core)
                realmEventRepository
            }.build()

        CatalogBinding(EVENT_CATALOG, handle.repo, handle.sync, remoteFactory.event())
    }

    val reminder: CatalogBinding<Reminder, ReminderRealmEntity, ReminderDTO> by lazy {
        val handle =
            GenericRepositoryBuilder(
                realm,
                REMINDER_CATALOG,
                mappers.reminder,
                { ReminderRealmEntity() },
                gc,
            ) { core ->
                val realmReminderRepository =
                    RealmReminderRepository(realm, core, referenceResolver)
                gc.subscribe(Reminder::class, core)
                gc.subscribe(Reminder::class, realmReminderRepository as SoftDeleteByOwnerPort)
                gc.subscribe(Task::class, realmReminderRepository as SoftDeleteByLinkPort)
                realmReminderRepository
            }.build()

        CatalogBinding(REMINDER_CATALOG, handle.repo, handle.sync, remoteFactory.reminder())
    }

    fun all(): List<CatalogBinding<*, *, *>> =
        listOf(user, group, project, membership, task, event, reminder)
}


/* ------------------------------------------------------------
 * ViewModelsFactoryBuilder
 * ------------------------------------------------------------ */



class ViewModelsFactoryBuilder(
    private val bindings: CatalogBindingsFactory
) {

    fun build(): ViewModelsFactory =
        ViewModelsFactory(
            mapOf(

                ProjectsViewModel::class to
                        ViewModelsFactory.FactoryEntry(
                            ProjectsViewModel::class.java
                        ) {
                            ProjectsViewModel(bindings.project.domainRepo as ProjectRepository)
                        },

                TasksViewModel::class to
                        ViewModelsFactory.FactoryEntry(
                            TasksViewModel::class.java
                        ) {
                            TasksViewModel(
                                bindings.task.domainRepo as TaskRepository,
                                bindings.project.domainRepo as ProjectRepository
                            )
                        },


                RemindersViewModel::class to
                        ViewModelsFactory.FactoryEntry(
                            RemindersViewModel::class.java
                        ) {
                            RemindersViewModel(
                                bindings.reminder.domainRepo as ReminderRepository,
                                bindings.task.domainRepo as TaskRepository,
                                bindings.event.domainRepo as EventRepository
                            )
                        },

                CalendarViewModel::class to
                        ViewModelsFactory.FactoryEntry(
                            CalendarViewModel::class.java
                        ) {
                            CalendarViewModel(
                                bindings.event.domainRepo as EventRepository,
                                bindings.task.domainRepo as TaskRepository
                            )
                        },

                AgendaViewModel::class to
                        ViewModelsFactory.FactoryEntry(
                            AgendaViewModel::class.java
                        ) {
                            AgendaViewModel(
                                bindings.event.domainRepo as EventRepository,
                                bindings.task.domainRepo as TaskRepository,
                                bindings.project.domainRepo as ProjectRepository
                            )
                        },

                HomeViewModel::class to
                        ViewModelsFactory.FactoryEntry(
                            HomeViewModel::class.java
                        ) {
                            HomeViewModel(
                                bindings.task.domainRepo as TaskRepository,
                                bindings.event.domainRepo as EventRepository,
                                bindings.project.domainRepo as ProjectRepository
                            )
                        },
                CreationViewModel::class to
                    ViewModelsFactory.FactoryEntry(CreationViewModel::class.java ){
                        CreationViewModel(
                            bindings.project.domainRepo as ProjectRepository,
                            bindings.event.domainRepo as EventRepository,
                            bindings.task.domainRepo as TaskRepository,
                            bindings.reminder.domainRepo as ReminderRepository
                        )
                    },
                CreationSourcesViewModel::class to
                        ViewModelsFactory.FactoryEntry(CreationSourcesViewModel::class.java ){
                            CreationSourcesViewModel(
                                bindings.project.domainRepo as ProjectRepository,
                                bindings.event.domainRepo as EventRepository,
                                bindings.task.domainRepo as TaskRepository,
                            )
                        }
            )
        )
}


/* ------------------------------------------------------------
 * SyncEngineBuilder — ordenado por DOM usando Catalog
 * ------------------------------------------------------------ */

class SyncEngineBuilder(
    private val domainOrder: List<KClass<out Identifiable>>,
    private val scope: CoroutineScope,
    private val connectivity: ConnectivityProvider,
    private val settings: SettingsDataStore,
    catalogBinding: CatalogBindingsFactory,
) {
    val bindings = catalogBinding.all()

    fun build(): SyncEngine {

        val ordered =
            domainOrder.map { dom ->
                bindings.first { it.catalog.domain == dom }
            }
        @Suppress("UNCHECKED_CAST")
        val entries =
            ordered.map { binding ->
                SyncRepositoryEntry(
                    dtoClass = binding.catalog.dto as KClass<SynchronizableDTO>,
                    local = binding.syncRepo       as SynchronizableRepository<SynchronizableDTO>,
                    remote = binding.remoteRepo    as SyncRepository<SynchronizableDTO>
                )
            }

        return SyncEngine(
            factory = SyncRepositoryFactory(
                entries = entries,
                order = ordered.map { it.catalog.dto }
            ),
            scope = scope,
            connectivity = connectivity,
            settings = settings
        )
    }
}


fun authViewmodelBuilder(auth: AuthClient, local: LocalConfigRepository, sync:SyncEngine, catalogBindingsFactory: CatalogBindingsFactory) = {
    LoginViewModel(
        local,
        auth,
        sync
    ) { dto -> catalogBindingsFactory.user.remoteRepo.push(dto.cloudId!!, dto)
        DebugLogcatLogger.logDTOToRemote(dto)
    }
}

fun authCheck(auth: AuthClient,local: LocalConfigRepository, catalogBindingsFactory: CatalogBindingsFactory) = {
    DebugLogcatLogger.log("Authentication check")
    DebugLogcatLogger.log("Local config: ${local.ready()}")
    DebugLogcatLogger.log("auth config: ${auth.getUser()}")
    if(local.ready()) catalogBindingsFactory.user.domainRepo.getById(local.get().user.id)
    else auth.getUser()
}
