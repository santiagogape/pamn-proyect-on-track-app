package com.example.on_track_app.data

import com.google.firebase.firestore.FirebaseFirestore

// Create a Singleton to initialize the DB Object
object FirestoreService {
    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
}