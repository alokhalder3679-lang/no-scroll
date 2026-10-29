package com.newswarajya.noswipe.reelshortblocker.domain.repository

import com.newswarajya.noswipe.reelshortblocker.domain.model.BlockMode
import com.newswarajya.noswipe.reelshortblocker.domain.model.CuriousConfig
import com.newswarajya.noswipe.reelshortblocker.domain.model.CuriousStatus
import com.newswarajya.noswipe.reelshortblocker.domain.model.DailyLimitConfig
import com.newswarajya.noswipe.reelshortblocker.domain.model.DetectionPlan
import com.newswarajya.noswipe.reelshortblocker.domain.model.Platform
import kotlinx.coroutines.flow.StateFlow

interface NoScrollRepository {
    val isMasterShieldEnabled: StateFlow<Boolean>
    val activePlan: StateFlow<DetectionPlan>
    val pauseEndsAtMillis: StateFlow<Long>
    val platforms: StateFlow<List<Platform>>
    val dailyLimitConfig: StateFlow<DailyLimitConfig>
    val curiousStatus: StateFlow<CuriousStatus>

    fun setMasterShieldEnabled(enabled: Boolean)
    fun setDetectionPlan(plan: DetectionPlan, pauseDurationMinutes: Int = 0)
    fun togglePlatformEnabled(platformId: String, enabled: Boolean)
    fun setPlatformBlockMode(platformId: String, mode: BlockMode)
    fun enableAllPlatforms(enabled: Boolean)
    fun updateDailyLimit(limitMinutes: Int, enabled: Boolean, hardLock: Boolean)
    fun incrementUsedMinutesToday(deltaMinutes: Int = 1)
    fun updateCuriousConfig(config: CuriousConfig)
    fun startCuriousSession(nowMillis: Long = System.currentTimeMillis()): CuriousStatus
    fun endCuriousSessionIntoCooldown(nowMillis: Long = System.currentTimeMillis()): CuriousStatus
    fun getPlatformsSnapshot(): List<Platform>
}
