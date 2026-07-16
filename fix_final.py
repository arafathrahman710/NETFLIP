import sys

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Fix fast.com
old_wv = """                                        settings.apply {
                                            javaScriptEnabled = true
                                            domStorageEnabled = true
                                            useWideViewPort = true
                                            loadWithOverviewMode = true
                                            databaseEnabled = true
                                            userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                                        }
                                        webViewClient = android.webkit.WebViewClient()
                                        loadUrl("https://fast.com")"""

new_wv = """                                        settings.apply {
                                            javaScriptEnabled = true
                                            domStorageEnabled = true
                                            useWideViewPort = true
                                            loadWithOverviewMode = true
                                            databaseEnabled = true
                                            userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                                            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                        }
                                        webChromeClient = android.webkit.WebChromeClient()
                                        webViewClient = android.webkit.WebViewClient()
                                        loadUrl("https://fast.com")"""
content = content.replace(old_wv, new_wv)

# Fix glassmorphism background
content = content.replace(".background(NetflixDarker, RoundedCornerShape(16.dp))",
                          ".background(if (isGlassmorphism) Color.Transparent else NetflixDarker, RoundedCornerShape(16.dp)).glassmorphism(isGlassmorphism)")

import re
# Fix redundant isGlassmorphism
content = re.sub(r"var isGlassmorphism by remember .*?\n", "val isGlassmorphism = com.example.util.PreferencesManager.glassmorphismFlow.collectAsStateWithLifecycle(initialValue = com.example.util.PreferencesManager.isGlassmorphismEnabled(context)).value\n", content)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
