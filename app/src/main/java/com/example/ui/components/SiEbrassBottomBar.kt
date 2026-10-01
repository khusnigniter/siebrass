package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.AppTab

data class NavigationTabItem(
    val tab: AppTab,
    val label: String,
    val icon: ImageVector,
    val tag: String
)

@Composable
fun SiEbrassBottomBar(
    currentTab: AppTab,
    unreadNotifCount: Int,
    onTabSelected: (AppTab) -> Unit
) {
    val items = listOf(
        NavigationTabItem(AppTab.PORTAL, "Portal", Icons.Default.Language, "tab_portal"),
        NavigationTabItem(AppTab.NOTIFICATIONS, "Notifikasi", Icons.Default.Notifications, "tab_notifications"),
        NavigationTabItem(AppTab.SHORTCUTS, "Menu Cepat", Icons.Default.Dashboard, "tab_shortcuts"),
        NavigationTabItem(AppTab.UPDATES, "Pembaruan", Icons.Default.SystemUpdate, "tab_updates"),
        NavigationTabItem(AppTab.SETTINGS, "Pengaturan", Icons.Default.Settings, "tab_settings")
    )

    NavigationBar(
        tonalElevation = 8.dp,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        items.forEach { item ->
            val selected = currentTab == item.tab

            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(item.tab) },
                modifier = Modifier.testTag(item.tag),
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                icon = {
                    if (item.tab == AppTab.NOTIFICATIONS && unreadNotifCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ) {
                                    Text(text = if (unreadNotifCount > 99) "99+" else unreadNotifCount.toString())
                                }
                            }
                        ) {
                            Icon(imageVector = item.icon, contentDescription = item.label)
                        }
                    } else {
                        Icon(imageVector = item.icon, contentDescription = item.label)
                    }
                },
                label = {
                    Text(text = item.label)
                }
            )
        }
    }
}
