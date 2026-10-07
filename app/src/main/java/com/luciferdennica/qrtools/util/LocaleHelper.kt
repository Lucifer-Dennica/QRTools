package com.luciferdennica.qrtools.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    private const val PREFS = "qrtools_lang"
    private const val KEY = "lang"
    private const val DEFAULT = "system"

    fun getLang(context: Context): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, DEFAULT) ?: DEFAULT
    }

    fun setLang(context: Context, lang: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, lang)
            .apply()
    }

    /** Оборачивает контекст нужной локалью. "system" — как есть. */
    fun wrap(context: Context): Context {
        val lang = getLang(context)
        if (lang == DEFAULT) return context

        val locale = when (lang) {
            "ru" -> Locale("ru", "RU")
            "en" -> Locale("en", "US")
            "zh" -> Locale("zh", "CN")
            else -> Locale.getDefault()
        }

        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
