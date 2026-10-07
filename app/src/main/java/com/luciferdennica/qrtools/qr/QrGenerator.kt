package com.luciferdennica.qrtools.qr

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.ui.graphics.toArgb
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrGenerator {

    fun generate(
        content: String,
        size: Int = 1080,
        style: QrStyle = QrStyle()
    ): Bitmap? {
        if (content.isBlank()) return null
        return runCatching {
            val errorLevel = if (style.logo != null) ErrorCorrectionLevel.H else ErrorCorrectionLevel.M

            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to errorLevel,
                EncodeHintType.MARGIN to 1,
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )

            val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)

            // Для больших размеров используем RGB_565 (в 2 раза меньше памяти)
            val config = if (size >= 2000) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888
            val bmp = Bitmap.createBitmap(size, size, config)
            val canvas = Canvas(bmp)

            val bgPaint = Paint().apply { isAntiAlias = false }

            // ФОН
            if (style.bgGradient) {
                bgPaint.shader = LinearGradient(
                    0f, 0f, size.toFloat(), size.toFloat(),
                    style.bgColor.toArgb(), style.bgColor2.toArgb(),
                    Shader.TileMode.CLAMP
                )
            } else {
                bgPaint.color = style.bgColor.toArgb()
            }
            canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

            // ТОЧКИ — рисуем через один Paint, без antiAlias для скорости
            val cellSize = size.toFloat() / matrix.width
            val dotPaint = Paint().apply { isAntiAlias = false }

            if (!style.dotGradient) {
                // Быстрый путь без градиента — один цвет, один проход
                dotPaint.color = style.dotColor.toArgb()
                for (x in 0 until matrix.width) {
                    val left = x * cellSize
                    val right = left + cellSize
                    for (y in 0 until matrix.height) {
                        if (matrix[x, y]) {
                            canvas.drawRect(left, y * cellSize, right, (y + 1) * cellSize, dotPaint)
                        }
                    }
                }
            } else {
                // Градиент — вычисляем цвет для каждой точки
                val fromArgb = style.dotColor.toArgb()
                val toArgb = style.dotColor2.toArgb()
                val maxT = (matrix.width + matrix.height - 2).toFloat()
                for (x in 0 until matrix.width) {
                    val left = x * cellSize
                    val right = left + cellSize
                    for (y in 0 until matrix.height) {
                        if (matrix[x, y]) {
                            dotPaint.color = interpolate(fromArgb, toArgb, (x + y) / maxT)
                            canvas.drawRect(left, y * cellSize, right, (y + 1) * cellSize, dotPaint)
                        }
                    }
                }
            }

            // ЛОГОТИП
            style.logo?.let { logo ->
                val logoSize = (size * 0.22f).toInt()
                val pad = logoSize * 0.14f
                val left = (size - logoSize) / 2f
                val top = (size - logoSize) / 2f

                val bgLogoPaint = Paint().apply {
                    isAntiAlias = true
                    color = style.logoBackground.toArgb()
                }
                canvas.drawRoundRect(
                    RectF(left - pad, top - pad, left + logoSize + pad, top + logoSize + pad),
                    pad * 1.5f, pad * 1.5f, bgLogoPaint
                )

                val scaled = Bitmap.createScaledBitmap(logo, logoSize, logoSize, true)
                canvas.drawBitmap(scaled, left, top, null)
                // Освобождаем масштабированную копию
                if (scaled != logo) scaled.recycle()
            }

            bmp
        }.getOrNull()
    }

    private fun interpolate(from: Int, to: Int, t: Float): Int {
        val tt = t.coerceIn(0f, 1f)
        val a = ((from shr 24 and 0xFF) + (((to shr 24 and 0xFF) - (from shr 24 and 0xFF)) * tt)).toInt()
        val r = ((from shr 16 and 0xFF) + (((to shr 16 and 0xFF) - (from shr 16 and 0xFF)) * tt)).toInt()
        val g = ((from shr 8 and 0xFF) + (((to shr 8 and 0xFF) - (from shr 8 and 0xFF)) * tt)).toInt()
        val b = ((from and 0xFF) + (((to and 0xFF) - (from and 0xFF)) * tt)).toInt()
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }
}
