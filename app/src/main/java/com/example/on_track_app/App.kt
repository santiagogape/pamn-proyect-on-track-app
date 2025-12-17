package com.example.on_track_app


import android.app.Application
import com.example.on_track_app.data.abstractions.repositories.UniqueRepository
import com.example.on_track_app.data.auth.GoogleAuthClient
import com.example.on_track_app.data.dependencyInjections.builders.RepositoryBuilderFactory
import com.example.on_track_app.data.firebase.FirestoreService
import com.example.on_track_app.data.firebase.FirestoreSyncRepository
import com.example.on_track_app.data.realm.RealmDatabase
import com.example.on_track_app.data.realm.entities.EventRealmEntity
import com.example.on_track_app.data.realm.entities.GroupRealmEntity
import com.example.on_track_app.data.realm.entities.ProjectRealmEntity
import com.example.on_track_app.data.realm.entities.RealmMembershipEntity
import com.example.on_track_app.data.realm.entities.ReminderRealmEntity
import com.example.on_track_app.data.realm.entities.TaskRealmEntity
import com.example.on_track_app.data.realm.entities.UserRealmEntity
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.data.realm.repositories.Filter
import com.example.on_track_app.data.realm.repositories.LocalConfigRepository
import com.example.on_track_app.data.realm.repositories.RealmMembershipRepository
import com.example.on_track_app.data.realm.repositories.decorated.SyncMapper
import com.example.on_track_app.data.synchronization.EventDTO
import com.example.on_track_app.data.synchronization.GroupDTO
import com.example.on_track_app.data.synchronization.MembershipDTO
import com.example.on_track_app.data.synchronization.ProjectDTO
import com.example.on_track_app.data.synchronization.ReferenceIntegrityManager
import com.example.on_track_app.data.synchronization.ReferenceIntegrityManagerEntry
import com.example.on_track_app.data.synchronization.ReminderDTO
import com.example.on_track_app.data.synchronization.SyncEngine
import com.example.on_track_app.data.synchronization.SyncRepositoryEntry
import com.example.on_track_app.data.synchronization.SyncRepositoryFactory
import com.example.on_track_app.data.synchronization.SynchronizableDTO
import com.example.on_track_app.data.synchronization.TaskDTO
import com.example.on_track_app.data.synchronization.UserDTO
import com.example.on_track_app.data.synchronization.toDTO
import com.example.on_track_app.data.synchronization.toRealm
import com.example.on_track_app.model.LocalConfigurations
import com.example.on_track_app.model.Membership
import com.example.on_track_app.model.MockEvent
import com.example.on_track_app.model.MockGroup
import com.example.on_track_app.model.MockProject
import com.example.on_track_app.model.MockReminder
import com.example.on_track_app.model.MockTask
import com.example.on_track_app.model.MockUser
import com.example.on_track_app.utils.DebugLogcatLogger
import com.example.on_track_app.utils.OwnershipContext
import com.example.on_track_app.viewModels.CreationViewModel
import com.example.on_track_app.viewModels.factory.ViewModelsFactoryMock
import com.example.on_track_app.viewModels.login.LoginViewModel
import com.example.on_track_app.viewModels.main.AgendaViewModel
import com.example.on_track_app.viewModels.main.CalendarViewModel
import com.example.on_track_app.viewModels.main.HomeViewModel
import com.example.on_track_app.viewModels.raw.EventsViewModel
import com.example.on_track_app.viewModels.raw.GroupsViewModel
import com.example.on_track_app.viewModels.raw.MembershipsViewModel
import com.example.on_track_app.viewModels.raw.ProjectsViewModel
import com.example.on_track_app.viewModels.raw.RemindersViewModel
import com.example.on_track_app.viewModels.raw.TasksViewModel
import com.example.on_track_app.viewModels.raw.UsersViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class OnTrackApp : Application() {


    lateinit var viewModelsFactory: ViewModelsFactoryMock
    lateinit var applicationScope: CoroutineScope

    lateinit var localConfig: UniqueRepository<LocalConfigurations>

    lateinit var currentOwnership: OwnershipContext
    lateinit var authViewModelFactory: ()-> LoginViewModel
    lateinit var authenticationCheck: () -> MockUser?


    override fun onCreate() {
        super.onCreate()


        // coroutine scope for global services and coroutines
        applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)




        //init
        val localConfigRepo = LocalConfigRepository(RealmDatabase.realm)

        localConfig = localConfigRepo
        currentOwnership = OwnershipContext("",null,null)


        val integrityManager = ReferenceIntegrityManager(mutableMapOf())

        //todo refactor this


        //local repositories
        val repoMembership = RealmMembershipRepository(RealmDatabase.realm,MEMBERSHIP_MAPPER, { RealmMembershipEntity() },
            RealmMembershipEntity::class, MembershipDTO::class,integrityManager)

        val factory = RepositoryBuilderFactory(
            realm = RealmDatabase.realm,
            integrityManager = integrityManager
        )

        val userHandler = factory.user(USER_MAPPER).build()
        val groupHandler = factory.group(GROUP_MAPPER).build()
        val projectHandler = factory.project(PROJECT_MAPPER).build()
        val taskHandler = factory.task(TASK_MAPPER).build()
        val reminderHandler = factory.reminder(REMINDER_MAPPER).build()
        val eventHandler = factory.event(EVENT_MAPPER).build()


        integrityManager.addClassAndEntry(UserDTO::class, ReferenceIntegrityManagerEntry(
                dtoClass = UserDTO::class,
                propagate = { localId, cloudId ->
                    groupHandler.reference?.synchronizeReferences(localId,cloudId)
                    projectHandler.reference?.synchronizeReferences(localId,cloudId)
                    repoMembership.synchronizeReferences(localId,cloudId)
                    taskHandler.reference?.synchronizeReferences(localId,cloudId)
                    eventHandler.reference?.synchronizeReferences(localId,cloudId)
                    reminderHandler.reference?.synchronizeReferences(localId,cloudId)
                },
                mapOf()
            )
        )

        integrityManager.addClassAndEntry(GroupDTO::class, ReferenceIntegrityManagerEntry(
            dtoClass = GroupDTO::class,
            propagate = { localId, cloudId ->
                projectHandler.reference?.synchronizeReferences(localId,cloudId)
                repoMembership.synchronizeReferences(localId,cloudId)
                taskHandler.reference?.synchronizeReferences(localId,cloudId)
                eventHandler.reference?.synchronizeReferences(localId,cloudId)
                reminderHandler.reference?.synchronizeReferences(localId,cloudId)
            },mapOf(
                Filter.OWNER to {id->userHandler.sync?.getRemoteOf(id)}
            )
        ))

        integrityManager.addClassAndEntry(ProjectDTO::class, ReferenceIntegrityManagerEntry(
            dtoClass = ProjectDTO::class,
            propagate = {localId, cloudId ->
                repoMembership.synchronizeReferences(localId,cloudId)
                taskHandler.reference?.synchronizeReferences(localId,cloudId)
                eventHandler.reference?.synchronizeReferences(localId,cloudId)
            },
            resolve = mapOf(
                Filter.OWNER to {id->userHandler.sync?.getRemoteOf(id) ?: groupHandler.sync?.getRemoteOf(id)}
            )
        ))

        integrityManager.addClassAndEntry(MembershipDTO::class, ReferenceIntegrityManagerEntry(
                dtoClass = MembershipDTO::class,
                propagate = { _,_ -> },
                resolve = mapOf(
                    Filter.MEMBERSHIP_ENTITY to {id->groupHandler.sync?.getRemoteOf(id) ?: projectHandler.sync?.getRemoteOf(id)},
                    Filter.MEMBERSHIP_MEMBER to {id->userHandler.sync?.getRemoteOf(id)}
                )
            )
        )

        integrityManager.addClassAndEntry(TaskDTO::class, ReferenceIntegrityManagerEntry(
                dtoClass = TaskDTO::class,
                propagate = { localId, cloudId ->
                    reminderHandler.reference?.synchronizeReferences(localId,cloudId)},
                resolve = mapOf(
                    Filter.PROJECT to {id->projectHandler.sync?.getRemoteOf(id)},
                    Filter.OWNER to {id->userHandler.sync?.getRemoteOf(id) ?: groupHandler.sync?.getRemoteOf(id)}
                )
            )
        )

        integrityManager.addClassAndEntry(EventDTO::class, ReferenceIntegrityManagerEntry(
                dtoClass = EventDTO::class,
                propagate = { localId, cloudId ->
                    reminderHandler.reference?.synchronizeReferences(localId,cloudId)},
                resolve = mapOf(
                    Filter.PROJECT to {id->projectHandler.sync?.getRemoteOf(id)},
                    Filter.OWNER to {id->userHandler.sync?.getRemoteOf(id) ?: groupHandler.sync?.getRemoteOf(id)}
                )
            )
        )

        integrityManager.addClassAndEntry(ReminderDTO::class, ReferenceIntegrityManagerEntry(
            dtoClass = ReminderDTO::class,
            propagate = {_,_ ->},
            resolve = mapOf(
                Filter.OWNER to {id->userHandler.sync?.getRemoteOf(id) ?: groupHandler.sync?.getRemoteOf(id)} ,
                Filter.LINK to {id->taskHandler.sync?.getRemoteOf(id) ?: eventHandler.sync?.getRemoteOf(id)}
            )
        ))

        //remote repositories

        val remoteUserRepo = FirestoreSyncRepository(
            clazz = UserDTO::class.java,
            db = FirestoreService.firestore,
            collectionName = "Users"
        )
        val remoteGroupRepo = FirestoreSyncRepository(
            clazz = GroupDTO::class.java,
            db = FirestoreService.firestore,
            collectionName = "Groups"
        )
        val remoteProjectRepo = FirestoreSyncRepository(
            clazz = ProjectDTO::class.java,
            db = FirestoreService.firestore,
            collectionName = "Projects"
        )

        val remoteMembershipRepo = FirestoreSyncRepository(
            clazz = MembershipDTO::class.java,
            db = FirestoreService.firestore,
            collectionName = "Memberships"
        )


        val remoteEventRepo = FirestoreSyncRepository(
            clazz = EventDTO::class.java,
            db = FirestoreService.firestore,
            collectionName = "Events"
        )
        val remoteTaskRepo = FirestoreSyncRepository(
            clazz = TaskDTO::class.java,
            db = FirestoreService.firestore,
            collectionName = "Tasks"
        )
        val remoteReminderRepo = FirestoreSyncRepository(
            clazz = ReminderDTO::class.java,
            db = FirestoreService.firestore,
            collectionName = "Reminders"
        )

        //view model factory using repositories
        viewModelsFactory = ViewModelsFactoryMock(
            mapOf(
                CreationViewModel::class to ViewModelsFactoryMock.FactoryEntry(
                    vmClass = CreationViewModel::class.java,
                    creator = { CreationViewModel(
                        projectHandler.repo,
                        eventHandler.repo,
                        taskHandler.repo,
                        reminderHandler.repo
                    ) }
                ),
                UsersViewModel::class to ViewModelsFactoryMock.FactoryEntry(
                    vmClass = UsersViewModel::class.java,
                    creator = { UsersViewModel(userHandler.repo) }
                ),
                GroupsViewModel::class to ViewModelsFactoryMock.FactoryEntry(
                    vmClass = GroupsViewModel::class.java,
                    creator = { GroupsViewModel(groupHandler.repo) }
                ),
                ProjectsViewModel::class to ViewModelsFactoryMock.FactoryEntry(
                    vmClass = ProjectsViewModel::class.java,
                    creator = { ProjectsViewModel(projectHandler.repo) }
                ),
                MembershipsViewModel::class to ViewModelsFactoryMock.FactoryEntry(
                    vmClass = MembershipsViewModel::class.java,
                    creator = { MembershipsViewModel(repoMembership) }
                ),
                TasksViewModel::class to ViewModelsFactoryMock.FactoryEntry(
                    vmClass = TasksViewModel::class.java,
                    creator = { TasksViewModel(taskHandler.repo, projectHandler.repo) }
                ),
                EventsViewModel::class to ViewModelsFactoryMock.FactoryEntry(
                    vmClass = EventsViewModel::class.java,
                    creator = { EventsViewModel(eventHandler.repo) }
                ),
                RemindersViewModel::class to ViewModelsFactoryMock.FactoryEntry(
                    vmClass = RemindersViewModel::class.java,
                    creator = { RemindersViewModel(reminderHandler.repo, taskHandler.repo, eventHandler.repo) }
                ),
                CalendarViewModel::class to ViewModelsFactoryMock.FactoryEntry(
                    vmClass = CalendarViewModel::class.java,
                    creator = { CalendarViewModel(eventHandler.repo, taskHandler.repo) }
                ),
                AgendaViewModel::class to ViewModelsFactoryMock.FactoryEntry(
                    vmClass = AgendaViewModel::class.java,
                    creator = { AgendaViewModel(eventHandler.repo, taskHandler.repo, projectHandler.repo) }
                ), HomeViewModel::class to ViewModelsFactoryMock.FactoryEntry(
                    vmClass = HomeViewModel::class.java,
                    creator = { HomeViewModel(taskHandler.repo, eventHandler.repo,projectHandler.repo) }
                )
            )
        )

        val syncRepositoryEntries = mutableListOf<SyncRepositoryEntry<out SynchronizableDTO>>()

        userHandler.sync?.let {
            DebugLogcatLogger.log("User handler has sync")
            syncRepositoryEntries.add(SyncRepositoryEntry(
                dtoClass = UserDTO::class,
                local = it,
                remote = remoteUserRepo
            ))
        }

        groupHandler.sync?.let {
            DebugLogcatLogger.log("Group handler has sync")
            syncRepositoryEntries.add(SyncRepositoryEntry(
                dtoClass = GroupDTO::class,
                local = it,
                remote = remoteGroupRepo
            ))
        }

        projectHandler.sync?.let {
            DebugLogcatLogger.log("Project handler has sync")
            syncRepositoryEntries.add(SyncRepositoryEntry(
                dtoClass = ProjectDTO::class,
                local = it,
                remote = remoteProjectRepo
            ))
        }

        syncRepositoryEntries.add(SyncRepositoryEntry(
            dtoClass = MembershipDTO::class,
            local = repoMembership,
            remote = remoteMembershipRepo
        ))

        taskHandler.sync?.let {
            DebugLogcatLogger.log("Task handler has sync")
            syncRepositoryEntries.add(SyncRepositoryEntry(
                dtoClass = TaskDTO::class,
                local = it,
                remote = remoteTaskRepo
            ))
        }

        eventHandler.sync?.let {
            DebugLogcatLogger.log("Event handler has sync")
            syncRepositoryEntries.add(SyncRepositoryEntry(
                dtoClass = EventDTO::class,
                local = it,
                remote = remoteEventRepo
            ))
        }

        reminderHandler.sync?.let {
            DebugLogcatLogger.log("Reminder handler has sync")
            syncRepositoryEntries.add(SyncRepositoryEntry(
                dtoClass = ReminderDTO::class,
                local = it,
                remote = remoteReminderRepo
            ))
        }

        val syncRepositoryFactory by lazy { SyncRepositoryFactory(syncRepositoryEntries) }

        // Sync Engine
        val syncEngine by lazy {
            SyncEngine(
                factory = syncRepositoryFactory,
                scope = applicationScope
            )
        }
        val auth = GoogleAuthClient(this)
        authViewModelFactory = {
            LoginViewModel(
                localConfigRepo,
                auth,
                syncEngine
            ) { dto -> remoteUserRepo.push(dto.cloudId!!, dto)
                DebugLogcatLogger.logDTOToRemote(dto)
            }
        }

        authenticationCheck = {
            DebugLogcatLogger.log("Authentication check")
            DebugLogcatLogger.log("Local config: ${localConfig.ready()}")
            DebugLogcatLogger.log("auth config: ${auth.getUser()}")
            if(localConfig.ready()) userHandler.repo.getById(localConfig.get().userID)
            else auth.getUser()
        }

    }
}


