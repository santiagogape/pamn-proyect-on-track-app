package com.example.on_track_app

import android.app.Application
import com.example.on_track_app.di.AppContainer
import com.example.on_track_app.di.DefaultAppContainer

class App : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}