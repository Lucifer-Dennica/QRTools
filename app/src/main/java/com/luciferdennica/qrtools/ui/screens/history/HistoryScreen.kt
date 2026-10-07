package com.luciferdennica.qrtools.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.luciferdennica.qrtools.R
import com.luciferdennica.qrtools.data.db.ScanEntity
import com.luciferdennica.qrtools.data.repo.HistoryRepository
import com.luciferdennica.qrtools.domain.model.ScanType
import com.luciferdennica.qrtools.ui.components.Placeholder
import com.luciferdennica.qrtools.util.ClipboardUtils
import com.luciferdennica.qrtools.util.ScanDisplay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private sealed class HistoryItem {
    data class Header(val title: String) : HistoryItem()
    data class Scan(val entity: ScanEntity) : HistoryItem()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(nav: NavController, repo: HistoryRepository, autoCopy: Boolean) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var selectionMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<Long>() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val items by (if (query.isBlank()) repo.getAll() else repo.search(query))
        .collectAsState(initial = emptyList())

    val todayLabel = stringResource(R.string.history_today)
    val yesterdayLabel = stringResource(R.string.history_yesterday)
    val listItems = remember(items, todayLabel, yesterdayLabel) {
        buildGroupedList(items, todayLabel, yesterdayLabel)
    }

    Scaffold(
        topBar = {
            if (selectionMode) {
                TopAppBar(
                    title = { Text(stringResource(R.string.history_selected, selectedIds.size)) },
                    navigationIcon = {
                        IconButton(onClick = {
                            selectionMode = false
                            selectedIds.clear()
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.history_cancel))
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            if (selectedIds.size == items.size) {
                                selectedIds.clear()
                            } else {
                                selectedIds.clear()
                                selectedIds.addAll(items.map { it.id })
                            }
                        }) {
                            Icon(
                                Icons.Default.DoneAll,
                                contentDescription = stringResource(R.string.history_select_all)
                            )
                        }
                        IconButton(
                            onClick = { if (selectedIds.isNotEmpty()) showDeleteDialog = true },
                            enabled = selectedIds.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.history_delete_selected))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            } else {
                TopAppBar(
                    title = { Text(stringResource(R.string.history)) },
                    navigationIcon = {
                        IconButton(onClick = { nav.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    },
                    actions = {
                        if (items.isNotEmpty()) {
                            TextButton(onClick = { selectionMode = true }) {
                                Text(stringResource(R.string.history_select))
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (!selectionMode) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(stringResource(R.string.search_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (items.isEmpty()) {
                Box(Modifier.fillMaxSize()) {
                    Placeholder(stringResource(R.string.history_empty))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listItems, key = { item ->
                        when (item) {
                            is HistoryItem.Header -> "header_${item.title}"
                            is HistoryItem.Scan -> "scan_${item.entity.id}"
                        }
                    }) { item ->
                        when (item) {
                            is HistoryItem.Header -> DayHeader(item.title)
                            is HistoryItem.Scan -> ScanRow(
                                item = item.entity,
                                selectionMode = selectionMode,
                                selected = selectedIds.contains(item.entity.id),
                                onToggleSelect = {
                                    if (selectedIds.contains(item.entity.id)) {
                                        selectedIds.remove(item.entity.id)
                                    } else {
                                        selectedIds.add(item.entity.id)
                                    }
                                },
                                onClick = {
                                    if (selectionMode) {
                                        if (selectedIds.contains(item.entity.id)) {
                                            selectedIds.remove(item.entity.id)
                                        } else {
                                            selectedIds.add(item.entity.id)
                                        }
                                    } else {
                                        if (autoCopy) {
                                            ClipboardUtils.copy(context, item.entity.content, context.getString(R.string.copied))
                                        }
                                        nav.navigate("result/${item.entity.id}")
                                    }
                                },
                                onToggleFavorite = { scope.launch { repo.toggleFavorite(item.entity) } },
                                onDelete = { scope.launch { repo.delete(item.entity) } }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.history_delete_confirm_title)) },
            text = { Text(stringResource(R.string.history_delete_confirm_desc, selectedIds.size)) },
            confirmButton = {
                TextButton(onClick = {
                    val toDelete = selectedIds.toList()
                    scope.launch {
                        repo.deleteByIds(toDelete)
                        selectedIds.clear()
                        selectionMode = false
                        showDeleteDialog = false
                    }
                }) {
                    Text(stringResource(R.string.clear_history_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun DayHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun ScanRow(
    item: ScanEntity,
    selectionMode: Boolean,
    selected: Boolean,
    onToggleSelect: () -> Unit,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val typeTitle = runCatching {
        stringResource(ScanType.valueOf(item.type).titleRes)
    }.getOrDefault(item.type)
    val accent = colorForType(item.type)
    val icon = iconForType(item.type)
    val displayTitle = ScanDisplay.shortTitle(item.content, item.type)

    Card(
        onClick = { if (selectionMode) onToggleSelect() else onClick() },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectionMode) {
                Icon(
                    imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.size(12.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.size(12.dp))
            }

            Column(Modifier.weight(1f)) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (item.note.isNotBlank()) {
                    Text(
                        text = "📝 ${item.note}",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Text(
                    text = "$typeTitle · ${timeFormat.format(Date(item.timestamp))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            if (!selectionMode) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(if (item.isFavorite) R.string.unfavorite else R.string.favorite),
                        tint = if (item.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
        }
    }
}

private fun buildGroupedList(
    items: List<ScanEntity>,
    todayLabel: String,
    yesterdayLabel: String
): List<HistoryItem> {
    if (items.isEmpty()) return emptyList()

    val dateFormat = SimpleDateFormat("dd MMMM", Locale.getDefault())

    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val yesterdayStart = todayStart - 24L * 60 * 60 * 1000

    val result = mutableListOf<HistoryItem>()
    var lastHeader: String? = null

    items.forEach { item ->
        val header = when {
            item.timestamp >= todayStart -> todayLabel
            item.timestamp >= yesterdayStart -> yesterdayLabel
            else -> dateFormat.format(Date(item.timestamp))
        }
        if (header != lastHeader) {
            result.add(HistoryItem.Header(header))
            lastHeader = header
        }
        result.add(HistoryItem.Scan(item))
    }
    return result
}

private fun iconForType(type: String): ImageVector = when (type) {
    ScanType.URL.name -> Icons.Default.Link
    ScanType.WIFI.name -> Icons.Default.Wifi
    ScanType.CONTACT.name -> Icons.Default.Person
    ScanType.SMS.name -> Icons.Default.Sms
    ScanType.EMAIL.name -> Icons.Default.Email
    ScanType.PHONE.name -> Icons.Default.Phone
    ScanType.BARCODE.name -> Icons.Default.QrCode
    ScanType.TEXT.name -> Icons.Default.Notes
    else -> Icons.Default.Info
}

private fun colorForType(type: String): Color = when (type) {
    ScanType.URL.name -> Color(0xFF2196F3)
    ScanType.WIFI.name -> Color(0xFF00ACC1)
    ScanType.CONTACT.name -> Color(0xFF7E57C2)
    ScanType.SMS.name -> Color(0xFF43A047)
    ScanType.EMAIL.name -> Color(0xFFE53935)
    ScanType.PHONE.name -> Color(0xFFFB8C00)
    ScanType.BARCODE.name -> Color(0xFF6D4C41)
    ScanType.TEXT.name -> Color(0xFF546E7A)
    else -> Color(0xFF757575)
}
