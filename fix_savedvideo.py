import sys

with open("app/src/main/java/com/example/data/SavedVideo.kt", "r") as f:
    content = f.read()

content = content.replace(
    "val url: String,",
    "val url: String,\n    val filePath: String? = null,"
)

with open("app/src/main/java/com/example/data/SavedVideo.kt", "w") as f:
    f.write(content)
