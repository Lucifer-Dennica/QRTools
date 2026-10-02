package com.luciferdennica.qrtools.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.luciferdennica.qrtools.R
import com.luciferdennica.qrtools.data.prefs.SettingsPrefs
import com.luciferdennica.qrtools.data.repo.HistoryRepository
import com.luciferdennica.qrtools.domain.model.ThemeMode
import com.luciferdennica.qrtools.ui.nav.Routes
import com.luciferdennica.qrtools.util.CsvExporter
import com.luciferdennica.qrtools.util.IntentUtils
import kotlinx.coroutines.launch

private const val SUPPORT_EMAIL = "denis22142qwe@gmail.com"
private const val SUPPORT_TELEGRAM = "Lucifer_Denicca_22142"
private const val DONATE_URL = "https://www.donationalerts.com/r/lucifer_dennica_1999"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    nav: NavController,
    prefs: SettingsPrefs,
    repo: HistoryRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentTheme by prefs.theme.collectAsState(initial = ThemeMode.SYSTEM)
    val sound by prefs.sound.collectAsState(initial = true)
    val vibro by prefs.vibro.collectAsState(initial = true)
    val autoCopy by prefs.autoCopy.collectAsState(initial = false)

    var showClearDialog by remember { mutableStateOf(false) }
    var themeExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ===== ТЕМА (выпадающий список) =====
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
                            value = currentTheme.title,
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
                                    text = { Text(mode.title) },
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

            // ===== ПОВЕДЕНИЕ =====
            SectionTitle(stringResource(R.string.settings_behavior))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.fillMaxWidth().padding(8.dp)) {
                    SettingSwitch(
                        title = stringResource(R.string.settings_sound),
                        checked = sound,
                        onCheckedChange = { scope.launch { prefs.setSound(it) } }
                    )
                    HorizontalDivider()
                    SettingSwitch(
                        title = stringResource(R.string.settings_vibro),
                        checked = vibro,
                        onCheckedChange = { scope.launch { prefs.setVibro(it) } }
                    )
                    HorizontalDivider()
                    SettingSwitch(
                        title = stringResource(R.string.settings_auto_copy),
                        checked = autoCopy,
                        onCheckedChange = { scope.launch { prefs.setAutoCopy(it) } }
                    )
                }
            }

            // ===== ИСТОРИЯ =====
            SectionTitle(stringResource(R.string.settings_history))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val items = repo.getAllOnce()
                                if (items.isEmpty()) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.csv_no_data),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    val ok = CsvExporter.exportAndShare(context, items)
                                    Toast.makeText(
                                        context,
                                        context.getString(
                                            if (ok) R.string.csv_exported else R.string.csv_export_failed
                                        ),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.settings_export_csv))
                    }

                    OutlinedButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.settings_clear_history))
                    }
                }
            }

            // ===== ПОДДЕРЖАТЬ АВТОРА =====
            SectionTitle(stringResource(R.string.settings_support))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_support_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    OutlinedButton(
                        onClick = {
                            val ok = IntentUtils.openUrlSafe(context, DONATE_URL)
                            Toast.makeText(
                                context,
                                context.getString(
                                    if (ok) R.string.donate_success else R.string.link_error
                                ),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(stringResource(R.string.settings_donate))
                    }
                }
            }

            // ===== ОБРАТНАЯ СВЯЗЬ =====
            SectionTitle(stringResource(R.string.settings_feedback))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            IntentUtils.openEmail(
                                context,
                                SUPPORT_EMAIL,
                                "QR Tools — обратная связь"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(stringResource(R.string.settings_email))
                    }

                    OutlinedButton(
                        onClick = {
                            IntentUtils.openUrlSafe(context, "https://t.me/$SUPPORT_TELEGRAM")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(stringResource(R.string.settings_telegram))
                    }
                }
            }

            // ===== О ПРИЛОЖЕНИИ =====
            SectionTitle(stringResource(R.string.settings_about))
            Card(
                onClick = { nav.navigate(Routes.ABOUT) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.padding(start = 12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.settings_about),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(R.string.settings_about_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.clear_history_title)) },
            text = { Text(stringResource(R.string.clear_history_desc)) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        repo.clearAll()
                        Toast.makeText(
                            context,
                            context.getString(R.string.history_cleared),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    showClearDialog = false
                }) {
                    Text(stringResource(R.string.clear_history_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
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
private fun SettingSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
