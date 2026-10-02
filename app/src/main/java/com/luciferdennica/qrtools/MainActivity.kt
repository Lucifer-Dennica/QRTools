package com.luciferdennica.qrtools

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.luciferdennica.qrtools.data.prefs.SettingsPrefs
import com.luciferdennica.qrtools.domain.model.ThemeMode
import com.luciferdennica.qrtools.ui.nav.NavGraph
import com.luciferdennica.qrtools.ui.theme.QRToolsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = SettingsPrefs(applicationContext)
        val repo = (application as App).historyRepo

        setContent {
            val theme by prefs.theme.collectAsState(initial = ThemeMode.SYSTEM)
            QRToolsTheme(themeMode = theme) {
                NavGraph(prefs = prefs, historyRepo = repo)
            }
        }
    }
}
