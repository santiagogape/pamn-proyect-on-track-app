package com.example.on_track_app.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

val DummyFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Return null or throw if the Preview actually tries to execute the VM logic
        throw IllegalStateException("View Model should not be instantiated in Preview")
    }
}