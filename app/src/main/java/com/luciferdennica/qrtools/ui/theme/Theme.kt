package com.luciferdennica.qrtools.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.luciferdennica.qrtools.domain.model.ThemeMode

private val LightScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    secondary = LightSecondary,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface
)

private val DarkScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    secondary = DarkSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface
)

private val OceanScheme = darkColorScheme(
    primary = OceanPrimary,
    onPrimary = OceanOnPrimary,
    secondary = OceanSecondary,
    background = OceanBackground,
    surface = OceanSurface,
    onBackground = OceanOnBackground,
    onSurface = OceanOnSurface
)

private val ForestScheme = darkColorScheme(
    primary = ForestPrimary,
    onPrimary = ForestOnPrimary,
    secondary = ForestSecondary,
    background = ForestBackground,
    surface = ForestSurface,
    onBackground = ForestOnBackground,
    onSurface = ForestOnSurface
)

private val AmethystScheme = darkColorScheme(
    primary = AmethystPrimary,
    onPrimary = AmethystOnPrimary,
    secondary = AmethystSecondary,
    background = AmethystBackground,
    surface = AmethystSurface,
    onBackground = AmethystOnBackground,
    onSurface = AmethystOnSurface
)

@Composable
fun QRToolsTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val scheme = when (themeMode) {
        ThemeMode.SYSTEM -> if (dark) DarkScheme else LightScheme
        ThemeMode.LIGHT -> LightScheme
        ThemeMode.DARK -> DarkScheme
        ThemeMode.OCEAN -> OceanScheme
        ThemeMode.FOREST -> ForestScheme
        ThemeMode.AMETHYST -> AmethystScheme
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
