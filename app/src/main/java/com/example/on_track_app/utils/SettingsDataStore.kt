package com.example.on_track_app.utils

import android.app.Activity
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.on_track_app.data.synchronization.SyncState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Locale

private val Context.dataStore by preferencesDataStore("settings")

fun Context.setAppLocale(language: Language) {
    val locale = Locale.forLanguageTag(language.label)
    Locale.setDefault(locale)

    val config = resources.configuration
    config.setLocale(locale)

    @Suppress("DEPRECATION")
    resources.updateConfiguration(config, resources.displayMetrics)
}

fun changeLanguage(lang:Language, settings: SettingsDataStore, context:Context, act: Activity, scope: CoroutineScope){
    scope.launch {
        settings.setLanguage(lang)
    }
    context.setAppLocale(lang)
    act.recreate()
}

enum class Language(val label:String){
    ENG("en"), ES("es")
}

class SettingsDataStore(private val context: Context) {

    // ---------------- KEYS ----------------
    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")

        val LAST_SYNC_PUSH = longPreferencesKey("last_sync_push")
        val LAST_SYNC_PULL = longPreferencesKey("last_sync_pull")
        val LANGUAGE = stringPreferencesKey("language")
    }
    // ---------------- UI ----------------

    val languageFlow = context.dataStore.data
        .map { prefs -> prefs[Keys.LANGUAGE]?.let {Language.valueOf(it)} ?: Language.ENG }


    suspend fun setLanguage(lang: Language) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = lang.label
        }
    }

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














