#!/bin/bash
# Replaces JS in NetflipWebView to include logo fix and video enhancements

sed -i '/val jsCode = """/c\
                        val isSmartEnhance = com.example.util.PreferencesManager.isSmartEnhanceEnabled(context)\
                        val isHdr = com.example.util.PreferencesManager.isHdrEnabled(context) || com.example.util.PreferencesManager.isDolbyVisionEnabled(context)\
                        val jsCode = """\
                            (function() {\
                                // --- Quick Logo Hide ---\
                                const logoStyle = document.createElement("style");\
                                logoStyle.textContent = ".navbar-brand, [class*=\"logo\"] { visibility: hidden !important; }";\
                                document.head.appendChild(logoStyle);\
                                \
                                // --- Video Enhancement ---\
                                const enhanceStyle = document.createElement("style");\
                                enhanceStyle.textContent = `\
                                    video {\
                                        ${if (isSmartEnhance) "filter: contrast(1.15) saturate(1.2) brightness(1.05) drop-shadow(0 0 5px rgba(255,255,255,0.2)) !important;" else ""}\
                                        ${if (isHdr) "filter: contrast(1.25) saturate(1.3) brightness(1.15) !important;" else ""}\
                                    }\
                                `;\
                                document.head.appendChild(enhanceStyle);\
' app/src/main/java/com/example/MainActivity.kt

