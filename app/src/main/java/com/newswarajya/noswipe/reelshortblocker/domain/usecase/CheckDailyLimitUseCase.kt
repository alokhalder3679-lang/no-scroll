package com.newswarajya.noswipe.reelshortblocker.domain.usecase

import com.newswarajya.noswipe.reelshortblocker.domain.model.DailyLimitConfig
import com.newswarajya.noswipe.reelshortblocker.domain.model.EmojiBand

data class DailyLimitEvaluation(
    val config: DailyLimitConfig,
    val emojiBand: EmojiBand,
    val isLimitReached: Boolean,
    val usageFraction: Float,
    val statusText: String
)

class CheckDailyLimitUseCase {

    fun classifyEmojiBand(minutes: Int): EmojiBand {
        return EmojiBand.forMinutes(minutes)
    }

    fun evaluate(config: DailyLimitConfig): DailyLimitEvaluation {
        val band = EmojiBand.forMinutes(config.dailyLimitMinutes)
        val isReached = config.isEnabled && config.usedMinutesToday >= config.dailyLimitMinutes
        val fraction = when {
            !config.isEnabled -> 0f
            config.dailyLimitMinutes <= 0 -> 1f
            else -> (config.usedMinutesToday.toFloat() / config.dailyLimitMinutes.toFloat()).coerceIn(0f, 1f)
        }
        val statusText = when {
            !config.isEnabled -> "Daily limit disabled"
            config.dailyLimitMinutes == 0 -> "Monk Mode active — 0m allowance"
            isReached -> "Limit reached (${config.usedMinutesToday}m / ${config.dailyLimitMinutes}m)"
            else -> "${config.remainingMinutes}m remaining today (${config.usedMinutesToday}m used)"
        }
        return DailyLimitEvaluation(
            config = config,
            emojiBand = band,
            isLimitReached = isReached,
            usageFraction = fraction,
            statusText = statusText
        )
    }
}