val PROJECT_MAPPER = object: SyncMapper<ProjectRealmEntity, ProjectDTO, MockProject> {
    override fun toLocal(dto: ProjectDTO, entity: ProjectRealmEntity) { dto.toRealm(entity) }
    override fun toDTO(entity: ProjectRealmEntity): ProjectDTO { return entity.toDTO() }
    override fun toDomain(entity: ProjectRealmEntity): MockProject { return entity.toDomain() }
}

val EVENT_MAPPER = object: SyncMapper<EventRealmEntity, EventDTO, MockEvent> {
    override fun toLocal(dto: EventDTO, entity: EventRealmEntity) { dto.toRealm(entity) }
    override fun toDTO(entity: EventRealmEntity): EventDTO { return entity.toDTO() }
    override fun toDomain(entity: EventRealmEntity): MockEvent { return entity.toDomain() }
}

val GROUP_MAPPER = object: SyncMapper<GroupRealmEntity, GroupDTO, MockGroup> {
    override fun toLocal(dto: GroupDTO, entity: GroupRealmEntity) { dto.toRealm(entity) }
    override fun toDTO(entity: GroupRealmEntity): GroupDTO { return entity.toDTO() }
    override fun toDomain(entity: GroupRealmEntity): MockGroup { return entity.toDomain() }
}

