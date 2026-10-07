package com.luciferdennica.qrtools.util

data class WifiData(
    val ssid: String,
    val password: String,
    val security: String,
    val hidden: Boolean
)

object WifiParser {

    fun parse(content: String): WifiData? {
        if (!content.startsWith("WIFI:", ignoreCase = true)) return null
        val body = content.substring(5).trimEnd(';')
        var ssid = ""
        var password = ""
        var security = "nopass"
        var hidden = false

        val sb = StringBuilder()
        var key = ""
        var readingKey = true
        var escaped = false

        for (c in body) {
            when {
                escaped -> { sb.append(c); escaped = false }
                c == '\\' -> escaped = true
                readingKey -> {
                    if (c == ':') {
                        key = sb.toString().uppercase()
                        sb.clear()
                        readingKey = false
                    } else sb.append(c)
                }
                else -> {
                    if (c == ';') {
                        val value = sb.toString()
                        when (key) {
                            "S" -> ssid = value
                            "P" -> password = value
                            "T" -> security = value.ifBlank { "nopass" }
                            "H" -> hidden = value.equals("true", ignoreCase = true)
                        }
                        sb.clear()
                        readingKey = true
                    } else sb.append(c)
                }
            }
        }
        return WifiData(ssid, password, security, hidden)
    }

    /** Возвращает отображаемое название типа защиты. openLabel — переведённая строка «Открытая». */
    fun securityLabel(security: String, openLabel: String): String = when (security.uppercase()) {
        "WPA", "WPA2", "WPA3", "SAE" -> "WPA/WPA2"
        "WEP" -> "WEP"
        "NOPASS", "" -> openLabel
        else -> security
    }
}
