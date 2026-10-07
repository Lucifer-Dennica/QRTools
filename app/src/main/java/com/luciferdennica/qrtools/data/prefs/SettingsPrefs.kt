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
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "qrtools_prefs")

enum class QrResolution(val size: Int) {
    HD(1080),
    TWO_K(2048),
    FOUR_K(3840)
}

data class QrTemplate(
    val name: String,
    val dotColor: Long,
    val dotColor2: Long,
    val bgColor: Long,
    val bgColor2: Long,
    val dotGradient: Boolean,
    val bgGradient: Boolean
)

/** Действия для свайпов */
enum class SwipeAction {
    OFF,
    SCANNER,
    GENERATOR,
    HISTORY,
    FAVORITES,
    SETTINGS
}

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
        private val KEY_QR_RESOLUTION = stringPreferencesKey("qr_resolution")
        private val KEY_QR_TEMPLATES = stringPreferencesKey("qr_templates")

        private val KEY_SWIPE_LEFT = stringPreferencesKey("swipe_left")
        private val KEY_SWIPE_RIGHT = stringPreferencesKey("swipe_right")
        private val KEY_SWIPE_UP = stringPreferencesKey("swipe_up")
        private val KEY_SWIPE_DOWN = stringPreferencesKey("swipe_down")

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

    val qrResolution: Flow<QrResolution> = context.dataStore.data.map {
        val name = it[KEY_QR_RESOLUTION] ?: QrResolution.HD.name
        runCatching { QrResolution.valueOf(name) }.getOrDefault(QrResolution.HD)
    }

    val qrTemplates: Flow<List<QrTemplate>> = context.dataStore.data.map {
        parseTemplates(it[KEY_QR_TEMPLATES] ?: "[]")
    }

    val swipeLeft: Flow<SwipeAction> = context.dataStore.data.map {
        runCatching { SwipeAction.valueOf(it[KEY_SWIPE_LEFT] ?: "OFF") }.getOrDefault(SwipeAction.OFF)
    }
    val swipeRight: Flow<SwipeAction> = context.dataStore.data.map {
        runCatching { SwipeAction.valueOf(it[KEY_SWIPE_RIGHT] ?: "OFF") }.getOrDefault(SwipeAction.OFF)
    }
    val swipeUp: Flow<SwipeAction> = context.dataStore.data.map {
        runCatching { SwipeAction.valueOf(it[KEY_SWIPE_UP] ?: "OFF") }.getOrDefault(SwipeAction.OFF)
    }
    val swipeDown: Flow<SwipeAction> = context.dataStore.data.map {
        runCatching { SwipeAction.valueOf(it[KEY_SWIPE_DOWN] ?: "OFF") }.getOrDefault(SwipeAction.OFF)
    }

    suspend fun setTheme(mode: ThemeMode) = context.dataStore.edit { it[KEY_THEME] = mode.name }
    suspend fun setAutoCopy(value: Boolean) = context.dataStore.edit { it[KEY_AUTO_COPY] = value }
    suspend fun setSound(value: Boolean) = context.dataStore.edit { it[KEY_SOUND] = value }
    suspend fun setVibro(value: Boolean) = context.dataStore.edit { it[KEY_VIBRO] = value }
    suspend fun setSaveHistory(value: Boolean) = context.dataStore.edit { it[KEY_SAVE_HISTORY] = value }
    suspend fun setAppIcon(icon: AppIcon) = context.dataStore.edit { it[KEY_ICON] = icon.key }

    suspend fun setQrResolution(res: QrResolution) =
        context.dataStore.edit { it[KEY_QR_RESOLUTION] = res.name }

    suspend fun setSwipeLeft(a: SwipeAction) = context.dataStore.edit { it[KEY_SWIPE_LEFT] = a.name }
    suspend fun setSwipeRight(a: SwipeAction) = context.dataStore.edit { it[KEY_SWIPE_RIGHT] = a.name }
    suspend fun setSwipeUp(a: SwipeAction) = context.dataStore.edit { it[KEY_SWIPE_UP] = a.name }
    suspend fun setSwipeDown(a: SwipeAction) = context.dataStore.edit { it[KEY_SWIPE_DOWN] = a.name }

    suspend fun getVibroOnce(): Boolean = vibro.first()
    suspend fun getSoundOnce(): Boolean = sound.first()
    suspend fun getAutoCopyOnce(): Boolean = autoCopy.first()
    suspend fun getSaveHistoryOnce(): Boolean = saveHistory.first()
    suspend fun getQrResolutionOnce(): QrResolution = qrResolution.first()
    suspend fun getSwipeLeftOnce(): SwipeAction = swipeLeft.first()
    suspend fun getSwipeRightOnce(): SwipeAction = swipeRight.first()
    suspend fun getSwipeUpOnce(): SwipeAction = swipeUp.first()
    suspend fun getSwipeDownOnce(): SwipeAction = swipeDown.first()

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
            if (dontAsk) return@edit
            val cur = prefs[KEY_REVIEW_COUNTER] ?: 0
            val next = cur + 1
            prefs[KEY_REVIEW_COUNTER] = next
            if (cur == 0 && next >= 10) shouldShow = true
            else if (cur >= 10 && next >= cur + 20) shouldShow = true
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

    suspend fun addQrTemplate(template: QrTemplate) {
        context.dataStore.edit { prefs ->
            val current = parseTemplates(prefs[KEY_QR_TEMPLATES] ?: "[]").toMutableList()
            current.removeAll { it.name.equals(template.name, ignoreCase = true) }
            current.add(template)
            prefs[KEY_QR_TEMPLATES] = serializeTemplates(current)
        }
    }

    suspend fun deleteQrTemplate(name: String) {
        context.dataStore.edit { prefs ->
            val current = parseTemplates(prefs[KEY_QR_TEMPLATES] ?: "[]").toMutableList()
            current.removeAll { it.name.equals(name, ignoreCase = true) }
            prefs[KEY_QR_TEMPLATES] = serializeTemplates(current)
        }
    }

    private fun parseTemplates(json: String): List<QrTemplate> = runCatching {
        val arr = JSONArray(json)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            QrTemplate(
                name = o.getString("name"),
                dotColor = o.getLong("dotColor"),
                dotColor2 = o.getLong("dotColor2"),
                bgColor = o.getLong("bgColor"),
                bgColor2 = o.getLong("bgColor2"),
                dotGradient = o.getBoolean("dotGradient"),
                bgGradient = o.getBoolean("bgGradient")
            )
        }
    }.getOrDefault(emptyList())

    private fun serializeTemplates(list: List<QrTemplate>): String {
        val arr = JSONArray()
        list.forEach { t ->
            val o = JSONObject().apply {
                put("name", t.name)
                put("dotColor", t.dotColor)
                put("dotColor2", t.dotColor2)
                put("bgColor", t.bgColor)
                put("bgColor2", t.bgColor2)
                put("dotGradient", t.dotGradient)
                put("bgGradient", t.bgGradient)
            }
            arr.put(o)
        }
        return arr.toString()
    }
}
