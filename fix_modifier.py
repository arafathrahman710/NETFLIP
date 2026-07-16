import sys

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Fix the modifier
content = content.replace(
    "modifier = Modifier.fillMaxSize().androidx.compose.ui.draw.alpha(webViewOpacity).let {",
    "modifier = Modifier.fillMaxSize().alpha(webViewOpacity).let {"
)

# Add missing imports
if "import androidx.compose.ui.draw.alpha" not in content:
    content = content.replace("import androidx.compose.ui.draw.clip", "import androidx.compose.ui.draw.clip\nimport androidx.compose.ui.draw.alpha")

if "import androidx.compose.foundation.layout.heightIn" not in content:
    content = content.replace("import androidx.compose.foundation.layout.height", "import androidx.compose.foundation.layout.height\nimport androidx.compose.foundation.layout.heightIn")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
