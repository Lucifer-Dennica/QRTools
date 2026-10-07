package com.luciferdennica.qrtools.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.luciferdennica.qrtools.App
import com.luciferdennica.qrtools.data.prefs.SettingsPrefs
import com.luciferdennica.qrtools.data.repo.HistoryRepository
import com.luciferdennica.qrtools.ui.screens.about.AboutScreen
import com.luciferdennica.qrtools.ui.screens.favorites.FavoritesScreen
import com.luciferdennica.qrtools.ui.screens.generator.GeneratorScreen
import com.luciferdennica.qrtools.ui.screens.history.HistoryScreen
import com.luciferdennica.qrtools.ui.screens.home.HomeScreen
import com.luciferdennica.qrtools.ui.screens.result.ResultScreen
import com.luciferdennica.qrtools.ui.screens.scanner.ScannerScreen
import com.luciferdennica.qrtools.ui.screens.settings.BehaviorSettingsScreen
import com.luciferdennica.qrtools.ui.screens.settings.DesignSettingsScreen
import com.luciferdennica.qrtools.ui.screens.settings.HistorySettingsScreen
import com.luciferdennica.qrtools.ui.screens.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val SCANNER = "scanner"
    const val GENERATOR = "generator"
    const val HISTORY = "history"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"
    const val SETTINGS_DESIGN = "settings/design"
    const val SETTINGS_BEHAVIOR = "settings/behavior"
    const val SETTINGS_HISTORY = "settings/history"
    const val ABOUT = "about"
    const val RESULT = "result"
    fun result(id: Long) = "$RESULT/$id"
}

@Composable
fun NavGraph(prefs: SettingsPrefs, historyRepo: HistoryRepository) {
    val nav = rememberNavController()
    val autoCopy by prefs.autoCopy.collectAsState(initial = false)
    val context = LocalContext.current
    val app = context.applicationContext as App

    // Если пришли из виджета — сразу открываем сканер
    LaunchedEffect(Unit) {
        if (app.openScannerOnStart) {
            app.openScannerOnStart = false
            nav.navigate(Routes.SCANNER)
        }
    }

    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) { HomeScreen(nav) }
        composable(Routes.SCANNER) { ScannerScreen(nav, historyRepo, prefs) }
        composable(Routes.GENERATOR) { GeneratorScreen(nav) }
        composable(Routes.HISTORY) { HistoryScreen(nav, historyRepo, autoCopy) }
        composable(Routes.FAVORITES) { FavoritesScreen(nav, historyRepo) }

        composable(Routes.SETTINGS) { SettingsScreen(nav) }
        composable(Routes.SETTINGS_DESIGN) { DesignSettingsScreen(nav, prefs) }
        composable(Routes.SETTINGS_BEHAVIOR) { BehaviorSettingsScreen(nav, prefs) }
        composable(Routes.SETTINGS_HISTORY) { HistorySettingsScreen(nav, prefs, historyRepo) }

        composable(Routes.ABOUT) { AboutScreen(nav) }
        composable(
            route = "${Routes.RESULT}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: 0L
            ResultScreen(nav, historyRepo, id)
        }
    }
}
