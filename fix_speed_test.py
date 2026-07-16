import sys
import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_code = """                                AndroidView(factory = { ctx ->
                                    android.webkit.WebView(ctx).apply {
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        webViewClient = android.webkit.WebViewClient()
                                        loadUrl("https://fast.com")
                                    }
                                }, modifier = Modifier.fillMaxSize())"""

new_code = """                                AndroidView(factory = { ctx ->
                                    android.webkit.WebView(ctx).apply {
                                        layoutParams = android.view.ViewGroup.LayoutParams(
                                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                        settings.apply {
                                            javaScriptEnabled = true
                                            domStorageEnabled = true
                                            useWideViewPort = true
                                            loadWithOverviewMode = true
                                            databaseEnabled = true
                                            userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                                        }
                                        webViewClient = android.webkit.WebViewClient()
                                        loadUrl("https://fast.com")
                                    }
                                }, modifier = Modifier.fillMaxSize())"""

content = content.replace(old_code, new_code)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
