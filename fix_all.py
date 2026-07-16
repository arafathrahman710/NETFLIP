import sys
import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# 1. Add NETFLIP logo to HomeHeader
new_home_header = """@Composable
fun HomeHeader(onProfileClick: () -> Unit, onNotificationsClick: () -> Unit) {
    val activeDownloads by com.example.util.DownloadSimulationManager.activeDownloads.collectAsStateWithLifecycle(initialValue = emptyList())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.Transparent)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "NETFLIP",
            color = NetflixRed,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
            letterSpacing = (-1).sp
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                androidx.compose.material3.IconButton(onClick = onNotificationsClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (activeDownloads.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(10.dp)
                            .background(NetflixRed, androidx.compose.foundation.shape.CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            androidx.compose.material3.IconButton(onClick = onProfileClick, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}"""

content = re.sub(r"@Composable\nfun HomeHeader\(.*?\}", new_home_header, content, flags=re.DOTALL)

# 2. Change subtitle for Glassmorphism UI
content = content.replace(
    'Text("Card ও Modal-এ frosted glass effect চালু করুন", color = NetflixGrayText, fontSize = 12.sp)',
    'Text("Enable frosted glass effect on Cards and Modals", color = NetflixGrayText, fontSize = 12.sp)'
)

# 3. Add Internet Speed option below Dolby Vision
internet_speed_ui = r"""                item {
                    var showSpeedTest by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(16.dp)
                            .androidx.compose.foundation.clickable { showSpeedTest = true },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Internet Speed", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Test your connection speed via fast.com", color = NetflixGrayText, fontSize = 12.sp)
                        }
                        Icon(androidx.compose.material.icons.Icons.Default.PlayArrow, contentDescription = "Test Speed", tint = Color.White)
                    }

                    if (showSpeedTest) {
                        androidx.compose.ui.window.Dialog(onDismissRequest = { showSpeedTest = false }) {
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                                .background(NetflixDarker, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                            ) {
                                AndroidView(factory = { ctx ->
                                    android.webkit.WebView(ctx).apply {
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        webViewClient = android.webkit.WebViewClient()
                                        loadUrl("https://fast.com")
                                    }
                                }, modifier = Modifier.fillMaxSize())
                                
                                androidx.compose.material3.IconButton(
                                    onClick = { showSpeedTest = false },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                                }
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(24.dp)) }"""

content = content.replace('                item { Spacer(modifier = Modifier.height(24.dp)) }\n                \n                item {\n                    Text("Appearance", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))\n                }', internet_speed_ui + '\n                \n                item {\n                    Text("Appearance", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))\n                }')

# Add missing import for Icons.Default.Close
if "import androidx.compose.material.icons.filled.Close" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Add", "import androidx.compose.material.icons.filled.Add\nimport androidx.compose.material.icons.filled.Close")

# Add missing import for clickable
if "import androidx.compose.foundation.clickable" not in content:
    content = content.replace("import androidx.compose.foundation.layout.Box", "import androidx.compose.foundation.layout.Box\nimport androidx.compose.foundation.clickable")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
