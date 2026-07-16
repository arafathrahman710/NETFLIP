import sys

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# MainScreen
if "val isGlassmorphism by com.example.util.PreferencesManager.glassmorphismFlow.collectAsStateWithLifecycle(initialValue = false)" not in content:
    content = content.replace("    val fullScreenHelper = remember(activity) { activity?.let { com.example.util.FullScreenHelper(it) } }\n",
                              "    val fullScreenHelper = remember(activity) { activity?.let { com.example.util.FullScreenHelper(it) } }\n    val isGlassmorphism by com.example.util.PreferencesManager.glassmorphismFlow.collectAsStateWithLifecycle(initialValue = false)\n")

# ProfileScreen
if "val isGlassmorphism by com.example.util.PreferencesManager.glassmorphismFlow.collectAsStateWithLifecycle(initialValue = false)" not in content.split("fun ProfileScreen")[1][:500]:
    content = content.replace("    val activeDownloads by com.example.util.RealDownloadManager.downloads.collectAsStateWithLifecycle(initialValue = emptyList())\n",
                              "    val activeDownloads by com.example.util.RealDownloadManager.downloads.collectAsStateWithLifecycle(initialValue = emptyList())\n    val isGlassmorphism by com.example.util.PreferencesManager.glassmorphismFlow.collectAsStateWithLifecycle(initialValue = false)\n")

# DownloadsScreen
if "val isGlassmorphism by com.example.util.PreferencesManager.glassmorphismFlow.collectAsStateWithLifecycle(initialValue = false)" not in content.split("fun DownloadsScreen")[1][:500]:
    content = content.replace("fun DownloadsScreen(onBackClick: () -> Unit) {\n    val context = androidx.compose.ui.platform.LocalContext.current\n",
                              "fun DownloadsScreen(onBackClick: () -> Unit) {\n    val context = androidx.compose.ui.platform.LocalContext.current\n    val isGlassmorphism by com.example.util.PreferencesManager.glassmorphismFlow.collectAsStateWithLifecycle(initialValue = false)\n")

# NetflipWebView
if "val isGlassmorphism by com.example.util.PreferencesManager.glassmorphismFlow.collectAsStateWithLifecycle(initialValue = false)" not in content.split("fun NetflipWebView")[1][:500]:
    content = content.replace("    modifier: Modifier = Modifier\n) {\n",
                              "    modifier: Modifier = Modifier\n) {\n    val isGlassmorphism by com.example.util.PreferencesManager.glassmorphismFlow.collectAsStateWithLifecycle(initialValue = false)\n")

# Also, remove any val isGlassmorphism = ... that we added to replace the old variable
import re
content = re.sub(r"val isGlassmorphism = com\.example\.util\.PreferencesManager\.glassmorphismFlow\.collectAsStateWithLifecycle\(initialValue = com\.example\.util\.PreferencesManager\.isGlassmorphismEnabled\(context\)\)\.value\n", "", content)

# Check for remaining uses of isGlassmorphism that need fixing in ProfileScreen because it might clash with my replacement
content = content.replace("""                        androidx.compose.material3.Switch(
                            checked = isGlassmorphism,
                            onCheckedChange = {
                                com.example.util.PreferencesManager.setGlassmorphismEnabled(context, it)
                            }
                        )""", """                        androidx.compose.material3.Switch(
                            checked = isGlassmorphism,
                            onCheckedChange = {
                                com.example.util.PreferencesManager.setGlassmorphismEnabled(context, it)
                            }
                        )""")

# Wait, in ProfileScreen, the switch onCheckedChange uses isGlassmorphism = it which is not allowed if isGlassmorphism is a val delegated by `by`
content = content.replace("""                        androidx.compose.material3.Switch(
                            checked = isGlassmorphism,
                            onCheckedChange = {
                                isGlassmorphism = it
                                com.example.util.PreferencesManager.setGlassmorphismEnabled(context, it)
                            }
                        )""", """                        androidx.compose.material3.Switch(
                            checked = isGlassmorphism,
                            onCheckedChange = {
                                com.example.util.PreferencesManager.setGlassmorphismEnabled(context, it)
                            }
                        )""")


with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
