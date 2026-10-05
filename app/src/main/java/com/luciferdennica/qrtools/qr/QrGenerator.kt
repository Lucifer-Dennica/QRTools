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
        size: Int = 1400,
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
            val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)

            // ФОН
            if (style.bgGradient) {
                val shader = LinearGradient(
                    0f, 0f, size.toFloat(), size.toFloat(),
                    style.bgColor.toArgb(), style.bgColor2.toArgb(),
                    Shader.TileMode.CLAMP
                )
                val paint = Paint().apply { this.shader = shader }
                canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
            } else {
                canvas.drawColor(style.bgColor.toArgb())
            }

            // ТОЧКИ
            val cellSize = size.toFloat() / matrix.width
            val dotPaint = Paint().apply { isAntiAlias = true }

            for (x in 0 until matrix.width) {
                for (y in 0 until matrix.height) {
                    if (matrix[x, y]) {
                        dotPaint.color = if (style.dotGradient) {
                            interpolate(
                                style.dotColor.toArgb(),
                                style.dotColor2.toArgb(),
                                (x + y).toFloat() / (matrix.width + matrix.height - 2)
                            )
                        } else {
                            style.dotColor.toArgb()
                        }

                        val left = x * cellSize
                        val top = y * cellSize
                        val right = left + cellSize
                        val bottom = top + cellSize

                        when (style.dotShape) {
                            DotShape.SQUARE -> canvas.drawRect(left, top, right, bottom, dotPaint)

                            // Более выраженное скругление (0.5 вместо 0.3)
                            DotShape.ROUNDED -> canvas.drawRoundRect(
                                RectF(left, top, right, bottom),
                                cellSize * 0.5f, cellSize * 0.5f, dotPaint
                            )

                            // Круг чуть меньше ячейки, чтобы был зазор между точками
                            DotShape.CIRCLE -> canvas.drawCircle(
                                left + cellSize / 2f,
                                top + cellSize / 2f,
                                cellSize * 0.45f,  // 0.45 вместо 0.5 — точки разделены
                                dotPaint
                            )
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

                val bgPaint = Paint().apply {
                    isAntiAlias = true
                    color = style.logoBackground.toArgb()
                }
                canvas.drawRoundRect(
                    RectF(left - pad, top - pad, left + logoSize + pad, top + logoSize + pad),
                    pad * 1.5f, pad * 1.5f, bgPaint
                )

                val scaled = Bitmap.createScaledBitmap(logo, logoSize, logoSize, true)
                canvas.drawBitmap(scaled, left, top, null)
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
