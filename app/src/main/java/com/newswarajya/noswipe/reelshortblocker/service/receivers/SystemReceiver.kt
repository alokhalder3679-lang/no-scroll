package com.newswarajya.noswipe.reelshortblocker.service.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.newswarajya.noswipe.reelshortblocker.data.ServiceLocator
import java.util.UUID

/**
 * Handles boot completion and device unlock/lock lifecycle events to audit
 * device_unlock_sessions in SQLite.
 */
class SystemReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        ServiceLocator.init(context)

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                val sessionId = "boot_${UUID.randomUUID().toString().take(8)}"
                ServiceLocator.analyticsRepository.startUnlockSession(sessionId, System.currentTimeMillis())
                ServiceLocator.refreshSystemPermissions(context)
            }

            Intent.ACTION_USER_PRESENT -> {
                val sessionId = "unlock_${UUID.randomUUID().toString().take(8)}"
                ServiceLocator.analyticsRepository.startUnlockSession(sessionId, System.currentTimeMillis())
            }

            Intent.ACTION_SCREEN_OFF -> {
                val currentSession = ServiceLocator.analyticsRepository.currentSessionId.value
                if (!currentSession.isNullOrBlank()) {
                    ServiceLocator.analyticsRepository.closeUnlockSession(
                        sessionId = currentSession,
                        lockedOnMillis = System.currentTimeMillis()
                    )
                }
            }
        }
    }
}
