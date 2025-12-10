package com.example.on_track_app


import android.app.Application
import com.example.on_track_app.data.firebase.FirestoreService
import com.example.on_track_app.data.firebase.FirestoreSyncRepository
import com.example.on_track_app.data.realm.RealmDatabase
import com.example.on_track_app.data.realm.entities.EventRealmEntity
import com.example.on_track_app.data.realm.entities.GroupRealmEntity
import com.example.on_track_app.data.realm.entities.ProjectRealmEntity
import com.example.on_track_app.data.realm.entities.ReminderRealmEntity
import com.example.on_track_app.data.realm.entities.SyncMapper
import com.example.on_track_app.data.realm.entities.TaskRealmEntity
import com.example.on_track_app.data.realm.entities.UserRealmEntity
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.data.realm.repositories.RealmEventRepository
import com.example.on_track_app.data.realm.repositories.RealmGroupRepository
import com.example.on_track_app.data.realm.repositories.RealmProjectRepository
import com.example.on_track_app.data.realm.repositories.RealmReminderRepository
import com.example.on_track_app.data.realm.repositories.RealmTaskRepository
import com.example.on_track_app.data.realm.repositories.RealmUserRepository
import com.example.on_track_app.data.synchronization.EventDTO
import com.example.on_track_app.data.synchronization.GroupDTO
import com.example.on_track_app.data.synchronization.ProjectDTO
import com.example.on_track_app.data.synchronization.ReminderDTO
import com.example.on_track_app.data.synchronization.SyncEngine
import com.example.on_track_app.data.synchronization.SyncRepositoryEntry
import com.example.on_track_app.data.synchronization.SyncRepositoryFactory
import com.example.on_track_app.data.synchronization.TaskDTO
import com.example.on_track_app.data.synchronization.UserDTO
import com.example.on_track_app.data.synchronization.toDTO
import com.example.on_track_app.data.synchronization.toRealm
import com.example.on_track_app.model.MockEvent
import com.example.on_track_app.model.MockGroup
import com.example.on_track_app.model.MockProject
import com.example.on_track_app.model.MockReminder
import com.example.on_track_app.model.MockTask
import com.example.on_track_app.model.MockUser
import com.example.on_track_app.viewModels.CreationViewModel
import com.example.on_track_app.viewModels.factory.ViewModelsFactoryMock
import com.example.on_track_app.viewModels.main.NotificationsViewModel
import com.example.on_track_app.viewModels.main.ProjectsViewModel
import com.example.on_track_app.viewModels.main.TasksViewModel
import kotlinx.coroutines.*

class OnTrackApp : Application() {


    lateinit var viewModelsFactory: ViewModelsFactoryMock
    lateinit var applicationScope: CoroutineScope


    override fun onCreate() {
        super.onCreate()



        //local repositories
        val repoProject = RealmProjectRepository(RealmDatabase.realm,PROJECT_MAPPER,{ ProjectRealmEntity() },
            ProjectRealmEntity::class )
        val repoUser = RealmUserRepository(
            RealmDatabase.realm, USER_MAPPER, { UserRealmEntity() },
            UserRealmEntity::class
        )
        val repoGroup = RealmGroupRepository(
            RealmDatabase.realm, GROUP_MAPPER, { GroupRealmEntity() },
            GroupRealmEntity::class
        )
        val repoEvent = RealmEventRepository(
            RealmDatabase.realm, EVENT_MAPPER, { EventRealmEntity() },
            EventRealmEntity::class
        )
        val repoTask = RealmTaskRepository(
            RealmDatabase.realm, TASK_MAPPER, { TaskRealmEntity() },
            TaskRealmEntity::class
        )
        val repoReminder = RealmReminderRepository(
            RealmDatabase.realm, REMINDER_MAPPER, { ReminderRealmEntity() },
            ReminderRealmEntity::class
        )
        //remote repositories
        val remoteProjectRepo = FirestoreSyncRepository(
            clazz = ProjectDTO::class.java,
            db = FirestoreService.firestore,
            collectionName = "Projects"
        )

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
                    creator = { ProjectsViewModel(repoProject) }
                ),
                TasksViewModel::class to ViewModelsFactoryMock.FactoryEntry(
                    vmClass = TasksViewModel::class.java,
                    creator = { TasksViewModel(repoTask) }
                ),
                NotificationsViewModel::class to ViewModelsFactoryMock.FactoryEntry(
                    vmClass = NotificationsViewModel::class.java,
                    creator = { NotificationsViewModel(repoEvent) }
                )
            )
        )

        // coroutine scope for global services and coroutines
        applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)



        // Sync Repository Factory for Sync Engine
        val syncRepositoryFactory by lazy {
            SyncRepositoryFactory(
                listOf(
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


        // att
        syncRepositoryFactory.allEntries().forEach { entry ->
            entry.local.attachToEngine(syncEngine)
        }

        syncEngine.start()
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