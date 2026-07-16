import sys
import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Smart Enhance
content = re.sub(
    r"smartEnhance = it\s+com\.example\.util\.PreferencesManager\.setSmartEnhanceEnabled\(context, it\)\s+if \(it\) \{\s+hdrEnabled = false\s+dolbyVision = false\s+\}",
    "smartEnhance = it\n                                com.example.util.PreferencesManager.setSmartEnhanceEnabled(context, it)",
    content
)

# HDR
hdr_old = """hdrEnabled = it
                                com.example.util.PreferencesManager.setHdrEnabled(context, it)
                                if (it) {
                                    smartEnhance = false
                                    dolbyVision = false
                                }"""
hdr_new = """hdrEnabled = it
                                com.example.util.PreferencesManager.setHdrEnabled(context, it)
                                if (it) {
                                    if (dolbyVision) {
                                        android.widget.Toast.makeText(context, "Dolby Vision disabled due to conflict", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    dolbyVision = false
                                }"""
content = content.replace(hdr_old, hdr_new)

# Dolby Vision
dv_old = """dolbyVision = it
                                com.example.util.PreferencesManager.setDolbyVisionEnabled(context, it)
                                if (it) {
                                    smartEnhance = false
                                    hdrEnabled = false
                                }"""
dv_new = """dolbyVision = it
                                com.example.util.PreferencesManager.setDolbyVisionEnabled(context, it)
                                if (it) {
                                    if (hdrEnabled) {
                                        android.widget.Toast.makeText(context, "HDR disabled due to conflict", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    hdrEnabled = false
                                }"""
content = content.replace(dv_old, dv_new)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
