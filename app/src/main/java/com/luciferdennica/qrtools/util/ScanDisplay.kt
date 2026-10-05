package com.luciferdennica.qrtools.util

import com.luciferdennica.qrtools.domain.model.ScanType

/**
 * Превращает сырую строку скана в красивый заголовок для списка.
 * Контакт → "Иван Петров"
 * WiFi → "zte_5/1"
 * Ссылка → "https://google.com"
 * Штрихкод → "4250369504466"
 */
object ScanDisplay {

    fun titleFor(content: String, type: String): String {
        val t = runCatching { ScanType.valueOf(type) }.getOrDefault(ScanType.TEXT)
        return when (t) {
            ScanType.WIFI -> WifiParser.parse(content)?.ssid?.ifBlank { content } ?: content
            ScanType.CONTACT -> VCardParser.parse(content)?.name?.ifBlank { content } ?: content
            ScanType.URL -> content
            ScanType.SMS -> {
                // SMSTO:+375291234567:Текст
                val body = content.removePrefix("SMSTO:").removePrefix("sms:")
                body.substringBefore(":").ifBlank { content }
            }
            ScanType.EMAIL -> content.removePrefix("mailto:")
            ScanType.PHONE -> content.removePrefix("tel:")
            else -> content
        }
    }

    /** Короткое представление для строки (обрезает длинные значения). */
    fun shortTitle(content: String, type: String, maxLen: Int = 60): String {
        val title = titleFor(content, type)
        return if (title.length <= maxLen) title
        else title.take(maxLen - 1) + "…"
    }
}
