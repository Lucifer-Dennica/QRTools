package com.luciferdennica.qrtools.qr

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color

data class QrStyle(
    val dotColor: Color = Color(0xFF000000),
    val dotColor2: Color = Color(0xFF424242),
    val bgColor: Color = Color(0xFFFFFFFF),
    val bgColor2: Color = Color(0xFFE3F2FD),
    val dotGradient: Boolean = false,
    val bgGradient: Boolean = false,
    val logo: Bitmap? = null,
    val logoBackground: Color = Color(0xFFFFFFFF)
) {
    companion object {
        val DOT_PALETTE: List<Pair<Color, Color>> = listOf(
            Color(0xFF000000) to Color(0xFF424242),
            Color(0xFFFFFFFF) to Color(0xFFBDBDBD),
            Color(0xFFE53935) to Color(0xFFFB8C00),
            Color(0xFFEC407A) to Color(0xFF8E24AA),
            Color(0xFF8E24AA) to Color(0xFF3949AB),
            Color(0xFF3949AB) to Color(0xFF1E88E5),
            Color(0xFF1E88E5) to Color(0xFF00ACC1),
            Color(0xFF00ACC1) to Color(0xFF00897B),
            Color(0xFF43A047) to Color(0xFF9CCC65),
            Color(0xFF00897B) to Color(0xFF43A047),
            Color(0xFFFDD835) to Color(0xFFFB8C00),
            Color(0xFFFB8C00) to Color(0xFFE53935),
            Color(0xFF6D4C41) to Color(0xFFA1887F)
        )

        val BG_PALETTE: List<Pair<Color, Color>> = listOf(
            Color(0xFFFFFFFF) to Color(0xFFE3F2FD),
            Color(0xFFF5F5F5) to Color(0xFFEEEEEE),
            Color(0xFF000000) to Color(0xFF1A1A1A),
            Color(0xFFFFF8E1) to Color(0xFFFFECB3),
            Color(0xFFE3F2FD) to Color(0xFFBBDEFB),
            Color(0xFFF3E5F5) to Color(0xFFE1BEE7),
            Color(0xFFE8F5E9) to Color(0xFFC8E6C9),
            Color(0xFFFFEBEE) to Color(0xFFFFCDD2),
            Color(0xFFFFF3E0) to Color(0xFFFFE0B2),
            Color(0xFFE0F7FA) to Color(0xFFB2EBF2)
        )

        val PRESETS: List<Pair<String, QrStyle>> = listOf(
            "Классика" to QrStyle(),
            "Синий" to QrStyle(dotColor = Color(0xFF0D47A1), bgColor = Color(0xFFFFFFFF)),
            "Тёмная" to QrStyle(dotColor = Color(0xFFFFFFFF), bgColor = Color(0xFF0A0A0A)),
            "Закат" to QrStyle(
                dotColor = Color(0xFFE53935),
                dotColor2 = Color(0xFFFB8C00),
                dotGradient = true,
                bgColor = Color(0xFFFFFFFF)
            ),
            "Океан" to QrStyle(
                dotColor = Color(0xFF0D47A1),
                dotColor2 = Color(0xFF00BCD4),
                dotGradient = true,
                bgColor = Color(0xFFFFFFFF)
            ),
            "Лес" to QrStyle(
                dotColor = Color(0xFF1B5E20),
                dotColor2 = Color(0xFF66BB6A),
                dotGradient = true,
                bgColor = Color(0xFFFFFFFF)
            ),
            "Аметист" to QrStyle(
                dotColor = Color(0xFF4A148C),
                dotColor2 = Color(0xFFE91E63),
                dotGradient = true,
                bgColor = Color(0xFFFFFFFF)
            ),
            "Инверсия" to QrStyle(
                dotColor = Color(0xFFFFFFFF),
                bgColor = Color(0xFF1E88E5),
                bgColor2 = Color(0xFF0D47A1),
                bgGradient = true
            )
        )
    }
}
