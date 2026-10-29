package com.newswarajya.noswipe.reelshortblocker.domain.repository

import com.newswarajya.noswipe.reelshortblocker.domain.model.AnalyticsSummary
import com.newswarajya.noswipe.reelshortblocker.domain.model.BlockRecord
import com.newswarajya.noswipe.reelshortblocker.domain.model.UnlockSession
import kotlinx.coroutines.flow.StateFlow

interface AnalyticsRepository {
    val analyticsSummary: StateFlow<AnalyticsSummary>
    val currentSessionId: StateFlow<String?>

    fun startUnlockSession(sessionId: String, unlockedOnMillis: Long = System.currentTimeMillis())
    fun closeUnlockSession(sessionId: String, lockedOnMillis: Long = System.currentTimeMillis())
    fun recordBlock(record: BlockRecord)
    fun refreshAnalytics(nowMillis: Long = System.currentTimeMillis())
    fun getRecentBlocks(limit: Int = 30): List<BlockRecord>
    fun getUnlockSessions(limit: Int = 20): List<UnlockSession>
}
