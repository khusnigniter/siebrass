package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppUpdateInfo
import com.example.data.NotificationCategory
import com.example.data.NotificationRepository
import com.example.data.SchoolNotification
import com.example.data.SchoolQuickLink
import com.example.data.UpdateCheckState
import com.example.data.UpdateRepository
import com.example.util.NetworkUtils
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    PORTAL("Portal SiEbrass"),
    NOTIFICATIONS("Notifikasi"),
    SHORTCUTS("Menu Cepat"),
    UPDATES("Pembaruan"),
    SETTINGS("Pengaturan")
}

enum class ViewportMode(val title: String, val targetWidth: Int?) {
    AUTO("Responsif Otomatis", null),
    MOBILE("Ponsel (390px)", 390),
    TABLET("Tablet (800px)", 800),
    DESKTOP("Desktop (1280px)", 1280)
}

data class WebviewUiState(
    val currentUrl: String = "https://app.sdbss.sch.id",
    val title: String = "SiEbrass - SD Brawijaya Smart School",
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isOffline: Boolean = false,
    val isDesktopMode: Boolean = false,
    val viewportMode: ViewportMode = ViewportMode.AUTO,
    val isFullscreen: Boolean = false,
    val textZoom: Int = 100,
    val refreshCounter: Int = 0,
    val clearCacheCounter: Int = 0
)

class SiEbrassViewModel(application: Application) : AndroidViewModel(application) {

    private val notifRepo = NotificationRepository(application)
    private val updateRepo = UpdateRepository()

    private val _currentTab = MutableStateFlow(AppTab.PORTAL)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _webState = MutableStateFlow(WebviewUiState())
    val webState: StateFlow<WebviewUiState> = _webState.asStateFlow()

    val notifications: StateFlow<List<SchoolNotification>> = notifRepo.notifications

    private val _selectedCategory = MutableStateFlow(NotificationCategory.ALL)
    val selectedCategory: StateFlow<NotificationCategory> = _selectedCategory.asStateFlow()

