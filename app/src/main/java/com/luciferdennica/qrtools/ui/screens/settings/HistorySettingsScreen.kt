package com.luciferdennica.qrtools.ui.screens.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.luciferdennica.qrtools.R
import com.luciferdennica.qrtools.data.prefs.SettingsPrefs
import com.luciferdennica.qrtools.data.repo.HistoryRepository
import com.luciferdennica.qrtools.ui.components.BannerAd
import com.luciferdennica.qrtools.util.CsvExporter
import com.luciferdennica.qrtools.util.CsvImporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySettingsScreen(
    nav: NavController,
    prefs: SettingsPrefs,
    repo: HistoryRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val count = CsvImporter.import(context, uri, repo)
            val msg = when {
                count < 0 -> context.getString(R.string.import_empty)
                else -> context.getString(R.string.import_success, count)
            }
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_history)) },
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                            onClick = { showImportDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.settings_import_csv))
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    repo.clearHidden()
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.hidden_cleared),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.settings_clear_hidden))
                        }

                        OutlinedButton(
                            onClick = { showClearDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.settings_clear_history))
                        }
                    }
                }
            }

            BannerAd()
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

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(stringResource(R.string.import_confirm_title)) },
            text = { Text(stringResource(R.string.import_confirm_desc)) },
            confirmButton = {
                TextButton(onClick = {
                    showImportDialog = false
                    importLauncher.launch("text/*")
                }) {
                    Text(stringResource(R.string.import_confirm_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
