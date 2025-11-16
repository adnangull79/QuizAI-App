package com.example.quizai.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Create DataStore instance
val Context.dataStore by preferencesDataStore("settings")

object ThemeDataStore {

    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

    fun isDarkMode(context: Context): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[DARK_MODE_KEY] ?: false   // default = light mode
        }

    suspend fun saveDarkMode(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = enabled
        }
    }
}
