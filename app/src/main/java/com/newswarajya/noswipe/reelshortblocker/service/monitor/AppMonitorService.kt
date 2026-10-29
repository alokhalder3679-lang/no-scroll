package com.newswarajya.noswipe.reelshortblocker.service.monitor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.R
import com.newswarajya.noswipe.reelshortblocker.data.ServiceLocator
import com.newswarajya.noswipe.reelshortblocker.ui.activities.home.HomeActivity

/**
 * Persistent monitor service keeping status notifications synchronized with the active
 * DetectionPlan and blocked reel counters.
 */
class AppMonitorService : Service() {

    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(applicationContext)
        ensureNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildStatusNotification()
        try {
            startForeground(NOTIFICATION_ID, notification)
        } catch (_: Exception) {
            // In restricted background contexts, update via NotificationManager
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.notify(NOTIFICATION_ID, notification)
        }
        return START_STICKY
    }

    private fun buildStatusNotification(): Notification {
        val repo = ServiceLocator.noScrollRepository
        val analytics = ServiceLocator.analyticsRepository.analyticsSummary.value
        val shieldActive = repo.isMasterShieldEnabled.value
        val plan = repo.activePlan.value

        val openAppIntent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (shieldActive) {
            "NoScroll Active • ${plan.badge}"
        } else {
            "NoScroll Shield Disarmed"
        }
        val body = "${analytics.totalBlockedToday} reels intercepted today • ${analytics.estimatedMinutesSavedToday}m saved"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_shield)
            .setContentTitle(title)
            .setContentText(body)
            .setOngoing(shieldActive)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "noscroll_active_shield_channel"
        const val NOTIFICATION_ID = 2102
    }
}
