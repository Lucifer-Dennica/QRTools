package com.luciferdennica.qrtools.ui.screens.scanner

import android.Manifest
import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.luciferdennica.qrtools.R
import com.luciferdennica.qrtools.ads.AdIds
import com.luciferdennica.qrtools.ads.AdsManager
import com.luciferdennica.qrtools.data.prefs.SettingsPrefs
import com.luciferdennica.qrtools.data.repo.HistoryRepository
import com.luciferdennica.qrtools.review.RuStoreReview
import com.luciferdennica.qrtools.scan.QrCodeAnalyzer
import com.luciferdennica.qrtools.scan.ScanFromBitmap
import com.luciferdennica.qrtools.ui.nav.Routes
import com.luciferdennica.qrtools.util.TypeDetector
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    nav: NavController,
    repo: HistoryRepository,
    prefs: SettingsPrefs
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    var batchProcessing by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
        AdsManager.preloadInterstitial(context)
    }

    fun scanFromClipboard() {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = cm.primaryClip
        if (clip == null || clip.itemCount == 0) {
            Toast.makeText(context, context.getString(R.string.clipboard_no_image), Toast.LENGTH_SHORT).show()
            return
        }

        val item = clip.getItemAt(0)
        val uri = item.uri
        if (uri == null) {
            Toast.makeText(context, context.getString(R.string.clipboard_no_image), Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            val bitmap = loadBitmapFromUri(context, uri)
            if (bitmap == null) {
                Toast.makeText(context, context.getString(R.string.clipboard_no_image), Toast.LENGTH_SHORT).show()
                return@launch
            }
            val barcode = ScanFromBitmap.decode(bitmap)
            val raw = barcode?.rawValue
            if (raw.isNullOrBlank()) {
                Toast.makeText(context, context.getString(R.string.no_code_in_image), Toast.LENGTH_SHORT).show()
            } else {
                val type = TypeDetector.detect(raw, barcode.format.toString())
                val saveHistory = prefs.getSaveHistoryOnce()
                val id = repo.add(raw, barcode.format.toString(), type, saveHistory)
                navigateAfterScan(nav, context, prefs, id) { showReviewDialog = true }
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult

        if (uris.size == 1) {
            QrCodeAnalyzer.decodeFromUri(
                context = context,
                uri = uris.first(),
                onResult = { barcode ->
                    val raw = barcode.rawValue
                    if (!raw.isNullOrBlank()) {
                        scope.launch {
                            val type = TypeDetector.detect(raw, barcode.format.toString())
                            val saveHistory = prefs.getSaveHistoryOnce()
                            val id = repo.add(raw, barcode.format.toString(), type, saveHistory)
                            navigateAfterScan(nav, context, prefs, id) { showReviewDialog = true }
                        }
                    } else {
                        Toast.makeText(context, context.getString(R.string.no_code_in_image), Toast.LENGTH_SHORT).show()
                    }
                },
                onError = {
                    Toast.makeText(context, context.getString(R.string.no_code_in_image), Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            batchProcessing = true
            scope.launch {
                val saveHistory = prefs.getSaveHistoryOnce()
                val results = QrCodeAnalyzer.decodeFromUris(context, uris)

                var lastId: Long = -1
                results.forEach { barcode ->
                    val raw = barcode.rawValue
                    if (!raw.isNullOrBlank()) {
                        val type = TypeDetector.detect(raw, barcode.format.toString())
                        lastId = repo.add(raw, barcode.format.toString(), type, saveHistory)
                    }
                }

                batchProcessing = false

                if (results.isEmpty()) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.batch_nothing),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.batch_result, results.size, uris.size),
                        Toast.LENGTH_LONG
                    ).show()
                    if (lastId > 0) {
                        nav.navigate(Routes.result(lastId)) {
                            popUpTo(Routes.SCANNER) { inclusive = true }
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.scanner)) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        if (!hasPermission) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.permission_camera_title), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.permission_camera_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text(stringResource(R.string.permission_grant))
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize().padding(padding)) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onDetected = { content, format ->
                        val vibroOn = prefs.vibro.valueOrNull() ?: true
                        val soundOn = prefs.sound.valueOrNull() ?: true
                        if (vibroOn) vibrate(context)
                        if (soundOn) playBeep()

                        scope.launch {
                            val type = TypeDetector.detect(content, format)
                            val saveHistory = prefs.getSaveHistoryOnce()
                            val id = repo.add(content, format, type, saveHistory)
                            navigateAfterScan(nav, context, prefs, id) { showReviewDialog = true }
                        }
                    }
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { scanFromClipboard() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !batchProcessing
                    ) {
                        Icon(
                            Icons.Default.ContentPaste,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "  " + stringResource(R.string.from_clipboard),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !batchProcessing
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "  " + if (batchProcessing) "…" else stringResource(R.string.batch_scan),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
        }
    }

    // ===== Диалог предложения оценить приложение =====
    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = {
                showReviewDialog = false
                scope.launch { prefs.setLastReviewTime(System.currentTimeMillis()) }
            },
            title = { Text(stringResource(R.string.review_dialog_title)) },
            text = { Text(stringResource(R.string.review_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showReviewDialog = false
                        scope.launch {
                            prefs.setLastReviewTime(System.currentTimeMillis())
                            prefs.setReviewDontAsk()
                        }
                        val activity = context as? Activity
                        if (activity != null && RuStoreReview.isRuStoreAvailable(context)) {
                            RuStoreReview.requestReview(activity)
                        }
                    }
                ) {
                    Text(stringResource(R.string.review_dialog_yes))
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            showReviewDialog = false
                            scope.launch {
                                prefs.setLastReviewTime(System.currentTimeMillis())
                                prefs.setReviewDontAsk()
                            }
                        }
                    ) {
                        Text(stringResource(R.string.review_dialog_never))
                    }
                    TextButton(
                        onClick = {
                            showReviewDialog = false
                            scope.launch { prefs.setLastReviewTime(System.currentTimeMillis()) }
                        }
                    ) {
                        Text(stringResource(R.string.review_dialog_later))
                    }
                }
            }
        )
    }
}

