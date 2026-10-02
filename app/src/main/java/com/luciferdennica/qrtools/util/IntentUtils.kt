package com.luciferdennica.qrtools.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object IntentUtils {

    /** Открывает ссылку. Возвращает true при успехе. */
    fun openUrlSafe(context: Context, url: String): Boolean {
        return runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    /** Старая версия — без возврата результата (для совместимости). */
    fun openUrl(context: Context, url: String) {
        openUrlSafe(context, url)
    }

    fun shareText(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }

    fun openEmail(context: Context, address: String, subject: String = "") {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$address")).apply {
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        runCatching { context.startActivity(intent) }
    }
}
