import sys

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Notifications dialog
content = content.replace(".background(NetflixDarker, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))",
                          ".background(if (isGlassmorphism) Color.Transparent else NetflixDarker, androidx.compose.foundation.shape.RoundedCornerShape(12.dp)).glassmorphism(isGlassmorphism)")

# Settings Rows
content = content.replace(".background(Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))",
                          ".background(if (isGlassmorphism) Color.Transparent else Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).glassmorphism(isGlassmorphism)")

# Watchlist empty box
content = content.replace(".background(NetflixDarker, RoundedCornerShape(8.dp))",
                          ".background(if (isGlassmorphism) Color.Transparent else NetflixDarker, RoundedCornerShape(8.dp)).glassmorphism(isGlassmorphism)")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
