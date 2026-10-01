package com.luciferdennica.qrtools.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.luciferdennica.qrtools.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "qrtools_prefs")

class SettingsPrefs(private val context: Context) {

    companion object {
        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_AUTO_COPY = booleanPreferencesKey("auto_copy")
        private val KEY_SOUND = booleanPreferencesKey("sound")
        private val KEY_VIBRO = booleanPreferencesKey("vibro")
    }

    val theme: Flow<ThemeMode> = context.dataStore.data.map {
        val name = it[KEY_THEME] ?: ThemeMode.SYSTEM.name
        runCatching { ThemeMode.valueOf(name) }.getOrDefault(ThemeMode.SYSTEM)
    }

    val autoCopy: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUTO_COPY] ?: false }
    val sound: Flow<Boolean> = context.dataStore.data.map { it[KEY_SOUND] ?: true }
    val vibro: Flow<Boolean> = context.dataStore.data.map { it[KEY_VIBRO] ?: true }

    suspend fun setTheme(mode: ThemeMode) = context.dataStore.edit { it[KEY_THEME] = mode.name }
    suspend fun setAutoCopy(value: Boolean) = context.dataStore.edit { it[KEY_AUTO_COPY] = value }
    suspend fun setSound(value: Boolean) = context.dataStore.edit { it[KEY_SOUND] = value }
    suspend fun setVibro(value: Boolean) = context.dataStore.edit { it[KEY_VIBRO] = value }
}
