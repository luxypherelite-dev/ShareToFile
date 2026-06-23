package com.aiexport.shareordirect.data

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

object AppDataStore {
    private val EXPORT_FOLDER_URI   = stringPreferencesKey("export_folder_uri")
    private val WATCHED_APPS        = stringSetPreferencesKey("watched_apps")
    private val MAX_SCROLL_ITERS    = intPreferencesKey("max_scroll_iters")

    fun exportFolderUri(ctx: Context): Flow<Uri?> =
        ctx.dataStore.data.map { prefs ->
            prefs[EXPORT_FOLDER_URI]?.let { Uri.parse(it) }
        }

    suspend fun setExportFolderUri(ctx: Context, uri: Uri) {
        ctx.dataStore.edit { it[EXPORT_FOLDER_URI] = uri.toString() }
    }

    fun watchedApps(ctx: Context): Flow<Set<String>> =
        ctx.dataStore.data.map { prefs -> prefs[WATCHED_APPS] ?: emptySet() }

    suspend fun setWatchedApps(ctx: Context, packages: Set<String>) {
        ctx.dataStore.edit { it[WATCHED_APPS] = packages }
    }

    fun maxScrollIterations(ctx: Context): Flow<Int> =
        ctx.dataStore.data.map { prefs -> prefs[MAX_SCROLL_ITERS] ?: 200 }

    suspend fun setMaxScrollIterations(ctx: Context, value: Int) {
        ctx.dataStore.edit { it[MAX_SCROLL_ITERS] = value }
    }
}
