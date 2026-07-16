import sys

with open("app/src/main/java/com/example/util/PreferencesManager.kt", "r") as f:
    content = f.read()

import_str = "import android.content.SharedPreferences\nimport kotlinx.coroutines.flow.MutableStateFlow\nimport kotlinx.coroutines.flow.StateFlow"
content = content.replace("import android.content.SharedPreferences", import_str)

old_def = """    private const val KEY_GLASSMORPHISM = "glassmorphism_enabled"

    private fun getPrefs(context: Context): SharedPreferences {"""

new_def = """    private const val KEY_GLASSMORPHISM = "glassmorphism_enabled"

    private val _glassmorphismFlow = MutableStateFlow(false)
    val glassmorphismFlow: StateFlow<Boolean> = _glassmorphismFlow

    fun initFlows(context: Context) {
        _glassmorphismFlow.value = isGlassmorphismEnabled(context)
    }

    private fun getPrefs(context: Context): SharedPreferences {"""

content = content.replace(old_def, new_def)

old_setter = """    fun setGlassmorphismEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_GLASSMORPHISM, enabled).apply()
    }"""
new_setter = """    fun setGlassmorphismEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_GLASSMORPHISM, enabled).apply()
        _glassmorphismFlow.value = enabled
    }"""
content = content.replace(old_setter, new_setter)

with open("app/src/main/java/com/example/util/PreferencesManager.kt", "w") as f:
    f.write(content)
