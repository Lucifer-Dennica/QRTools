package com.luciferdennica.qrtools

import android.app.Application
import com.luciferdennica.qrtools.data.db.AppDatabase
import com.luciferdennica.qrtools.data.repo.HistoryRepository

class App : Application() {
    val database by lazy { AppDatabase.get(this) }
    val historyRepo by lazy { HistoryRepository(database.scanDao()) }
}
