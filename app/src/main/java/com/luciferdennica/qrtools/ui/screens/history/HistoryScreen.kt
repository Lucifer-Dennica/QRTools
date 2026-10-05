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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Элемент списка: либо заголовок дня, либо запись */
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

    val items by (if (query.isBlank()) repo.getAll() else repo.search(query))
        .collectAsState(initial = emptyList())

    // Группировка по дням
    val listItems = remember(items) { buildGroupedList(items) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history)) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Поле поиска
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
                                onClick = {
                                    if (autoCopy) {
                                        ClipboardUtils.copy(context, item.entity.content, context.getString(R.string.copied))
                                    }
                                    nav.navigate("result/${item.entity.id}")
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
}

// ========================================================
// КОМПОНЕНТЫ
// ========================================================

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
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val typeTitle = runCatching { ScanType.valueOf(item.type).title }.getOrDefault(item.type)
    val accent = colorForType(item.type)
    val icon = iconForType(item.type)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Цветная иконка типа
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

            Column(Modifier.weight(1f)) {
                Text(
                    text = item.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$typeTitle · ${timeFormat.format(Date(item.timestamp))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

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

// ========================================================
// ГРУППИРОВКА ПО ДНЯМ
// ========================================================

private fun buildGroupedList(items: List<ScanEntity>): List<HistoryItem> {
    if (items.isEmpty()) return emptyList()

    val todayFormat = SimpleDateFormat("dd MMMM", Locale.getDefault())
    val calendar = Calendar.getInstance()

    // Начало сегодняшнего дня
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    // Начало вчерашнего дня
    val yesterdayStart = todayStart - 24L * 60 * 60 * 1000

    val result = mutableListOf<HistoryItem>()
    var lastHeader: String? = null

    items.forEach { item ->
        val header = when {
            item.timestamp >= todayStart -> "Сегодня"
            item.timestamp >= yesterdayStart -> "Вчера"
            else -> todayFormat.format(Date(item.timestamp))
        }
        if (header != lastHeader) {
            result.add(HistoryItem.Header(header))
            lastHeader = header
        }
        result.add(HistoryItem.Scan(item))
    }
    return result
}

// ========================================================
// ИКОНКА И ЦВЕТ ПО ТИПУ
// ========================================================

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
