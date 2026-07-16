import sys
with open("app/src/main/java/com/example/util/PreferencesManager.kt", "r") as f:
    content = f.read()

new_prefs = """package com.example.util

import android.content.Context
import android.content.SharedPreferences

object PreferencesManager {
    private const val PREFS_NAME = "netflip_settings"
    private const val KEY_SMART_ENHANCE = "smart_enhance"
    private const val KEY_HDR = "hdr_enabled"
    private const val KEY_DOLBY_VISION = "dolby_vision_enabled"
    private const val KEY_GLASSMORPHISM = "glassmorphism_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isGlassmorphismEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_GLASSMORPHISM, false)
    fun setGlassmorphismEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_GLASSMORPHISM, enabled).apply()
    }

    fun isSmartEnhanceEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_SMART_ENHANCE, false)
    fun setSmartEnhanceEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SMART_ENHANCE, enabled).apply()
    }

    fun isHdrEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_HDR, false)
    fun setHdrEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_HDR, enabled).apply()
        if (enabled) {
            setDolbyVisionEnabled(context, false)
        }
    }

    fun isDolbyVisionEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_DOLBY_VISION, false)
    fun setDolbyVisionEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_DOLBY_VISION, enabled).apply()
        if (enabled) {
            setHdrEnabled(context, false)
        }
    }
}"""

with open("app/src/main/java/com/example/util/PreferencesManager.kt", "w") as f:
    f.write(new_prefs)
