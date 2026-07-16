import sys
import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Add webViewOpacity state
opacity_state = """    var downloadIsSeries by remember { mutableStateOf(false) }
    var webViewOpacity by remember { androidx.compose.runtime.mutableFloatStateOf(1f) }"""
content = content.replace("    var downloadIsSeries by remember { mutableStateOf(false) }", opacity_state)

# Add LaunchedEffect for URL change
effect_code = """            LaunchedEffect(currentUrl) {
                webViewOpacity = 0f
                androidx.compose.animation.core.animate(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 250, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                ) { value, _ ->
                    webViewOpacity = value
                }
            }
            LaunchedEffect(isLoading) {"""
content = content.replace("            LaunchedEffect(isLoading) {", effect_code)

# Apply modifier to NetflipWebView
content = content.replace(
    "modifier = Modifier.fillMaxSize().let {",
    "modifier = Modifier.fillMaxSize().androidx.compose.ui.draw.alpha(webViewOpacity).let {"
)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
