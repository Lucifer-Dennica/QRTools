package com.luciferdennica.qrtools.qr

object QrTypeBuilders {

    fun text(value: String) = value.trim()

    fun url(value: String): String {
        val v = value.trim()
        return if (v.startsWith("http://", true) || v.startsWith("https://", true)) v
        else "https://$v"
    }

    fun wifi(ssid: String, password: String, security: String): String {
        // security = WPA | WEP | nopass
        val s = escape(ssid)
        val p = escape(password)
        return "WIFI:T:$security;S:$s;P:$p;;"
    }

    fun contact(name: String, phone: String, email: String): String {
        return buildString {
            appendLine("BEGIN:VCARD")
            appendLine("VERSION:3.0")
            appendLine("FN:${name.trim()}")
            if (phone.isNotBlank()) appendLine("TEL:${phone.trim()}")
            if (email.isNotBlank()) appendLine("EMAIL:${email.trim()}")
            append("END:VCARD")
        }
    }

    fun sms(phone: String, message: String): String {
        return "SMSTO:${phone.trim()}:${message.trim()}"
    }

    private fun escape(s: String): String =
        s.replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(",", "\\,")
            .replace(":", "\\:")
            .replace("\"", "\\\"")
}
