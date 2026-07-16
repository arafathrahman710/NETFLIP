import sys

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("com.example.util.DownloadSimulationManager", "com.example.util.RealDownloadManager")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
