package com.luciferdennica.qrtools.scan

import android.content.Context
import android.net.Uri
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class QrCodeAnalyzer(
    private val onDetected: (Barcode) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
    )

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(input)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()?.let(onDetected)
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    companion object {

        /** Декодирует один URI (для одиночного сканирования). */
        fun decodeFromUri(
            context: Context,
            uri: Uri,
            onResult: (Barcode) -> Unit,
            onError: () -> Unit
        ) {
            val scanner = BarcodeScanning.getClient(
                BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                    .build()
            )
            runCatching {
                val image = InputImage.fromFilePath(context, uri)
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        val first = barcodes.firstOrNull()
                        if (first != null) onResult(first) else onError()
                    }
                    .addOnFailureListener { onError() }
            }.onFailure { onError() }
        }

        /** Декодирует список URI. Возвращает список успешно распознанных Barcode. */
        suspend fun decodeFromUris(
            context: Context,
            uris: List<Uri>
        ): List<Barcode> {
            val scanner = BarcodeScanning.getClient(
                BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                    .build()
            )
            val results = mutableListOf<Barcode>()

            for (uri in uris) {
                val barcode = runCatching {
                    val image = InputImage.fromFilePath(context, uri)
                    scanner.process(image).await()
                }.getOrNull()?.firstOrNull()

                if (barcode != null) results.add(barcode)
            }

            return results
        }
    }
}

/** Простой await для Task от ML Kit без лишних зависимостей. */
private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T =
    suspendCancellableCoroutine { cont ->
        addOnSuccessListener { cont.resume(it) }
        addOnFailureListener { cont.cancel(it) }
        addOnCanceledListener { cont.cancel() }
    }
