import sys

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("`video { filter: ${filterString} }`", "`video { filter: \\${filterString} }`")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
