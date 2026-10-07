package com.luciferdennica.qrtools.ui.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.luciferdennica.qrtools.App
import com.luciferdennica.qrtools.R
import com.luciferdennica.qrtools.data.prefs.SwipeAction
import com.luciferdennica.qrtools.ui.components.BannerAd
import com.luciferdennica.qrtools.ui.nav.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as App
    val prefs = app.prefs

    val swipeLeft by prefs.swipeLeft.collectAsState(initial = SwipeAction.OFF)
    val swipeRight by prefs.swipeRight.collectAsState(initial = SwipeAction.OFF)
    val swipeUp by prefs.swipeUp.collectAsState(initial = SwipeAction.OFF)
    val swipeDown by prefs.swipeDown.collectAsState(initial = SwipeAction.OFF)

    fun perform(action: SwipeAction) {
        if (action == SwipeAction.OFF) return
        val route = when (action) {
            SwipeAction.SCANNER -> Routes.SCANNER
            SwipeAction.GENERATOR -> Routes.GENERATOR
            SwipeAction.HISTORY -> Routes.HISTORY
            SwipeAction.FAVORITES -> Routes.FAVORITES
            SwipeAction.SETTINGS -> Routes.SETTINGS
            SwipeAction.OFF -> return
        }
        nav.navigate(route)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_name),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { nav.navigate(Routes.SETTINGS) }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(swipeLeft, swipeRight, swipeUp, swipeDown) {
                    var totalHorizontal = 0f
                    var totalVertical = 0f
                    val threshold = 120f

                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (totalHorizontal > threshold) perform(swipeRight)
                            else if (totalHorizontal < -threshold) perform(swipeLeft)
                            totalHorizontal = 0f
                        }
                    ) { _, dragAmount ->
                        totalHorizontal += dragAmount
                    }
                }
                .pointerInput(swipeLeft, swipeRight, swipeUp, swipeDown) {
                    var total = 0f
                    val threshold = 120f
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (total > threshold) perform(swipeDown)
                            else if (total < -threshold) perform(swipeUp)
                            total = 0f
                        }
                    ) { _, dragAmount ->
                        total += dragAmount
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                GradientCard(
                    icon = Icons.Default.QrCodeScanner,
                    title = stringResource(R.string.scan_qr),
                    subtitle = stringResource(R.string.home_scan_sub),
                    colors = listOf(Color(0xFF2196F3), Color(0xFF0D47A1)),
                    onClick = { nav.navigate(Routes.SCANNER) }
                )
                GradientCard(
                    icon = Icons.Default.QrCode,
                    title = stringResource(R.string.create_qr),
                    subtitle = stringResource(R.string.home_create_sub),
                    colors = listOf(Color(0xFFAB47BC), Color(0xFF4A148C)),
                    onClick = { nav.navigate(Routes.GENERATOR) }
                )
                GradientCard(
                    icon = Icons.Default.History,
                    title = stringResource(R.string.history),
                    subtitle = stringResource(R.string.home_history_sub),
                    colors = listOf(Color(0xFFFB8C00), Color(0xFFE65100)),
                    onClick = { nav.navigate(Routes.HISTORY) }
                )
                GradientCard(
                    icon = Icons.Default.FavoriteBorder,
                    title = stringResource(R.string.favorites),
                    subtitle = stringResource(R.string.home_favorites_sub),
                    colors = listOf(Color(0xFFEC407A), Color(0xFFAD1457)),
                    onClick = { nav.navigate(Routes.FAVORITES) }
                )
            }
            BannerAd()
        }
    }
}

@Composable
private fun GradientCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    colors: List<Color>,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        label = "cardScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(colors))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(20.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.20f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 76.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(24.dp)
        )
    }
}
