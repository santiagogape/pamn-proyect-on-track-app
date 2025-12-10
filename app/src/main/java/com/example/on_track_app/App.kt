package com.example.on_track_app


import android.app.Application
import com.example.on_track_app.data.firebase.FirestoreService
import com.example.on_track_app.data.firebase.FirestoreSyncRepository
import com.example.on_track_app.data.realm.RealmDatabase
import com.example.on_track_app.data.realm.entities.ProjectRealmEntity
import com.example.on_track_app.data.realm.entities.SyncMapper
import com.example.on_track_app.data.realm.entities.toDomain
import com.example.on_track_app.data.realm.repositories.RealmProjectRepository
import com.example.on_track_app.data.synchronization.ProjectDTO
import com.example.on_track_app.data.synchronization.SyncEngine
import com.example.on_track_app.data.synchronization.SyncRepositoryEntry
import com.example.on_track_app.data.synchronization.SyncRepositoryFactory
import com.example.on_track_app.data.synchronization.toDTO
import com.example.on_track_app.data.synchronization.toRealm
import com.example.on_track_app.model.MockProject
import com.example.on_track_app.viewModels.CreationViewModel
import com.example.on_track_app.viewModels.factory.ViewModelsFactoryMock
import com.example.on_track_app.viewModels.main.ProjectsViewModel
import kotlinx.coroutines.*

class OnTrackApp : Application() {


    lateinit var viewModelsFactory: ViewModelsFactoryMock
    lateinit var applicationScope: CoroutineScope


    override fun onCreate() {
        super.onCreate()

        val mapper = object: SyncMapper<ProjectRealmEntity, ProjectDTO, MockProject> {
            override fun toLocal(
                dto: ProjectDTO,
                entity: ProjectRealmEntity
            ) {
                dto.toRealm(entity)
            }

            override fun toDTO(entity: ProjectRealmEntity): ProjectDTO {
                return entity.toDTO()
            }

            override fun toDomain(entity: ProjectRealmEntity): MockProject {
                return entity.toDomain()
            }

        }

        //local repositories
        val projectRepo = RealmProjectRepository(RealmDatabase.realm,mapper,{ ProjectRealmEntity() },
            ProjectRealmEntity::class )

        //remote repositories
        val remoteProjectRepo = FirestoreSyncRepository(
            clazz = ProjectDTO::class.java,
            db = FirestoreService.firestore,
            collectionName = "projects"
        )

        //view model factory using repositories
        viewModelsFactory = ViewModelsFactoryMock(
            mapOf(
                CreationViewModel::class to ViewModelsFactoryMock.FactoryEntry(
                    vmClass = CreationViewModel::class.java,
                    creator = { CreationViewModel(projectRepo) }
                ),
                ProjectsViewModel::class to ViewModelsFactoryMock.FactoryEntry(
                    vmClass = ProjectsViewModel::class.java,
                    creator = { ProjectsViewModel(projectRepo) }
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
                        local = projectRepo,
                        remote = remoteProjectRepo
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
        projectRepo.attachToEngine(syncEngine)

        syncEngine.start()
    }
}


