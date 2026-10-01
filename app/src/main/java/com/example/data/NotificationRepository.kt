package com.example.data

import android.content.Context
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NotificationRepository(private val context: Context) {

    private val _notifications = MutableStateFlow<List<SchoolNotification>>(emptyList())
    val notifications: StateFlow<List<SchoolNotification>> = _notifications.asStateFlow()

    init {
        loadInitialNotifications()
    }

    private fun loadInitialNotifications() {
        val now = System.currentTimeMillis()
        val hour = 3600 * 1000L
        val day = 24 * hour

        _notifications.value = listOf(
            SchoolNotification(
                id = "notif_1",
                title = "Presensi Masuk Berhasil",
                message = "Ananda telah tercatat hadir di SD Brawijaya Smart School pada pukul 06:48 WIB.",
                timestamp = now - (20 * 60 * 1000L),
                category = NotificationCategory.ATTENDANCE,
                isRead = false,
                targetUrl = "https://app.sdbss.sch.id"
            ),
            SchoolNotification(
                id = "notif_2",
                title = "Pengumuman Penilaian Tengah Semester (PTS)",
                message = "Jadwal dan tata tertib PTS Semester Ganjil TA 2026/2027 telah diunggah pada portal SiEbrass. Harap orang tua/wali murid memeriksa jadwal putra-putri.",
                timestamp = now - (3 * hour),
                category = NotificationCategory.ACADEMIC,
                isRead = false,
                targetUrl = "https://app.sdbss.sch.id",
                priorityHigh = true
            ),
            SchoolNotification(
                id = "notif_3",
                title = "Surat Edaran Libur Hari Guru Nasional",
                message = "Berdasarkan kalender akademik SD Brawijaya Smart School, kegiatan belajar mengajar tanggal 25 diliburkan dan diganti upacara peringatan.",
                timestamp = now - (1 * day),
                category = NotificationCategory.ANNOUNCEMENT,
                isRead = true,
                targetUrl = "https://app.sdbss.sch.id"
            ),
            SchoolNotification(
                id = "notif_4",
                title = "Rapor & Rekap Nilai Harian Telah Diperbarui",
                message = "Bapak/Ibu guru kelas telah memperbarui nilai harian Matematika dan Tematik untuk pekan ke-4.",
                timestamp = now - (2 * day),
                category = NotificationCategory.ACADEMIC,
                isRead = true,
                targetUrl = "https://app.sdbss.sch.id"
            ),
            SchoolNotification(
                id = "notif_5",
                title = "Pembaruan Aplikasi SiEbrass Versi 1.2.0 Tersedia",
                message = "Versi terbaru SiEbrass kini telah tersedia dengan fitur perbaikan performa webview dan integrasi notifikasi push.",
                timestamp = now - (3 * day),
                category = NotificationCategory.UPDATE,
                isRead = false,
                targetUrl = "https://app.sdbss.sch.id"
            ),
            SchoolNotification(
                id = "notif_6",
                title = "Informasi Pembayaran Iuran Sekolah (SPP)",
                message = "Pemberitahuan tagihan SPP bulan berjalan telah diterbitkan. Pembayaran dapat dilakukan via virtual account yang tertera di menu Keuangan SiEbrass.",
                timestamp = now - (5 * day),
                category = NotificationCategory.BILLING,
                isRead = true,
                targetUrl = "https://app.sdbss.sch.id"
            )
        )
    }

    fun markAsRead(id: String) {
        _notifications.update { list ->
            list.map { if (it.id == id) it.copy(isRead = true) else it }
        }
    }

    fun markAllAsRead() {
        _notifications.update { list ->
            list.map { it.copy(isRead = true) }
        }
    }

    fun deleteNotification(id: String) {
        _notifications.update { list ->
            list.filterNot { it.id == id }
        }
    }

    fun clearAll() {
        _notifications.value = emptyList()
    }

    fun sendTestPushNotification(category: NotificationCategory): SchoolNotification {
        val id = "test_${System.currentTimeMillis()}"
        val (title, message) = when (category) {
            NotificationCategory.ATTENDANCE -> {
                "Presensi Pulang Siswa" to "Siswa telah selesai mengikuti KBM dan meninggalkan lingkungan SD Brawijaya Smart School pada 14:15 WIB."
            }
            NotificationCategory.ACADEMIC -> {
                "Tugas Baru: Proyek Sains & Lingkungan" to "Guru kelas telah mengunggah tugas baru. Batas pengumpulan: Jumat pekan ini di portal SiEbrass."
            }
            NotificationCategory.UPDATE -> {
                "Pembaruan SiEbrass Siap Diunduh" to "Pembaruan otomatis versi 1.2.0 telah siap dipasang untuk perangkat Android Anda."
            }
            NotificationCategory.BILLING -> {
                "Kuitansi Pembayaran Terverifikasi" to "Pembayaran SPP Anda telah terverifikasi secara otomatis oleh sistem keuangan sekolah."
            }
            else -> {
                "Pengumuman Khusus Kepala Sekolah" to "Undangan pertemuan komite sekolah dan orang tua murid hari Sabtu pukul 09.00 WIB di Aula SDBSS."
            }
        }

        val newNotif = SchoolNotification(
            id = id,
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            category = category,
            isRead = false,
            targetUrl = "https://app.sdbss.sch.id",
            priorityHigh = true
        )

        // Prepend to in-app list
        _notifications.update { listOf(newNotif) + it }

        // Also fire actual system notification
        val notificationId = (System.currentTimeMillis() % 100000).toInt()
        NotificationHelper.showSystemNotification(
            context = context,
            notificationId = notificationId,
            title = title,
            message = message,
            category = category,
            targetUrl = "https://app.sdbss.sch.id"
        )

        return newNotif
    }
}
