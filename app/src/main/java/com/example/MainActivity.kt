package com.example

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.filled.Download
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppDatabase
import com.example.data.SavedVideo
import com.example.data.WatchlistVideo
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NetflixDark
import com.example.ui.theme.NetflixDarker
import com.example.ui.theme.NetflixGrayText
import com.example.ui.theme.NetflixRed
import com.example.ui.theme.glassmorphism
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppNavigator()
            }
        }
    }
}

@Composable
fun AppNavigator() {
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2500)
        showSplash = false
    }

    Box(modifier = Modifier.fillMaxSize().background(NetflixDark)) {
        MainScreen()
        
        AnimatedVisibility(
            visible = showSplash,
            enter = fadeIn(animationSpec = tween(0)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            SplashScreen()
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "NETFLIP",
                color = NetflixRed,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .size(width = 120.dp, height = 4.dp)
                    .background(NetflixRed)
            )
        }
    }
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var canGoBack by remember { mutableStateOf(false) }
    var showDownloadDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var downloadIsSeries by remember { mutableStateOf(false) }
    var webViewOpacity by remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    var webViewScale by remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    var webViewOffsetY by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }

    var currentUrl by remember { mutableStateOf("") }
    var wasMoviePage by remember { mutableStateOf(false) }
    
    val activity = androidx.compose.ui.platform.LocalContext.current as? androidx.activity.ComponentActivity
    val fullScreenHelper = remember(activity) { activity?.let { com.example.util.FullScreenHelper(it) } }
    val isGlassmorphism by com.example.util.PreferencesManager.glassmorphismFlow.collectAsStateWithLifecycle(initialValue = false)

    BackHandler(enabled = true) {
        if (fullScreenHelper?.isFullScreen() == true) {
            fullScreenHelper?.hideCustomView()
        } else if (selectedTab != 0) {
            selectedTab = 0
        } else {
            webViewRef?.evaluateJavascript(
                "(function() { if (window.location.pathname !== \"/\" && window.location.pathname !== \"\") { window.history.back(); return \"true\"; } else { return \"false\"; } })();"
            ) { result ->
                if (result == "\"false\"") {
                    if (webViewRef?.canGoBack() == true) {
                        webViewRef?.goBack()
                    } else {
                        activity?.finish()
                    }
                }
            }
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(NetflixDark)
        ) {
            // Keep WebView always in composition but control visibility/layering
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopCenter)
            ) {
                // The Web Content
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(top = if (selectedTab == 1) 72.dp else 0.dp)
                ) {
                    NetflipWebView(
                        onWebViewCreated = { webViewRef = it },
                        onLoadingStateChanged = { isLoading = it },
                        onNavigationStateChanged = { canGoBack = it },
                        onUrlChanged = { currentUrl = it },
                        fullScreenHelper = fullScreenHelper,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = webViewScale
                                scaleY = webViewScale
                                translationY = webViewOffsetY
                                alpha = webViewOpacity
                            }
                            .let {
                                if (selectedTab > 1) it.background(NetflixDark) else it
                            }
                    )
                    
                    if (selectedTab > 1) { 
                         Box(modifier = Modifier.fillMaxSize().background(NetflixDark))
                    }
                }
                
                // Top header for Home
                if (selectedTab == 0) {
                    HomeHeader(onProfileClick = { selectedTab = 3 }, onNotificationsClick = { showNotificationsDialog = true })
                }
                
                // Loading Overlay for WebView
                if (isLoading && selectedTab <= 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(NetflixDark)
                    ) {
                        SkeletonLoader()
                    }
                }
            }

            LaunchedEffect(currentUrl) {
                val cleanUrl = currentUrl.trim().lowercase()
                var isMoviePage = false
                if (cleanUrl.isNotEmpty() && 
                    cleanUrl != "https://stream.terousd.online" && 
                    cleanUrl != "https://stream.terousd.online/" && 
                    cleanUrl != "http://stream.terousd.online" && 
                    cleanUrl != "http://stream.terousd.online/") {
                    
                    val hasGenreCategory = cleanUrl.contains("/genre/") ||
                                           cleanUrl.contains("/genres/") ||
                                           cleanUrl.contains("/category/") ||
                                           cleanUrl.contains("/year/") ||
                                           cleanUrl.contains("/country/") ||
                                           cleanUrl.contains("/page/") ||
                                           cleanUrl.contains("/search/") ||
                                           cleanUrl.contains("/type/") ||
                                           cleanUrl.contains("?s=") ||
                                           cleanUrl.contains("?search=") ||
                                           cleanUrl.contains("?genre=") ||
                                           cleanUrl.contains("?year=")
                    
                    if (!hasGenreCategory) {
                        try {
                            val uri = android.net.Uri.parse(currentUrl)
                            val segments = uri.pathSegments ?: emptyList()
                            val cleanSegments = segments.filter { it.isNotEmpty() }
                            if (cleanSegments.isNotEmpty()) {
                                val firstSegment = cleanSegments.first().lowercase()
                                val systemPages = listOf(
                                    "wp-admin", "wp-content", "wp-includes", "assets", "css", "js", "images",
                                    "contact", "about", "dmca", "privacy-policy", "terms", "search", 
                                    "genre", "genres", "category", "categories", "tag", "tags", 
                                    "year", "years", "country", "countries", "page", "trending", 
                                    "popular", "top-imdb", "ratings"
                                )
                                if (firstSegment !in systemPages) {
                                    val singleSegmentListingPages = listOf(
                                        "movies", "movie", "series", "tv", "tvshows", "tvshow", 
                                        "episodes", "episode", "seasons", "season"
                                    )
                                    if (cleanSegments.size == 1) {
                                        isMoviePage = firstSegment !in singleSegmentListingPages
                                    } else {
                                        val validPrefixes = listOf(
                                            "movie", "movies", "series", "tv", "tvshows", "tvshow", 
                                            "episode", "episodes", "season", "seasons", "watch", "title", "play"
                                        )
                                        if (firstSegment in validPrefixes) {
                                            isMoviePage = cleanSegments[1].isNotEmpty()
                                        } else {
                                            val host = uri.host ?: ""
                                            isMoviePage = host.contains("terousd")
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                }

                val wasMovie = wasMoviePage
                wasMoviePage = isMoviePage

                if (isMoviePage) {
                    webViewOpacity = 0f
                    webViewScale = 0.85f
                    webViewOffsetY = 200f
                    
                    // Animate parallelly using coroutine launch
                    launch {
                        androidx.compose.animation.core.animate(
                            initialValue = 0f,
                            targetValue = 1f,
                            animationSpec = androidx.compose.animation.core.tween(
                                durationMillis = 250, 
                                easing = androidx.compose.animation.core.LinearEasing
                            )
                        ) { value, _ ->
                            webViewOpacity = value
                        }
                    }
                    launch {
                        androidx.compose.animation.core.animate(
                            initialValue = 0.85f,
                            targetValue = 1f,
                            animationSpec = androidx.compose.animation.core.spring(
                                dampingRatio = 0.8f,
                                stiffness = 300f
                            )
                        ) { value, _ ->
                            webViewScale = value
                        }
                    }
                    launch {
                        androidx.compose.animation.core.animate(
                            initialValue = 200f,
                            targetValue = 0f,
                            animationSpec = androidx.compose.animation.core.spring(
                                dampingRatio = 0.8f,
                                stiffness = 300f
                            )
                        ) { value, _ ->
                            webViewOffsetY = value
                        }
                    }
                } else if (wasMovie) {
                    webViewOpacity = 0f
                    webViewScale = 1.15f
                    webViewOffsetY = 0f
                    
                    launch {
                        androidx.compose.animation.core.animate(
                            initialValue = 0f,
                            targetValue = 1f,
                            animationSpec = androidx.compose.animation.core.tween(
                                durationMillis = 200, 
                                easing = androidx.compose.animation.core.LinearEasing
                            )
                        ) { value, _ ->
                            webViewOpacity = value
                        }
                    }
                    launch {
                        androidx.compose.animation.core.animate(
                            initialValue = 1.15f,
                            targetValue = 1f,
                            animationSpec = androidx.compose.animation.core.spring(
                                dampingRatio = 0.85f,
                                stiffness = 350f
                            )
                        ) { value, _ ->
                            webViewScale = value
                        }
                    }
                } else {
                    webViewScale = 1f
                    webViewOffsetY = 0f
                    webViewOpacity = 0f
                    androidx.compose.animation.core.animate(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = androidx.compose.animation.core.tween(
                            durationMillis = 280, 
                            easing = androidx.compose.animation.core.FastOutSlowInEasing
                        )
                    ) { value, _ ->
                        webViewOpacity = value
                    }
                }
            }
            LaunchedEffect(isLoading) {
                if (isLoading) {
                    delay(3000)
                    if (isLoading) {
                        webViewRef?.reload()
                    }
                }
            }

            // Search Bar Overlay
            if (selectedTab == 1) {
                com.example.ui.SearchOverlay(
                    onSearch = { query ->
                        val js = """
                            const searchInput = document.querySelector('input[type="search"]') || document.querySelector('input[placeholder*="Search"]');
                            if (searchInput) {
                                searchInput.value = '$query';
                                searchInput.dispatchEvent(new Event('input', { bubbles: true }));
                                const form = searchInput.closest('form');
                                if (form) form.submit();
                                else {
                                    searchInput.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
                                }
                            }
                        """.trimIndent()
                        webViewRef?.evaluateJavascript(js, null)
                    }
                )
            }
            
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState == 3) {
                        (fadeIn(animationSpec = tween(300, easing = androidx.compose.animation.core.EaseInOut)) + 
                         androidx.compose.animation.slideInVertically(animationSpec = tween(300, easing = androidx.compose.animation.core.EaseInOut)) { it / 2 }) togetherWith 
                        fadeOut(animationSpec = tween(300, easing = androidx.compose.animation.core.EaseInOut))
                    } else if (initialState == 3) {
                        fadeIn(animationSpec = tween(300, easing = androidx.compose.animation.core.EaseInOut)) togetherWith 
                        (fadeOut(animationSpec = tween(300, easing = androidx.compose.animation.core.EaseInOut)) + 
                         androidx.compose.animation.slideOutVertically(animationSpec = tween(300, easing = androidx.compose.animation.core.EaseInOut)) { it / 2 })
                    } else {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    }
                },
                label = "Tab Transition"
            ) { targetTab ->
                when (targetTab) {
                    0, 1 -> {
                        // Empty box because WebView is behind
                        Box(modifier = Modifier.fillMaxSize()) {
                            val isMovieOrSeries = remember(currentUrl) {
                                val cleanUrl = currentUrl.trim().lowercase()
                                if (cleanUrl.isEmpty()) return@remember false
                                
                                if (cleanUrl == "https://stream.terousd.online" ||
                                    cleanUrl == "https://stream.terousd.online/" ||
                                    cleanUrl == "http://stream.terousd.online" ||
                                    cleanUrl == "http://stream.terousd.online/") {
                                    return@remember false
                                }
                                
                                if (cleanUrl.contains("/genre/") ||
                                    cleanUrl.contains("/genres/") ||
                                    cleanUrl.contains("/category/") ||
                                    cleanUrl.contains("/year/") ||
                                    cleanUrl.contains("/country/") ||
                                    cleanUrl.contains("/page/") ||
                                    cleanUrl.contains("/search/") ||
                                    cleanUrl.contains("/type/") ||
                                    cleanUrl.contains("?s=") ||
                                    cleanUrl.contains("?search=") ||
                                    cleanUrl.contains("?genre=") ||
                                    cleanUrl.contains("?year=")) {
                                    return@remember false
                                }
                                
                                try {
                                    val uri = android.net.Uri.parse(currentUrl)
                                    val segments = uri.pathSegments ?: emptyList()
                                    
                                    val cleanSegments = segments.filter { it.isNotEmpty() }
                                    if (cleanSegments.isEmpty()) {
                                        return@remember false
                                    }
                                    
                                    val firstSegment = cleanSegments.first().lowercase()
                                    
                                    // System segments/pages to exclude
                                    val systemPages = listOf(
                                        "wp-admin", "wp-content", "wp-includes", "assets", "css", "js", "images",
                                        "contact", "about", "dmca", "privacy-policy", "terms", "search", 
                                        "genre", "genres", "category", "categories", "tag", "tags", 
                                        "year", "years", "country", "countries", "page", "trending", 
                                        "popular", "top-imdb", "ratings"
                                    )
                                    
                                    if (firstSegment in systemPages) {
                                        return@remember false
                                    }
                                    
                                    // Single segment listing pages to exclude
                                    val singleSegmentListingPages = listOf(
                                        "movies", "movie", "series", "tv", "tvshows", "tvshow", 
                                        "episodes", "episode", "seasons", "season"
                                    )
                                    
                                    if (cleanSegments.size == 1) {
                                        // If there's only 1 segment, check that it's not a general listing page like "/movies/" or "/tv/"
                                        firstSegment !in singleSegmentListingPages
                                    } else {
                                        // If there are 2 or more segments, and the first segment is a listing type prefix,
                                        // e.g. "/movie/avengers/", we want to show buttons.
                                        val validPrefixes = listOf(
                                            "movie", "movies", "series", "tv", "tvshows", "tvshow", 
                                            "episode", "episodes", "season", "seasons", "watch", "title", "play"
                                        )
                                        if (firstSegment in validPrefixes) {
                                            cleanSegments[1].isNotEmpty()
                                        } else {
                                            val host = uri.host ?: ""
                                            host.contains("terousd")
                                        }
                                    }
                                } catch (e: Exception) {
                                    false
                                }
                            }
                            androidx.compose.animation.AnimatedVisibility(
                                visible = isMovieOrSeries,
                                enter = androidx.compose.animation.slideInVertically(
                                    initialOffsetY = { it },
                                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 400, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                                ) + androidx.compose.animation.fadeIn(),
                                exit = androidx.compose.animation.slideOutVertically(
                                    targetOffsetY = { it },
                                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 300, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                                ) + androidx.compose.animation.fadeOut(),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(24.dp)
                                    .padding(bottom = 72.dp)
                            ) {
                                // Add Floating Action Buttons for Watchlist, Favourite, and Download
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    FloatingActionButton(
                                        onClick = {
                                            webViewRef?.let { wv ->
                                                val title = wv.title?.replace(Regex("Terousd|terousd", RegexOption.IGNORE_CASE), "NETFLIP") ?: "Unknown Video"
                                                val url = wv.url ?: ""
                                                if (url.isNotEmpty()) {
                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        val db = AppDatabase.getDatabase(wv.context)
                                                        db.watchlistDao().insertVideo(WatchlistVideo(title = title, url = url))
                                                    }
                                                    android.widget.Toast.makeText(wv.context, "Added to Watchlist", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        containerColor = Color.DarkGray,
                                    ) {
                                        Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Add to Watchlist", tint = Color.White)
                                    }

                                    FloatingActionButton(
                                        onClick = {
                                            webViewRef?.let { wv ->
                                                val title = wv.title?.replace(Regex("Terousd|terousd", RegexOption.IGNORE_CASE), "NETFLIP") ?: "Unknown Video"
                                                val url = wv.url ?: ""
                                                if (url.isNotEmpty()) {
                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        val db = AppDatabase.getDatabase(wv.context)
                                                        db.favouriteVideoDao().insertFavourite(com.example.data.FavouriteVideo(title = title, url = url))
                                                    }
                                                    android.widget.Toast.makeText(wv.context, "Added to Favourites", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        containerColor = Color.DarkGray,
                                    ) {
                                        Icon(Icons.Default.Favorite, contentDescription = "Add to Favourites", tint = NetflixRed)
                                    }
                                    
                                    FloatingActionButton(
                                        onClick = {
                                            webViewRef?.let { wv ->
                                                val url = wv.url ?: ""
                                                // Trigger fake download simulation
                                                com.example.util.RealDownloadManager.startDownload(wv.context, wv.title?.replace(Regex("Terousd|terousd", RegexOption.IGNORE_CASE), "NETFLIP") ?: "Unknown Video", url)
                                            }
                                        },
                                        containerColor = NetflixRed,
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = "Download Video", tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                    2 -> DownloadsScreen(onBackClick = { selectedTab = 0 })
                    3 -> ProfileScreen(onHomeClick = { selectedTab = 0 }, onDownloadClick = { selectedTab = 2 })
                }
            }
            
            if (showNotificationsDialog) {
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
            }

            if (showDownloadDialog) {
                AlertDialog(
                    onDismissRequest = { showDownloadDialog = false },
                    title = { Text(if (downloadIsSeries) "Download Series" else "Download Movie") },
                    text = { 
                        Text(if (downloadIsSeries) "Would you like to download a single episode or the entire series?" 
                             else "Would you like to download this movie for offline viewing?") 
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                webViewRef?.let { wv ->
                                    val title = wv.title?.replace(Regex("Terousd|terousd", RegexOption.IGNORE_CASE), "NETFLIP") ?: "Unknown Video"
                                    val url = wv.url ?: ""
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val db = AppDatabase.getDatabase(wv.context)
                                        db.savedVideoDao().insertVideo(SavedVideo(title = title, url = url))
                                    }
                                    android.widget.Toast.makeText(wv.context, "Download Started in Background...", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                showDownloadDialog = false
                            }
                        ) {
                            Text(if (downloadIsSeries) "Download Entire Series" else "Download", color = NetflixRed)
                        }
                    },
                    dismissButton = {
                        if (downloadIsSeries) {
                            TextButton(
                                onClick = {
                                    webViewRef?.let { wv ->
                                        val title = wv.title?.replace(Regex("Terousd|terousd", RegexOption.IGNORE_CASE), "NETFLIP") ?: "Unknown Video"
                                        val url = wv.url ?: ""
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val db = AppDatabase.getDatabase(wv.context)
                                            db.savedVideoDao().insertVideo(SavedVideo(title = "$title (Episode)", url = url))
                                        }
                                        android.widget.Toast.makeText(wv.context, "Downloading Episode...", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    showDownloadDialog = false
                                }
                            ) {
                                Text("Single Episode", color = Color.White)
                            }
                        } else {
                            TextButton(onClick = { showDownloadDialog = false }) {
                                Text("Cancel", color = Color.White)
                            }
                        }
                    },
                    containerColor = NetflixDarker,
                    titleContentColor = Color.White,
                    textContentColor = NetflixGrayText
                )
            }
        }
    }
}

@Composable
fun HomeHeader(onProfileClick: () -> Unit, onNotificationsClick: () -> Unit) {
    val activeDownloads by com.example.util.RealDownloadManager.downloads.collectAsStateWithLifecycle(initialValue = emptyList())
    val isGlassmorphism by com.example.util.PreferencesManager.glassmorphismFlow.collectAsStateWithLifecycle(initialValue = false)
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
}



@Composable
fun ProfileScreen(onHomeClick: () -> Unit, onDownloadClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val watchlistVideos by db.watchlistDao().getAllWatchlistVideos().collectAsStateWithLifecycle(initialValue = emptyList())
    val favouriteVideos by db.favouriteVideoDao().getAllFavourites().collectAsStateWithLifecycle(initialValue = emptyList())
    val activeDownloads by com.example.util.RealDownloadManager.downloads.collectAsStateWithLifecycle(initialValue = emptyList())
    val isGlassmorphism by com.example.util.PreferencesManager.glassmorphismFlow.collectAsStateWithLifecycle(initialValue = false)


    var smartEnhance by remember { mutableStateOf(com.example.util.PreferencesManager.isSmartEnhanceEnabled(context)) }
    var hdrEnabled by remember { mutableStateOf(com.example.util.PreferencesManager.isHdrEnabled(context)) }
    var dolbyVision by remember { mutableStateOf(com.example.util.PreferencesManager.isDolbyVisionEnabled(context)) }

    fun checkHdrSupport(type: Int): Boolean {
        return try {
            val display = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                context.display
            } else {
                @Suppress("DEPRECATION")
                (context.getSystemService(android.content.Context.WINDOW_SERVICE) as android.view.WindowManager).defaultDisplay
            }
            val capabilities = display?.hdrCapabilities?.supportedHdrTypes ?: intArrayOf()
            if (type == -1) capabilities.isNotEmpty() else capabilities.contains(type)
        } catch (e: Exception) {
            false
        }
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
                Box {
                    
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
                                Text(video.url.replace("stream.terousd.online", "netflip.com"), color = NetflixGrayText, fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
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
                                    if (dolbyVision) {
                                        android.widget.Toast.makeText(context, "Dolby Vision disabled due to conflict", android.widget.Toast.LENGTH_SHORT).show()
                                    }
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
                                    if (hdrEnabled) {
                                        android.widget.Toast.makeText(context, "HDR disabled due to conflict", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    hdrEnabled = false
                                }
                            }
                        )
                    }
                }
                
                item {
                    var showSpeedTest by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(16.dp)
                            .clickable { showSpeedTest = true },
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
                                .background(if (isGlassmorphism) Color.Transparent else NetflixDarker, RoundedCornerShape(16.dp)).glassmorphism(isGlassmorphism)
                                .clip(RoundedCornerShape(16.dp))
                            ) {
                                AndroidView(factory = { ctx ->
                                    android.webkit.WebView(ctx).apply {
                                        layoutParams = android.view.ViewGroup.LayoutParams(
                                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                        settings.apply {
                                            javaScriptEnabled = true
                                            domStorageEnabled = true
                                            useWideViewPort = true
                                            loadWithOverviewMode = true
                                            databaseEnabled = true
                                            userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                                            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                        }
                                        webChromeClient = android.webkit.WebChromeClient()
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
                
                item { Spacer(modifier = Modifier.height(24.dp)) }
                
                item {
                    Text("Appearance", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
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
                            Text("Glassmorphism UI", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Enable frosted glass effect on Cards and Modals", color = NetflixGrayText, fontSize = 12.sp)
                        }
                        androidx.compose.material3.Switch(
                            checked = isGlassmorphism,
                            onCheckedChange = {
                                com.example.util.PreferencesManager.setGlassmorphismEnabled(context, it)
                            }
                        )
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
                                Text(video.url.replace("stream.terousd.online", "netflip.com"), color = NetflixGrayText, fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
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
    val isGlassmorphism by com.example.util.PreferencesManager.glassmorphismFlow.collectAsStateWithLifecycle(initialValue = false)
    val db = remember { AppDatabase.getDatabase(context) }
    val savedVideos by db.savedVideoDao().getAllSavedVideos().collectAsStateWithLifecycle(initialValue = emptyList())
    val activeDownloads by com.example.util.RealDownloadManager.downloads.collectAsStateWithLifecycle(initialValue = emptyList())

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
                                Text(video.url.replace("stream.terousd.online", "netflip.com"), color = NetflixGrayText, fontSize = 12.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NetflipWebView(
    onWebViewCreated: (WebView) -> Unit,
    onLoadingStateChanged: (Boolean) -> Unit,
    onNavigationStateChanged: (Boolean) -> Unit,
    onUrlChanged: (String) -> Unit = {},
    fullScreenHelper: com.example.util.FullScreenHelper? = null,
    modifier: Modifier = Modifier
) {
    val isGlassmorphism by com.example.util.PreferencesManager.glassmorphismFlow.collectAsStateWithLifecycle(initialValue = false)
    AndroidView(
        factory = { context ->
            val swipeRefreshLayout = SwipeRefreshLayout(context)
            swipeRefreshLayout.setColorSchemeColors(android.graphics.Color.parseColor("#E50914"))
            swipeRefreshLayout.setProgressBackgroundColorSchemeColor(android.graphics.Color.parseColor("#141414"))
            
            val webView = WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    loadsImagesAutomatically = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    setSupportZoom(false)
                    builtInZoomControls = false
                    displayZoomControls = false
                    cacheMode = WebSettings.LOAD_DEFAULT
                    databaseEnabled = true
                    setSupportMultipleWindows(true)
                    javaScriptCanOpenWindowsAutomatically = false
                }
                
                webChromeClient = object : android.webkit.WebChromeClient() {
                    override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: android.os.Message?): Boolean {
                        // Return false to block the popup window entirely (like 1xbet, aliexpress)
                        return false
                    }
                    
                    override fun onShowCustomView(view: android.view.View?, callback: CustomViewCallback?) {
                        super.onShowCustomView(view, callback)
                        if (view != null && callback != null) {
                            fullScreenHelper?.showCustomView(view, callback)
                        }
                    }

                    override fun onHideCustomView() {
                        super.onHideCustomView()
                        fullScreenHelper?.hideCustomView()
                    }
                }
                
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                setBackgroundColor(android.graphics.Color.parseColor("#141414"))
                
                setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                    val request = android.app.DownloadManager.Request(android.net.Uri.parse(url)).apply {
                        setMimeType(mimetype)
                        addRequestHeader("cookie", android.webkit.CookieManager.getInstance().getCookie(url))
                        addRequestHeader("User-Agent", userAgent)
                        setDescription("Downloading file...")
                        setTitle(android.webkit.URLUtil.guessFileName(url, contentDisposition, mimetype))
                        allowScanningByMediaScanner()
                        setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, android.webkit.URLUtil.guessFileName(url, contentDisposition, mimetype))
                    }
                    val dm = context.getSystemService(android.content.Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
                    dm.enqueue(request)
                    android.widget.Toast.makeText(context, "Download Started...", android.widget.Toast.LENGTH_LONG).show()
                }
                
                val adBlockDomains = listOf(
                    "doubleclick.net", "googlesyndication.com", "googleadservices.com",
                    "adservice.google.com", "ads.google.com", "amazon-adsystem.com",
                    "facebook.com/tr", "connect.facebook.net", "analytics.google.com",
                    "googletagmanager.com", "hotjar.com", "mc.yandex.ru",
                    "pagead2.googlesyndication.com", "adnxs.com", "adsystem",
                    "popads.net", "popcash.net", "propellerads.com", "exoclick.com",
                    "trafficjunky.net", "revenueads.net", "juicyads.com",
                    "ad-delivery.net", "ad.yieldmanager.com", "adbrite.com",
                    "adecn.com", "admob.com", "adtech.de", "adzerk.net",
                    "affiliates.com", "bannertrack.net", "clicksor.com",
                    "criteo.com", "falkag.net", "fastclick.net", "ibill.com",
                    "infinitesimall.com", "linkexchange.com", "mediaplex.com",
                    "scorecardresearch.com", "taboola.com", "outbrain.com",
                    "bidswitch.net", "rubiconproject.com", "pubmatic.com",
                    "openx.net", "casalemedia.com", "quantserve.com",
                    "lijit.com", "sharethis.com", "yieldoptimizer.com",
                    "adform.net", "mookie1.com", "tynt.com", "w55c.net",
                    "exponential.com", "undertone.com", "zedo.com",
                    "moatads.com", "imrworldwide.com", "krxd.net",
                    "innovid.com", "spotxchange.com", "teads.tv",
                    "adsterra.com", "a-ads.com", "onclickgate.com", "realsrv.com",
                    "onclickmaha.com", "best-onclick.com", "realsrv.com", "adxpremium.click",
                    "adxpansion.com", "bidgear.com", "hilltopads.net", "onclickadvert.com",
                    "adtrue.com", "adk2x.com", "1xbet.com", "aliexpress.com", "awin1.com"
                )
                
                val adBlockPaths = listOf(
                    "/ads/", "/banner/", "/popup/", "/popunder/", "/adserv", "/adsense",
                    "/adscript/", "/banners/", "/tracking/", "/track/", "/affiliate/",
                    "/pixel", "/analytics", "/pagead"
                )
                
                val adBlockKeywords = listOf(
                    "popunder", "adnetwork", "advertisement", "ad-script", "tracking",
                    "banner_ad", "popads", "propeller", "adsterra", "exoclick",
                    "1xbet", "ix-bet", "1xlite", "aliexpress", "s.click.aliexpress", 
                    "bet365", "melbet", "linebet", "trafficjunky.net", "vast", "vmap", "ima3", "adserver", "preroll", "midroll", "postroll"
                )

                webViewClient = object : WebViewClient() {
                    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                        super.doUpdateVisitedHistory(view, url, isReload)
                        onNavigationStateChanged(view?.canGoBack() == true)
                        url?.let { onUrlChanged(it) }
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        onLoadingStateChanged(true)
                        swipeRefreshLayout.isRefreshing = true
                        onNavigationStateChanged(view?.canGoBack() == true)
                        url?.let { onUrlChanged(it) }
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        onLoadingStateChanged(false)
                        swipeRefreshLayout.isRefreshing = false
                        onNavigationStateChanged(view?.canGoBack() == true)
                        url?.let { onUrlChanged(it) }
                        

                        val isSmartEnhance = com.example.util.PreferencesManager.isSmartEnhanceEnabled(context)
                        val isHdr = com.example.util.PreferencesManager.isHdrEnabled(context) || com.example.util.PreferencesManager.isDolbyVisionEnabled(context)
                        val isGlassmorphism = com.example.util.PreferencesManager.isGlassmorphismEnabled(context)
                        
                        val jsCode = """
                            (function() {
                                // --- Glassmorphism UI ---
                                ${if (isGlassmorphism) "const glassStyle = document.createElement('style'); glassStyle.textContent = `.card, .modal, .bottom-sheet, .drawer, [class*=\"card\"], [class*=\"modal\"], [class*=\"sheet\"], [class*=\"drawer\"], [class*=\"dialog\"], [class*=\"menu\"], .dropdown, [class*=\"dropdown\"], .trailer-modal-inner, .search-results-container, .mobile-search-overlay { background: rgba(255, 255, 255, 0.15) !important; backdrop-filter: blur(20px) !important; -webkit-backdrop-filter: blur(20px) !important; border: 1px solid rgba(255, 255, 255, 0.25) !important; box-shadow: 0 4px 30px rgba(0, 0, 0, 0.1) !important; }`; document.head.appendChild(glassStyle);" else ""}
                                
                                // --- Quick Logo Hide ---
                                const logoStyle = document.createElement("style");
                                logoStyle.textContent = ".navbar-brand, [class*=\"logo\"] { visibility: hidden !important; }";
                                document.head.appendChild(logoStyle);
                                
                                // --- Video Enhancement ---
                                const enhanceStyle = document.createElement("style");
                                let filterString = "";
                                ${if (isSmartEnhance && isHdr) "filterString = 'contrast(1.4) saturate(1.5) brightness(1.2) drop-shadow(0 0 5px rgba(255,255,255,0.2)) !important;';" else if (isSmartEnhance) "filterString = 'contrast(1.15) saturate(1.2) brightness(1.05) drop-shadow(0 0 5px rgba(255,255,255,0.2)) !important;';" else if (isHdr) "filterString = 'contrast(1.25) saturate(1.3) brightness(1.15) !important;';" else ""}
                                if (filterString) {
                                    enhanceStyle.textContent = `video { filter: ${'$'}{filterString} }`;
                                }

                                document.head.appendChild(enhanceStyle);

                                // --- Hide ad elements via CSS ---
                                const style = document.createElement('style');
                                style.textContent = `
                                  iframe[src*="ad"], iframe[id*="ad"], iframe[class*="ad"],
                                  div[id*="ad-"], div[class*="ad-"], div[id*="ads"], div[class*="ads"],
                                  div[id*="banner"], div[class*="banner"],
                                  ins.adsbygoogle, .adsbygoogle,
                                  [id^="google_ads"], [id*="doubleclick"],
                                  .popup, .pop-up, .overlay-ad, .ad-overlay,
                                  [class*="popup"], [id*="popup"],
                                  .interstitial, [class*="interstitial"],
                                  .vast-ad, .video-ad-overlay, [id*="ad-overlay"],
                                  #ad, .ad, #ads, .ads, .advertisement,
                                  [data-ad], [data-ads], [aria-label="advertisement"]
                                  { display: none !important; visibility: hidden !important; opacity: 0 !important; height: 0 !important; }
                                `;
                                document.head.appendChild(style);

                                // --- Disable window.open (popunder block) ---
                                window.open = function() { return null; };
                                window._open = window.open;

                                // --- Prevent click hijacking / forced redirects ---
                                document.addEventListener('click', function(e) {
                                  let el = e.target;
                                  while (el) {
                                    if (el.tagName === 'A' && el.href &&
                                        !el.href.includes('stream.terousd.online') && !el.href.includes('netflip') && !el.href.includes('terousd')) {
                                      e.preventDefault();
                                      e.stopImmediatePropagation();
                                    }
                                    el = el.parentElement;
                                  }
                                }, true);

                                // --- Continuously remove dynamically injected ad elements ---
                                // --- Hide the app logo via CSS if possible ---
                                const webHeaderStyle = document.createElement('style');
                                webHeaderStyle.innerHTML = 'img[alt*=\"terousd\" i], .header-icon-btn, .theme-switcher-btn, .mobile-search-btn, .header-search, .theme-switcher, .theme-toggle, .three-dot-menu-container { display: none !important; }';
                                document.head.appendChild(webHeaderStyle);

                                const replaceText = () => {
                                    if (document.title.match(/Terousd|terousd/i)) { document.title = "NETFLIP"; }
                                    const walk = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, null, false);
                                    let node;
                                    while(node = walk.nextNode()) {
                                        if(node.nodeValue.match(/Terousd/gi)) {
                                            node.nodeValue = node.nodeValue.replace(/Terousd/gi, "NETFLIP");
                                        }
                                    }
                                    document.querySelectorAll('*').forEach(el => {
                                        if(el.placeholder && el.placeholder.match(/Terousd/i)) {
                                            el.placeholder = el.placeholder.replace(/Terousd/gi, "NETFLIP");
                                        }
                                        if(el.title && el.title.match(/Terousd/i)) {
                                            el.title = el.title.replace(/Terousd/gi, "NETFLIP");
                                        }
                                        if(el.getAttribute('aria-label') && el.getAttribute('aria-label').match(/Terousd/i)) {
                                            el.setAttribute('aria-label', el.getAttribute('aria-label').replace(/Terousd/gi, "NETFLIP"));
                                        }
                                    });
                                };
                                replaceText();

                                // --- MutationObserver for ads added after page load ---
                                const adSelectors = ['iframe[src*="ad"]', '.adsbygoogle', '[id*="ad-slot"]', '[class*="ad-unit"]'];
                                const observer = new MutationObserver(function(mutations) {
                                  replaceText();
                                  adSelectors.forEach(sel => {
                                    document.querySelectorAll(sel).forEach(el => el.remove());
                                  });
                                  const skipBtn = document.querySelector('.ytp-ad-skip-button, .skip-ad, .video-ad-skip');
                                  if(skipBtn) skipBtn.click();
                                  const vids = document.querySelectorAll('video');
                                  vids.forEach(v => {
                                      if(v.closest('.ad-showing') || document.querySelector('.video-ads') || (v.src && (v.src.includes('1xbet') || v.src.includes('ads') || v.src.includes('aliexpress')))) {
                                          v.muted = true;
                                          v.style.display = 'none';
                                      }
                                  });
                                });
                                observer.observe(document.body, { childList: true, subtree: true, characterData: true });
                            })();
                        """.trimIndent()
                        
                        view?.evaluateJavascript(jsCode, null)
                    }
                    
                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        val url = request?.url?.toString() ?: return super.shouldInterceptRequest(view, request)
                        
                        val isAdDomain = adBlockDomains.any { url.contains(it, ignoreCase = true) }
                        val isAdPath = adBlockPaths.any { url.contains(it, ignoreCase = true) }
                        val isAdKeyword = adBlockKeywords.any { url.contains(it, ignoreCase = true) && !url.contains("stream.terousd.online", ignoreCase = true) }
                        
                        if (isAdDomain || isAdPath || isAdKeyword) {
                            return WebResourceResponse("text/plain", "UTF-8", java.io.ByteArrayInputStream("".toByteArray()))
                        }
                        return super.shouldInterceptRequest(view, request)
                    }
                    
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val url = request?.url?.toString() ?: return false
                        val isAdDomain = adBlockDomains.any { url.contains(it, ignoreCase = true) }
                        val isAdPath = adBlockPaths.any { url.contains(it, ignoreCase = true) }
                        val isAdKeyword = adBlockKeywords.any { url.contains(it, ignoreCase = true) && !url.contains("stream.terousd.online", ignoreCase = true) }
                        
                        if (isAdDomain || isAdPath || isAdKeyword) {
                            return true // Block
                        }
                        
                        return false // Allow
                    }
                }
                
                loadUrl("https://stream.terousd.online/")
                onWebViewCreated(this)
            }
            
            swipeRefreshLayout.addView(webView)
            swipeRefreshLayout.setOnRefreshListener {
                webView.reload()
            }
            
            swipeRefreshLayout
        },
        modifier = modifier
    )
}

@Composable
fun SkeletonLoader() {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 72.dp)
            .padding(horizontal = 16.dp)
    ) {
        // Hero banner skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.DarkGray.copy(alpha = alpha), RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Rows of items
        repeat(3) {
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(24.dp)
                    .background(Color.DarkGray.copy(alpha = alpha), RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(160.dp)
                            .background(Color.DarkGray.copy(alpha = alpha), RoundedCornerShape(8.dp))
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
