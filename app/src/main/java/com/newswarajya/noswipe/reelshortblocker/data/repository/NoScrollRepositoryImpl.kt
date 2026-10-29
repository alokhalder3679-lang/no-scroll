package com.newswarajya.noswipe.reelshortblocker.data.repository

import com.newswarajya.noswipe.reelshortblocker.data.database.NoScrollDbHelper
import com.newswarajya.noswipe.reelshortblocker.data.datasource.PlatformConfigDataSource
import com.newswarajya.noswipe.reelshortblocker.data.preferences.NoScrollPreferences
import com.newswarajya.noswipe.reelshortblocker.domain.model.BlockMode
import com.newswarajya.noswipe.reelshortblocker.domain.model.CuriousConfig
import com.newswarajya.noswipe.reelshortblocker.domain.model.CuriousStatus
import com.newswarajya.noswipe.reelshortblocker.domain.model.DailyLimitConfig
import com.newswarajya.noswipe.reelshortblocker.domain.model.DetectionPlan
import com.newswarajya.noswipe.reelshortblocker.domain.model.Platform
import com.newswarajya.noswipe.reelshortblocker.domain.repository.NoScrollRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NoScrollRepositoryImpl(
    private val preferences: NoScrollPreferences,
    private val platformDataSource: PlatformConfigDataSource,
    private val dbHelper: NoScrollDbHelper
) : NoScrollRepository {

    private val _isMasterShieldEnabled = MutableStateFlow(preferences.isMasterShieldEnabled)
    override val isMasterShieldEnabled: StateFlow<Boolean> = _isMasterShieldEnabled.asStateFlow()

    private val _activePlan = MutableStateFlow(preferences.activePlan)
    override val activePlan: StateFlow<DetectionPlan> = _activePlan.asStateFlow()

    private val _pauseEndsAtMillis = MutableStateFlow(preferences.pauseEndsAtMillis)
    override val pauseEndsAtMillis: StateFlow<Long> = _pauseEndsAtMillis.asStateFlow()

    private val _dailyLimitConfig = MutableStateFlow(preferences.getDailyLimitConfig())
    override val dailyLimitConfig: StateFlow<DailyLimitConfig> = _dailyLimitConfig.asStateFlow()

    private val _curiousStatus = MutableStateFlow(preferences.getCuriousStatus())
    override val curiousStatus: StateFlow<CuriousStatus> = _curiousStatus.asStateFlow()

    private val _platforms = MutableStateFlow<List<Platform>>(emptyList())
    override val platforms: StateFlow<List<Platform>> = _platforms.asStateFlow()

    init {
        reloadPlatformsWithState()
    }

    fun reloadPlatformsWithState() {
        val rawCatalog = platformDataSource.loadPlatforms()
        val countsByPlatform = dbHelper.getBlockCountsByPlatform()
        val hydrated = rawCatalog.map { base ->
            val enabled = preferences.isPlatformEnabled(base.id, base.isEnabled)
            val mode = preferences.getPlatformBlockMode(base.id, base.defaultBlockMode)
            val blockedCount = countsByPlatform[base.id] ?: 0
            base.copy(
                isEnabled = enabled,
                currentBlockMode = mode,
                blockedCount = blockedCount
            )
        }
        _platforms.value = hydrated
    }

    override fun setMasterShieldEnabled(enabled: Boolean) {
        preferences.isMasterShieldEnabled = enabled
        _isMasterShieldEnabled.value = enabled
    }

    override fun setDetectionPlan(plan: DetectionPlan, pauseDurationMinutes: Int) {
        preferences.activePlan = plan
        _activePlan.value = plan
        if (plan == DetectionPlan.PAUSED && pauseDurationMinutes > 0) {
            val endsAt = System.currentTimeMillis() + pauseDurationMinutes * 60_000L
            preferences.pauseEndsAtMillis = endsAt
            _pauseEndsAtMillis.value = endsAt
        } else if (plan != DetectionPlan.PAUSED) {
            preferences.pauseEndsAtMillis = 0L
            _pauseEndsAtMillis.value = 0L
        }
    }

    override fun togglePlatformEnabled(platformId: String, enabled: Boolean) {
        preferences.setPlatformEnabled(platformId, enabled)
        _platforms.value = _platforms.value.map { platform ->
            if (platform.id == platformId) platform.copy(isEnabled = enabled) else platform
        }
    }

    override fun setPlatformBlockMode(platformId: String, mode: BlockMode) {
        preferences.setPlatformBlockMode(platformId, mode)
        _platforms.value = _platforms.value.map { platform ->
            if (platform.id == platformId) platform.copy(currentBlockMode = mode) else platform
        }
    }

    override fun enableAllPlatforms(enabled: Boolean) {
        _platforms.value = _platforms.value.map { platform ->
            preferences.setPlatformEnabled(platform.id, enabled)
            platform.copy(isEnabled = enabled)
        }
    }

    override fun updateDailyLimit(limitMinutes: Int, enabled: Boolean, hardLock: Boolean) {
        val clamped = limitMinutes.coerceIn(0, 300)
        val updated = _dailyLimitConfig.value.copy(
            isEnabled = enabled,
            dailyLimitMinutes = clamped,
            enforceHardLockOnExceed = hardLock
        )
        preferences.saveDailyLimitConfig(updated)
        _dailyLimitConfig.value = updated
    }

    override fun incrementUsedMinutesToday(deltaMinutes: Int) {
        val current = _dailyLimitConfig.value
        val updated = current.copy(
            usedMinutesToday = (current.usedMinutesToday + deltaMinutes).coerceAtLeast(0)
        )
        preferences.saveDailyLimitConfig(updated)
        _dailyLimitConfig.value = updated
    }

    override fun updateCuriousConfig(config: CuriousConfig) {
        val updated = _curiousStatus.value.copy(config = config)
        preferences.saveCuriousStatus(updated)
        _curiousStatus.value = updated
    }

    override fun startCuriousSession(nowMillis: Long): CuriousStatus {
        val current = _curiousStatus.value
        val durationMillis = current.config.sessionDurationMinutes * 60_000L
        val sessionEndsAt = nowMillis + durationMillis
        val cooldownEndsAt = sessionEndsAt + current.config.cooldownDurationMinutes * 60_000L
        val updated = current.copy(
            activeSessionEndsAtMillis = sessionEndsAt,
            cooldownEndsAtMillis = cooldownEndsAt,
            sessionsCompletedToday = current.sessionsCompletedToday + 1
        )
        preferences.saveCuriousStatus(updated)
        _curiousStatus.value = updated
        return updated
    }

    override fun endCuriousSessionIntoCooldown(nowMillis: Long): CuriousStatus {
        val current = _curiousStatus.value
        val cooldownEndsAt = nowMillis + current.config.cooldownDurationMinutes * 60_000L
        val updated = current.copy(
            activeSessionEndsAtMillis = 0L,
            cooldownEndsAtMillis = cooldownEndsAt
        )
        preferences.saveCuriousStatus(updated)
        _curiousStatus.value = updated
        return updated
    }

    override fun getPlatformsSnapshot(): List<Platform> {
        return _platforms.value
    }
}
