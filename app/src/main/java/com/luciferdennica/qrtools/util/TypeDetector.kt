package com.luciferdennica.qrtools.util

import com.luciferdennica.qrtools.domain.model.ScanType

object TypeDetector {
    fun detect(content: String): ScanType {
        val c = content.trim()
        return when {
            c.startsWith("http://", true) || c.startsWith("https://", true) -> ScanType.URL
            c.startsWith("WIFI:", true) -> ScanType.WIFI
            c.startsWith("BEGIN:VCARD", true) -> ScanType.CONTACT
            c.startsWith("SMSTO:", true) || c.startsWith("sms:", true) -> ScanType.SMS
            c.startsWith("mailto:", true) || c.contains("@") && !c.contains(" ") -> ScanType.EMAIL
            c.startsWith("tel:", true) -> ScanType.PHONE
            else -> ScanType.TEXT
        }
    }
}
