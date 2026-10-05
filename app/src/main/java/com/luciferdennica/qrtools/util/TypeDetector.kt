package com.luciferdennica.qrtools.util

import com.luciferdennica.qrtools.domain.model.ScanType

object TypeDetector {

    /** Список форматов штрихкодов (не QR). */
    private val BARCODE_FORMATS = setOf(
        "EAN_13", "EAN_8", "UPC_A", "UPC_E", "UPC_EAN_EXTENSION",
        "CODE_39", "CODE_93", "CODE_128", "ITF", "CODABAR",
        "PDF417", "AZTEC", "DATA_MATRIX"
    )

    /**
     * Определяет тип сканирования по формату (из ML Kit) и содержимому.
     * @param content — raw value от сканера
     * @param format — формат от ML Kit (например, "QR_CODE", "EAN_13")
     */
    fun detect(content: String, format: String = ""): ScanType {
        val f = format.uppercase()
        val c = content.trim()

        // Если формат — штрихкод (не QR) — сразу BARCODE
        if (f in BARCODE_FORMATS) return ScanType.BARCODE

        return when {
            c.startsWith("http://", true) || c.startsWith("https://", true) -> ScanType.URL
            c.startsWith("WIFI:", true) -> ScanType.WIFI
            c.startsWith("BEGIN:VCARD", true) -> ScanType.CONTACT
            c.startsWith("SMSTO:", true) || c.startsWith("sms:", true) -> ScanType.SMS
            c.startsWith("mailto:", true) || (c.contains("@") && !c.contains(" ")) -> ScanType.EMAIL
            c.startsWith("tel:", true) -> ScanType.PHONE
            // Если это число и формат QR — скорее всего, просто текст с цифрами
            else -> ScanType.TEXT
        }
    }
}
