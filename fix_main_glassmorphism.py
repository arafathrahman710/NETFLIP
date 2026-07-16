import sys

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

init_call = """        com.example.util.PreferencesManager.initFlows(this)
        setContent {"""
content = content.replace("        setContent {", init_call)

val_decl = """    val activity = androidx.compose.ui.platform.LocalContext.current as? androidx.activity.ComponentActivity
    val fullScreenHelper = remember(activity) { activity?.let { com.example.util.FullScreenHelper(it) } }
    
    val isGlassmorphism by com.example.util.PreferencesManager.glassmorphismFlow.collectAsStateWithLifecycle(initialValue = false)
"""
content = content.replace("""    val activity = androidx.compose.ui.platform.LocalContext.current as? androidx.activity.ComponentActivity
    val fullScreenHelper = remember(activity) { activity?.let { com.example.util.FullScreenHelper(it) } }""", val_decl)

content = content.replace("var isGlassmorphism by remember { mutableStateOf(com.example.util.PreferencesManager.isGlassmorphismEnabled(context)) }", "// Using top-level flow")

# Let's apply glassmorphism to dialogs and bottom nav and home header
if "import com.example.ui.theme.glassmorphism" not in content:
    content = content.replace("import com.example.ui.theme.NetflixRed", "import com.example.ui.theme.NetflixRed\nimport com.example.ui.theme.glassmorphism")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
