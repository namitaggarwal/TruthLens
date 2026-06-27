package com.truthlens.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.truthlens.app.MainActivity
import com.truthlens.app.R
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MonitoringForegroundService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Timber.d("MonitoringForegroundService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startMonitoring()
            ACTION_STOP  -> stopMonitoring()
        }
        return START_STICKY
    }

    private fun startMonitoring() {
        Timber.d("Starting foreground monitoring service")
        startForeground(NOTIFICATION_ID, buildNotification())

        // Start the overlay service so it's ready
        val overlayIntent = Intent(this, OverlayService::class.java)
        startService(overlayIntent)
    }

    private fun stopMonitoring() {
        Timber.d("Stopping foreground monitoring service")
        stopService(Intent(this, OverlayService::class.java))
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, MonitoringForegroundService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TruthLens is active")
            .setContentText("Monitoring for potentially misleading content")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_delete, "Stop monitoring", stopIntent)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "TruthLens Monitoring",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shown while TruthLens is actively monitoring for misinformation"
            setShowBadge(false)
        }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("MonitoringForegroundService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START    = "com.truthlens.START_MONITORING"
        const val ACTION_STOP     = "com.truthlens.STOP_MONITORING"
        const val CHANNEL_ID      = "truthlens_monitoring"
        const val NOTIFICATION_ID = 1001

        fun startIntent(context: Context) =
            Intent(context, MonitoringForegroundService::class.java).apply { action = ACTION_START }

        fun stopIntent(context: Context) =
            Intent(context, MonitoringForegroundService::class.java).apply { action = ACTION_STOP }
    }
}
