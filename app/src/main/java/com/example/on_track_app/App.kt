package com.example.on_track_app


import android.app.Application
import android.util.Log
import com.example.on_track_app.data.abstractions.repositories.UniqueRepository
import com.example.on_track_app.data.firebase.FirestoreService
import com.example.on_track_app.data.firebase.FirestoreSyncRepository
import com.example.on_track_app.data.realm.RealmDatabase
import com.example.on_track_app.data.realm.entities.EventRealmEntity
import com.example.on_track_app.data.realm.entities.GroupRealmEntity
import com.example.on_track_app.data.realm.entities.ProjectRealmEntity
import com.example.on_track_app.data.realm.entities.RealmMembershipEntity
import com.example.on_track_app.data.realm.entities.ReminderRealmEntity
import com.example.on_track_app.data.realm.entities.SyncMapper
import com.example.on_track_app.data.realm.entities.TaskRealmEntity
import com.example.on_track_app.data.realm.entities.UserRealmEntity
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.data.realm.repositories.Filter
import com.example.on_track_app.data.realm.repositories.LocalConfigRepository
import com.example.on_track_app.data.realm.repositories.RealmEventRepository
import com.example.on_track_app.data.realm.repositories.RealmGroupRepository
import com.example.on_track_app.data.realm.repositories.RealmMembershipRepository
import com.example.on_track_app.data.realm.repositories.RealmProjectRepository
import com.example.on_track_app.data.realm.repositories.RealmReminderRepository
import com.example.on_track_app.data.realm.repositories.RealmTaskRepository
import com.example.on_track_app.data.realm.repositories.RealmUserRepository
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
import com.example.on_track_app.viewModels.CreationViewModel
import com.example.on_track_app.viewModels.factory.ViewModelsFactoryMock
import com.example.on_track_app.viewModels.main.CalendarViewModel
import com.example.on_track_app.viewModels.main.NotificationsViewModel
import com.example.on_track_app.viewModels.main.ProjectsViewModel
import com.example.on_track_app.viewModels.main.TasksViewModel
import kotlinx.coroutines.*

class OnTrackApp : Application() {


    lateinit var viewModelsFactory: ViewModelsFactoryMock
    lateinit var applicationScope: CoroutineScope

    lateinit var localConfig: UniqueRepository<LocalConfigurations>