val USER_MAPPER = object: SyncMapper<UserRealmEntity, UserDTO, MockUser> {
    override fun toLocal(dto: UserDTO, entity: UserRealmEntity) { dto.toRealm(entity) }
    override fun toDTO(entity: UserRealmEntity): UserDTO { return entity.toDTO() }
    override fun toDomain(entity: UserRealmEntity): MockUser { return entity.toDomain() }
}

val REMINDER_MAPPER = object: SyncMapper<ReminderRealmEntity, ReminderDTO, MockReminder> {
    override fun toLocal(dto: ReminderDTO, entity: ReminderRealmEntity) { dto.toRealm(entity) }
    override fun toDTO(entity: ReminderRealmEntity): ReminderDTO { return entity.toDTO() }
    override fun toDomain(entity: ReminderRealmEntity): MockReminder { return entity.toDomain() }
}
val TASK_MAPPER = object: SyncMapper<TaskRealmEntity, TaskDTO, MockTask> {
    override fun toLocal(dto: TaskDTO, entity: TaskRealmEntity) { dto.toRealm(entity) }
    override fun toDTO(entity: TaskRealmEntity): TaskDTO { return entity.toDTO() }
    override fun toDomain(entity: TaskRealmEntity): MockTask { return entity.toDomain() }
}

val MEMBERSHIP_MAPPER = object: SyncMapper<RealmMembershipEntity, MembershipDTO, Membership> {
    override fun toLocal(dto: MembershipDTO, entity: RealmMembershipEntity) { dto.toRealm(entity) }
    override fun toDTO(entity: RealmMembershipEntity): MembershipDTO { return entity.toDTO() }
    override fun toDomain(entity: RealmMembershipEntity): Membership { return entity.toDomain() }
}