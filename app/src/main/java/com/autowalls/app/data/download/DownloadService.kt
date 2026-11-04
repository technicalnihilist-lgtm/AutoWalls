package com.autowalls.app.data.download

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.autowalls.app.AutoWallsApplication
import com.autowalls.app.MainActivity
import com.autowalls.app.R
import com.autowalls.app.domain.model.Wallpaper
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground service so a full-resolution download survives the user leaving the
 * detail screen or backgrounding the app.
 */
class DownloadService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val json = intent?.getStringExtra(EXTRA_WALLPAPER)
        if (json.isNullOrBlank()) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        val wallpaper = runCatching { Gson().fromJson(json, Wallpaper::class.java) }.getOrNull()
        if (wallpaper == null) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildNotification(wallpaper, 0, indeterminate = true))

        scope.launch {
            val repository = (application as AutoWallsApplication).container.repository
            DownloadTracker.update(wallpaper.id, DownloadState.InProgress(0))
            var lastNotified = -5

            val result = repository.download(wallpaper) { percent ->
                DownloadTracker.update(wallpaper.id, DownloadState.InProgress(percent))
                if (percent - lastNotified >= 5 || percent == 100) {
                    lastNotified = percent
                    notify(buildNotification(wallpaper, percent, indeterminate = false))
                }
            }

            result
                .onSuccess { file ->
                    DownloadTracker.update(wallpaper.id, DownloadState.Completed(file.absolutePath))
                    notify(buildCompletedNotification(wallpaper))
                }
                .onFailure { error ->
                    DownloadTracker.update(
                        wallpaper.id,
                        DownloadState.Failed(error.message ?: "Download failed")
                    )
                    notify(buildFailedNotification(wallpaper))
                }

            ServiceCompat.stopForeground(this@DownloadService, ServiceCompat.STOP_FOREGROUND_REMOVE)
            stopSelf(startId)
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun notify(notification: Notification) {
        runCatching {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
        }
    }

    private fun contentIntent(): PendingIntent = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun buildNotification(
        wallpaper: Wallpaper,
        percent: Int,
        indeterminate: Boolean
    ): Notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_stat_download)
        .setContentTitle("Downloading wallpaper")
        .setContentText(wallpaper.displayName)
        .setContentIntent(contentIntent())
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setProgress(100, percent, indeterminate)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    private fun buildCompletedNotification(wallpaper: Wallpaper): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_download)
            .setContentTitle("Wallpaper saved")
            .setContentText(wallpaper.displayName)
            .setContentIntent(contentIntent())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    private fun buildFailedNotification(wallpaper: Wallpaper): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_download)
            .setContentTitle("Download failed")
            .setContentText(wallpaper.displayName)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    companion object {
        private const val CHANNEL_ID = "autowalls_downloads"
        private const val NOTIFICATION_ID = 4201
        private const val EXTRA_WALLPAPER = "extra_wallpaper"

        fun start(context: Context, wallpaper: Wallpaper) {
            val intent = Intent(context, DownloadService::class.java).apply {
                putExtra(EXTRA_WALLPAPER, Gson().toJson(wallpaper))
            }
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
