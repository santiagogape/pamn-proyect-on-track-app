package com.example.on_track_app.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

class SettingsDataStore(private val context: Context) {


    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
    }

    val darkThemeFlow = context.dataStore.data
        .map { prefs -> prefs[Keys.DARK_THEME] ?: false }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DARK_THEME] = enabled
        }
    }
}















