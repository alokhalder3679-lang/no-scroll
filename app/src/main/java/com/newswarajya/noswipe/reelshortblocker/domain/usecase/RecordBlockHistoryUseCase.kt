package com.newswarajya.noswipe.reelshortblocker.domain.usecase

import com.newswarajya.noswipe.reelshortblocker.domain.model.BlockMode
import com.newswarajya.noswipe.reelshortblocker.domain.model.BlockRecord
import com.newswarajya.noswipe.reelshortblocker.domain.model.DetectionPlan
import com.newswarajya.noswipe.reelshortblocker.domain.model.Platform
import com.newswarajya.noswipe.reelshortblocker.domain.repository.AnalyticsRepository

class RecordBlockHistoryUseCase(
    private val analyticsRepository: AnalyticsRepository
) {
    fun execute(
        platform: Platform,
        blockMode: BlockMode,
        planWhenBlocked: DetectionPlan,
        timestampMillis: Long = System.currentTimeMillis()
    ): BlockRecord {
        val sessionId = analyticsRepository.currentSessionId.value
        val record = BlockRecord(
            timestamp = timestampMillis,
            packageName = platform.packageName,
            blockMode = blockMode,
            planWhenBlocked = planWhenBlocked,
            platformId = platform.id,
            platformName = platform.name,
            sessionId = sessionId
        )
        analyticsRepository.recordBlock(record)
        return record
    }
}
