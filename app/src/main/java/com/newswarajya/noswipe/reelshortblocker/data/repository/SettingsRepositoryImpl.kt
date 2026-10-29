package com.newswarajya.noswipe.reelshortblocker.data.repository

import com.newswarajya.noswipe.reelshortblocker.data.preferences.NoScrollPreferences
import com.newswarajya.noswipe.reelshortblocker.domain.repository.SecuritySettings
import com.newswarajya.noswipe.reelshortblocker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepositoryImpl(
    private val preferences: NoScrollPreferences
) : SettingsRepository {

    private val _settings = MutableStateFlow(preferences.getSecuritySettings())
    override val settings: StateFlow<SecuritySettings> = _settings.asStateFlow()

    override fun updatePinSecurity(enabled: Boolean, newPin: String) {
        val sanitizedPin = newPin.filter { it.isDigit() }.take(4).ifEmpty { "2580" }
        val updated = _settings.value.copy(
            isPinEnabled = enabled,
            pinCode = sanitizedPin
        )
        preferences.saveSecuritySettings(updated)
        _settings.value = updated
    }

    override fun verifyPin(candidatePin: String): Boolean {
        return _settings.value.pinCode == candidatePin.trim()
    }

    override fun setBiometricEnabled(enabled: Boolean) {
        val updated = _settings.value.copy(isBiometricUnlockEnabled = enabled)
        preferences.saveSecuritySettings(updated)
        _settings.value = updated
    }

    override fun setHapticFeedback(enabled: Boolean) {
        val updated = _settings.value.copy(isHapticFeedbackEnabled = enabled)
        preferences.saveSecuritySettings(updated)
        _settings.value = updated
    }

    override fun setPersistentNotification(enabled: Boolean) {
        val updated = _settings.value.copy(isPersistentNotificationEnabled = enabled)
        preferences.saveSecuritySettings(updated)
        _settings.value = updated
    }

    override fun setMindfulBreathDuration(seconds: Int) {
        val updated = _settings.value.copy(mindfulBreathDurationSeconds = seconds.coerceIn(6, 60))
        preferences.saveSecuritySettings(updated)
        _settings.value = updated
    }

    override fun updateSystemPermissionStates(
        accessibilityActive: Boolean,
        adminActive: Boolean,
        overlayGranted: Boolean,
        batteryOptimizationIgnored: Boolean
    ) {
        val updated = _settings.value.copy(
            isAccessibilityServiceActive = accessibilityActive,
            isAntiTamperAdminEnabled = adminActive,
            isOverlayPermissionGranted = overlayGranted,
            isBatteryOptimizationIgnored = batteryOptimizationIgnored
        )
        preferences.saveSecuritySettings(updated)
        _settings.value = updated
    }
}
