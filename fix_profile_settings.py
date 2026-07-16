import sys
import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

new_ui = r"""                item { Spacer(modifier = Modifier.height(24.dp)) }
                
                item {
                    Text("Video Playback", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                }
                
                item {
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
                            Text("Smart Enhance", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Enhances video contrast and brightness", color = NetflixGrayText, fontSize = 12.sp)
                        }
                        androidx.compose.material3.Switch(
                            checked = smartEnhance,
                            onCheckedChange = {
                                smartEnhance = it
                                com.example.util.PreferencesManager.setSmartEnhanceEnabled(context, it)
                                if (it) {
                                    hdrEnabled = false
                                    dolbyVision = false
                                }
                            }
                        )
                    }
                }
                
                item {
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
                            Text("HDR Simulation", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Simulates High Dynamic Range colors", color = NetflixGrayText, fontSize = 12.sp)
                            if (!checkHdrSupport(-1)) {
                                Text("Not natively supported on this device", color = NetflixRed, fontSize = 10.sp)
                            }
                        }
                        androidx.compose.material3.Switch(
                            checked = hdrEnabled,
                            onCheckedChange = {
                                hdrEnabled = it
                                com.example.util.PreferencesManager.setHdrEnabled(context, it)
                                if (it) {
                                    smartEnhance = false
                                    dolbyVision = false
                                }
                            }
                        )
                    }
                }
                
                item {
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
                            Text("Dolby Vision", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Enable Dolby Vision color profile", color = NetflixGrayText, fontSize = 12.sp)
                            if (!checkHdrSupport(android.view.Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION)) {
                                Text("Not natively supported on this device", color = NetflixRed, fontSize = 10.sp)
                            }
                        }
                        androidx.compose.material3.Switch(
                            checked = dolbyVision,
                            onCheckedChange = {
                                dolbyVision = it
                                com.example.util.PreferencesManager.setDolbyVisionEnabled(context, it)
                                if (it) {
                                    smartEnhance = false
                                    hdrEnabled = false
                                }
                            }
                        )
                    }
                }
                
                item { Spacer(modifier = Modifier.height(24.dp)) }
                
                item {
                    Text("Appearance", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                }"""

content = re.sub(
    r"                item \{ Spacer\(modifier = Modifier.height\(24.dp\)\) \}\s+item \{\s+Text\(\"Appearance\", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding\(bottom = 16.dp\)\)\s+\}",
    new_ui,
    content
)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
