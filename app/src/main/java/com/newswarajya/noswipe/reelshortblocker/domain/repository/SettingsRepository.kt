package com.newswarajya.noswipe.reelshortblocker.domain.repository

import kotlinx.coroutines.flow.StateFlow

data class SecuritySettings(
    val isPinEnabled: Boolean = false,
    val pinCode: String = "2580",
    val isBiometricUnlockEnabled: Boolean = false,
    val isAntiTamperAdminEnabled: Boolean = false,
    val isHapticFeedbackEnabled: Boolean = true,
    val isPersistentNotificationEnabled: Boolean = true,
    val isAccessibilityServiceActive: Boolean = false,
    val isOverlayPermissionGranted: Boolean = false,
    val isBatteryOptimizationIgnored: Boolean = false,
    val mindfulBreathDurationSeconds: Int = 12
)

interface SettingsRepository {
    val settings: StateFlow<SecuritySettings>

    fun updatePinSecurity(enabled: Boolean, newPin: String)
    fun verifyPin(candidatePin: String): Boolean
    fun setBiometricEnabled(enabled: Boolean)
    fun setHapticFeedback(enabled: Boolean)
    fun setPersistentNotification(enabled: Boolean)
    fun setMindfulBreathDuration(seconds: Int)
    fun updateSystemPermissionStates(
        accessibilityActive: Boolean,
        adminActive: Boolean,
        overlayGranted: Boolean,
        batteryOptimizationIgnored: Boolean
    )
}
