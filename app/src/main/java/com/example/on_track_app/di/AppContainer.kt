package com.example.on_track_app.di

import com.example.on_track_app.data.FirestoreRepository
import com.example.on_track_app.model.Event
import com.example.on_track_app.model.Project
import com.example.on_track_app.model.Task
import com.google.firebase.firestore.FirebaseFirestore

object AppContainer {
    private val firestoreInstance = FirebaseFirestore.getInstance()

    val taskRepository: FirestoreRepository<Task> = FirestoreRepository(
        db = firestoreInstance,
        collectionName = "tasks",
        clazz = Task::class.java
    )

    val projectRepository: FirestoreRepository<Project> = FirestoreRepository(
        db = firestoreInstance,
        collectionName = "projects",
        clazz = Project::class.java
    )

    val eventRepository: FirestoreRepository<Event> = FirestoreRepository(
        db = firestoreInstance,
        collectionName = "events",
        clazz = Event::class.java
    )

    val viewModelFactory: AppViewModelFactory = AppViewModelFactory(this)

}