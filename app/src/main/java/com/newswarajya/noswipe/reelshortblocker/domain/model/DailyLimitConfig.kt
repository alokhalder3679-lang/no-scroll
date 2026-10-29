package com.newswarajya.noswipe.reelshortblocker.domain.model

enum class EmojiBand(
    val emoji: String,
    val label: String,
    val subtitle: String,
    val minMinutes: Int,
    val maxMinutes: Int
) {
    MONK_MODE(
        emoji = "🛑",
        label = "Total Lockdown (Monk Mode)",
        subtitle = "0 min daily allowance. Complete immunity from short-form dopamine feeds.",
        minMinutes = 0,
        maxMinutes = 0
    ),
    LASER_FOCUS(
        emoji = "🚀",
        label = "Laser Focus",
        subtitle = "1–30 min allowance. High intentionality with minimal cognitive residue.",
        minMinutes = 1,
        maxMinutes = 30
    ),
    HEALTHY_BALANCE(
        emoji = "⚖️",
        label = "Healthy Balance",
        subtitle = "31–120 min allowance. Controlled recreation without derailing deep work.",
        minMinutes = 31,
        maxMinutes = 120
    ),
    SLIPPING_AWAY(
        emoji = "🕰️",
        label = "Slipping Away...",
        subtitle = "121–240 min allowance. Warning zone — up to 4 hours of daily scroll exposure.",
        minMinutes = 121,
        maxMinutes = 240
    ),
    DOPAMINE_OVERLOAD(
        emoji = "💀",
        label = "Dopamine Overload",
        subtitle = "241+ min allowance. Severe attention fragmentation risk. Consider tightening.",
        minMinutes = 241,
        maxMinutes = Int.MAX_VALUE
    );

    val formattedDisplay: String
        get() = "$emoji $label"

    companion object {
        fun forMinutes(minutes: Int): EmojiBand {
            val clamped = minutes.coerceAtLeast(0)
            return when {
                clamped == 0 -> MONK_MODE
                clamped in 1..30 -> LASER_FOCUS
                clamped in 31..120 -> HEALTHY_BALANCE
                clamped in 121..240 -> SLIPPING_AWAY
                else -> DOPAMINE_OVERLOAD
            }
        }
    }
}

data class DailyLimitConfig(
    val isEnabled: Boolean = true,
    val dailyLimitMinutes: Int = 20,
    val usedMinutesToday: Int = 0,
    val enforceHardLockOnExceed: Boolean = true
) {
    val emojiBand: EmojiBand
        get() = EmojiBand.forMinutes(dailyLimitMinutes)

    val isExceeded: Boolean
        get() = isEnabled && usedMinutesToday >= dailyLimitMinutes

    val remainingMinutes: Int
        get() = (dailyLimitMinutes - usedMinutesToday).coerceAtLeast(0)
}
