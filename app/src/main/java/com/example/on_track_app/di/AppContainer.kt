package com.example.on_track_app.di

import com.example.on_track_app.data.FirestoreRepository
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.Task
import com.google.firebase.firestore.FirebaseFirestore

interface AppContainer {
    val taskRepository: FirestoreRepository<Task>
    val projectRepository: FirestoreRepository<Project>
    val eventRepository: FirestoreRepository<Event>
    val viewModelFactory: AppViewModelFactory
}

class DefaultAppContainer : AppContainer {
    private val firestoreInstance = FirebaseFirestore.getInstance()

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

    override val viewModelFactory: AppViewModelFactory = AppViewModelFactory(this)

}