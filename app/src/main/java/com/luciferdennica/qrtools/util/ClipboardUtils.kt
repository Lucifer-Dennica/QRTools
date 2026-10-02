package com.luciferdennica.qrtools.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

object ClipboardUtils {
    fun copy(context: Context, text: String, toastMessage: String = "Скопировано") {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("qr", text))
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
    }
}
