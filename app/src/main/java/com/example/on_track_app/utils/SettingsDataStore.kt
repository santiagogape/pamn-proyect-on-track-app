package com.example.on_track_app.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.on_track_app.data.synchronization.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

class SettingsDataStore(private val context: Context) {

    // ---------------- KEYS ----------------
    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")

        val LAST_SYNC_PUSH = longPreferencesKey("last_sync_push")
        val LAST_SYNC_PULL = longPreferencesKey("last_sync_pull")
    }

    // ---------------- UI ----------------
    val darkThemeFlow = context.dataStore.data
        .map { prefs -> prefs[Keys.DARK_THEME] ?: false }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DARK_THEME] = enabled
        }
    }

    // ---------------- SYNC STATE ----------------
    val syncStateFlow: Flow<SyncState> =
        context.dataStore.data.map { prefs ->
            SyncState(
                lastSuccessfulPush = prefs[Keys.LAST_SYNC_PUSH] ?: 0L,
                lastSuccessfulPull = prefs[Keys.LAST_SYNC_PULL] ?: 0L
            )
        }

    suspend fun updateLastPush(timestamp: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_SYNC_PUSH] = timestamp
        }
    }

    suspend fun updateLastPull(timestamp: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_SYNC_PULL] = timestamp
        }
    }
}














