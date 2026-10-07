package com.luciferdennica.qrtools.util

import android.content.Context
import android.net.Uri
import com.luciferdennica.qrtools.R
import com.luciferdennica.qrtools.data.repo.HistoryRepository
import com.luciferdennica.qrtools.domain.model.ScanType
import java.text.SimpleDateFormat
import java.util.Locale

object CsvImporter {

    /**
     * Импортирует записи из CSV (формат QRTools).
     * Возвращает количество успешно импортированных записей.
     * -1 = ошибка/пустой файл.
     */
    suspend fun import(context: Context, uri: Uri, repo: HistoryRepository): Int {
        val text = runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        }.getOrNull() ?: return -1

        // Удаляем BOM
        val cleanText = text.removePrefix("\uFEFF")
        val lines = cleanText.lines().filter { it.isNotBlank() }
        if (lines.size < 2) return -1

        val df = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        var imported = 0

        // Пропускаем заголовок (первая строка)
        lines.drop(1).forEach { line ->
            val parts = parseCsvLine(line)
            if (parts.size < 4) return@forEach

            val dateStr = parts[0].trim()
            val typeStr = parts[1].trim()
            val formatStr = parts[2].trim()
            val content = parts[3].trim()
            val isFav = parts.getOrNull(4)?.trim()?.equals("Да", true) == true ||
                    parts.getOrNull(4)?.trim()?.equals("Yes", true) == true

            if (content.isBlank()) return@forEach

            val timestamp = runCatching { df.parse(dateStr)?.time }.getOrNull()
                ?: System.currentTimeMillis()

            // Определяем тип: сначала по локализованному названию, потом по содержимому
            val type = ScanType.values().firstOrNull { st ->
                runCatching {
                    context.getString(st.titleRes).equals(typeStr, ignoreCase = true)
                }.getOrDefault(false)
            } ?: TypeDetector.detect(content, formatStr)

            runCatching {
                repo.importScan(content, formatStr, type, timestamp, isFav)
                imported++
            }
        }

        return if (imported == 0) -1 else imported
    }

    /** Парсит строку CSV с учётом кавычек. */
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        sb.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ';' && !inQuotes -> {
                    result.add(sb.toString())
                    sb.clear()
                }
                else -> sb.append(c)
            }
            i++
        }
        result.add(sb.toString())
        return result
    }
}
