import sys

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

redundant = """                    val isGlassmorphism = com.example.util.PreferencesManager.glassmorphismFlow.collectAsStateWithLifecycle(initialValue = com.example.util.PreferencesManager.isGlassmorphismEnabled(context)).value
                    Row("""
fixed = """                    Row("""
content = content.replace(redundant, fixed)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
