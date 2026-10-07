package com.luciferdennica.qrtools.ui.screens.result

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.luciferdennica.qrtools.R
import com.luciferdennica.qrtools.data.db.ScanEntity
import com.luciferdennica.qrtools.data.repo.HistoryRepository
import com.luciferdennica.qrtools.domain.model.ScanType
import com.luciferdennica.qrtools.ui.components.NoteDialog
import com.luciferdennica.qrtools.ui.components.Placeholder
import com.luciferdennica.qrtools.ui.nav.Routes
import com.luciferdennica.qrtools.util.ClipboardUtils
import com.luciferdennica.qrtools.util.ContactData
import com.luciferdennica.qrtools.util.IntentUtils
import com.luciferdennica.qrtools.util.OffProduct
import com.luciferdennica.qrtools.util.OpenFoodFacts
import com.luciferdennica.qrtools.util.VCardParser
import com.luciferdennica.qrtools.util.WifiConnector
import com.luciferdennica.qrtools.util.WifiData
import com.luciferdennica.qrtools.util.WifiParser
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(nav: NavController, repo: HistoryRepository, id: Long) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var item by remember { mutableStateOf<ScanEntity?>(null) }
    var showRaw by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(id) { item = repo.getById(id) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.scan_result)) },
                navigationIcon = {
                    IconButton(onClick = {
                        nav.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        val data = item
        if (data == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Placeholder("...")
            }
        } else {
            ResultContent(
                data = data,
                showRaw = showRaw,
                onToggleRaw = { showRaw = !showRaw },
                onToggleFavorite = {
                    scope.launch {
                        repo.setFavorite(data.id, !data.isFavorite)
                        item = repo.getById(data.id)
                    }
                },
                onEditNote = { showNoteDialog = true },
                onScanAgain = {
                    nav.navigate(Routes.SCANNER) { popUpTo(Routes.HOME) }
                },
                context = context
            )
        }
    }

    // Диалог заметки
    val currentItem = item
    if (showNoteDialog && currentItem != null) {
        NoteDialog(
            initialNote = currentItem.note,
            onSave = { note ->
                scope.launch {
                    repo.setNote(currentItem.id, note)
                    item = repo.getById(currentItem.id)
                    showNoteDialog = false
                    Toast.makeText(
                        context,
                        context.getString(R.string.note_saved),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onDelete = {
                scope.launch {
                    repo.setNote(currentItem.id, "")
                    item = repo.getById(currentItem.id)
                    showNoteDialog = false
                }
            },
            onDismiss = { showNoteDialog = false }
        )
    }
}

@Composable
private fun ResultContent(
    data: ScanEntity,
    showRaw: Boolean,
    onToggleRaw: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEditNote: () -> Unit,
    onScanAgain: () -> Unit,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val typeTitle = runCatching {
            stringResource(ScanType.valueOf(data.type).titleRes)
        }.getOrDefault(data.type)
        Text(
            text = typeTitle,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        // Основная карточка в зависимости от типа
        when {
            data.type == ScanType.WIFI.name -> {
                val wifi = WifiParser.parse(data.content)
                if (wifi != null) WifiCard(wifi) else RawCard(data.content)
            }
            data.type == ScanType.CONTACT.name -> {
                val contact = VCardParser.parse(data.content)
                if (contact != null) ContactCard(contact) else RawCard(data.content)
            }
            OpenFoodFacts.isFoodBarcode(data.format, data.content) -> {
                RawCard(data.content)
                OpenFoodFactsCard(data.content)
            }
            else -> RawCard(data.content)
        }

        // Заметка (если есть)
        if (data.note.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        text = "📝 ${stringResource(R.string.note_title)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = data.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Кнопка «Открыть» для URL/email/phone
        if (data.type == ScanType.URL.name ||
            data.type == ScanType.EMAIL.name ||
            data.type == ScanType.PHONE.name
        ) {
            Button(
                onClick = { openByType(context, data) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.action_open))
            }
        }

        OutlinedButton(
            onClick = {
                ClipboardUtils.copy(context, data.content, context.getString(R.string.copied))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.action_copy))
        }

        OutlinedButton(
            onClick = { IntentUtils.shareText(context, data.content) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.action_share))
        }

        // Заметка — кнопка
        OutlinedButton(
            onClick = onEditNote,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.size(8.dp))
            Text(
                stringResource(
                    if (data.note.isBlank()) R.string.note_add else R.string.note_edit
                )
            )
        }

        OutlinedButton(
            onClick = onToggleFavorite,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (data.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.size(8.dp))
            Text(stringResource(if (data.isFavorite) R.string.action_unsave else R.string.action_save))
        }

        Button(
            onClick = onScanAgain,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.action_scan_again))
        }

        TextButton(
            onClick = onToggleRaw,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(if (showRaw) R.string.hide_raw else R.string.show_raw))
        }

        if (showRaw) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = data.content,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun RawCard(content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Text(
            text = content,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun OpenFoodFactsCard(barcode: String) {
    val context = LocalContext.current
    var loading by remember { mutableStateOf(true) }
    var product by remember { mutableStateOf<OffProduct?>(null) }
    var error by remember { mutableStateOf(false) }

    LaunchedEffect(barcode) {
        loading = true
        error = false
        val result = OpenFoodFacts.fetch(barcode)
        product = result
        error = result == null
        loading = false
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.off_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            when {
                loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.size(12.dp))
                    Text(stringResource(R.string.off_loading))
                }

                error -> Text(
                    text = stringResource(R.string.off_not_found),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                product != null -> {
                    val p = product!!
                    if (!p.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = p.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }

                    if (p.name.isNotBlank()) {
                        Text(
                            text = p.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (p.brand.isNotBlank()) {
                        HorizontalDivider()
                        InfoRow(label = stringResource(R.string.off_brand), value = p.brand)
                    }
                    if (p.quantity.isNotBlank()) {
                        HorizontalDivider()
                        InfoRow(label = stringResource(R.string.off_quantity), value = p.quantity)
                    }
                    if (p.categories.isNotBlank()) {
                        HorizontalDivider()
                        InfoRow(
                            label = stringResource(R.string.off_categories),
                            value = p.categories.split(",").take(3).joinToString(", ").trim()
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            IntentUtils.openUrlSafe(
                                context,
                                "https://world.openfoodfacts.org/product/$barcode"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.off_show_details))
                    }
                }
            }
        }
    }
}

@Composable
private fun WifiCard(data: WifiData) {
    val context = LocalContext.current
    var connecting by remember { mutableStateOf(false) }

    val permLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            connecting = true
            WifiConnector.connect(context, data) { ok ->
                connecting = false
                if (!ok) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.wifi_connect_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InfoRow(label = stringResource(R.string.wifi_network), value = data.ssid)
            HorizontalDivider()
            InfoRow(
                label = stringResource(R.string.wifi_password),
                value = data.password.ifEmpty { stringResource(R.string.wifi_no_password) }
            )
            HorizontalDivider()
            InfoRow(
                label = stringResource(R.string.wifi_security),
                value = WifiParser.securityLabel(data.security, stringResource(R.string.wifi_open))
            )
            if (data.hidden) {
                HorizontalDivider()
                InfoRow(
                    label = stringResource(R.string.wifi_hidden),
                    value = stringResource(R.string.wifi_hidden_yes)
                )
            }

            Spacer(Modifier.height(6.dp))

            Button(
                onClick = {
                    val need = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                    if (need) {
                        permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    } else {
                        connecting = true
                        WifiConnector.connect(context, data) { ok ->
                            connecting = false
                            if (!ok) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.wifi_connect_failed),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                },
                enabled = !connecting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(stringResource(if (connecting) R.string.wifi_connecting else R.string.wifi_connect))
            }
        }
    }
}

@Composable
private fun ContactCard(data: ContactData) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (data.name.isNotBlank()) {
                InfoRow(label = stringResource(R.string.contact_name), value = data.name)
            }
            if (data.phone.isNotBlank()) {
                HorizontalDivider()
                InfoRow(label = stringResource(R.string.contact_phone), value = data.phone)
            }
            if (data.email.isNotBlank()) {
                HorizontalDivider()
                InfoRow(label = stringResource(R.string.contact_email), value = data.email)
            }

            if (data.phone.isNotBlank() || data.email.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (data.phone.isNotBlank()) {
                        OutlinedButton(
                            onClick = { IntentUtils.openUrl(context, "tel:${data.phone}") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.contact_call))
                        }
                    }
                    if (data.email.isNotBlank()) {
                        OutlinedButton(
                            onClick = { IntentUtils.openEmail(context, data.email) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.action_send_email))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f)
        )
    }
}

private fun openByType(context: Context, data: ScanEntity) {
    when (data.type) {
        ScanType.URL.name -> IntentUtils.openUrl(context, data.content)
        ScanType.EMAIL.name -> IntentUtils.openEmail(context, data.content.removePrefix("mailto:"))
        ScanType.PHONE.name -> {
            val num = data.content.removePrefix("tel:")
            IntentUtils.openUrl(context, "tel:$num")
        }
        else -> IntentUtils.openUrl(context, data.content)
    }
}
