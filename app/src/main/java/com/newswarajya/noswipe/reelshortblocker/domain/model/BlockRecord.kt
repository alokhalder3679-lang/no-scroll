package com.newswarajya.noswipe.reelshortblocker.domain.model

data class UnlockSession(
    val sessionId: String,
    val unlockedOn: Long,
    val lockedOn: Long? = null
)

data class BlockRecord(
    val id: Long = 0L,
    val timestamp: Long,
    val packageName: String,
    val blockMode: BlockMode,
    val planWhenBlocked: DetectionPlan,
    val platformId: String,
    val platformName: String = platformId,
    val sessionId: String? = null
)

data class DailyBarPoint(
    val dayLabel: String,
    val dateEpochDay: Long,
    val blockedCount: Int,
    val savedMinutes: Int
)

data class PlatformStat(
    val platformId: String,
    val platformName: String,
    val packageName: String,
    val blockedCount: Int,
    val percentageOfTotal: Float
)

data class AnalyticsSummary(
    val totalBlockedToday: Int = 0,
    val totalBlockedAllTime: Int = 0,
    val estimatedMinutesSavedToday: Int = 0,
    val estimatedMinutesSavedWeek: Int = 0,
    val activeUnlockSessionsToday: Int = 0,
    val currentStreakDays: Int = 1,
    val weeklyBreakdown: List<DailyBarPoint> = emptyList(),
    val topPlatforms: List<PlatformStat> = emptyList(),
    val recentBlocks: List<BlockRecord> = emptyList()
)
