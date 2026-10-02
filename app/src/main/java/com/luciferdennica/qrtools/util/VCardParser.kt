package com.luciferdennica.qrtools.util

data class ContactData(
    val name: String,
    val phone: String,
    val email: String
)

object VCardParser {
    fun parse(content: String): ContactData? {
        if (!content.contains("BEGIN:VCARD", ignoreCase = true)) return null
        var name = ""
        var phone = ""
        var email = ""
        content.lines().forEach { raw ->
            val l = raw.trim()
            when {
                l.startsWith("FN:", true) -> name = l.substring(3).trim()
                l.startsWith("TEL", true) && l.contains(":") -> phone = l.substringAfter(":").trim()
                l.startsWith("EMAIL", true) && l.contains(":") -> email = l.substringAfter(":").trim()
            }
        }
        return ContactData(name, phone, email)
    }
}
