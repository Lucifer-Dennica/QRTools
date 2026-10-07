package com.luciferdennica.qrtools.qr

object QrTypeBuilders {

    fun text(value: String) = value.trim()

    fun url(value: String): String {
        val v = value.trim()
        return if (v.startsWith("http://", true) || v.startsWith("https://", true)) v
        else "https://$v"
    }

    fun wifi(ssid: String, password: String, security: String): String {
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

    /** Полноценная визитка (vCard 3.0) */
    fun businessCard(
        firstName: String,
        lastName: String,
        company: String,
        position: String,
        phone: String,
        email: String,
        website: String,
        address: String,
        note: String
    ): String {
        val fullName = listOf(firstName.trim(), lastName.trim())
            .filter { it.isNotBlank() }
            .joinToString(" ")

        return buildString {
            appendLine("BEGIN:VCARD")
            appendLine("VERSION:3.0")
            if (lastName.isNotBlank() || firstName.isNotBlank()) {
                appendLine("N:${lastName.trim()};${firstName.trim()};;;")
            }
            if (fullName.isNotBlank()) appendLine("FN:$fullName")
            if (company.isNotBlank()) appendLine("ORG:${company.trim()}")
            if (position.isNotBlank()) appendLine("TITLE:${position.trim()}")
            if (phone.isNotBlank()) appendLine("TEL:${phone.trim()}")
            if (email.isNotBlank()) appendLine("EMAIL:${email.trim()}")
            if (website.isNotBlank()) appendLine("URL:${website.trim()}")
            if (address.isNotBlank()) appendLine("ADR:;;${address.trim()};;;;")
            if (note.isNotBlank()) appendLine("NOTE:${note.trim()}")
            append("END:VCARD")
        }
    }

    private fun escape(s: String): String =
        s.replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(",", "\\,")
            .replace(":", "\\:")
            .replace("\"", "\\\"")
}
