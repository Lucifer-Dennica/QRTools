package com.luciferdennica.qrtools

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.luciferdennica.qrtools.domain.model.ThemeMode
import com.luciferdennica.qrtools.ui.nav.NavGraph
import com.luciferdennica.qrtools.ui.theme.QRToolsTheme
import com.luciferdennica.qrtools.util.LocaleHelper
import com.luciferdennica.qrtools.widget.QrWidgetProvider

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as App

        if (intent?.action == QrWidgetProvider.ACTION_OPEN_SCANNER) {
            app.openScannerOnStart = true
        }

        setContent {
            val theme by app.prefs.theme.collectAsState(initial = ThemeMode.SYSTEM)
            QRToolsTheme(themeMode = theme) {
                NavGraph(prefs = app.prefs, historyRepo = app.historyRepo)
            }
        }
    }

    fun restart() {
        recreate()
    }
}
