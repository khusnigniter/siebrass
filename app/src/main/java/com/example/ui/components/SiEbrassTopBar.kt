package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppTab
import com.example.ui.WebviewUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiEbrassTopBar(
    currentTab: AppTab,
    webState: WebviewUiState,
    unreadCount: Int,
    onReload: () -> Unit,
    onToggleDesktop: () -> Unit,
    onMarkAllRead: () -> Unit,
    onTestNotification: () -> Unit,
    onCheckUpdate: () -> Unit,
    onOpenExternal: () -> Unit,
    onShareUrl: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "SiEbrass Logo",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SiEbrass",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        if (currentTab == AppTab.PORTAL) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "SSL Aman",
                                tint = Color(0xFFA5D6A7),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    Text(
                        text = when (currentTab) {
                            AppTab.PORTAL -> "app.sdbss.sch.id"
                            AppTab.NOTIFICATIONS -> "Notifikasi & Pengumuman Sekolah"
                            AppTab.SHORTCUTS -> "Menu Cepat SiEbrass"
                            AppTab.UPDATES -> "Pembaruan Aplikasi"
                            AppTab.SETTINGS -> "Pengaturan Aplikasi"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        actions = {
            when (currentTab) {
                AppTab.PORTAL -> {
                    IconButton(
                        onClick = onReload,
                        modifier = Modifier.testTag("reload_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Muat Ulang Halaman"
                        )
                    }

                    IconButton(
                        onClick = onToggleDesktop,
                        modifier = Modifier.testTag("desktop_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (webState.isDesktopMode) Icons.Default.PhoneAndroid else Icons.Default.Computer,
                            contentDescription = if (webState.isDesktopMode) "Ganti ke Mode Ponsel" else "Ganti ke Mode Desktop"
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("portal_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu Lainnya"
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Buka di Peramban Luar") },
                                onClick = {
                                    showMenu = false
                                    onOpenExternal()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Bagikan Tautan Laman") },
                                onClick = {
                                    showMenu = false
                                    onShareUrl()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Periksa Pembaruan") },
                                onClick = {
                                    showMenu = false
                                    onCheckUpdate()
                                }
                            )
                        }
                    }
                }

                AppTab.NOTIFICATIONS -> {
                    IconButton(
                        onClick = onMarkAllRead,
                        modifier = Modifier.testTag("mark_all_read_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Tandai Semua Telah Dibaca"
                        )
                    }

                    IconButton(
                        onClick = onTestNotification,
                        modifier = Modifier.testTag("test_push_notif_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Kirim Notifikasi Uji Coba"
                        )
                    }
                }

                AppTab.UPDATES -> {
                    IconButton(
                        onClick = onCheckUpdate,
                        modifier = Modifier.testTag("topbar_check_update_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Periksa Pembaruan Sekarang"
                        )
                    }
                }

                else -> {}
            }
        }
    )
}
