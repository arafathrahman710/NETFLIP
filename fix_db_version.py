import sys

with open("app/src/main/java/com/example/data/AppDatabase.kt", "r") as f:
    content = f.read()

content = content.replace("version = 4", "version = 5")

with open("app/src/main/java/com/example/data/AppDatabase.kt", "w") as f:
    f.write(content)
