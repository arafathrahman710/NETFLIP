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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.filled.Download
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
    var downloadIsSeries by remember { mutableStateOf(false) }

    var currentUrl by remember { mutableStateOf("") }
    
    val activity = androidx.compose.ui.platform.LocalContext.current as? ComponentActivity
    BackHandler(enabled = true) {
        if (selectedTab != 0) {
            selectedTab = 0
        } else if (webViewRef?.canGoBack() == true) {
            webViewRef?.goBack()
        } else {
            activity?.finish()
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
                        modifier = Modifier.fillMaxSize().let {
                            if (selectedTab > 1) it.background(NetflixDark) else it
                        }
                    )
                    
                    if (selectedTab > 1) { 
                         Box(modifier = Modifier.fillMaxSize().background(NetflixDark))
                    }
                }
                
                // Top header for Home
                if (selectedTab == 0) {
                    HomeHeader(onProfileClick = { selectedTab = 3 })
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
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "Tab Transition"
            ) { targetTab ->
                when (targetTab) {
                    0, 1 -> {
                        // Empty box because WebView is behind
                        Box(modifier = Modifier.fillMaxSize()) {
                            val isMovieOrSeries = currentUrl.contains("movie") || currentUrl.contains("series") || currentUrl.contains("tv") || currentUrl.contains("episode") || currentUrl.contains("watch") || currentUrl.contains("netflip.com/title")
                            if (isMovieOrSeries) {
                                // Add a Floating Action Button to simulate saving a video on Home/Search tab
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(24.dp)
                                        .padding(bottom = 72.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    FloatingActionButton(
                                        onClick = {
                                            webViewRef?.let { wv ->
                                                val title = wv.title ?: "Unknown Video"
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
                                        Icon(Icons.Default.Favorite, contentDescription = "Add to Watchlist", tint = Color.White)
                                    }
                                    
                                    FloatingActionButton(
                                        onClick = {
                                            webViewRef?.let { wv ->
                                                val url = wv.url ?: ""
                                                if (url.contains("series") || url.contains("tv")) {
                                                    downloadIsSeries = true
                                                    showDownloadDialog = true
                                                } else if (url.contains("movie") || url.contains("episode") || url.contains("video")) {
                                                    downloadIsSeries = false
                                                    showDownloadDialog = true
                                                } else {
                                                    // Fallback just save it
                                                    val title = wv.title ?: "Unknown Video"
                                                    if (url.isNotEmpty()) {
                                                        CoroutineScope(Dispatchers.IO).launch {
                                                            val db = AppDatabase.getDatabase(wv.context)
                                                            db.savedVideoDao().insertVideo(SavedVideo(title = title, url = url))
                                                        }
                                                        android.widget.Toast.makeText(wv.context, "Added to Downloads", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                }
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
                                    val title = wv.title ?: "Unknown Video"
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
                                        val title = wv.title ?: "Unknown Video"
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
fun HomeHeader(onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.Transparent)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
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



@Composable
fun ProfileScreen(onHomeClick: () -> Unit, onDownloadClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val watchlistVideos by db.watchlistDao().getAllWatchlistVideos().collectAsStateWithLifecycle(initialValue = emptyList())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixDark)
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("My Profile", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
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
            
            Text("My Watchlist", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(16.dp))
            
            if (watchlistVideos.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("Your watchlist is empty", color = NetflixGrayText)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(watchlistVideos) { video ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixDark),
        contentAlignment = if (savedVideos.isEmpty()) Alignment.Center else Alignment.TopStart
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
            
            if (savedVideos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
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
                            // Delete button could go here
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun NetflipWebView(
    onWebViewCreated: (WebView) -> Unit,
    onLoadingStateChanged: (Boolean) -> Unit,
    onNavigationStateChanged: (Boolean) -> Unit,
    onUrlChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
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
                    "1xbet", "aliexpress", "bet365", "melbet", "linebet"
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
                        
                        val jsCode = """
                            (function() {
                                // 1. CSS Blocker
                                const style = document.createElement('style');
                                style.textContent = `
                                    ins.adsbygoogle, .adsbygoogle,
                                    [id^="google_ads"], [id*="doubleclick"],
                                    .popup, .pop-up, .overlay-ad, .ad-overlay,
                                    .interstitial, [class*="interstitial"],
                                    script[src*="ads"], script[src*="doubleclick"],
                                    .advertisement, [data-ad], [aria-label="advertisement"],
                                    #ad-container, .video-ads, .ytp-ad-module, .ad-showing,
                                    div[id^="ad_"], div[class^="ad_"],
                                    iframe[src*="ads"], iframe[src*="doubleclick"], iframe[src*="popads"]
                                    { display: none !important; visibility: hidden !important; opacity: 0 !important; height: 0 !important; width: 0 !important; position: absolute !important; pointer-events: none !important; }
                                `;
                                document.head.appendChild(style);

                                // 2. Popup Blocker (Aggressive)
                                window.open = function() { return null; };
                                window._open = window.open;
                                document.addEventListener('click', function(e) {
                                    const target = e.target.closest('a');
                                    if (target && target.target === '_blank') {
                                        const href = target.href;
                                        if (href && !href.includes('stream.terousd.online') && !href.includes('netflip') && !href.includes('terousd')) {
                                            e.preventDefault();
                                            e.stopPropagation();
                                        }
                                    }
                                }, true);

                                // Intercept dynamic script injections
                                const originalAppendChild = Node.prototype.appendChild;
                                Node.prototype.appendChild = function(node) {
                                    if (node.tagName === 'SCRIPT') {
                                        const src = node.src || '';
                                        if (src.includes('ads') || src.includes('pop') || src.includes('track') || src.includes('click')) {
                                            if (!src.includes('stream.terousd.online') && !src.includes('netflip')) {
                                                return node; // block execution
                                            }
                                        }
                                    }
                                    return originalAppendChild.call(this, node);
                                };

                                // 3. Mutation Observer for Ads and Text Replacement
                                const adSelectors = ['.adsbygoogle', '[id*="ad-slot"]', '[class*="ad-unit"]', '.video-ads', '.ytp-ad-module', 'iframe[src*="ads"]', 'div[class*="ad-"]'];
                                const observer = new MutationObserver(function(mutations) {
                                    // Remove Ads
                                    adSelectors.forEach(sel => {
                                        document.querySelectorAll(sel).forEach(el => el.remove());
                                    });
                                    // Auto-skip video ads
                                    const skipBtn = document.querySelector('.ytp-ad-skip-button, .skip-ad, .video-ad-skip');
                                    if(skipBtn) skipBtn.click();
                                    const vids = document.querySelectorAll('video');
                                    vids.forEach(v => {
                                        if(v.closest('.ad-showing') || document.querySelector('.video-ads') || (v.src && (v.src.includes('1xbet') || v.src.includes('ads') || v.src.includes('aliexpress')))) {
                                            if(isFinite(v.duration) && !isNaN(v.duration)) {
                                                v.currentTime = v.duration;
                                            } else {
                                                v.muted = true;
                                                v.style.display = 'none';
                                            }
                                        }
                                    });
                                });
                                observer.observe(document.body, { childList: true, subtree: true });

                                // 4. Text Replacement
                                const replaceText = () => {
                                    const walk = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, null, false);
                                    let node;
                                    while(node = walk.nextNode()) {
                                        if(node.nodeValue.match(/Terousd/i)) {
                                            node.nodeValue = node.nodeValue.replace(/Terousd/gi, 'NETFLIP');
                                        }
                                    }
                                };
                                replaceText();
                                setInterval(replaceText, 2000); // Check periodically
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
                
                webChromeClient = WebChromeClient()
                
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
