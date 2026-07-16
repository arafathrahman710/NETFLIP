import sys

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_settings = """                item {
                    // Using top-level flow
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Glassmorphism UI", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Enable frosted glass effect on Cards and Modals", color = NetflixGrayText, fontSize = 12.sp)
                        }
                        androidx.compose.material3.Switch(
                            checked = isGlassmorphism,
                            onCheckedChange = {
                                isGlassmorphism = it
                                com.example.util.PreferencesManager.setGlassmorphismEnabled(context, it)
                            }
                        )"""

new_settings = """                item {
                    val isGlassmorphism = com.example.util.PreferencesManager.glassmorphismFlow.collectAsStateWithLifecycle(initialValue = com.example.util.PreferencesManager.isGlassmorphismEnabled(context)).value
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Glassmorphism UI", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Enable frosted glass effect on Cards and Modals", color = NetflixGrayText, fontSize = 12.sp)
                        }
                        androidx.compose.material3.Switch(
                            checked = isGlassmorphism,
                            onCheckedChange = {
                                com.example.util.PreferencesManager.setGlassmorphismEnabled(context, it)
                            }
                        )"""

content = content.replace(old_settings, new_settings)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
