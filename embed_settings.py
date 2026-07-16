import sys

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Add logic vars
logic_vars = """
    var smartEnhance by remember { mutableStateOf(com.example.util.PreferencesManager.isSmartEnhanceEnabled(context)) }
    var hdrEnabled by remember { mutableStateOf(com.example.util.PreferencesManager.isHdrEnabled(context)) }
    var dolbyVision by remember { mutableStateOf(com.example.util.PreferencesManager.isDolbyVisionEnabled(context)) }

    fun checkHdrSupport(type: Int): Boolean {
        val display = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            context.display
        } else {
            @Suppress("DEPRECATION")
            (context.getSystemService(android.content.Context.WINDOW_SERVICE) as android.view.WindowManager).defaultDisplay
        }
        val capabilities = display?.hdrCapabilities?.supportedHdrTypes ?: intArrayOf()
        return if (type == -1) capabilities.isNotEmpty() else capabilities.contains(type)
    }

    Box(
"""

content = content.replace("    Box(\n        modifier = Modifier\n            .fillMaxSize()\n            .background(NetflixDark)\n            .padding(16.dp)\n    ) {", logic_vars + "        modifier = Modifier\n            .fillMaxSize()\n            .background(NetflixDark)\n            .padding(16.dp)\n    ) {")

settings_ui = """
                item { Spacer(modifier = Modifier.height(24.dp)) }
                item {
                    Text("Video Playback", color = NetflixRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Smart Enhance", color = Color.White, fontSize = 18.sp)
                            Text("Improves video clarity, color, and sharpness", color = Color.Gray, fontSize = 14.sp)
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
                            },
                            colors = androidx.compose.material3.SwitchDefaults.colors(checkedThumbColor = NetflixRed, checkedTrackColor = NetflixRed.copy(alpha = 0.5f))
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("HDR", color = Color.White, fontSize = 18.sp)
                            Text("High Dynamic Range (requires display support)", color = Color.Gray, fontSize = 14.sp)
                        }
                        androidx.compose.material3.Switch(
                            checked = hdrEnabled,
                            onCheckedChange = {
                                if (it && !checkHdrSupport(-1)) {
                                    android.widget.Toast.makeText(context, "This device doesn't have HDR support.", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    hdrEnabled = it
                                    com.example.util.PreferencesManager.setHdrEnabled(context, it)
                                    if (it) {
                                        smartEnhance = false
                                        dolbyVision = false
                                    }
                                }
                            },
                            colors = androidx.compose.material3.SwitchDefaults.colors(checkedThumbColor = NetflixRed, checkedTrackColor = NetflixRed.copy(alpha = 0.5f))
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dolby Vision", color = Color.White, fontSize = 18.sp)
                            Text("Dolby Vision HDR (requires display support)", color = Color.Gray, fontSize = 14.sp)
                        }
                        androidx.compose.material3.Switch(
                            checked = dolbyVision,
                            onCheckedChange = {
                                if (it && !checkHdrSupport(android.view.Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION)) {
                                    android.widget.Toast.makeText(context, "This device doesn't have Dolby Vision support.", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    dolbyVision = it
                                    com.example.util.PreferencesManager.setDolbyVisionEnabled(context, it)
                                    if (it) {
                                        smartEnhance = false
                                        hdrEnabled = false
                                    }
                                }
                            },
                            colors = androidx.compose.material3.SwitchDefaults.colors(checkedThumbColor = NetflixRed, checkedTrackColor = NetflixRed.copy(alpha = 0.5f))
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
"""

content = content.replace("                item { Spacer(modifier = Modifier.height(24.dp)) }\n                item {\n                    Text(\"Favourite\", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))\n                }", settings_ui + "\n                item { Spacer(modifier = Modifier.height(24.dp)) }\n                item {\n                    Text(\"Favourite\", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))\n                }")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
