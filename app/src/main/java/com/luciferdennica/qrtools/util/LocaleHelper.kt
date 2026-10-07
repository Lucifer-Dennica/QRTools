package com.luciferdennica.qrtools.util

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import com.luciferdennica.qrtools.MainActivity
import java.util.Locale

object LocaleHelper {

    private const val PREFS = "qrtools_lang"
    private const val KEY = "lang"
    private const val DEFAULT = "system"

    fun getLang(context: Context): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, DEFAULT) ?: DEFAULT
    }

    /** Синхронная запись — commit, а не apply. Гарантирует запись до перезапуска. */
    fun setLang(context: Context, lang: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, lang)
            .commit()
    }

    /**
     * Аккуратный перезапуск активити.
     * Не используем recreate() — он фризит из-за Compose + Menu.
     */
    fun restartActivity(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        (context as? android.app.Activity)?.finish()
    }

    fun wrap(context: Context): Context {
        val lang = getLang(context)
        if (lang == DEFAULT) return context

        val locale = when (lang) {
            "ru" -> Locale("ru", "RU")
            "en" -> Locale("en", "US")
            "zh" -> Locale("zh", "CN")
            "es" -> Locale("es", "ES")
            "de" -> Locale("de", "DE")
            "fr" -> Locale("fr", "FR")
            else -> Locale.getDefault()
        }

        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
