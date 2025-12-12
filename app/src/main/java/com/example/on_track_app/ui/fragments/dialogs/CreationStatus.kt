package com.example.on_track_app.ui.fragments.dialogs

sealed interface CreationStatus {
    object Idle : CreationStatus          // Waiting for user input
    object Loading : CreationStatus       // Sending data to Firestore
    object Success : CreationStatus       // Data saved!
    data class Error(val msg: String) : CreationStatus
}