import sys

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

hdr_old = """                            onCheckedChange = {
                                hdrEnabled = it
                                com.example.util.PreferencesManager.setHdrEnabled(context, it)
                                if (it) {
                                    if (dolbyVision) {
                                        android.widget.Toast.makeText(context, "Dolby Vision disabled due to conflict", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    dolbyVision = false
                                }
                            }"""

hdr_new = """                            onCheckedChange = {
                                hdrEnabled = it
                                com.example.util.PreferencesManager.setHdrEnabled(context, it)
                                if (it) {
                                    if (dolbyVision) {
                                        android.widget.Toast.makeText(context, "Dolby Vision disabled due to conflict", android.widget.Toast.LENGTH_SHORT).show()
                                        com.example.util.PreferencesManager.setDolbyVisionEnabled(context, false)
                                    }
                                    dolbyVision = false
                                }
                            }"""

content = content.replace(hdr_old, hdr_new)

dolby_old = """                            onCheckedChange = {
                                dolbyVision = it
                                com.example.util.PreferencesManager.setDolbyVisionEnabled(context, it)
                                if (it) {
                                    if (hdrEnabled) {
                                        android.widget.Toast.makeText(context, "HDR disabled due to conflict", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    hdrEnabled = false
                                }
                            }"""

dolby_new = """                            onCheckedChange = {
                                dolbyVision = it
                                com.example.util.PreferencesManager.setDolbyVisionEnabled(context, it)
                                if (it) {
                                    if (hdrEnabled) {
                                        android.widget.Toast.makeText(context, "HDR disabled due to conflict", android.widget.Toast.LENGTH_SHORT).show()
                                        com.example.util.PreferencesManager.setHdrEnabled(context, false)
                                    }
                                    hdrEnabled = false
                                }
                            }"""

content = content.replace(dolby_old, dolby_new)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
