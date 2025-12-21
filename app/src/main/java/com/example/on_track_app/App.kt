package com.example.on_track_app


import android.app.Application
import com.example.on_track_app.data.GarbageCollector
import com.example.on_track_app.data.abstractions.repositories.UniqueRepository
import com.example.on_track_app.data.auth.AuthClient
import com.example.on_track_app.data.auth.GoogleAuthClient
import com.example.on_track_app.data.di.CatalogBindingsFactory
import com.example.on_track_app.data.di.SyncEngineBuilder
import com.example.on_track_app.data.di.ViewModelsFactoryBuilder
import com.example.on_track_app.data.di.authCheck
import com.example.on_track_app.data.di.authViewmodelBuilder
import com.example.on_track_app.data.realm.RealmDatabase
import com.example.on_track_app.data.realm.repositories.LocalConfigRepository
import com.example.on_track_app.data.synchronization.ConnectivityProvider
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Group
import com.example.on_track_app.model.Identifiable
import com.example.on_track_app.model.LocalConfigurations
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.Reminder
import com.example.on_track_app.model.Task
import com.example.on_track_app.model.User
import com.example.on_track_app.model.UserMembership
import com.example.on_track_app.utils.AndroidConnectivityProvider
import com.example.on_track_app.utils.SettingsDataStore
import com.example.on_track_app.utils.setAppLocale
import com.example.on_track_app.viewModels.factory.ViewModelsFactory
import com.example.on_track_app.viewModels.login.LoginViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

class OnTrackApp : Application() {

    val domain : List<KClass<out Identifiable>> = listOf(
        User::class,
        Group::class,
        Project::class,
        UserMembership::class,
        Task::class,
        Event::class,
        Reminder::class
        )

    lateinit var viewModelsFactory: ViewModelsFactory
        private set

    lateinit var applicationScope: CoroutineScope
        private set

    lateinit var localConfig: UniqueRepository<LocalConfigurations>
        private set

    lateinit var authViewModelFactory: ()-> LoginViewModel
        private set
    lateinit var authenticationCheck: () -> User?
        private set
    lateinit var connectivityProvider: ConnectivityProvider
        private set

    lateinit var authClient: AuthClient
        private set


    override fun onCreate() {
        super.onCreate()



        // coroutine scope for global services and coroutines
        applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        connectivityProvider = AndroidConnectivityProvider(
            context = this,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        )

        val settings = SettingsDataStore(this)

        applicationScope.launch {
            settings.languageFlow.first().let { lang ->
                setAppLocale(lang)
            }
        }

        //init
        val localConfigRepo = LocalConfigRepository(RealmDatabase.realm)

        localConfig = localConfigRepo
        val garbageCollector = GarbageCollector(domain)
        val catalogBindingsFactory = CatalogBindingsFactory(garbageCollector)

        //view model factory using repositories
        viewModelsFactory = ViewModelsFactoryBuilder(catalogBindingsFactory).build()



        // Sync Engine
        val syncEngine by lazy {
            SyncEngineBuilder(
                domainOrder = domain,
                scope = applicationScope,
                connectivity = connectivityProvider,
                settings = SettingsDataStore(this),
                catalogBinding = catalogBindingsFactory,
            ).build()
        }
        authClient = GoogleAuthClient(this)
        authViewModelFactory = authViewmodelBuilder(authClient,localConfigRepo,syncEngine,catalogBindingsFactory)
        authenticationCheck = authCheck(authClient,localConfigRepo,catalogBindingsFactory)

        garbageCollector.init()

        applicationScope.launch {
            //start gc for periodic or event trigger deleteOnCascade
        }

    }
}