    override fun onCreate() {
        super.onCreate()


        // coroutine scope for global services and coroutines
        applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        //init
        val localConfigRepo = LocalConfigRepository(RealmDatabase.realm)
        runBlocking(Dispatchers.IO) {
            localConfigRepo.init()
        }
        localConfig = localConfigRepo
        localConfig.get()?.let { DebugLogcatLogger.logConfig(it, "app") }


        val integrityManager = ReferenceIntegrityManager(mutableMapOf())



        //local repositories
        val repoUser = RealmUserRepository(
            RealmDatabase.realm, USER_MAPPER, { UserRealmEntity() },
            UserRealmEntity::class, UserDTO::class,integrityManager
        )
        val repoGroup = RealmGroupRepository(
            RealmDatabase.realm, GROUP_MAPPER, { GroupRealmEntity() },
            GroupRealmEntity::class, GroupDTO::class, integrityManager
        )
        val repoProject = RealmProjectRepository(RealmDatabase.realm,PROJECT_MAPPER,{ ProjectRealmEntity() },
            ProjectRealmEntity::class, ProjectDTO::class,integrityManager )
        val repoMembership = RealmMembershipRepository(RealmDatabase.realm,MEMBERSHIP_MAPPER, { RealmMembershipEntity() },
            RealmMembershipEntity::class, MembershipDTO::class,integrityManager)
        val repoEvent = RealmEventRepository(
            RealmDatabase.realm, EVENT_MAPPER, { EventRealmEntity() },
            EventRealmEntity::class, EventDTO::class, integrityManager
        )
        val repoTask = RealmTaskRepository(
            RealmDatabase.realm, TASK_MAPPER, { TaskRealmEntity() },
            TaskRealmEntity::class, TaskDTO::class, integrityManager
        )
        val repoReminder = RealmReminderRepository(
            RealmDatabase.realm, REMINDER_MAPPER, { ReminderRealmEntity() },
            ReminderRealmEntity::class, ReminderDTO::class, integrityManager
        )

        integrityManager.addClassAndEntry(UserDTO::class, ReferenceIntegrityManagerEntry(
                dtoClass = UserDTO::class,
                propagate = { localId, cloudId ->
                    repoGroup.synchronizeReferences(localId,cloudId)
                    repoProject.synchronizeReferences(localId,cloudId)
                    repoMembership.synchronizeReferences(localId,cloudId)
                    repoTask.synchronizeReferences(localId,cloudId)
                    repoEvent.synchronizeReferences(localId,cloudId)
                    repoReminder.synchronizeReferences(localId,cloudId)
                },
                mapOf()
            )
        )

        integrityManager.addClassAndEntry(GroupDTO::class, ReferenceIntegrityManagerEntry(
            dtoClass = GroupDTO::class,
            propagate = { localId, cloudId ->
                repoProject.synchronizeReferences(localId,cloudId)
                repoMembership.synchronizeReferences(localId,cloudId)
                repoTask.synchronizeReferences(localId,cloudId)
                repoEvent.synchronizeReferences(localId,cloudId)
                repoReminder.synchronizeReferences(localId,cloudId)
            },mapOf(
                Filter.OWNER to {id->repoUser.getRemoteOf(id)}
            )
        ))

        integrityManager.addClassAndEntry(ProjectDTO::class, ReferenceIntegrityManagerEntry(
            dtoClass = ProjectDTO::class,
            propagate = {localId, cloudId ->
                repoMembership.synchronizeReferences(localId,cloudId)
                repoTask.synchronizeReferences(localId,cloudId)
                repoEvent.synchronizeReferences(localId,cloudId)
            },
            resolve = mapOf(
                Filter.OWNER to {id->repoUser.getRemoteOf(id) ?: repoGroup.getRemoteOf(id)}
            )
        ))

        integrityManager.addClassAndEntry(MembershipDTO::class, ReferenceIntegrityManagerEntry(
                dtoClass = MembershipDTO::class,
                propagate = { _,_ -> },
                resolve = mapOf(
                    Filter.MEMBERSHIP_ENTITY to {id->repoGroup.getRemoteOf(id) ?: repoProject.getRemoteOf(id)},
                    Filter.MEMBERSHIP_MEMBER to {id->repoUser.getRemoteOf(id)}
                )
            )
        )

        integrityManager.addClassAndEntry(TaskDTO::class, ReferenceIntegrityManagerEntry(
                dtoClass = TaskDTO::class,
                propagate = { localId, cloudId ->
                    repoReminder.synchronizeReferences(localId,cloudId)},
                resolve = mapOf(
                    Filter.PROJECT to {id->repoProject.getRemoteOf(id)},
                    Filter.OWNER to {id->repoUser.getRemoteOf(id) ?: repoGroup.getRemoteOf(id)}
                )
            )
        )

        integrityManager.addClassAndEntry(EventDTO::class, ReferenceIntegrityManagerEntry(
                dtoClass = EventDTO::class,
                propagate = { localId, cloudId ->
                    repoReminder.synchronizeReferences(localId,cloudId)},
                resolve = mapOf(
                    Filter.PROJECT to {id->repoProject.getRemoteOf(id)},
                    Filter.OWNER to {id->repoUser.getRemoteOf(id) ?: repoGroup.getRemoteOf(id)}
                )
            )
        )

        integrityManager.addClassAndEntry(ReminderDTO::class, ReferenceIntegrityManagerEntry(
            dtoClass = ReminderDTO::class,
            propagate = {_,_ ->},
            resolve = mapOf(
                Filter.OWNER to {id->repoUser.getRemoteOf(id) ?: repoGroup.getRemoteOf(id)} ,
                Filter.LINK to {id->repoTask.getRemoteOf(id) ?: repoEvent.getRemoteOf(id)}

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
                    creator = { CreationViewModel(repoProject, repoEvent, repoTask) }
                ),
                ProjectsViewModel::class to ViewModelsFactoryMock.FactoryEntry(
                    vmClass = ProjectsViewModel::class.java,
                    creator = { ProjectsViewModel(repoProject, localConfigRepo) }
                ),
                TasksViewModel::class to ViewModelsFactoryMock.FactoryEntry(
                    vmClass = TasksViewModel::class.java,
                    creator = { TasksViewModel(repoTask) }
                ),
                CalendarViewModel::class to ViewModelsFactoryMock.FactoryEntry(
                    vmClass = CalendarViewModel::class.java,
                    creator = { CalendarViewModel(repoEvent) }
                ),
                NotificationsViewModel::class to ViewModelsFactoryMock.FactoryEntry(
                    vmClass = NotificationsViewModel::class.java,
                    creator = { NotificationsViewModel(repoEvent) }
                )
            )
        )




        // Sync Repository Factory for Sync Engine
        val syncRepositoryFactory by lazy {
            SyncRepositoryFactory(
                listOf(
                    SyncRepositoryEntry(
                        dtoClass = UserDTO::class,
                        local = repoUser,
                        remote = remoteUserRepo),
                    SyncRepositoryEntry(
                        dtoClass = ProjectDTO::class,
                        local = repoProject,
                        remote = remoteProjectRepo
                    ),SyncRepositoryEntry(
                        dtoClass = EventDTO::class,
                        local = repoEvent,
                        remote = remoteEventRepo
                    ),SyncRepositoryEntry(
                        dtoClass = TaskDTO::class,
                        local = repoTask,
                        remote = remoteTaskRepo
                    )
                )
            )
        }

        // Sync Engine
        val syncEngine by lazy {
            SyncEngine(
                factory = syncRepositoryFactory,
                scope = applicationScope
            )
        }

        syncEngine.start()

        // forcing to sync user -> todo not do this
        runBlocking(Dispatchers.IO){

            localConfig.get()?.let {
                Log.d("SyncDebug","\nuser:${it.userID}\n\n")
                syncEngine.onLocalChange(it.userID,UserDTO::class)
            }
        }

        runBlocking(Dispatchers.IO){
            localConfig.get()?.let {
                Log.d("SyncDebug","\nproject:${it.defaultProjectID}\n\n")
                syncEngine.onLocalChange(it.defaultProjectID,ProjectDTO::class)
            }
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