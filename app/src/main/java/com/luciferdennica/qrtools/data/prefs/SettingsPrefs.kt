package com.luciferdennica.qrtools.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.luciferdennica.qrtools.domain.model.ThemeMode
import com.luciferdennica.qrtools.util.AppIcon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "qrtools_prefs")

class SettingsPrefs(private val context: Context) {

    companion object {
        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_AUTO_COPY = booleanPreferencesKey("auto_copy")
        private val KEY_SOUND = booleanPreferencesKey("sound")
        private val KEY_VIBRO = booleanPreferencesKey("vibro")
        private val KEY_SCAN_COUNTER = intPreferencesKey("scan_counter")
        private val KEY_ICON = stringPreferencesKey("app_icon")
    }

    val theme: Flow<ThemeMode> = context.dataStore.data.map {
        val name = it[KEY_THEME] ?: ThemeMode.SYSTEM.name
        runCatching { ThemeMode.valueOf(name) }.getOrDefault(ThemeMode.SYSTEM)
    }

    val autoCopy: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUTO_COPY] ?: false }
    val sound: Flow<Boolean> = context.dataStore.data.map { it[KEY_SOUND] ?: true }
    val vibro: Flow<Boolean> = context.dataStore.data.map { it[KEY_VIBRO] ?: true }

    val appIcon: Flow<AppIcon> = context.dataStore.data.map {
        AppIcon.fromKey(it[KEY_ICON] ?: AppIcon.BLUE.key)
    }

    suspend fun setTheme(mode: ThemeMode) = context.dataStore.edit { it[KEY_THEME] = mode.name }
    suspend fun setAutoCopy(value: Boolean) = context.dataStore.edit { it[KEY_AUTO_COPY] = value }
    suspend fun setSound(value: Boolean) = context.dataStore.edit { it[KEY_SOUND] = value }
    suspend fun setVibro(value: Boolean) = context.dataStore.edit { it[KEY_VIBRO] = value }
    suspend fun setAppIcon(icon: AppIcon) = context.dataStore.edit { it[KEY_ICON] = icon.key }

    suspend fun getVibroOnce(): Boolean = vibro.first()
    suspend fun getSoundOnce(): Boolean = sound.first()
    suspend fun getAutoCopyOnce(): Boolean = autoCopy.first()

    suspend fun incrementScanCounter(): Int {
        var result = 0
        context.dataStore.edit { prefs ->
            val cur = prefs[KEY_SCAN_COUNTER] ?: 0
            result = cur + 1
            prefs[KEY_SCAN_COUNTER] = result
        }
        return result
    }

    suspend fun resetScanCounter() {
        context.dataStore.edit { it[KEY_SCAN_COUNTER] = 0 }
    }
}
