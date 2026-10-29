package com.newswarajya.noswipe.reelshortblocker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.newswarajya.noswipe.reelshortblocker.data.ServiceLocator
import com.newswarajya.noswipe.reelshortblocker.data.datasource.MindfulQuote
import com.newswarajya.noswipe.reelshortblocker.domain.model.AnalyticsSummary
import com.newswarajya.noswipe.reelshortblocker.domain.model.BlockMode
import com.newswarajya.noswipe.reelshortblocker.domain.model.CuriousStatus
import com.newswarajya.noswipe.reelshortblocker.domain.model.DailyLimitConfig
import com.newswarajya.noswipe.reelshortblocker.domain.model.DetectionPlan
import com.newswarajya.noswipe.reelshortblocker.domain.model.Platform
import com.newswarajya.noswipe.reelshortblocker.domain.model.ViewNode
import com.newswarajya.noswipe.reelshortblocker.domain.repository.SecuritySettings
import com.newswarajya.noswipe.reelshortblocker.domain.usecase.BlockDecision
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NoScrollDestination(val title: String) {
    DASHBOARD("Shield"),
    PLATFORMS("Platforms"),
    LIMITS("Limits"),
    CURIOUS("Curious"),
    MINDFUL("Mindful"),
    ANALYTICS("Analytics"),
    SETTINGS("Settings")
}

data class InterceptionSimulationFeedback(
    val timestampMillis: Long,
    val platformName: String,
    val matchedViewId: String,
    val wasBlocked: Boolean,
    val actionTaken: String,
    val reason: String
)

