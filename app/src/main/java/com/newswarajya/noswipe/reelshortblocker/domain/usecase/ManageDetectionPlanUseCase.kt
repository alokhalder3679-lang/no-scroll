package com.newswarajya.noswipe.reelshortblocker.domain.usecase

import com.newswarajya.noswipe.reelshortblocker.domain.model.CuriousConfig
import com.newswarajya.noswipe.reelshortblocker.domain.model.CuriousStatus
import com.newswarajya.noswipe.reelshortblocker.domain.model.DetectionPlan
import com.newswarajya.noswipe.reelshortblocker.domain.repository.NoScrollRepository

class ManageDetectionPlanUseCase(
    private val repository: NoScrollRepository
) {
    fun selectPlan(plan: DetectionPlan, pauseDurationMinutes: Int = 15) {
        repository.setDetectionPlan(plan, pauseDurationMinutes)
    }

    fun startMindfulPause(minutes: Int) {
        val boundedMinutes = minutes.coerceIn(1, 180)
        repository.setDetectionPlan(DetectionPlan.PAUSED, boundedMinutes)
    }

    fun resumeStrictProtection() {
        repository.setDetectionPlan(DetectionPlan.BLOCK_ALL, 0)
    }

    fun configureCuriousMode(
        sessionMinutes: Int,
        cooldownMinutes: Int,
        maxDailySessions: Int,
        requireMindfulBreath: Boolean
    ) {
        val config = CuriousConfig(
            isEnabled = true,
            sessionDurationMinutes = sessionMinutes.coerceIn(1, 30),
            cooldownDurationMinutes = cooldownMinutes.coerceIn(5, 180),
            maxDailySessions = maxDailySessions.coerceIn(1, 12),
            requireMindfulBreathBeforeStart = requireMindfulBreath
        )
        repository.updateCuriousConfig(config)
    }

    fun launchCuriousMicroSession(nowMillis: Long = System.currentTimeMillis()): CuriousStatus {
        repository.setDetectionPlan(DetectionPlan.CURIOUS, 0)
        return repository.startCuriousSession(nowMillis)
    }

    fun forceCuriousCooldown(nowMillis: Long = System.currentTimeMillis()): CuriousStatus {
        return repository.endCuriousSessionIntoCooldown(nowMillis)
    }
}
