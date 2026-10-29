package com.newswarajya.noswipe.reelshortblocker.domain.model

enum class DetectionPlan(
    val title: String,
    val subtitle: String,
    val badge: String
) {
    BLOCK_ALL(
        title = "Strict Shield (Block All)",
        subtitle = "Zero tolerance. Instant interception the moment a short-form feed appears.",
        badge = "STRICT"
    ),
    ONE_REEL(
        title = "1-Reel Grace Mode",
        subtitle = "Allows viewing a single shared link or reel, then blocks subsequent vertical scrolling.",
        badge = "1-REEL"
    ),
    CURIOUS(
        title = "Curious Micro-Sessions",
        subtitle = "Permits timed micro-windows followed by mandatory dopamine cooldowns.",
        badge = "CURIOUS"
    ),
    PAUSED(
        title = "Mindful Pause Timer",
        subtitle = "Temporarily suspends interception for a bounded countdown window.",
        badge = "PAUSED"
    );

    companion object {
        fun fromName(value: String?): DetectionPlan {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: BLOCK_ALL
        }
    }
}
