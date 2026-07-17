package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object PreferencesManager {
    private const val PREFS_NAME = "netflip_settings"
    private const val KEY_SMART_ENHANCE = "smart_enhance"
    private const val KEY_HDR = "hdr_enabled"
    private const val KEY_DOLBY_VISION = "dolby_vision_enabled"
    private const val KEY_GLASSMORPHISM = "glassmorphism_enabled"

    private val _glassmorphismFlow = MutableStateFlow(false)
    val glassmorphismFlow: StateFlow<Boolean> = _glassmorphismFlow

    private val _smartEnhanceFlow = MutableStateFlow(false)
    val smartEnhanceFlow: StateFlow<Boolean> = _smartEnhanceFlow

    private val _hdrFlow = MutableStateFlow(false)
    val hdrFlow: StateFlow<Boolean> = _hdrFlow

    private val _dolbyVisionFlow = MutableStateFlow(false)
    val dolbyVisionFlow: StateFlow<Boolean> = _dolbyVisionFlow

    fun initFlows(context: Context) {
        _glassmorphismFlow.value = isGlassmorphismEnabled(context)
        _smartEnhanceFlow.value = isSmartEnhanceEnabled(context)
        _hdrFlow.value = isHdrEnabled(context)
        _dolbyVisionFlow.value = isDolbyVisionEnabled(context)
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isGlassmorphismEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_GLASSMORPHISM, false)
    fun setGlassmorphismEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_GLASSMORPHISM, enabled).apply()
        _glassmorphismFlow.value = enabled
    }

    fun isSmartEnhanceEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_SMART_ENHANCE, false)
    fun setSmartEnhanceEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SMART_ENHANCE, enabled).apply()
        _smartEnhanceFlow.value = enabled
    }

    fun isHdrEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_HDR, false)
    fun setHdrEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_HDR, enabled).apply()
        _hdrFlow.value = enabled
        if (enabled) {
            setDolbyVisionEnabled(context, false)
        }
    }

    fun isDolbyVisionEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_DOLBY_VISION, false)
    fun setDolbyVisionEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_DOLBY_VISION, enabled).apply()
        _dolbyVisionFlow.value = enabled
        if (enabled) {
            setHdrEnabled(context, false)
        }
    }
}