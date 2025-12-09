package com.example.on_track_app


import android.app.Application
import com.example.on_track_app.data.firebase.FirestoreService
import com.example.on_track_app.data.firebase.FirestoreSyncRepository
import com.example.on_track_app.data.realm.RealmDatabase
import com.example.on_track_app.data.realm.repositories.RealmProjectRepository
import com.example.on_track_app.data.synchronization.ProjectDTO
import com.example.on_track_app.data.synchronization.SyncEngine
import com.example.on_track_app.data.synchronization.SyncRepositoryEntry
import com.example.on_track_app.data.synchronization.SyncRepositoryFactory
import com.example.on_track_app.viewModels.CreationViewModel
import com.example.on_track_app.viewModels.factory.ViewModelsFactoryMock
import com.example.on_track_app.viewModels.main.ProjectsViewModel
import kotlinx.coroutines.*

class OnTrackApp : Application() {


    lateinit var viewModelsFactory: ViewModelsFactoryMock
    lateinit var applicationScope: CoroutineScope


    override fun onCreate() {
        super.onCreate()

        //local repositories
        val projectRepo = RealmProjectRepository(RealmDatabase.realm)

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


