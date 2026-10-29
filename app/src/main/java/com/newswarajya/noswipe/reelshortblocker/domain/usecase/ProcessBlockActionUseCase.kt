package com.newswarajya.noswipe.reelshortblocker.domain.usecase

import com.newswarajya.noswipe.reelshortblocker.domain.model.BlockMode
import com.newswarajya.noswipe.reelshortblocker.domain.model.CuriousStatus
import com.newswarajya.noswipe.reelshortblocker.domain.model.DailyLimitConfig
import com.newswarajya.noswipe.reelshortblocker.domain.model.DetectionPlan
import com.newswarajya.noswipe.reelshortblocker.domain.model.Platform

sealed class BlockDecision {
    data class Allow(val reason: String) : BlockDecision()
    data class Block(
        val platform: Platform,
        val blockMode: BlockMode,
        val activePlan: DetectionPlan,
        val reason: String
    ) : BlockDecision()
}

/**
 * Evaluates the active DetectionPlan (BLOCK_ALL, ONE_REEL, PAUSED, CURIOUS) and DailyLimitConfig
 * whenever short-form video content is detected, maintaining stateful 1-Reel grace tracking.
 */
class ProcessBlockActionUseCase {

    // Tracks how many reels have been viewed in the current app visit per platformId
    private val oneReelVisitCounts = mutableMapOf<String, Int>()

    fun resetOneReelGrace(platformId: String? = null) {
        if (platformId == null) {
            oneReelVisitCounts.clear()
        } else {
            oneReelVisitCounts.remove(platformId)
        }
    }

    fun evaluate(
        platform: Platform,
        activePlan: DetectionPlan,
        dailyLimitConfig: DailyLimitConfig,
        curiousStatus: CuriousStatus,
        pauseEndsAtMillis: Long,
        nowMillis: Long = System.currentTimeMillis()
    ): BlockDecision {
        // 1. Daily limit override: if daily limit is enabled and exceeded, force block immediately
        if (dailyLimitConfig.isEnabled && dailyLimitConfig.isExceeded) {
            val mode = if (dailyLimitConfig.enforceHardLockOnExceed) {
                platform.currentBlockMode
            } else {
                BlockMode.PRESS_BACK
            }
            return BlockDecision.Block(
                platform = platform,
                blockMode = mode,
                activePlan = activePlan,
                reason = "Daily limit of ${dailyLimitConfig.dailyLimitMinutes}m exceeded (${dailyLimitConfig.usedMinutesToday}m used)"
            )
        }

        // 2. Evaluate based on active protection plan
        return when (activePlan) {
            DetectionPlan.BLOCK_ALL -> {
                BlockDecision.Block(
                    platform = platform,
                    blockMode = platform.currentBlockMode,
                    activePlan = DetectionPlan.BLOCK_ALL,
                    reason = "Strict Shield (Block All) active"
                )
            }

            DetectionPlan.ONE_REEL -> {
                val seenCount = oneReelVisitCounts.getOrDefault(platform.id, 0)
                if (seenCount == 0) {
                    oneReelVisitCounts[platform.id] = 1
                    BlockDecision.Allow(
                        reason = "1-Reel Grace: allowing first reel for ${platform.name}"
                    )
                } else {
                    oneReelVisitCounts[platform.id] = seenCount + 1
                    BlockDecision.Block(
                        platform = platform,
                        blockMode = platform.currentBlockMode,
                        activePlan = DetectionPlan.ONE_REEL,
                        reason = "1-Reel Grace exhausted (reel #${seenCount + 1} blocked)"
                    )
                }
            }

            DetectionPlan.PAUSED -> {
                if (pauseEndsAtMillis > nowMillis) {
                    val remainingSec = (pauseEndsAtMillis - nowMillis) / 1000L
                    BlockDecision.Allow(
                        reason = "Mindful Pause active (${remainingSec}s remaining)"
                    )
                } else {
                    BlockDecision.Block(
                        platform = platform,
                        blockMode = platform.currentBlockMode,
                        activePlan = DetectionPlan.PAUSED,
                        reason = "Mindful Pause timer expired"
                    )
                }
            }

            DetectionPlan.CURIOUS -> {
                when {
                    curiousStatus.isInActiveSession(nowMillis) -> {
                        val remainingSec = curiousStatus.remainingSessionSeconds(nowMillis)
                        BlockDecision.Allow(
                            reason = "Curious Micro-Session active (${remainingSec}s remaining)"
                        )
                    }
                    curiousStatus.isInCooldown(nowMillis) -> {
                        val cooldownSec = curiousStatus.remainingCooldownSeconds(nowMillis)
                        BlockDecision.Block(
                            platform = platform,
                            blockMode = platform.currentBlockMode,
                            activePlan = DetectionPlan.CURIOUS,
                            reason = "Curious Mode in mandatory cooldown (${cooldownSec}s left)"
                        )
                    }
                    else -> {
                        BlockDecision.Block(
                            platform = platform,
                            blockMode = platform.currentBlockMode,
                            activePlan = DetectionPlan.CURIOUS,
                            reason = "No active Curious Micro-Session started"
                        )
                    }
                }
            }
        }
    }
}
