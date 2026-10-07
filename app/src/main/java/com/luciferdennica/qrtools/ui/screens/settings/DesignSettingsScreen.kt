package com.luciferdennica.qrtools.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.luciferdennica.qrtools.R
import com.luciferdennica.qrtools.data.prefs.QrResolution
import com.luciferdennica.qrtools.data.prefs.SettingsPrefs
import com.luciferdennica.qrtools.domain.model.ThemeMode
import com.luciferdennica.qrtools.ui.components.BannerAd
import com.luciferdennica.qrtools.util.AppIcon
import com.luciferdennica.qrtools.util.IconManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignSettingsScreen(nav: NavController, prefs: SettingsPrefs) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentTheme by prefs.theme.collectAsState(initial = ThemeMode.SYSTEM)
    val currentIcon by prefs.appIcon.collectAsState(initial = AppIcon.BLUE)
    val currentRes by prefs.qrResolution.collectAsState(initial = QrResolution.HD)

    var themeExpanded by remember { mutableStateOf(false) }
    var resExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_design)) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Тема
                SectionTitle(stringResource(R.string.settings_theme))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = themeExpanded,
                            onExpandedChange = { themeExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = stringResource(currentTheme.titleRes),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.settings_theme)) },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            )
                            ExposedDropdownMenu(
                                expanded = themeExpanded,
                                onDismissRequest = { themeExpanded = false }
                            ) {
                                ThemeMode.values().forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text(stringResource(mode.titleRes)) },
                                        onClick = {
                                            scope.launch { prefs.setTheme(mode) }
                                            themeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Иконка
                SectionTitle(stringResource(R.string.settings_icon))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconOption(AppIcon.BLUE, R.drawable.ic_launcher, currentIcon) {
                            scope.launch {
                                prefs.setAppIcon(AppIcon.BLUE)
                                IconManager.setIcon(context, AppIcon.BLUE)
                                Toast.makeText(context, context.getString(R.string.icon_changed), Toast.LENGTH_LONG).show()
                            }
                        }
                        IconOption(AppIcon.DARK, R.drawable.ic_launcher_dark, currentIcon) {
                            scope.launch {
                                prefs.setAppIcon(AppIcon.DARK)
                                IconManager.setIcon(context, AppIcon.DARK)
                                Toast.makeText(context, context.getString(R.string.icon_changed), Toast.LENGTH_LONG).show()
                            }
                        }
                        IconOption(AppIcon.GREEN, R.drawable.ic_launcher_green, currentIcon) {
                            scope.launch {
                                prefs.setAppIcon(AppIcon.GREEN)
                                IconManager.setIcon(context, AppIcon.GREEN)
                                Toast.makeText(context, context.getString(R.string.icon_changed), Toast.LENGTH_LONG).show()
                            }
                        }
                        IconOption(AppIcon.PURPLE, R.drawable.ic_launcher_purple, currentIcon) {
                            scope.launch {
                                prefs.setAppIcon(AppIcon.PURPLE)
                                IconManager.setIcon(context, AppIcon.PURPLE)
                                Toast.makeText(context, context.getString(R.string.icon_changed), Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }

                // Разрешение QR
                SectionTitle(stringResource(R.string.settings_qr_resolution))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = resExpanded,
                            onExpandedChange = { resExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = resolutionLabel(currentRes),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.settings_qr_resolution)) },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = resExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            )
                            ExposedDropdownMenu(
                                expanded = resExpanded,
                                onDismissRequest = { resExpanded = false }
                            ) {
                                QrResolution.values().forEach { res ->
                                    DropdownMenuItem(
                                        text = { Text(resolutionLabel(res)) },
                                        onClick = {
                                            scope.launch { prefs.setQrResolution(res) }
                                            resExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            BannerAd()
        }
    }
}

@Composable
private fun resolutionLabel(res: QrResolution): String = when (res) {
    QrResolution.HD -> stringResource(R.string.resolution_hd)
    QrResolution.TWO_K -> stringResource(R.string.resolution_2k)
    QrResolution.FOUR_K -> stringResource(R.string.resolution_4k)
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun IconOption(
    icon: AppIcon,
    drawableRes: Int,
    current: AppIcon,
    onClick: () -> Unit
) {
    val selected = current == icon
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Image(
            painter = painterResource(drawableRes),
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(3.dp, borderColor, CircleShape)
        )
        Text(
            text = when (icon) {
                AppIcon.BLUE -> stringResource(R.string.icon_blue)
                AppIcon.DARK -> stringResource(R.string.icon_dark)
                AppIcon.GREEN -> stringResource(R.string.icon_green)
                AppIcon.PURPLE -> stringResource(R.string.icon_purple)
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
