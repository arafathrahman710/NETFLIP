#!/bin/bash
# Replaces ProfileScreen and DownloadsScreen in MainActivity.kt

sed -i '/fun ProfileScreen/,/fun NetflipWebView/{
  /fun ProfileScreen/!{
    /fun NetflipWebView/!d
  }
}' app/src/main/java/com/example/MainActivity.kt

cat << 'CODE' > temp_screens.kt
@Composable
fun ProfileScreen(onHomeClick: () -> Unit, onDownloadClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val watchlistVideos by db.watchlistDao().getAllWatchlistVideos().collectAsStateWithLifecycle(initialValue = emptyList())
    val favouriteVideos by db.favouriteVideoDao().getAllFavourites().collectAsStateWithLifecycle(initialValue = emptyList())
    val activeDownloads by com.example.util.DownloadSimulationManager.activeDownloads.collectAsStateWithLifecycle(initialValue = emptyList())
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        com.example.ui.SettingsScreen(onBackClick = { showSettings = false })
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixDark)
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(48.dp))
                Text("My Profile", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                androidx.compose.material3.IconButton(onClick = { showSettings = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Guest", color = Color.White, fontSize = 18.sp)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = onHomeClick) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Home", color = Color.White)
                }
                TextButton(onClick = onDownloadClick) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Downloads", color = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                if (activeDownloads.isNotEmpty()) {
                    item {
                        Text("Active Downloads", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                    }
                    items(activeDownloads) { download ->
                        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).background(NetflixDarker, RoundedCornerShape(8.dp)).padding(16.dp)) {
                            Text(download.title, color = Color.White, maxLines = 1)
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = { download.progress },
                                modifier = Modifier.fillMaxWidth(),
                                color = NetflixRed,
                                trackColor = Color.DarkGray
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                item {
                    Text("My Watchlist", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                }
                
                if (watchlistVideos.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                            Text("Your watchlist is empty", color = NetflixGrayText)
                        }
                    }
                } else {
                    items(watchlistVideos) { video ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(video.title.takeIf { it.isNotBlank() } ?: "Unknown Title", color = Color.White, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                Text(video.url, color = NetflixGrayText, fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            androidx.compose.material3.IconButton(
                                onClick = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        db.watchlistDao().deleteVideoById(video.id)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NetflixRed)
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                item {
                    Text("Favourite", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                }
                
                if (favouriteVideos.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                            Text("No favourites yet", color = NetflixGrayText)
                        }
                    }
                } else {
                    items(favouriteVideos) { video ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = NetflixRed)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(video.title.takeIf { it.isNotBlank() } ?: "Unknown Title", color = Color.White, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                Text(video.url, color = NetflixGrayText, fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            androidx.compose.material3.IconButton(
                                onClick = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        db.favouriteVideoDao().deleteFavourite(video.url)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NetflixRed)
                            }
                        }
                    }
                }
            }
                        
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
fun DownloadsScreen(onBackClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val savedVideos by db.savedVideoDao().getAllSavedVideos().collectAsStateWithLifecycle(initialValue = emptyList())
    val activeDownloads by com.example.util.DownloadSimulationManager.activeDownloads.collectAsStateWithLifecycle(initialValue = emptyList())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                androidx.compose.material3.IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "My Downloads",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (activeDownloads.isNotEmpty()) {
                    item {
                        Text("Downloading...", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                    }
                    items(activeDownloads) { download ->
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).background(NetflixDarker, RoundedCornerShape(8.dp)).padding(16.dp)) {
                            Text(download.title, color = Color.White, maxLines = 1)
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = { download.progress },
                                modifier = Modifier.fillMaxWidth(),
                                color = NetflixRed,
                                trackColor = Color.DarkGray
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                if (savedVideos.isEmpty() && activeDownloads.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = NetflixGrayText,
                                    modifier = Modifier.size(80.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No downloads yet", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Movies and shows you download will appear here.", color = NetflixGrayText, fontSize = 14.sp)
                            }
                        }
                    }
                } else {
                    item {
                        if (savedVideos.isNotEmpty()) {
                            Text("Completed", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                        }
                    }
                    items(savedVideos) { video ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .background(NetflixDarker, RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(video.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text(video.url, color = NetflixGrayText, fontSize = 12.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

CODE

sed -i '/fun ProfileScreen/r temp_screens.kt' app/src/main/java/com/example/MainActivity.kt
sed -i '/fun ProfileScreen/d' app/src/main/java/com/example/MainActivity.kt
rm temp_screens.kt
