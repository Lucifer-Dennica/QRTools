package com.luciferdennica.qrtools.domain.model

enum class ScanType(val title: String) {
    TEXT("Текст"),
    URL("Ссылка"),
    WIFI("WiFi"),
    CONTACT("Контакт"),
    SMS("SMS"),
    EMAIL("Email"),
    PHONE("Телефон"),
    BARCODE("Штрихкод"),
    OTHER("Другое")
}
