import sys
import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Add import
if "import com.example.ui.theme.glassmorphism" not in content:
    content = content.replace("import com.example.ui.theme.NetflixRed", "import com.example.ui.theme.NetflixRed\nimport com.example.ui.theme.glassmorphism")

# Fix duplicate definition in DownloadsScreen
content = content.replace("""    val db = remember { AppDatabase.getDatabase(context) }
    val savedVideos by db.savedVideoDao().getAllSavedVideos().collectAsStateWithLifecycle(initialValue = emptyList())
    val activeDownloads by com.example.util.RealDownloadManager.downloads.collectAsStateWithLifecycle(initialValue = emptyList())
    val isGlassmorphism by com.example.util.PreferencesManager.glassmorphismFlow.collectAsStateWithLifecycle(initialValue = false)""", """    val db = remember { AppDatabase.getDatabase(context) }
    val savedVideos by db.savedVideoDao().getAllSavedVideos().collectAsStateWithLifecycle(initialValue = emptyList())
    val activeDownloads by com.example.util.RealDownloadManager.downloads.collectAsStateWithLifecycle(initialValue = emptyList())""")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
