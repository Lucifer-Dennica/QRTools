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
        private val KEY_SAVE_HISTORY = booleanPreferencesKey("save_history")
        private val KEY_SCAN_COUNTER = intPreferencesKey("scan_counter")
        private val KEY_ICON = stringPreferencesKey("app_icon")
        private val KEY_REVIEW_COUNTER = intPreferencesKey("review_counter")
        private val KEY_REVIEW_DONT_ASK = booleanPreferencesKey("review_dont_ask")

        private val KEY_LAST_WIFI_SSID = stringPreferencesKey("last_wifi_ssid")
        private val KEY_LAST_WIFI_PASS = stringPreferencesKey("last_wifi_pass")
        private val KEY_LAST_WIFI_SEC = stringPreferencesKey("last_wifi_sec")
        private val KEY_LAST_SMS_PHONE = stringPreferencesKey("last_sms_phone")
        private val KEY_LAST_URL = stringPreferencesKey("last_url")
        private val KEY_LAST_TEXT = stringPreferencesKey("last_text")
    }

    val theme: Flow<ThemeMode> = context.dataStore.data.map {
        val name = it[KEY_THEME] ?: ThemeMode.SYSTEM.name
        runCatching { ThemeMode.valueOf(name) }.getOrDefault(ThemeMode.SYSTEM)
    }

    val autoCopy: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUTO_COPY] ?: false }
    val sound: Flow<Boolean> = context.dataStore.data.map { it[KEY_SOUND] ?: true }
    val vibro: Flow<Boolean> = context.dataStore.data.map { it[KEY_VIBRO] ?: true }
    val saveHistory: Flow<Boolean> = context.dataStore.data.map { it[KEY_SAVE_HISTORY] ?: true }

    val appIcon: Flow<AppIcon> = context.dataStore.data.map {
        AppIcon.fromKey(it[KEY_ICON] ?: AppIcon.BLUE.key)
    }

    suspend fun setTheme(mode: ThemeMode) = context.dataStore.edit { it[KEY_THEME] = mode.name }
    suspend fun setAutoCopy(value: Boolean) = context.dataStore.edit { it[KEY_AUTO_COPY] = value }
    suspend fun setSound(value: Boolean) = context.dataStore.edit { it[KEY_SOUND] = value }
    suspend fun setVibro(value: Boolean) = context.dataStore.edit { it[KEY_VIBRO] = value }
    suspend fun setSaveHistory(value: Boolean) = context.dataStore.edit { it[KEY_SAVE_HISTORY] = value }
    suspend fun setAppIcon(icon: AppIcon) = context.dataStore.edit { it[KEY_ICON] = icon.key }

    suspend fun getVibroOnce(): Boolean = vibro.first()
    suspend fun getSoundOnce(): Boolean = sound.first()
    suspend fun getAutoCopyOnce(): Boolean = autoCopy.first()
    suspend fun getSaveHistoryOnce(): Boolean = saveHistory.first()

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

    suspend fun shouldShowReviewDialog(): Boolean {
        var shouldShow = false
        context.dataStore.edit { prefs ->
            val dontAsk = prefs[KEY_REVIEW_DONT_ASK] ?: false
            if (dontAsk) {
                shouldShow = false
                return@edit
            }
            val cur = prefs[KEY_REVIEW_COUNTER] ?: 0
            val next = cur + 1
            prefs[KEY_REVIEW_COUNTER] = next

            if (cur == 0 && next >= 10) {
                shouldShow = true
            } else if (cur >= 10 && next >= cur + 20) {
                shouldShow = true
            }
        }
        return shouldShow
    }

    suspend fun setReviewDontAsk() {
        context.dataStore.edit { it[KEY_REVIEW_DONT_ASK] = true }
    }

    suspend fun resetReviewCounter() {
        context.dataStore.edit { it[KEY_REVIEW_COUNTER] = 10 }
    }

    suspend fun getLastWifi(): Triple<String, String, String> {
        val prefs = context.dataStore.data.first()
        return Triple(
            prefs[KEY_LAST_WIFI_SSID] ?: "",
            prefs[KEY_LAST_WIFI_PASS] ?: "",
            prefs[KEY_LAST_WIFI_SEC] ?: "WPA"
        )
    }

    suspend fun setLastWifi(ssid: String, pass: String, sec: String) {
        context.dataStore.edit {
            it[KEY_LAST_WIFI_SSID] = ssid
            it[KEY_LAST_WIFI_PASS] = pass
            it[KEY_LAST_WIFI_SEC] = sec
        }
    }

    suspend fun getLastSmsPhone(): String =
        context.dataStore.data.first()[KEY_LAST_SMS_PHONE] ?: ""

    suspend fun setLastSmsPhone(phone: String) {
        context.dataStore.edit { it[KEY_LAST_SMS_PHONE] = phone }
    }

    suspend fun getLastUrl(): String =
        context.dataStore.data.first()[KEY_LAST_URL] ?: ""

    suspend fun setLastUrl(url: String) {
        context.dataStore.edit { it[KEY_LAST_URL] = url }
    }

    suspend fun getLastText(): String =
        context.dataStore.data.first()[KEY_LAST_TEXT] ?: ""

    suspend fun setLastText(text: String) {
        context.dataStore.edit { it[KEY_LAST_TEXT] = text }
    }
}
