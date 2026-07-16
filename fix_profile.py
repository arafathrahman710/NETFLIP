import sys
import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

new_ui = """                item { Spacer(modifier = Modifier.height(24.dp)) }
                
                item {
                    Text("Appearance", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                }
                
                item {
                    var isGlassmorphism by remember { mutableStateOf(com.example.util.PreferencesManager.isGlassmorphismEnabled(context)) }
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
                            Text("Card ও Modal-এ frosted glass effect চালু করুন", color = NetflixGrayText, fontSize = 12.sp)
                        }
                        androidx.compose.material3.Switch(
                            checked = isGlassmorphism,
                            onCheckedChange = {
                                isGlassmorphism = it
                                com.example.util.PreferencesManager.setGlassmorphismEnabled(context, it)
                            }
                        )
                    }
                }
                
                item { Spacer(modifier = Modifier.height(24.dp)) }
                
                item {
                    Text("Favourite", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))"""

content = content.replace('''                item { Spacer(modifier = Modifier.height(24.dp)) }
                item {
                    Text("Favourite", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))''', new_ui)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
