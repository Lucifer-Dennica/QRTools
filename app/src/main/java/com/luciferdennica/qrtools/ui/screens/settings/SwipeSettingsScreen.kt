package com.luciferdennica.qrtools.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.luciferdennica.qrtools.R
import com.luciferdennica.qrtools.data.prefs.SettingsPrefs
import com.luciferdennica.qrtools.data.prefs.SwipeAction
import com.luciferdennica.qrtools.ui.components.BannerAd
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeSettingsScreen(nav: NavController, prefs: SettingsPrefs) {
    val scope = rememberCoroutineScope()

    val swipeLeft by prefs.swipeLeft.collectAsState(initial = SwipeAction.OFF)
    val swipeRight by prefs.swipeRight.collectAsState(initial = SwipeAction.OFF)
    val swipeUp by prefs.swipeUp.collectAsState(initial = SwipeAction.OFF)
    val swipeDown by prefs.swipeDown.collectAsState(initial = SwipeAction.OFF)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_swipes)) },
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
                SwipeActionRow(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    title = stringResource(R.string.swipe_left),
                    current = swipeLeft,
                    onChange = { scope.launch { prefs.setSwipeLeft(it) } }
                )
                SwipeActionRow(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    title = stringResource(R.string.swipe_right),
                    current = swipeRight,
                    onChange = { scope.launch { prefs.setSwipeRight(it) } }
                )
                SwipeActionRow(
                    icon = Icons.Default.KeyboardArrowUp,
                    title = stringResource(R.string.swipe_up),
                    current = swipeUp,
                    onChange = { scope.launch { prefs.setSwipeUp(it) } }
                )
                SwipeActionRow(
                    icon = Icons.Default.KeyboardArrowDown,
                    title = stringResource(R.string.swipe_down),
                    current = swipeDown,
                    onChange = { scope.launch { prefs.setSwipeDown(it) } }
                )
            }

            BannerAd()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeActionRow(
    icon: ImageVector,
    title: String,
    current: SwipeAction,
    onChange: (SwipeAction) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = actionLabel(current),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        SwipeAction.values().forEach { action ->
                            DropdownMenuItem(
                                text = { Text(actionLabel(action)) },
                                onClick = {
                                    onChange(action)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun actionLabel(action: SwipeAction): String = when (action) {
    SwipeAction.OFF -> stringResource(R.string.swipe_off)
    SwipeAction.SCANNER -> stringResource(R.string.swipe_scanner)
    SwipeAction.GENERATOR -> stringResource(R.string.swipe_generator)
    SwipeAction.HISTORY -> stringResource(R.string.swipe_history)
    SwipeAction.FAVORITES -> stringResource(R.string.swipe_favorites)
    SwipeAction.SETTINGS -> stringResource(R.string.swipe_settings)
}
