package com.luciferdennica.qrtools

import android.app.Application
import com.luciferdennica.qrtools.data.db.AppDatabase
import com.luciferdennica.qrtools.data.prefs.SettingsPrefs
import com.luciferdennica.qrtools.data.repo.HistoryRepository
import com.yandex.mobile.ads.common.YandexAds

class App : Application() {
    val database by lazy { AppDatabase.get(this) }
    val historyRepo by lazy { HistoryRepository(database.scanDao()) }
    val prefs by lazy { SettingsPrefs(this) }

    override fun onCreate() {
        super.onCreate()
        // Класс MobileAds переименован в YandexAds
        YandexAds.initialize(this) { }
    }
}
