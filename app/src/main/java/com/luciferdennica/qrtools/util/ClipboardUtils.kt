package com.luciferdennica.qrtools.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ClipboardUtils {

    fun copy(context: Context, text: String, toastMessage: String = "Скопировано") {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("qr", text))
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
    }

    /** Пытается скопировать картинку. Если не получилось — молча возвращает false. */
    fun copyImage(context: Context, bitmap: Bitmap): Boolean {
        return runCatching {
            val dir = File(context.cacheDir, "clipboard").apply { if (!exists()) mkdirs() }
            val file = File(dir, "qr_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val clip = ClipData.newUri(context.contentResolver, "QR", uri)
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(clip)
            true
        }.getOrDefault(false)
    }
}
