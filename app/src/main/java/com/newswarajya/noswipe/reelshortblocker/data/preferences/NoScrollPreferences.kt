package com.newswarajya.noswipe.reelshortblocker.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.newswarajya.noswipe.reelshortblocker.domain.model.BlockMode
import com.newswarajya.noswipe.reelshortblocker.domain.model.CuriousConfig
import com.newswarajya.noswipe.reelshortblocker.domain.model.CuriousStatus
import com.newswarajya.noswipe.reelshortblocker.domain.model.DailyLimitConfig
import com.newswarajya.noswipe.reelshortblocker.domain.model.DetectionPlan
import com.newswarajya.noswipe.reelshortblocker.domain.repository.SecuritySettings

class NoScrollPreferences(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    var isMasterShieldEnabled: Boolean
        get() = prefs.getBoolean(KEY_MASTER_SHIELD, true)
        set(value) = prefs.edit().putBoolean(KEY_MASTER_SHIELD, value).apply()

    var activePlan: DetectionPlan
        get() = DetectionPlan.fromName(prefs.getString(KEY_ACTIVE_PLAN, DetectionPlan.BLOCK_ALL.name))
        set(value) = prefs.edit().putString(KEY_ACTIVE_PLAN, value.name).apply()

    var pauseEndsAtMillis: Long
        get() = prefs.getLong(KEY_PAUSE_ENDS_AT, 0L)
        set(value) = prefs.edit().putLong(KEY_PAUSE_ENDS_AT, value).apply()

    fun isPlatformEnabled(platformId: String, defaultEnabled: Boolean): Boolean {
        return prefs.getBoolean("platform_enabled_$platformId", defaultEnabled)
    }

    fun setPlatformEnabled(platformId: String, enabled: Boolean) {
        prefs.edit().putBoolean("platform_enabled_$platformId", enabled).apply()
    }

    fun getPlatformBlockMode(platformId: String, defaultMode: BlockMode): BlockMode {
        val raw = prefs.getString("platform_mode_$platformId", null)
        return if (raw != null) BlockMode.fromName(raw) else defaultMode
    }

    fun setPlatformBlockMode(platformId: String, mode: BlockMode) {
        prefs.edit().putString("platform_mode_$platformId", mode.name).apply()
    }

    fun getDailyLimitConfig(): DailyLimitConfig {
        return DailyLimitConfig(
            isEnabled = prefs.getBoolean(KEY_DAILY_LIMIT_ENABLED, true),
            dailyLimitMinutes = prefs.getInt(KEY_DAILY_LIMIT_MINUTES, 25),
            usedMinutesToday = prefs.getInt(KEY_USED_MINUTES_TODAY, 8),
            enforceHardLockOnExceed = prefs.getBoolean(KEY_DAILY_LIMIT_HARD_LOCK, true)
        )
    }

    fun saveDailyLimitConfig(config: DailyLimitConfig) {
        prefs.edit()
            .putBoolean(KEY_DAILY_LIMIT_ENABLED, config.isEnabled)
            .putInt(KEY_DAILY_LIMIT_MINUTES, config.dailyLimitMinutes)
            .putInt(KEY_USED_MINUTES_TODAY, config.usedMinutesToday)
            .putBoolean(KEY_DAILY_LIMIT_HARD_LOCK, config.enforceHardLockOnExceed)
            .apply()
    }

    fun getCuriousStatus(): CuriousStatus {
        val config = CuriousConfig(
            isEnabled = prefs.getBoolean(KEY_CURIOUS_ENABLED, true),
            sessionDurationMinutes = prefs.getInt(KEY_CURIOUS_SESSION_MINS, 5),
            cooldownDurationMinutes = prefs.getInt(KEY_CURIOUS_COOLDOWN_MINS, 25),
            maxDailySessions = prefs.getInt(KEY_CURIOUS_MAX_DAILY, 3),
            requireMindfulBreathBeforeStart = prefs.getBoolean(KEY_CURIOUS_REQUIRE_BREATH, true)
        )
        return CuriousStatus(
            config = config,
            activeSessionEndsAtMillis = prefs.getLong(KEY_CURIOUS_ACTIVE_ENDS_AT, 0L),
            cooldownEndsAtMillis = prefs.getLong(KEY_CURIOUS_COOLDOWN_ENDS_AT, 0L),
            sessionsCompletedToday = prefs.getInt(KEY_CURIOUS_SESSIONS_TODAY, 1)
        )
    }

    fun saveCuriousStatus(status: CuriousStatus) {
        prefs.edit()
            .putBoolean(KEY_CURIOUS_ENABLED, status.config.isEnabled)
            .putInt(KEY_CURIOUS_SESSION_MINS, status.config.sessionDurationMinutes)
            .putInt(KEY_CURIOUS_COOLDOWN_MINS, status.config.cooldownDurationMinutes)
            .putInt(KEY_CURIOUS_MAX_DAILY, status.config.maxDailySessions)
            .putBoolean(KEY_CURIOUS_REQUIRE_BREATH, status.config.requireMindfulBreathBeforeStart)
            .putLong(KEY_CURIOUS_ACTIVE_ENDS_AT, status.activeSessionEndsAtMillis)
            .putLong(KEY_CURIOUS_COOLDOWN_ENDS_AT, status.cooldownEndsAtMillis)
            .putInt(KEY_CURIOUS_SESSIONS_TODAY, status.sessionsCompletedToday)
            .apply()
    }

    fun getSecuritySettings(): SecuritySettings {
        return SecuritySettings(
            isPinEnabled = prefs.getBoolean(KEY_PIN_ENABLED, false),
            pinCode = prefs.getString(KEY_PIN_CODE, "2580") ?: "2580",
            isBiometricUnlockEnabled = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false),
            isAntiTamperAdminEnabled = prefs.getBoolean(KEY_ADMIN_ACTIVE, false),
            isHapticFeedbackEnabled = prefs.getBoolean(KEY_HAPTIC_ENABLED, true),
            isPersistentNotificationEnabled = prefs.getBoolean(KEY_NOTIFICATION_ENABLED, true),
            isAccessibilityServiceActive = prefs.getBoolean(KEY_ACCESSIBILITY_ACTIVE, false),
            isOverlayPermissionGranted = prefs.getBoolean(KEY_OVERLAY_GRANTED, false),
            isBatteryOptimizationIgnored = prefs.getBoolean(KEY_BATTERY_OPT_IGNORED, false),
            mindfulBreathDurationSeconds = prefs.getInt(KEY_BREATH_DURATION_SEC, 12)
        )
    }

    fun saveSecuritySettings(settings: SecuritySettings) {
        prefs.edit()
            .putBoolean(KEY_PIN_ENABLED, settings.isPinEnabled)
            .putString(KEY_PIN_CODE, settings.pinCode)
            .putBoolean(KEY_BIOMETRIC_ENABLED, settings.isBiometricUnlockEnabled)
            .putBoolean(KEY_ADMIN_ACTIVE, settings.isAntiTamperAdminEnabled)
            .putBoolean(KEY_HAPTIC_ENABLED, settings.isHapticFeedbackEnabled)
            .putBoolean(KEY_NOTIFICATION_ENABLED, settings.isPersistentNotificationEnabled)
            .putBoolean(KEY_ACCESSIBILITY_ACTIVE, settings.isAccessibilityServiceActive)
            .putBoolean(KEY_OVERLAY_GRANTED, settings.isOverlayPermissionGranted)
            .putBoolean(KEY_BATTERY_OPT_IGNORED, settings.isBatteryOptimizationIgnored)
            .putInt(KEY_BREATH_DURATION_SEC, settings.mindfulBreathDurationSeconds)
            .apply()
    }

    var isInitialSeedCompleted: Boolean
        get() = prefs.getBoolean(KEY_INITIAL_SEED_DONE, false)
        set(value) = prefs.edit().putBoolean(KEY_INITIAL_SEED_DONE, value).apply()

    companion object {
        private const val PREFS_NAME = "noscroll_clean_prefs"
        private const val KEY_MASTER_SHIELD = "master_shield_enabled"
        private const val KEY_ACTIVE_PLAN = "active_detection_plan"
        private const val KEY_PAUSE_ENDS_AT = "pause_ends_at_millis"
        private const val KEY_DAILY_LIMIT_ENABLED = "daily_limit_enabled"
        private const val KEY_DAILY_LIMIT_MINUTES = "daily_limit_minutes"
        private const val KEY_USED_MINUTES_TODAY = "used_minutes_today"
        private const val KEY_DAILY_LIMIT_HARD_LOCK = "daily_limit_hard_lock"
        private const val KEY_CURIOUS_ENABLED = "curious_enabled"
        private const val KEY_CURIOUS_SESSION_MINS = "curious_session_mins"
        private const val KEY_CURIOUS_COOLDOWN_MINS = "curious_cooldown_mins"
        private const val KEY_CURIOUS_MAX_DAILY = "curious_max_daily"
        private const val KEY_CURIOUS_REQUIRE_BREATH = "curious_require_breath"
        private const val KEY_CURIOUS_ACTIVE_ENDS_AT = "curious_active_ends_at"
        private const val KEY_CURIOUS_COOLDOWN_ENDS_AT = "curious_cooldown_ends_at"
        private const val KEY_CURIOUS_SESSIONS_TODAY = "curious_sessions_today"
        private const val KEY_PIN_ENABLED = "security_pin_enabled"
        private const val KEY_PIN_CODE = "security_pin_code"
        private const val KEY_BIOMETRIC_ENABLED = "security_biometric_enabled"
        private const val KEY_ADMIN_ACTIVE = "security_admin_active"
        private const val KEY_HAPTIC_ENABLED = "security_haptic_enabled"
        private const val KEY_NOTIFICATION_ENABLED = "security_notification_enabled"
        private const val KEY_ACCESSIBILITY_ACTIVE = "system_accessibility_active"
        private const val KEY_OVERLAY_GRANTED = "system_overlay_granted"
        private const val KEY_BATTERY_OPT_IGNORED = "system_battery_opt_ignored"
        private const val KEY_BREATH_DURATION_SEC = "mindful_breath_duration_sec"
        private const val KEY_INITIAL_SEED_DONE = "initial_seed_completed"
    }
}
