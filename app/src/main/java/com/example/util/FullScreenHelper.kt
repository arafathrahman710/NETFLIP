package com.example.util

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class FullScreenHelper(private val activity: Activity) {
    private var customView: View? = null
    private var customViewCallback: android.webkit.WebChromeClient.CustomViewCallback? = null
    private var originalOrientation: Int = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    fun showCustomView(view: View, callback: android.webkit.WebChromeClient.CustomViewCallback) {
        if (customView != null) {
            callback.onCustomViewHidden()
            return
        }
        
        customView = view
        customViewCallback = callback
        
        val decorView = activity.window.decorView as FrameLayout
        decorView.addView(customView, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
        
        originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        
        val windowInsetsController = WindowCompat.getInsetsController(activity.window, decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    fun hideCustomView() {
        if (customView == null) return
        
        val decorView = activity.window.decorView as FrameLayout
        decorView.removeView(customView)
        customView = null
        
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        activity.requestedOrientation = originalOrientation
        
        val windowInsetsController = WindowCompat.getInsetsController(activity.window, decorView)
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        
        customViewCallback?.onCustomViewHidden()
        customViewCallback = null
    }

    fun isFullScreen(): Boolean = customView != null
}
