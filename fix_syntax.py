import sys

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("document.head.appendChild(enhanceStyle);\n\n                            (function() {", "document.head.appendChild(enhanceStyle);\n")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
