package com.newswarajya.noswipe.reelshortblocker.domain.usecase

import com.newswarajya.noswipe.reelshortblocker.domain.model.AnalyticsSummary
import com.newswarajya.noswipe.reelshortblocker.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.StateFlow

class GetDailyAnalyticsUseCase(
    private val analyticsRepository: AnalyticsRepository
) {
    val summaryFlow: StateFlow<AnalyticsSummary>
        get() = analyticsRepository.analyticsSummary

    fun refresh(nowMillis: Long = System.currentTimeMillis()): AnalyticsSummary {
        analyticsRepository.refreshAnalytics(nowMillis)
        return analyticsRepository.analyticsSummary.value
    }
}
