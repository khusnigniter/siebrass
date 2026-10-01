package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.NotificationCategory

object NotificationHelper {

    const val CHANNEL_ANNOUNCEMENTS_ID = "siebrass_announcements"
    const val CHANNEL_ATTENDANCE_ID = "siebrass_attendance"
    const val CHANNEL_UPDATES_ID = "siebrass_updates"

    const val EXTRA_TARGET_URL = "com.example.siebrass.TARGET_URL"
    const val EXTRA_NOTIFICATION_ID = "com.example.siebrass.NOTIFICATION_ID"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return

            // Channel 1: Pengumuman Sekolah (High Priority)
            val announcementChannel = NotificationChannel(
                CHANNEL_ANNOUNCEMENTS_ID,
                context.getString(R.string.channel_announcements_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_announcements_desc)
                enableVibration(true)
                enableLights(true)
            }

            // Channel 2: Presensi & Siswa
            val attendanceChannel = NotificationChannel(
                CHANNEL_ATTENDANCE_ID,
                context.getString(R.string.channel_attendance_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_attendance_desc)
                enableVibration(true)
            }

            // Channel 3: Pembaruan Aplikasi
            val updateChannel = NotificationChannel(
                CHANNEL_UPDATES_ID,
                context.getString(R.string.channel_updates_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_updates_desc)
            }

            notificationManager.createNotificationChannels(
                listOf(announcementChannel, attendanceChannel, updateChannel)
            )
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun showSystemNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        category: NotificationCategory = NotificationCategory.ANNOUNCEMENT,
        targetUrl: String = "https://app.sdbss.sch.id"
    ): Boolean {
        if (!hasNotificationPermission(context)) {
            return false
        }

        val channelId = when (category) {
            NotificationCategory.ANNOUNCEMENT, NotificationCategory.ACADEMIC -> CHANNEL_ANNOUNCEMENTS_ID
            NotificationCategory.ATTENDANCE -> CHANNEL_ATTENDANCE_ID
            NotificationCategory.UPDATE -> CHANNEL_UPDATES_ID
            else -> CHANNEL_ANNOUNCEMENTS_ID
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TARGET_URL, targetUrl)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 250, 150, 250))
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(notificationId, builder.build())
            return true
        } catch (e: SecurityException) {
            return false
        } catch (e: Exception) {
            return false
        }
    }
}
