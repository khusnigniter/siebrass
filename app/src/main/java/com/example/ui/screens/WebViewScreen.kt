package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.WebviewUiState
import com.example.ui.components.OfflineErrorView

private const val DESKTOP_USER_AGENT =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
    webState: WebviewUiState,
    onUrlChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onLoadingChange: (Boolean) -> Unit,
    onProgressChange: (Int) -> Unit,
    onNavigationStateChange: (canGoBack: Boolean, canGoForward: Boolean) -> Unit,
    onError: () -> Unit,
    onHomeClick: () -> Unit,
    onToggleDesktop: () -> Unit,
    onZoomChange: (Int) -> Unit,
    onOpenShortcuts: () -> Unit
) {
    val context = LocalContext.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var filePathCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    // File Chooser Launcher for uploads (assignments, profile picture, homework documents)
    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (filePathCallback != null) {
            val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
            filePathCallback?.onReceiveValue(uris)
            filePathCallback = null
        }
    }

    // Hardware back press handling for WebView history
    BackHandler(enabled = webState.canGoBack) {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        }
    }

    // Handle reload trigger from ViewModel
    LaunchedEffect(webState.refreshCounter) {
        if (webState.refreshCounter > 0) {
            webViewInstance?.reload()
        }
    }

    // Handle clear cache trigger
    LaunchedEffect(webState.clearCacheCounter) {
        if (webState.clearCacheCounter > 0) {
            webViewInstance?.clearCache(true)
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
            webViewInstance?.reload()
        }
    }

    // Handle text zoom change
    LaunchedEffect(webState.textZoom) {
        webViewInstance?.settings?.textZoom = webState.textZoom
    }

    // Handle desktop mode toggle
    LaunchedEffect(webState.isDesktopMode) {
        webViewInstance?.let { wv ->
            if (webState.isDesktopMode) {
                wv.settings.userAgentString = DESKTOP_USER_AGENT
                wv.settings.useWideViewPort = true
                wv.settings.loadWithOverviewMode = true
            } else {
                wv.settings.userAgentString = null // Use default mobile user agent
                wv.settings.useWideViewPort = true
                wv.settings.loadWithOverviewMode = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (webState.isOffline) {
            OfflineErrorView(
                url = webState.currentUrl,
                onRetry = {
                    webViewInstance?.reload()
                },
                onOpenShortcuts = onOpenShortcuts
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Progress Indicator
                AnimatedVisibility(visible = webState.isLoading) {
                    LinearProgressIndicator(
                        progress = { (webState.progress.coerceIn(0, 100)) / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = MaterialTheme.colorScheme.tertiary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                // Main AndroidView hosting the responsive WebView
                Box(modifier = Modifier.weight(1f)) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("siebrass_webview"),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )

                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    useWideViewPort = true
                                    loadWithOverviewMode = true
                                    setSupportZoom(true)
                                    builtInZoomControls = true
                                    displayZoomControls = false
                                    allowFileAccess = true
                                    allowContentAccess = true
                                    mediaPlaybackRequiresUserGesture = false
                                    mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                                    cacheMode = WebSettings.LOAD_DEFAULT
                                    textZoom = webState.textZoom
                                    if (webState.isDesktopMode) {
                                        userAgentString = DESKTOP_USER_AGENT
                                    }
                                }

                                CookieManager.getInstance().setAcceptCookie(true)
                                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): Boolean {
                                        val url = request?.url?.toString() ?: return false

                                        // Handle special protocols: WhatsApp, Tel, Mailto
                                        return when {
                                            url.startsWith("whatsapp://") ||
                                                    url.contains("api.whatsapp.com/send") ||
                                                    url.contains("wa.me") -> {
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                    ctx.startActivity(intent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(ctx, "Aplikasi WhatsApp tidak ditemukan", Toast.LENGTH_SHORT).show()
                                                }
                                                true
                                            }

                                            url.startsWith("tel:") -> {
                                                try {
                                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse(url))
                                                    ctx.startActivity(intent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(ctx, "Tidak dapat membuka panggilan telepon", Toast.LENGTH_SHORT).show()
                                                }
                                                true
                                            }

                                            url.startsWith("mailto:") -> {
                                                try {
                                                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(url))
                                                    ctx.startActivity(intent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(ctx, "Aplikasi email tidak ditemukan", Toast.LENGTH_SHORT).show()
                                                }
                                                true
                                            }

                                            // Direct download of APK or documents
                                            url.endsWith(".apk") || url.endsWith(".pdf") || url.endsWith(".xlsx") || url.endsWith(".docx") -> {
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                    ctx.startActivity(intent)
                                                } catch (e: Exception) {
                                                    // Allow webview to handle
                                                    return false
                                                }
                                                true
                                            }

                                            else -> {
                                                // Load URL within webview
                                                false
                                            }
                                        }
                                    }

                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        super.onPageStarted(view, url, favicon)
                                        onLoadingChange(true)
                                        url?.let { onUrlChange(it) }
                                        onNavigationStateChange(canGoBack(), canGoForward())
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        onLoadingChange(false)
                                        url?.let { onUrlChange(it) }
                                        view?.title?.let { onTitleChange(it) }
                                        onNavigationStateChange(canGoBack(), canGoForward())
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        super.onReceivedError(view, request, error)
                                        if (request?.isForMainFrame == true) {
                                            onError()
                                        }
                                    }
                                }

                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        super.onProgressChanged(view, newProgress)
                                        onProgressChange(newProgress)
                                    }

                                    override fun onReceivedTitle(view: WebView?, title: String?) {
                                        super.onReceivedTitle(view, title)
                                        title?.let { onTitleChange(it) }
                                    }

                                    // Support File Uploads (assignments, profile picture, homework documents)
                                    override fun onShowFileChooser(
                                        webView: WebView?,
                                        filePathCallbackResult: ValueCallback<Array<Uri>>?,
                                        fileChooserParams: FileChooserParams?
                                    ): Boolean {
                                        filePathCallback?.onReceiveValue(null)
                                        filePathCallback = filePathCallbackResult

                                        val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                                            type = "*/*"
                                            addCategory(Intent.CATEGORY_OPENABLE)
                                        }

                                        try {
                                            fileChooserLauncher.launch(intent)
                                        } catch (e: ActivityNotFoundException) {
                                            filePathCallback = null
                                            Toast.makeText(ctx, "Tidak dapat membuka pemilih berkas", Toast.LENGTH_SHORT).show()
                                            return false
                                        }
                                        return true
                                    }
                                }

                                loadUrl(webState.currentUrl)
                                webViewInstance = this
                            }
                        },
                        update = { wv ->
                            webViewInstance = wv
                        }
                    )
                }

                // Bottom WebView Mini-Controls Bar
                Surface(
                    tonalElevation = 2.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Navigation controls
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { webViewInstance?.goBack() },
                                enabled = webState.canGoBack,
                                modifier = Modifier.size(36.dp).testTag("webview_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Halaman Sebelumnya",
                                    tint = if (webState.canGoBack) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { webViewInstance?.goForward() },
                                enabled = webState.canGoForward,
                                modifier = Modifier.size(36.dp).testTag("webview_forward_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Halaman Berikutnya",
                                    tint = if (webState.canGoForward) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { webViewInstance?.reload() },
                                modifier = Modifier.size(36.dp).testTag("webview_refresh_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Muat Ulang",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = onHomeClick,
                                modifier = Modifier.size(36.dp).testTag("webview_home_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Kembali ke Beranda SiEbrass",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Viewport & Zoom Indicators
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Zoom controls
                            IconButton(
                                onClick = { onZoomChange(webState.textZoom - 10) },
                                modifier = Modifier.size(32.dp).testTag("webview_zoom_out_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomOut,
                                    contentDescription = "Perkecil Teks",
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            Text(
                                text = "${webState.textZoom}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            IconButton(
                                onClick = { onZoomChange(webState.textZoom + 10) },
                                modifier = Modifier.size(32.dp).testTag("webview_zoom_in_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = "Perbesar Teks",
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Desktop / Mobile Mode Toggle Button
                            IconButton(
                                onClick = onToggleDesktop,
                                modifier = Modifier.size(34.dp).testTag("webview_mode_toggle_bottom")
                            ) {
                                Icon(
                                    imageVector = if (webState.isDesktopMode) Icons.Default.Computer else Icons.Default.PhoneAndroid,
                                    contentDescription = "Mode Tampilan",
                                    tint = if (webState.isDesktopMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Cleanup webView if needed
        }
    }
}
