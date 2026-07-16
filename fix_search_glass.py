import sys

with open("app/src/main/java/com/example/ui/SearchOverlay.kt", "r") as f:
    content = f.read()

import_glass = "import com.example.ui.theme.glassmorphism"
if import_glass not in content:
    content = content.replace("import com.example.ui.theme.NetflixRed", "import com.example.ui.theme.NetflixRed\nimport com.example.ui.theme.glassmorphism")

old_col = """    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NetflixDark)
    ) {"""
new_col = """    val isGlassmorphism = com.example.util.PreferencesManager.glassmorphismFlow.collectAsStateWithLifecycle(initialValue = false).value
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isGlassmorphism) Color.Transparent else NetflixDark)
            .glassmorphism(isGlassmorphism)
    ) {"""
content = content.replace(old_col, new_col)

old_lazy = """            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NetflixDarker)
            ) {"""
new_lazy = """            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isGlassmorphism) Color.Transparent else NetflixDarker)
            ) {"""
content = content.replace(old_lazy, new_lazy)

with open("app/src/main/java/com/example/ui/SearchOverlay.kt", "w") as f:
    f.write(content)
