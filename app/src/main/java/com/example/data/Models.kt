package com.example.data

enum class NotificationCategory(val title: String) {
    ALL("Semua"),
    ANNOUNCEMENT("Pengumuman"),
    ATTENDANCE("Presensi"),
    ACADEMIC("Akademik & Rapor"),
    UPDATE("Pembaruan App"),
    BILLING("Keuangan / SPP")
}

data class SchoolNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val category: NotificationCategory,
    val isRead: Boolean = false,
    val targetUrl: String = "https://app.sdbss.sch.id",
    val priorityHigh: Boolean = false
)

data class AppUpdateInfo(
    val currentVersion: String = "1.0.0",
    val latestVersion: String = "1.2.0",
    val versionCode: Int = 120,
    val releaseDate: String = "Oktober 2026",
    val isUpdateAvailable: Boolean = true,
    val isMandatory: Boolean = false,
    val fileSizeMb: Double = 18.5,
    val changelog: List<String> = listOf(
        "Pembaruan responsive WebView SiEbrass untuk seluruh perangkat",
        "Integrasi notifikasi push pengumuman & presensi siswa",
        "Dukungan unggah berkas tugas & rapor foto langsung dari kamera/dokumen",
        "Peningkatan performa pemuatan laman SD Brawijaya Smart School",
        "Dukungan multi-platform pembaruan otomatis (Android & iOS)"
    ),
    val downloadUrlAndroid: String = "https://app.sdbss.sch.id/download/siebrass-latest.apk",
    val playStoreUrl: String = "https://play.google.com/store/apps/details?id=com.sdbss.siebrass",
    val appStoreIosUrl: String = "https://apps.apple.com/id/app/siebrass-sdbss/id123456789",
    val testFlightIosUrl: String = "https://testflight.apple.com/join/siebrass"
)

data class SchoolQuickLink(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconName: String,
    val targetUrl: String,
    val badge: String? = null
)

data class WebviewConfig(
    val url: String = "https://app.sdbss.sch.id",
    val isDesktopMode: Boolean = false,
    val textZoom: Int = 100, // percentage 80% - 150%
    val javaScriptEnabled: Boolean = true,
    val cacheClearRequested: Boolean = false
)