private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = false
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }.getOrNull()
}

private fun <T> kotlinx.coroutines.flow.Flow<T>.valueOrNull(): T? {
    return (this as? kotlinx.coroutines.flow.StateFlow<T>)?.value
}

private suspend fun navigateAfterScan(
    nav: NavController,
    context: Context,
    prefs: SettingsPrefs,
    id: Long,
    onShowReview: () -> Unit = {}
) {
    val count = prefs.incrementScanCounter()

    // Проверяем — пора ли показать диалог оценки.
    // 1-й раз: после 10 сканов. 2-й и далее: раз в 30 дней.
    val dontAsk = prefs.shouldShowReviewDialog()
    val lastReview = prefs.getLastReviewTime()
    val daysSinceLast = (System.currentTimeMillis() - lastReview) / (1000L * 60 * 60 * 24)

    if (dontAsk) {
        onShowReview()
    } else if (count >= 50 && lastReview > 0L && daysSinceLast >= 30) {
        onShowReview()
    }

    val goNext = {
        nav.navigate(Routes.result(id)) {
            popUpTo(Routes.SCANNER) { inclusive = true }
        }
    }
    if (count >= AdIds.SCAN_INTERVAL) {
        prefs.resetScanCounter()
        val activity = context as? Activity
        if (activity != null) {
            AdsManager.showInterstitial(activity) { goNext() }
            AdsManager.preloadInterstitial(context)
        } else {
            goNext()
        }
    } else {
        goNext()
    }
}

@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    onDetected: (String, String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    var handled by remember { mutableStateOf(false) }

    val analyzer = remember {
        QrCodeAnalyzer { barcode ->
            if (!handled) {
                val raw = barcode.rawValue
                if (!raw.isNullOrBlank()) {
                    handled = true
                    onDetected(raw, barcode.format.toString())
                }
            }
        }
    }

    Box(modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { it.setAnalyzer(executor, analyzer) }

                    val selector = CameraSelector.DEFAULT_BACK_CAMERA

                    runCatching {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, analysis)
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(260.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
        )
        Text(
            text = stringResource(R.string.scan_hint),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 40.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }

    DisposableEffect(Unit) {
        onDispose { executor.shutdown() }
    }
}

private fun vibrate(context: Context) {
    try {
        val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (vibrator == null || !vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100L, 255))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100L)
        }
    } catch (_: Exception) {
    }
}

private fun playBeep() {
    runCatching {
        val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
        tone.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            tone.release()
        }, 200)
    }
}
