package com.example.on_track_app

import android.app.Application
import com.example.on_track_app.di.AppContainer

class App : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer
    }
}