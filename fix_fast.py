import sys

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

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
                                            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                        }
                                        webChromeClient = android.webkit.WebChromeClient()
                                        webViewClient = android.webkit.WebViewClient()
                                        loadUrl("https://fast.com")"""

content = content.replace(old_wv, new_wv)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
