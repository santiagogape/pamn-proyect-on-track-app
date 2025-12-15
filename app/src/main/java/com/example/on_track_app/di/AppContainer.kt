package com.example.on_track_app.di

import android.content.Context
import com.example.on_track_app.data.FirestoreRepository
import com.example.on_track_app.data.auth.GoogleAuthClient
import com.example.on_track_app.domain.usecase.EventManager
import com.example.on_track_app.domain.usecase.TaskManager
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.Reminder
import com.example.on_track_app.model.Task
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.firestore.FirebaseFirestore

interface AppContainer {
    val googleAuthClient: GoogleAuthClient
    val taskRepository: FirestoreRepository<Task>
    val projectRepository: FirestoreRepository<Project>
    val eventRepository: FirestoreRepository<Event>
    val reminderRepository: FirestoreRepository<Reminder>
    val viewModelFactory: AppViewModelFactory
    val taskManager: TaskManager
    val eventManager: EventManager
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val firestoreInstance = FirebaseFirestore.getInstance()
    override val googleAuthClient: GoogleAuthClient by lazy {
        GoogleAuthClient(context, Identity.getSignInClient(context))
    }

    override val taskRepository: FirestoreRepository<Task> by lazy {
        FirestoreRepository(
            db = firestoreInstance,
            collectionName = "tasks",
            clazz = Task::class.java
        )
    }

    override val projectRepository: FirestoreRepository<Project> by lazy {
        FirestoreRepository(
            db = firestoreInstance,
            collectionName = "projects",
            clazz = Project::class.java
        )
    }

    override val eventRepository: FirestoreRepository<Event> by lazy {
        FirestoreRepository(
            db = firestoreInstance,
            collectionName = "events",
            clazz = Event::class.java
        )
    }

    override val reminderRepository: FirestoreRepository<Reminder> by lazy {
        FirestoreRepository(
            db = firestoreInstance,
            collectionName = "reminders",
            clazz = Reminder::class.java
        )
    }

    override val taskManager: TaskManager by lazy {
        TaskManager(taskRepository)
    }

    override val eventManager: EventManager by lazy {
        EventManager(eventRepository)
    }

    override val viewModelFactory: AppViewModelFactory = AppViewModelFactory(this)

}