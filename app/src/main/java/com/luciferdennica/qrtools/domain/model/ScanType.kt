package com.luciferdennica.qrtools.domain.model

import com.luciferdennica.qrtools.R

enum class ScanType(val titleRes: Int) {
    TEXT(R.string.type_text),
    URL(R.string.type_url),
    WIFI(R.string.type_wifi),
    CONTACT(R.string.type_contact),
    SMS(R.string.type_sms),
    EMAIL(R.string.type_email),
    PHONE(R.string.type_phone),
    BARCODE(R.string.type_barcode),
    OTHER(R.string.type_other)
}
