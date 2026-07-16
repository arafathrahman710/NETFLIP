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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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

    BackHandler(enabled = selectedTab != 0 || canGoBack) {
        if (selectedTab != 0) {
            selectedTab = 0
        } else if (canGoBack) {
            webViewRef?.goBack()
        }
    }
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = NetflixDarker.copy(alpha = 0.9f),
                contentColor = Color.White,
                modifier = Modifier.height(72.dp)
            ) {
                val tabs = listOf(
                    Icons.Default.Home to "Home",
                    Icons.Default.Search to "Search",
                    Icons.Default.Download to "Downloads",
                    Icons.Default.Person to "Profile"
                )
                
                tabs.forEachIndexed { index, (icon, label) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(icon, contentDescription = label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            unselectedIconColor = NetflixGrayText,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
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
                // Top header for Home
                if (selectedTab == 0) {
                    HomeHeader()
                }

                // The Web Content
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(top = if (selectedTab == 0) 56.dp else if (selectedTab == 1) 72.dp else 0.dp)
                ) {
                    TerousdWebView(
                        onWebViewCreated = { webViewRef = it },
                        onLoadingStateChanged = { isLoading = it },
                        onNavigationStateChanged = { canGoBack = it },
                        modifier = Modifier.fillMaxSize().let {
                            if (selectedTab > 1) it.background(NetflixDark) else it
                        }
                    )
                    
                    if (selectedTab > 1) {
                         Box(modifier = Modifier.fillMaxSize().background(NetflixDark))
                    }
                }
                
                // Loading Overlay for WebView
                if (isLoading && selectedTab <= 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(NetflixDark),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NetflixRed)
                    }
                }
            }

            // Search Bar Overlay
            if (selectedTab == 1) {
                SearchBar(
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
                            // Add a Floating Action Button to simulate saving a video on Home tab
                            if (targetTab == 0) {
                                FloatingActionButton(
                                    onClick = {
                                        webViewRef?.let { wv ->
                                            val title = wv.title ?: "Unknown Video"
                                            val url = wv.url ?: ""
                                            if (url.isNotEmpty()) {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    val db = AppDatabase.getDatabase(wv.context)
                                                    db.savedVideoDao().insertVideo(SavedVideo(title = title, url = url))
                                                }
                                            }
                                        }
                                    },
                                    containerColor = NetflixRed,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(24.dp)
                                        .padding(bottom = 72.dp) // above bottom bar
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Save Page")
                                }
                            }
                        }
                    }
                    2 -> DownloadsScreen()
                    3 -> ProfileScreen()
                }
            }
        }
    }
}

@Composable
fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.Transparent)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "NETFLIP",
            color = NetflixRed,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notifications",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun SearchBar(onSearch: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(NetflixDark)
            .padding(16.dp)
    ) {
        BasicTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxSize()
                .background(NetflixDarker, CircleShape)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
            cursorBrush = SolidColor(NetflixRed),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { onSearch(query) }
            ),
            decorationBox = { innerTextField ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = NetflixGrayText,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Box(Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text("Search movies, shows...", color = NetflixGrayText, fontSize = 16.sp)
                        }
                        innerTextField()
                    }
                }
            }
        )
    }
}

@Composable
fun ProfileScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixDark)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Who's Watching?", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Guest", color = Color.White, fontSize = 18.sp)
            
            Spacer(modifier = Modifier.height(64.dp))
            Text("App Version 1.0.0", color = NetflixGrayText, fontSize = 12.sp)
        }
    }
}

@Composable
fun DownloadsScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val savedVideos by db.savedVideoDao().getAllSavedVideos().collectAsStateWithLifecycle(initialValue = emptyList())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixDark),
        contentAlignment = if (savedVideos.isEmpty()) Alignment.Center else Alignment.TopStart
    ) {
        if (savedVideos.isEmpty()) {
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
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "My List",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
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
fun TerousdWebView(
    onWebViewCreated: (WebView) -> Unit,
    onLoadingStateChanged: (Boolean) -> Unit,
    onNavigationStateChanged: (Boolean) -> Unit,
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
                    "trafficjunky.net", "revenueads.net", "juicyads.com"
                )
                
                val adBlockPaths = listOf(
                    "/ads/", "/banner/", "/popup/", "/popunder/", "/adserv", "/adsense"
                )

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        onLoadingStateChanged(true)
                        swipeRefreshLayout.isRefreshing = true
                        onNavigationStateChanged(view?.canGoBack() == true)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        onLoadingStateChanged(false)
                        swipeRefreshLayout.isRefreshing = false
                        onNavigationStateChanged(view?.canGoBack() == true)
                        
                        val cssBlocker = """
                            (function() {
                                const style = document.createElement('style');
                                style.textContent = `
                                    ins.adsbygoogle, .adsbygoogle,
                                    [id^="google_ads"], [id*="doubleclick"],
                                    .popup, .pop-up, .overlay-ad, .ad-overlay,
                                    .interstitial, [class*="interstitial"],
                                    script[src*="ads"], script[src*="doubleclick"],
                                    .advertisement, [data-ad], [aria-label="advertisement"]
                                    { display: none !important; visibility: hidden !important; opacity: 0 !important; height: 0 !important; }
                                `;
                                document.head.appendChild(style);
                            })();
                        """.trimIndent()
                        
                        val popupBlocker = """
                            (function() {
                                window.open = function() { return null; };
                                window._open = window.open;
                                document.addEventListener('click', function(e) {
                                    const target = e.target;
                                    if (target.tagName === 'A' && target.target === '_blank') {
                                        const href = target.href;
                                        if (href && !href.includes('stream.terousd.online')) {
                                            e.preventDefault();
                                            e.stopPropagation();
                                        }
                                    }
                                }, true);
                            })();
                        """.trimIndent()
                        
                        val mutationBlocker = """
                            (function() {
                                const adSelectors = ['.adsbygoogle', '[id*="ad-slot"]', '[class*="ad-unit"]'];
                                const observer = new MutationObserver(function(mutations) {
                                    adSelectors.forEach(sel => {
                                        document.querySelectorAll(sel).forEach(el => el.remove());
                                    });
                                });
                                observer.observe(document.body, { childList: true, subtree: true });
                            })();
                        """.trimIndent()
                        
                        view?.evaluateJavascript(cssBlocker, null)
                        view?.evaluateJavascript(popupBlocker, null)
                        view?.evaluateJavascript(mutationBlocker, null)
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
