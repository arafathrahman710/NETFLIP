#!/bin/bash
sed -i '/if (showDownloadDialog) {/i \
            if (showNotificationsDialog) {\
                androidx.compose.ui.window.Dialog(onDismissRequest = { showNotificationsDialog = false }) {\
                    val activeDownloads by com.example.util.DownloadSimulationManager.activeDownloads.collectAsStateWithLifecycle(initialValue = emptyList())\
                    Box(\
                        modifier = Modifier\
                            .fillMaxWidth()\
                            .background(NetflixDarker, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))\
                            .padding(16.dp)\
                    ) {\
                        Column {\
                            Text("Notifications", color = Color.White, fontSize = 20.sp, androidx.compose.ui.text.font.FontWeight.Bold)\
                            Spacer(modifier = Modifier.height(16.dp))\
                            if (activeDownloads.isEmpty()) {\
                                Text("No active downloads or notifications.", color = Color.Gray)\
                            } else {\
                                androidx.compose.foundation.lazy.LazyColumn {\
                                    items(activeDownloads) { download ->\
                                        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {\
                                            Text(download.title, color = Color.White, maxLines = 1)\
                                            Spacer(modifier = Modifier.height(4.dp))\
                                            LinearProgressIndicator(\
                                                progress = { download.progress },\
                                                modifier = Modifier.fillMaxWidth().height(4.dp),\
                                                color = NetflixRed,\
                                                trackColor = Color.DarkGray\
                                            )\
                                            Text("${(download.progress * 100).toInt()}%", color = Color.Gray, fontSize = 12.sp)\
                                        }\
                                    }\
                                }\
                            }\
                        }\
                    }\
                }\
            }\
' app/src/main/java/com/example/MainActivity.kt