    val filteredNotifications: StateFlow<List<SchoolNotification>> =
        combine(notifications, selectedCategory) { notifs, category ->
            if (category == NotificationCategory.ALL) notifs
            else notifs.filter { it.category == category }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotifCount: StateFlow<Int> =
        notifications.combine(MutableStateFlow(Unit)) { notifs, _ ->
            notifs.count { !it.isRead }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val updateCheckState: StateFlow<UpdateCheckState> = updateRepo.checkState
    val updateInfo: StateFlow<AppUpdateInfo> = updateRepo.updateInfo
    val autoCheckEnabled: StateFlow<Boolean> = updateRepo.autoCheckEnabled

    private val _showUpdateModal = MutableStateFlow(false)
    val showUpdateModal: StateFlow<Boolean> = _showUpdateModal.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    val quickLinks = listOf(
        SchoolQuickLink(
            id = "q1",
            title = "Presensi Digital Siswa",
            subtitle = "Pantau kehadiran harian & jam masuk-pulang",
            iconName = "CheckCircle",
            targetUrl = "https://app.sdbss.sch.id",
            badge = "Harian"
        ),
        SchoolQuickLink(
            id = "q2",
            title = "Rapor & Nilai Hasil Belajar",
            subtitle = "Transkrip nilai PTS, PAS, dan catatan wali kelas",
            iconName = "School",
            targetUrl = "https://app.sdbss.sch.id",
            badge = "Baru"
        ),
        SchoolQuickLink(
            id = "q3",
            title = "Jadwal Pelajaran & KBM",
            subtitle = "Susunan mata pelajaran tematik & ekstrakurikuler",
            iconName = "CalendarToday",
            targetUrl = "https://app.sdbss.sch.id"
        ),
        SchoolQuickLink(
            id = "q4",
            title = "Informasi Keuangan / SPP",
            subtitle = "Cek tagihan iuran sekolah & status verifikasi",
            iconName = "AccountBalanceWallet",
            targetUrl = "https://app.sdbss.sch.id"
        ),
        SchoolQuickLink(
            id = "q5",
            title = "Pengumuman Resmi Sekolah",
            subtitle = "Surat edaran, agenda sekolah & kegiatan siswa",
            iconName = "Campaign",
            targetUrl = "https://app.sdbss.sch.id"
        ),
        SchoolQuickLink(
            id = "q6",
            title = "Kontak Guru & Tata Usaha SDBSS",
            subtitle = "Hubungi pihak sekolah via WhatsApp / Telepon",
            iconName = "SupportAgent",
            targetUrl = "https://app.sdbss.sch.id"
        )
    )

    init {
        // Initialize notification channels
        NotificationHelper.createNotificationChannels(application)
        checkNetworkStatus()
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun checkNetworkStatus() {
        val hasNet = NetworkUtils.isNetworkAvailable(getApplication())
        _webState.value = _webState.value.copy(isOffline = !hasNet)
    }

    // WebView actions
    fun onUrlChanged(url: String) {
        _webState.value = _webState.value.copy(currentUrl = url)
    }

    fun onTitleChanged(title: String) {
        _webState.value = _webState.value.copy(title = title)
    }

    fun onLoadingChanged(isLoading: Boolean) {
        _webState.value = _webState.value.copy(isLoading = isLoading)
    }

    fun onProgressChanged(progress: Int) {
        _webState.value = _webState.value.copy(progress = progress)
    }

    fun onNavigationStateChanged(canGoBack: Boolean, canGoForward: Boolean) {
        _webState.value = _webState.value.copy(
            canGoBack = canGoBack,
            canGoForward = canGoForward
        )
    }

    fun reloadWebview() {
        checkNetworkStatus()
        _webState.value = _webState.value.copy(
            refreshCounter = _webState.value.refreshCounter + 1,
            isOffline = !NetworkUtils.isNetworkAvailable(getApplication())
        )
    }

    fun navigateToUrl(url: String) {
        checkNetworkStatus()
        _webState.value = _webState.value.copy(
            currentUrl = url,
            refreshCounter = _webState.value.refreshCounter + 1
        )
        _currentTab.value = AppTab.PORTAL
    }

    fun resetToHome() {
        navigateToUrl("https://app.sdbss.sch.id")
    }

    fun toggleDesktopMode() {
        val newMode = !_webState.value.isDesktopMode
        val newViewport = if (newMode) ViewportMode.DESKTOP else ViewportMode.MOBILE
        _webState.value = _webState.value.copy(
            isDesktopMode = newMode,
            viewportMode = newViewport
        )
        reloadWebview()
        viewModelScope.launch {
            _toastEvent.emit(if (newMode) "Mode Desktop Aktif (Viewport 1280px)" else "Mode Ponsel Aktif (Viewport Responsif)")
        }
    }

    fun setViewportMode(mode: ViewportMode) {
        val isDesktop = mode == ViewportMode.DESKTOP
        _webState.value = _webState.value.copy(
            viewportMode = mode,
            isDesktopMode = isDesktop
        )
        reloadWebview()
        viewModelScope.launch {
            _toastEvent.emit("Viewport diubah ke: ${mode.title}")
        }
    }

    fun toggleFullscreen() {
        val newFs = !_webState.value.isFullscreen
        _webState.value = _webState.value.copy(isFullscreen = newFs)
        viewModelScope.launch {
            _toastEvent.emit(if (newFs) "Layar Penuh Aktif" else "Layar Penuh Dinonaktifkan")
        }
    }

    fun setTextZoom(zoomPercent: Int) {
        _webState.value = _webState.value.copy(textZoom = zoomPercent.coerceIn(70, 160))
    }

    fun clearCache() {
        _webState.value = _webState.value.copy(
            clearCacheCounter = _webState.value.clearCacheCounter + 1
        )
        viewModelScope.launch {
            _toastEvent.emit("Cache & data peramban berhasil dibersihkan")
        }
    }

    fun onWebError() {
        _webState.value = _webState.value.copy(isOffline = true)
    }

    // Notification actions
    fun setNotificationCategory(category: NotificationCategory) {
        _selectedCategory.value = category
    }

    fun markNotifAsRead(id: String) {
        notifRepo.markAsRead(id)
    }

    fun markAllNotifAsRead() {
        notifRepo.markAllAsRead()
        viewModelScope.launch {
            _toastEvent.emit("Semua notifikasi ditandai telah dibaca")
        }
    }

    fun deleteNotif(id: String) {
        notifRepo.deleteNotification(id)
    }

    fun clearAllNotif() {
        notifRepo.clearAll()
        viewModelScope.launch {
            _toastEvent.emit("Daftar notifikasi telah dibersihkan")
        }
    }

    fun sendTestPushNotification(category: NotificationCategory = NotificationCategory.ANNOUNCEMENT) {
        val notif = notifRepo.sendTestPushNotification(category)
        viewModelScope.launch {
            _toastEvent.emit("Notifikasi push dikirim: ${notif.title}")
        }
    }

    // Update actions
    fun checkForUpdates(userInitiated: Boolean = true) {
        viewModelScope.launch {
            updateRepo.checkForUpdates(forceSimulateAvailable = true)
            if (userInitiated) {
                _showUpdateModal.value = true
            }
        }
    }

    fun dismissUpdateModal() {
        _showUpdateModal.value = false
    }

    fun showUpdateDialog() {
        _showUpdateModal.value = true
    }

    fun startDownloadUpdate() {
        viewModelScope.launch {
            updateRepo.simulateDownloadUpdate {
                viewModelScope.launch {
                    _toastEvent.emit("Pembaruan SiEbrass v1.2.0 berhasil diunduh. Siap dipasang.")
                }
            }
        }
    }

    fun toggleAutoCheckUpdate(enabled: Boolean) {
        updateRepo.setAutoCheckEnabled(enabled)
    }
}
