package com.luciferdennica.qrtools.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.luciferdennica.qrtools.data.prefs.SettingsPrefs
import com.luciferdennica.qrtools.ui.screens.about.AboutScreen
import com.luciferdennica.qrtools.ui.screens.favorites.FavoritesScreen
import com.luciferdennica.qrtools.ui.screens.generator.GeneratorScreen
import com.luciferdennica.qrtools.ui.screens.history.HistoryScreen
import com.luciferdennica.qrtools.ui.screens.home.HomeScreen
import com.luciferdennica.qrtools.ui.screens.scanner.ScannerScreen
import com.luciferdennica.qrtools.ui.screens.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val SCANNER = "scanner"
    const val GENERATOR = "generator"
    const val HISTORY = "history"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
}

@Composable
fun NavGraph(prefs: SettingsPrefs) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) { HomeScreen(nav) }
        composable(Routes.SCANNER) { ScannerScreen(nav) }
        composable(Routes.GENERATOR) { GeneratorScreen(nav) }
        composable(Routes.HISTORY) { HistoryScreen(nav) }
        composable(Routes.FAVORITES) { FavoritesScreen(nav) }
        composable(Routes.SETTINGS) { SettingsScreen(nav, prefs) }
        composable(Routes.ABOUT) { AboutScreen(nav) }
    }
}
