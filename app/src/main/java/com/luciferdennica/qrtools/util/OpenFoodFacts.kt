package com.luciferdennica.qrtools.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class OffProduct(
    val name: String,
    val brand: String,
    val quantity: String,
    val categories: String,
    val imageUrl: String?,
    val barcode: String
)

object OpenFoodFacts {

    /** Проверяет, является ли штрихкод продуктовым (EAN/UPC). */
    fun isFoodBarcode(format: String, content: String): Boolean {
        val f = format.uppercase()
        val isEanUpc = f.contains("EAN") || f.contains("UPC")
        val isDigits = content.all { it.isDigit() } && content.length in 8..14
        return isEanUpc && isDigits
    }

    /**
     * Загружает информацию о продукте из Open Food Facts.
     * Возвращает null если продукт не найден или нет интернета.
     */
    suspend fun fetch(barcode: String): OffProduct? = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL("https://world.openfoodfacts.org/api/v2/product/$barcode.json")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 7000
                readTimeout = 7000
                setRequestProperty("User-Agent", "QRTools/1.0 (Android)")
            }

            val code = conn.responseCode
            if (code != 200) return@runCatching null

            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)

            if (json.optInt("status", 0) != 1) return@runCatching null
            val product = json.optJSONObject("product") ?: return@runCatching null

            val name = product.optString("product_name_ru", "").ifBlank {
                product.optString("product_name", "")
            }
            val brand = product.optString("brands", "")
            val quantity = product.optString("quantity", "")
            val categories = product.optString("categories", "")
            val imageUrl = product.optString("image_url", "").ifBlank { null }

            OffProduct(
                name = name,
                brand = brand,
                quantity = quantity,
                categories = categories,
                imageUrl = imageUrl,
                barcode = barcode
            )
        }.getOrNull()
    }
}
