import sys
import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

new_js = """
                        val isSmartEnhance = com.example.util.PreferencesManager.isSmartEnhanceEnabled(context)
                        val isHdr = com.example.util.PreferencesManager.isHdrEnabled(context) || com.example.util.PreferencesManager.isDolbyVisionEnabled(context)
                        val isGlassmorphism = com.example.util.PreferencesManager.isGlassmorphismEnabled(context)
                        
                        val jsCode = \"\"\"
                            (function() {
                                // --- Glassmorphism UI ---
                                ${if (isGlassmorphism) "const glassStyle = document.createElement('style'); glassStyle.textContent = `.card, .modal, .bottom-sheet, .drawer, [class*=\\\"card\\\"], [class*=\\\"modal\\\"], [class*=\\\"sheet\\\"], [class*=\\\"drawer\\\"], [class*=\\\"dialog\\\"], [class*=\\\"menu\\\"], .dropdown, [class*=\\\"dropdown\\\"], .trailer-modal-inner, .search-results-container, .mobile-search-overlay { background: rgba(255, 255, 255, 0.15) !important; backdrop-filter: blur(20px) !important; -webkit-backdrop-filter: blur(20px) !important; border: 1px solid rgba(255, 255, 255, 0.25) !important; box-shadow: 0 4px 30px rgba(0, 0, 0, 0.1) !important; }`; document.head.appendChild(glassStyle);" else ""}
                                
                                // --- Quick Logo Hide ---"""

content = content.replace('''                        val isSmartEnhance = com.example.util.PreferencesManager.isSmartEnhanceEnabled(context)
                        val isHdr = com.example.util.PreferencesManager.isHdrEnabled(context) || com.example.util.PreferencesManager.isDolbyVisionEnabled(context)
                        val jsCode = """
                            (function() {
                                // --- Quick Logo Hide ---''', new_js)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
