Aplikasi Android SiEbrass 
Sistem Informasi dan Manajemen Sekolah 
========================================
SIMS SD Brawijaya Smart School

Aplikasi Android SiEbrass (Sistem Informasi dan Manajemen Sekolah SD Brawijaya Smart School) telah berhasil dibangun dengan fitur-fitur lengkap:

Fitur Utama yang Telah Diimplementasikan:
1.Responsive WebView Engine:
  * Memuat URL utama https://app.sdbss.sch.id secara responsif untuk semua resolusi layar ponsel dan tablet.
  * Dilengkapi WebChromeClient dengan dukungan pemilih berkas (File Chooser) untuk mengunggah tugas siswa, foto kegiatan, dan dokumen rapor.
  * Penanganan otomatis tautan eksternal: WhatsApp sekolah (wa.me), panggilan telepon (tel:), email (mailto:), dan unduhan dokumen PDF/APK.
  * Kontrol peramban: tombol kembali (Back), maju (Forward), muat ulang (Reload), beranda (Home), sakelar Mode Desktop, serta kontrol pembesaran ukuran teks (Text Zoom 70%–150%).
  * Layar penanganan galat (Offline / Server Error Fallback) lengkap dengan tombol muat ulang dan akses menu cepat.
2.Integrasi Notifikasi Push (Push Notification System):
  * Dukungan izin notifikasi modern Android 13+ (POST_NOTIFICATIONS) dengan spanduk aktivasi izin.
  * Pendaftaran 3 Saluran Notifikasi (Notification Channels):
    * Pengumuman Sekolah (High Priority, suara & getar).
    * Presensi & Kegiatan Siswa (kehadiran, jam masuk/pulang, dan tugas harian).
    * Pembaruan Aplikasi (notifikasi ketersediaan versi baru).
  * Pusat Kotak Masuk Notifikasi (Notification Inbox) dengan filter kategori, badge jumlah pesan belum dibaca, dan tombol "Uji Push Notif" untuk menguji pengiriman notifikasi ke bilah sistem perangkat secara langsung.
3.Sistem Pembaruan Otomatis (Auto Update System - Android & iOS):
  * Pengecekan versi aplikasi terpasang (v1.0.0) terhadap rilis server terbaru (v1.2.0) saat aplikasi dibuka atau melalui tombol Periksa Pembaruan.
  * Dialog dan layar Pembaruan interaktif lengkap dengan Changelog / catatan rilis.
  * Dukungan distribusi pembaruan lintas platform:
    * Android: Pengunduh APK langsung dengan indikator progres unduhan serta integrasi ke Google Play Store.
    * iOS: Tautan dan panduan pembaruan via Apple TestFlight dan Apple App Store.
4. Menu Cepat & Pengaturan Peramban:
  * Pintasan langsung ke modul Presensi Digital, Rekap Nilai & Rapor, Jadwal Pelajaran, Keuangan/SPP, serta kontak WhatsApp CS SDBSS Malang.
  * Pembersihan Cache & Cookies peramban jika portal sekolah mengalami pembaruan tampilan.
  * Identitas resmi SD Brawijaya Smart School (SDBSS) dengan palet warna biru akademik dan emas cerdas, serta Adaptive App Icon custom.
