package com.newswarajya.noswipe.reelshortblocker.domain.model

data class CuriousConfig(
    val isEnabled: Boolean = false,
    val sessionDurationMinutes: Int = 5,
    val cooldownDurationMinutes: Int = 25,
    val maxDailySessions: Int = 3,
    val requireMindfulBreathBeforeStart: Boolean = true
)

data class CuriousStatus(
    val config: CuriousConfig = CuriousConfig(),
    val activeSessionEndsAtMillis: Long = 0L,
    val cooldownEndsAtMillis: Long = 0L,
    val sessionsCompletedToday: Int = 0
) {
    fun isInActiveSession(nowMillis: Long): Boolean {
        return activeSessionEndsAtMillis > nowMillis
    }

    fun isInCooldown(nowMillis: Long): Boolean {
        return !isInActiveSession(nowMillis) && cooldownEndsAtMillis > nowMillis
    }

    fun remainingSessionSeconds(nowMillis: Long): Long {
        return ((activeSessionEndsAtMillis - nowMillis).coerceAtLeast(0L)) / 1000L
    }

    fun remainingCooldownSeconds(nowMillis: Long): Long {
        return ((cooldownEndsAtMillis - nowMillis).coerceAtLeast(0L)) / 1000L
    }
}
