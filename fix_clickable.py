import sys

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace(".androidx.compose.foundation.clickable { showSpeedTest = true }", ".clickable { showSpeedTest = true }")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
