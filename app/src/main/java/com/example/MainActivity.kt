package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.NotificationCategory
import com.example.ui.AppTab
import com.example.ui.SiEbrassViewModel
import com.example.ui.components.SiEbrassBottomBar
import com.example.ui.components.SiEbrassTopBar
import com.example.ui.components.UpdateDialog
import com.example.ui.screens.NotificationScreen
import com.example.ui.screens.QuickMenuScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.UpdateScreen
import com.example.ui.screens.WebViewScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val viewModel: SiEbrassViewModel = viewModel()
                val context = LocalContext.current
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
                val webState by viewModel.webState.collectAsStateWithLifecycle()
                val filteredNotifications by viewModel.filteredNotifications.collectAsStateWithLifecycle()
                val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
                val unreadCount by viewModel.unreadNotifCount.collectAsStateWithLifecycle()
                val updateInfo by viewModel.updateInfo.collectAsStateWithLifecycle()
                val updateState by viewModel.updateCheckState.collectAsStateWithLifecycle()
                val autoCheckEnabled by viewModel.autoCheckEnabled.collectAsStateWithLifecycle()
                val showUpdateModal by viewModel.showUpdateModal.collectAsStateWithLifecycle()

                // Check for incoming intent from Notification or deep link
                LaunchedEffect(intent) {
                    val targetUrl = intent?.getStringExtra(NotificationHelper.EXTRA_TARGET_URL)
                        ?: intent?.data?.toString()

                    if (!targetUrl.isNullOrBlank() && targetUrl.startsWith("http")) {
                        viewModel.navigateToUrl(targetUrl)
                    }
                }

                // Handle Toast / Snackbar messages from ViewModel
                LaunchedEffect(Unit) {
                    viewModel.toastEvent.collectLatest { message ->
                        scope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                }

                // Auto-check updates on app launch if enabled
                LaunchedEffect(Unit) {
                    if (autoCheckEnabled) {
                        viewModel.checkForUpdates(userInitiated = false)
                    }
                }

                // Back navigation: if on secondary tab, return to Portal
                BackHandler(enabled = currentTab != AppTab.PORTAL) {
                    viewModel.selectTab(AppTab.PORTAL)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        if (!webState.isFullscreen || currentTab != AppTab.PORTAL) {
                            SiEbrassTopBar(
                                currentTab = currentTab,
                                webState = webState,
                                unreadCount = unreadCount,
                                onReload = { viewModel.reloadWebview() },
                                onToggleDesktop = { viewModel.toggleDesktopMode() },
                                onMarkAllRead = { viewModel.markAllNotifAsRead() },
                                onTestNotification = { viewModel.sendTestPushNotification(NotificationCategory.ANNOUNCEMENT) },
                                onCheckUpdate = { viewModel.checkForUpdates(userInitiated = true) },
                                onOpenExternal = {
                                    try {
                                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webState.currentUrl))
                                        context.startActivity(browserIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Tidak dapat membuka peramban luar", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onShareUrl = {
                                    try {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, webState.currentUrl)
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Bagikan Tautan SiEbrass"))
                                    } catch (e: Exception) {}
                                }
                            )
                        }
                    },
                    bottomBar = {
                        if (!webState.isFullscreen || currentTab != AppTab.PORTAL) {
                            SiEbrassBottomBar(
                                currentTab = currentTab,
                                unreadNotifCount = unreadCount,
                                onTabSelected = { tab -> viewModel.selectTab(tab) }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                            when (tab) {
                                AppTab.PORTAL -> {
                                    WebViewScreen(
                                        webState = webState,
                                        onUrlChange = { viewModel.onUrlChanged(it) },
                                        onTitleChange = { viewModel.onTitleChanged(it) },
                                        onLoadingChange = { viewModel.onLoadingChanged(it) },
                                        onProgressChange = { viewModel.onProgressChanged(it) },
                                        onNavigationStateChange = { canBack, canForward ->
                                            viewModel.onNavigationStateChanged(canBack, canForward)
                                        },
                                        onError = { viewModel.onWebError() },
                                        onHomeClick = { viewModel.resetToHome() },
                                        onToggleDesktop = { viewModel.toggleDesktopMode() },
                                        onSetViewportMode = { viewModel.setViewportMode(it) },
                                        onToggleFullscreen = { viewModel.toggleFullscreen() },
                                        onZoomChange = { viewModel.setTextZoom(it) },
                                        onOpenShortcuts = { viewModel.selectTab(AppTab.SHORTCUTS) }
                                    )
                                }

                                AppTab.NOTIFICATIONS -> {
                                    NotificationScreen(
                                        notifications = filteredNotifications,
                                        selectedCategory = selectedCategory,
                                        onCategorySelect = { viewModel.setNotificationCategory(it) },
                                        onNotificationClick = { notif ->
                                            viewModel.markNotifAsRead(notif.id)
                                            viewModel.navigateToUrl(notif.targetUrl)
                                        },
                                        onDeleteNotification = { viewModel.deleteNotif(it) },
                                        onSendTestNotification = { cat ->
                                            viewModel.sendTestPushNotification(cat)
                                        }
                                    )
                                }

                                AppTab.SHORTCUTS -> {
                                    QuickMenuScreen(
                                        quickLinks = viewModel.quickLinks,
                                        onLinkClick = { url ->
                                            viewModel.navigateToUrl(url)
                                        }
                                    )
                                }

                                AppTab.UPDATES -> {
                                    UpdateScreen(
                                        updateInfo = updateInfo,
                                        checkState = updateState,
                                        autoCheckEnabled = autoCheckEnabled,
                                        onCheckUpdate = { viewModel.checkForUpdates(userInitiated = true) },
                                        onStartDownload = { viewModel.startDownloadUpdate() },
                                        onToggleAutoCheck = { viewModel.toggleAutoCheckUpdate(it) }
                                    )
                                }

                                AppTab.SETTINGS -> {
                                    SettingsScreen(
                                        webState = webState,
                                        onToggleDesktop = { viewModel.toggleDesktopMode() },
                                        onZoomChange = { viewModel.setTextZoom(it) },
                                        onClearCache = { viewModel.clearCache() },
                                        onResetHome = {
                                            viewModel.resetToHome()
                                            viewModel.selectTab(AppTab.PORTAL)
                                        },
                                        onOpenUpdateCenter = { viewModel.selectTab(AppTab.UPDATES) }
                                    )
                                }
                            }
                        }

                        // Update Dialog when updates found
                        if (showUpdateModal) {
                            UpdateDialog(
                                updateInfo = updateInfo,
                                onDismiss = { viewModel.dismissUpdateModal() },
                                onStartDownload = { viewModel.startDownloadUpdate() },
                                onOpenUpdateCenter = {
                                    viewModel.dismissUpdateModal()
                                    viewModel.selectTab(AppTab.UPDATES)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
