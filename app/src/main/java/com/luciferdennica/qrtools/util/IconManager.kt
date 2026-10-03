package com.luciferdennica.qrtools.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

enum class AppIcon(val key: String, val alias: String) {
    BLUE("blue", "IconBlue"),
    DARK("dark", "IconDark"),
    GREEN("green", "IconGreen"),
    PURPLE("purple", "IconPurple");

    companion object {
        fun fromKey(key: String): AppIcon =
            values().firstOrNull { it.key == key } ?: BLUE
    }
}

object IconManager {

    /**
     * Переключает иконку приложения. Включает только выбранный alias,
     * все остальные — выключает.
     */
    fun setIcon(context: Context, icon: AppIcon) {
        val pm = context.packageManager
        val pkg = context.packageName

        AppIcon.values().forEach { item ->
            val component = ComponentName(pkg, "$pkg.${item.alias}")
            val newState = if (item == icon) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            runCatching {
                pm.setComponentEnabledSetting(
                    component,
                    newState,
                    PackageManager.DONT_KILL_APP
                )
            }
        }
    }
}
