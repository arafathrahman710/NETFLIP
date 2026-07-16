import sys

with open("app/src/main/java/com/example/util/PreferencesManager.kt", "r") as f:
    content = f.read()

content = content.replace('''    fun setGlassmorphismEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_GLASSMORPHISM, enabled).apply()
    }
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }''', '''    fun setGlassmorphismEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_GLASSMORPHISM, enabled).apply()
    }''')

with open("app/src/main/java/com/example/util/PreferencesManager.kt", "w") as f:
    f.write(content)
