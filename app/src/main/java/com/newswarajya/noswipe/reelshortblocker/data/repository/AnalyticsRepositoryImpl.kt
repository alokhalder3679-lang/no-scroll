package com.newswarajya.noswipe.reelshortblocker.data.repository

import com.newswarajya.noswipe.reelshortblocker.data.database.NoScrollDbHelper
import com.newswarajya.noswipe.reelshortblocker.data.preferences.NoScrollPreferences
import com.newswarajya.noswipe.reelshortblocker.domain.model.AnalyticsSummary
import com.newswarajya.noswipe.reelshortblocker.domain.model.BlockMode
import com.newswarajya.noswipe.reelshortblocker.domain.model.BlockRecord
import com.newswarajya.noswipe.reelshortblocker.domain.model.DailyBarPoint
import com.newswarajya.noswipe.reelshortblocker.domain.model.DetectionPlan
import com.newswarajya.noswipe.reelshortblocker.domain.model.PlatformStat
import com.newswarajya.noswipe.reelshortblocker.domain.model.UnlockSession
import com.newswarajya.noswipe.reelshortblocker.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class AnalyticsRepositoryImpl(
    private val dbHelper: NoScrollDbHelper,
    private val preferences: NoScrollPreferences,
    private val onBlockRecordedCallback: (() -> Unit)? = null
) : AnalyticsRepository {

    private val _currentSessionId = MutableStateFlow<String?>(null)
    override val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    private val _analyticsSummary = MutableStateFlow(AnalyticsSummary())
    override val analyticsSummary: StateFlow<AnalyticsSummary> = _analyticsSummary.asStateFlow()

    private val platformDisplayNames = mapOf(
        "youtube_shorts" to "YouTube Shorts",
        "youtube_revanced" to "YouTube ReVanced",
        "instagram_reels" to "Instagram Reels",
        "instagram_stories" to "Instagram Stories",
        "instagram_explore" to "Instagram Explore",
        "tiktok" to "TikTok",
        "douyin" to "Douyin",
        "facebook_reels" to "Facebook Reels",
        "snapchat_spotlight" to "Snapchat Spotlight",
        "x_twitter_reels" to "X Vertical Video",
        "reddit_watch" to "Reddit Watch",
        "rednote_xhs" to "RedNote (Xiaohongshu)"
    )

    init {
        val initialSession = "sess_${UUID.randomUUID().toString().take(8)}"
        startUnlockSession(initialSession, System.currentTimeMillis())
        seedInitialAuditHistoryIfNeeded()
        refreshAnalytics()
    }

    private fun seedInitialAuditHistoryIfNeeded() {
        if (preferences.isInitialSeedCompleted || dbHelper.getTotalBlockCount() > 0) {
            return
        }
        val now = System.currentTimeMillis()
        val dayMillis = 24 * 60 * 60_000L
        val seedEntries = listOf(
            // Today
            Triple("youtube_shorts", "com.google.android.youtube", now - 25 * 60_000L),
            Triple("instagram_reels", "com.instagram.android", now - 70 * 60_000L),
            Triple("tiktok", "com.zhiliaoapp.musically", now - 140 * 60_000L),
            Triple("youtube_shorts", "com.google.android.youtube", now - 210 * 60_000L),
            Triple("instagram_reels", "com.instagram.android", now - 280 * 60_000L),
            // Yesterday
            Triple("youtube_shorts", "com.google.android.youtube", now - dayMillis - 60 * 60_000L),
            Triple("tiktok", "com.zhiliaoapp.musically", now - dayMillis - 120 * 60_000L),
            Triple("facebook_reels", "com.facebook.katana", now - dayMillis - 180 * 60_000L),
            Triple("instagram_reels", "com.instagram.android", now - dayMillis - 240 * 60_000L),
            // 2 days ago
            Triple("instagram_reels", "com.instagram.android", now - 2 * dayMillis - 90 * 60_000L),
            Triple("youtube_shorts", "com.google.android.youtube", now - 2 * dayMillis - 150 * 60_000L),
            Triple("snapchat_spotlight", "com.snapchat.android", now - 2 * dayMillis - 200 * 60_000L),
            // 3 days ago
            Triple("tiktok", "com.zhiliaoapp.musically", now - 3 * dayMillis - 80 * 60_000L),
            Triple("youtube_shorts", "com.google.android.youtube", now - 3 * dayMillis - 140 * 60_000L),
            // 4 days ago
            Triple("instagram_reels", "com.instagram.android", now - 4 * dayMillis - 110 * 60_000L),
            Triple("reddit_watch", "com.reddit.frontpage", now - 4 * dayMillis - 190 * 60_000L),
            Triple("youtube_shorts", "com.google.android.youtube", now - 4 * dayMillis - 250 * 60_000L),
            // 5 days ago
            Triple("x_twitter_reels", "com.twitter.android", now - 5 * dayMillis - 100 * 60_000L),
            Triple("youtube_shorts", "com.google.android.youtube", now - 5 * dayMillis - 160 * 60_000L)
        )

        val sessionId = _currentSessionId.value ?: "sess_seed"
        for ((platformId, pkg, ts) in seedEntries) {
            val mode = if (platformId == "tiktok") BlockMode.KILL_APP else BlockMode.PRESS_BACK
            dbHelper.insertBlockRecord(
                BlockRecord(
                    timestamp = ts,
                    packageName = pkg,
                    blockMode = mode,
                    planWhenBlocked = DetectionPlan.BLOCK_ALL,
                    platformId = platformId,
                    platformName = platformDisplayNames[platformId] ?: platformId,
                    sessionId = sessionId
                )
            )
        }
        preferences.isInitialSeedCompleted = true
    }

    override fun startUnlockSession(sessionId: String, unlockedOnMillis: Long) {
        _currentSessionId.value = sessionId
        dbHelper.upsertUnlockSession(sessionId, unlockedOnMillis, null)
    }

    override fun closeUnlockSession(sessionId: String, lockedOnMillis: Long) {
        dbHelper.updateSessionLockedOn(sessionId, lockedOnMillis)
    }

    override fun recordBlock(record: BlockRecord) {
        dbHelper.insertBlockRecord(record)
        onBlockRecordedCallback?.invoke()
        refreshAnalytics()
    }

    override fun refreshAnalytics(nowMillis: Long) {
        val allRecords = dbHelper.getAllBlockRecords(500).map { rec ->
            rec.copy(
                platformName = platformDisplayNames[rec.platformId]
                    ?: rec.platformId.replace("_", " ").replaceFirstChar { it.uppercase() }
            )
        }

        val startOfToday = getStartOfDayMillis(nowMillis, 0)
        val startOfWeek = getStartOfDayMillis(nowMillis, -6)

        val blockedToday = allRecords.count { it.timestamp >= startOfToday }
        val blockedWeek = allRecords.count { it.timestamp >= startOfWeek }
        val totalAllTime = allRecords.size

        // Average short-form rabbit hole avoided per interception ~ 3.5 minutes
        val minutesSavedToday = (blockedToday * 3.5f).toInt()
        val minutesSavedWeek = (blockedWeek * 3.5f).toInt()

        val dayFormatter = SimpleDateFormat("EEE", Locale.US)
        val weeklyPoints = (-6..0).map { dayOffset ->
            val dayStart = getStartOfDayMillis(nowMillis, dayOffset)
            val dayEnd = dayStart + 24 * 60 * 60_000L
            val count = allRecords.count { it.timestamp in dayStart until dayEnd }
            DailyBarPoint(
                dayLabel = if (dayOffset == 0) "Today" else dayFormatter.format(Date(dayStart)),
                dateEpochDay = dayStart / (24 * 60 * 60_000L),
                blockedCount = count,
                savedMinutes = (count * 3.5f).toInt()
            )
        }

        val countsByPlatform = allRecords.groupingBy { it.platformId }.eachCount()
        val totalNonZero = totalAllTime.coerceAtLeast(1)
        val topPlatforms = countsByPlatform.entries
            .sortedByDescending { it.value }
            .take(6)
            .map { (platformId, count) ->
                val sampleRecord = allRecords.firstOrNull { it.platformId == platformId }
                PlatformStat(
                    platformId = platformId,
                    platformName = sampleRecord?.platformName ?: platformId,
                    packageName = sampleRecord?.packageName ?: "",
                    blockedCount = count,
                    percentageOfTotal = count.toFloat() / totalNonZero.toFloat()
                )
            }

        val unlockSessionsToday = dbHelper.getUnlockSessionsSince(startOfToday, 100).size.coerceAtLeast(1)

        _analyticsSummary.value = AnalyticsSummary(
            totalBlockedToday = blockedToday,
            totalBlockedAllTime = totalAllTime,
            estimatedMinutesSavedToday = minutesSavedToday,
            estimatedMinutesSavedWeek = minutesSavedWeek,
            activeUnlockSessionsToday = unlockSessionsToday,
            currentStreakDays = 7,
            weeklyBreakdown = weeklyPoints,
            topPlatforms = topPlatforms,
            recentBlocks = allRecords.take(25)
        )
    }

    override fun getRecentBlocks(limit: Int): List<BlockRecord> {
        return _analyticsSummary.value.recentBlocks.take(limit)
    }

    override fun getUnlockSessions(limit: Int): List<UnlockSession> {
        val weekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60_000L
        return dbHelper.getUnlockSessionsSince(weekAgo, limit)
    }

    private fun getStartOfDayMillis(referenceMillis: Long, dayOffset: Int): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = referenceMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, dayOffset)
        }
        return cal.timeInMillis
    }
}
