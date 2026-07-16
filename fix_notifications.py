import sys

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_notif = """            if (showNotificationsDialog) {
                androidx.compose.ui.window.Dialog(onDismissRequest = { showNotificationsDialog = false }) {
                    val activeDownloads by com.example.util.RealDownloadManager.activeDownloads.collectAsStateWithLifecycle(initialValue = emptyList())
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NetflixDarker, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text("Notifications", color = Color.White, fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            if (activeDownloads.isEmpty()) {
                                Text("No active downloads or notifications.", color = Color.Gray)
                            } else {
                                androidx.compose.foundation.lazy.LazyColumn {
                                    items(activeDownloads) { download ->
                                        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                                            Text(download.title, color = Color.White, maxLines = 1)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            androidx.compose.material3.LinearProgressIndicator(
                                                progress = { download.progress },
                                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                                color = NetflixRed,
                                                trackColor = Color.DarkGray
                                            )
                                            Text("${(download.progress * 100).toInt()}%", color = Color.Gray, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }"""

new_notif = """            if (showNotificationsDialog) {
                androidx.compose.ui.window.Dialog(onDismissRequest = { showNotificationsDialog = false }) {
                    val allDownloads by com.example.util.RealDownloadManager.downloads.collectAsStateWithLifecycle(initialValue = emptyList())
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NetflixDarker, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text("Notifications", color = Color.White, fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            if (allDownloads.isEmpty()) {
                                Text("No active downloads or notifications.", color = Color.Gray)
                            } else {
                                androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                                    items(allDownloads) { download ->
                                        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                                            Text(download.title, color = Color.White, maxLines = 1, fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            androidx.compose.material3.LinearProgressIndicator(
                                                progress = { download.progress },
                                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                                color = when (download.status) {
                                                    com.example.util.DownloadStatus.COMPLETED -> Color.Green
                                                    com.example.util.DownloadStatus.FAILED -> NetflixRed
                                                    else -> NetflixRed
                                                },
                                                trackColor = Color.DarkGray
                                            )
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(
                                                    text = when (download.status) {
                                                        com.example.util.DownloadStatus.COMPLETED -> "Completed"
                                                        com.example.util.DownloadStatus.FAILED -> "Failed"
                                                        else -> "${(download.progress * 100).toInt()}%"
                                                    },
                                                    color = Color.Gray, fontSize = 12.sp
                                                )
                                                Text(text = download.speedStr, color = Color.Gray, fontSize = 12.sp)
                                            }
                                            if (download.status == com.example.util.DownloadStatus.FAILED) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                TextButton(onClick = {
                                                    com.example.util.RealDownloadManager.retryDownload(activity ?: return@TextButton, download.id)
                                                }, modifier = Modifier.height(30.dp)) {
                                                    Text("Retry", color = NetflixRed, fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }"""

content = content.replace(old_notif, new_notif)

# Also fix the HomeHeader activeDownloads reference
content = content.replace(
    "val activeDownloads by com.example.util.RealDownloadManager.activeDownloads.collectAsStateWithLifecycle(initialValue = emptyList())",
    "val activeDownloads by com.example.util.RealDownloadManager.downloads.collectAsStateWithLifecycle(initialValue = emptyList())"
)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
