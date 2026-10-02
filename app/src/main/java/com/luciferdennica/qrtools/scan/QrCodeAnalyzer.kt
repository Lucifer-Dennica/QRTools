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
        /**
         * Декодирует QR/штрихкод из картинки (URI из галереи).
         * onResult(barcode) — если найдено.
         * onError — если не найдено или ошибка.
         */
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
    }
}