data class HomeUiState(
    val isMasterShieldEnabled: Boolean = true,
    val activePlan: DetectionPlan = DetectionPlan.BLOCK_ALL,
    val pauseRemainingSeconds: Long = 0L,
    val platforms: List<Platform> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val dailyLimitConfig: DailyLimitConfig = DailyLimitConfig(),
    val curiousStatus: CuriousStatus = CuriousStatus(),
    val nowMillis: Long = System.currentTimeMillis(),
    val analyticsSummary: AnalyticsSummary = AnalyticsSummary(),
    val securitySettings: SecuritySettings = SecuritySettings(),
    val mindfulQuotes: List<MindfulQuote> = emptyList(),
    val activeQuoteIndex: Int = 0,
    val lastSimulationFeedback: InterceptionSimulationFeedback? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    init {
        ServiceLocator.init(application)
    }

    private val noScrollRepo = ServiceLocator.noScrollRepository
    private val analyticsRepo = ServiceLocator.analyticsRepository
    private val settingsRepo = ServiceLocator.settingsRepository

    private val _currentDestination = MutableStateFlow(NoScrollDestination.DASHBOARD)
    val currentDestination: StateFlow<NoScrollDestination> = _currentDestination.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("All")
    private val _nowMillis = MutableStateFlow(System.currentTimeMillis())
    private val _activeQuoteIndex = MutableStateFlow(0)
    private val _lastSimulationFeedback = MutableStateFlow<InterceptionSimulationFeedback?>(null)

    private val quotesList: List<MindfulQuote> = ServiceLocator.platformDataSource.loadMindfulQuotes()

    val uiState: StateFlow<HomeUiState> = combine(
        noScrollRepo.isMasterShieldEnabled,
        noScrollRepo.activePlan,
        noScrollRepo.pauseEndsAtMillis,
        noScrollRepo.platforms,
        noScrollRepo.dailyLimitConfig
    ) { shield, plan, pauseEnds, platforms, dailyLimit ->
         Quintuple(shield, plan, pauseEnds, platforms, dailyLimit)
    }.combine(
        combine(
            noScrollRepo.curiousStatus,
            analyticsRepo.analyticsSummary,
            settingsRepo.settings,
            _searchQuery,
            _selectedCategory
        ) { curious, analytics, settings, query, category ->
            Quintuple(curious, analytics, settings, query, category)
        }
    ) { first, second ->
        val now = _nowMillis.value
        val pauseRemSec = ((first.third - now).coerceAtLeast(0L)) / 1000L
        HomeUiState(
            isMasterShieldEnabled = first.first,
            activePlan = first.second,
            pauseRemainingSeconds = pauseRemSec,
            platforms = first.fourth,
            searchQuery = second.fourth,
            selectedCategory = second.fifth,
            dailyLimitConfig = first.fifth,
            curiousStatus = second.first,
            nowMillis = now,
            analyticsSummary = second.second,
            securitySettings = second.third,
            mindfulQuotes = quotesList,
            activeQuoteIndex = _activeQuoteIndex.value,
            lastSimulationFeedback = _lastSimulationFeedback.value
        )
    }.combine(
        combine(_nowMillis, _activeQuoteIndex, _lastSimulationFeedback) { now, qIdx, sim ->
            Triple(now, qIdx, sim)
        }
    ) { baseState, dynamicTriple ->
        val now = dynamicTriple.first
        val pauseEnds = noScrollRepo.pauseEndsAtMillis.value
        val pauseRemSec = ((pauseEnds - now).coerceAtLeast(0L)) / 1000L
        baseState.copy(
            nowMillis = now,
            pauseRemainingSeconds = pauseRemSec,
            activeQuoteIndex = dynamicTriple.second,
            lastSimulationFeedback = dynamicTriple.third
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(
            isMasterShieldEnabled = noScrollRepo.isMasterShieldEnabled.value,
            activePlan = noScrollRepo.activePlan.value,
            platforms = noScrollRepo.platforms.value,
            dailyLimitConfig = noScrollRepo.dailyLimitConfig.value,
            curiousStatus = noScrollRepo.curiousStatus.value,
            analyticsSummary = analyticsRepo.analyticsSummary.value,
            securitySettings = settingsRepo.settings.value,
            mindfulQuotes = quotesList
        )
    )

    init {
        viewModelScope.launch {
            while (true) {
                delay(1_000L)
                _nowMillis.value = System.currentTimeMillis()
            }
        }
    }

    fun navigateTo(destination: NoScrollDestination) {
        _currentDestination.value = destination
    }

    fun setMasterShield(enabled: Boolean) {
        noScrollRepo.setMasterShieldEnabled(enabled)
    }

    fun selectDetectionPlan(plan: DetectionPlan, pauseMinutes: Int = 15) {
        ServiceLocator.processBlockActionUseCase.resetOneReelGrace()
        ServiceLocator.manageDetectionPlanUseCase.selectPlan(plan, pauseMinutes)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun togglePlatform(platformId: String, enabled: Boolean) {
        noScrollRepo.togglePlatformEnabled(platformId, enabled)
    }

    fun updatePlatformBlockMode(platformId: String, mode: BlockMode) {
        noScrollRepo.setPlatformBlockMode(platformId, mode)
    }

    fun enableAllPlatforms(enabled: Boolean) {
        noScrollRepo.enableAllPlatforms(enabled)
    }

    fun updateDailyLimit(limitMinutes: Int, enabled: Boolean, hardLock: Boolean) {
        noScrollRepo.updateDailyLimit(limitMinutes, enabled, hardLock)
    }

    fun addSimulatedScrollMinutes(deltaMinutes: Int) {
        noScrollRepo.incrementUsedMinutesToday(deltaMinutes)
    }

    fun updateCuriousConfiguration(
        sessionMinutes: Int,
        cooldownMinutes: Int,
        maxDailySessions: Int,
        requireMindfulBreath: Boolean
    ) {
        ServiceLocator.manageDetectionPlanUseCase.configureCuriousMode(
            sessionMinutes = sessionMinutes,
            cooldownMinutes = cooldownMinutes,
            maxDailySessions = maxDailySessions,
            requireMindfulBreath = requireMindfulBreath
        )
    }

    fun startCuriousMicroSession() {
        ServiceLocator.manageDetectionPlanUseCase.launchCuriousMicroSession(System.currentTimeMillis())
        _nowMillis.value = System.currentTimeMillis()
    }

    fun endCuriousSessionEarly() {
        ServiceLocator.manageDetectionPlanUseCase.forceCuriousCooldown(System.currentTimeMillis())
        _nowMillis.value = System.currentTimeMillis()
    }

    fun startMindfulPauseTimer(minutes: Int) {
        ServiceLocator.manageDetectionPlanUseCase.startMindfulPause(minutes)
        _nowMillis.value = System.currentTimeMillis()
    }

    fun nextMindfulQuote() {
        if (quotesList.isNotEmpty()) {
            _activeQuoteIndex.value = (_activeQuoteIndex.value + 1) % quotesList.size
        }
    }

    fun updatePinSettings(enabled: Boolean, pinCode: String) {
        settingsRepo.updatePinSecurity(enabled, pinCode)
    }

    fun setBiometricUnlock(enabled: Boolean) {
        settingsRepo.setBiometricEnabled(enabled)
    }

    fun setHapticFeedback(enabled: Boolean) {
        settingsRepo.setHapticFeedback(enabled)
    }

    fun setPersistentNotification(enabled: Boolean) {
        settingsRepo.setPersistentNotification(enabled)
    }

    fun setMindfulBreathDuration(seconds: Int) {
        settingsRepo.setMindfulBreathDuration(seconds)
    }

    fun refreshPermissions() {
        ServiceLocator.refreshSystemPermissions(getApplication())
    }

    /**
     * Executes the real DetectShortContentUseCase and ProcessBlockActionUseCase against a
     * synthetic Accessibility ViewNode hierarchy for the chosen platform and records the
     * resulting block audit into SQLite.
     */
    fun triggerLiveTreeInterceptionTest(platformId: String) {
        val platforms = noScrollRepo.getPlatformsSnapshot()
        val targetPlatform = platforms.firstOrNull { it.id == platformId } ?: platforms.firstOrNull() ?: return
        val primaryDetector = targetPlatform.detectors.firstOrNull() ?: return

        val syntheticChild = when (primaryDetector.type) {
            com.newswarajya.noswipe.reelshortblocker.domain.model.DetectorType.CONT_DESC ->
                ViewNode(
                    packageName = targetPlatform.packageName,
                    contentDescription = primaryDetector.value,
                    isVisibleToUser = true
                )
            com.newswarajya.noswipe.reelshortblocker.domain.model.DetectorType.TEXT_CONTAINS ->
                ViewNode(
                    packageName = targetPlatform.packageName,
                    text = "https://www.${primaryDetector.value}watch123",
                    isVisibleToUser = true
                )
            else ->
                ViewNode(
                    packageName = targetPlatform.packageName,
                    viewIdResourceName = primaryDetector.value,
                    isVisibleToUser = true
                )
        }

        val rootTree = ViewNode(
            packageName = targetPlatform.packageName,
            className = "android.widget.FrameLayout",
            isVisibleToUser = true,
            children = listOf(syntheticChild)
        )

        val detection = ServiceLocator.detectShortContentUseCase.execute(rootTree, platforms)
        val now = System.currentTimeMillis()

        if (!noScrollRepo.isMasterShieldEnabled.value) {
            _lastSimulationFeedback.value = InterceptionSimulationFeedback(
                timestampMillis = now,
                platformName = targetPlatform.name,
                matchedViewId = primaryDetector.value,
                wasBlocked = false,
                actionTaken = "BYPASSED",
                reason = "Master Shield is currently switched OFF"
            )
            return
        }

        if (!detection.isDetected || detection.matchedPlatform == null) {
            _lastSimulationFeedback.value = InterceptionSimulationFeedback(
                timestampMillis = now,
                platformName = targetPlatform.name,
                matchedViewId = primaryDetector.value,
                wasBlocked = false,
                actionTaken = "IGNORED",
                reason = "Platform '${targetPlatform.name}' is disabled in Platforms catalog"
            )
            return
        }

        val decision = ServiceLocator.processBlockActionUseCase.evaluate(
            platform = detection.matchedPlatform,
            activePlan = noScrollRepo.activePlan.value,
            dailyLimitConfig = noScrollRepo.dailyLimitConfig.value,
            curiousStatus = noScrollRepo.curiousStatus.value,
            pauseEndsAtMillis = noScrollRepo.pauseEndsAtMillis.value,
            nowMillis = now
        )

        when (decision) {
            is BlockDecision.Block -> {
                ServiceLocator.recordBlockHistoryUseCase.execute(
                    platform = decision.platform,
                    blockMode = decision.blockMode,
                    planWhenBlocked = decision.activePlan,
                    timestampMillis = now
                )
                _lastSimulationFeedback.value = InterceptionSimulationFeedback(
                    timestampMillis = now,
                    platformName = decision.platform.name,
                    matchedViewId = detection.matchedNodeId ?: primaryDetector.value,
                    wasBlocked = true,
                    actionTaken = decision.blockMode.shortLabel.uppercase(),
                    reason = decision.reason
                )
            }

            is BlockDecision.Allow -> {
                _lastSimulationFeedback.value = InterceptionSimulationFeedback(
                    timestampMillis = now,
                    platformName = targetPlatform.name,
                    matchedViewId = detection.matchedNodeId ?: primaryDetector.value,
                    wasBlocked = false,
                    actionTaken = "ALLOWED",
                    reason = decision.reason
                )
            }
        }
    }

    private data class Quintuple<A, B, C, D, E>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E
    )
}
