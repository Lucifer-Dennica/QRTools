package com.luciferdennica.qrtools.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.luciferdennica.qrtools.data.db.ScanEntity
import com.luciferdennica.qrtools.domain.model.ScanType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    fun exportAndShare(context: Context, items: List<ScanEntity>): Boolean {
        return runCatching {
            val csv = buildCsv(context, items)
            val fileName = "qrtools_${System.currentTimeMillis()}.csv"
            val uri = saveFile(context, fileName, csv)

            if (uri != null) {
                shareCsv(context, uri)
                true
            } else false
        }.getOrDefault(false)
    }

    private fun buildCsv(context: Context, items: List<ScanEntity>): String {
        val df = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        val sb = StringBuilder()
        sb.append("\uFEFF")
        sb.append("Дата;Тип;Формат;Содержимое;Избранное\n")
        items.forEach { item ->
            val typeTitle = runCatching {
                context.getString(ScanType.valueOf(item.type).titleRes)
            }.getOrDefault(item.type)
            sb.append(escape(df.format(Date(item.timestamp)))).append(';')
            sb.append(escape(typeTitle)).append(';')
            sb.append(escape(item.format)).append(';')
            sb.append(escape(item.content)).append(';')
            sb.append(if (item.isFavorite) "Да" else "Нет").append('\n')
        }
        return sb.toString()
    }

    private fun escape(value: String): String {
        val needsQuotes = value.contains(';') || value.contains('"') || value.contains('\n')
        val escaped = value.replace("\"", "\"\"")
        return if (needsQuotes) "\"$escaped\"" else escaped
    }

    private fun saveFile(context: Context, fileName: String, content: String): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/QRTools")
            }
            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
            ) ?: return null
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(content.toByteArray(Charsets.UTF_8))
            }
            uri
        } else {
            @Suppress("DEPRECATION")
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "QRTools"
            )
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            FileOutputStream(file).use { out ->
                out.write(content.toByteArray(Charsets.UTF_8))
            }
            androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        }
    }

    private fun shareCsv(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }
}
