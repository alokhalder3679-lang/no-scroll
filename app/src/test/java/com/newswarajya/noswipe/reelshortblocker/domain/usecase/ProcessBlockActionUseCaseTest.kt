package com.newswarajya.noswipe.reelshortblocker.domain.usecase

import com.newswarajya.noswipe.reelshortblocker.domain.model.BlockMode
import com.newswarajya.noswipe.reelshortblocker.domain.model.CuriousConfig
import com.newswarajya.noswipe.reelshortblocker.domain.model.CuriousStatus
import com.newswarajya.noswipe.reelshortblocker.domain.model.DailyLimitConfig
import com.newswarajya.noswipe.reelshortblocker.domain.model.DetectionPlan
import com.newswarajya.noswipe.reelshortblocker.domain.model.Platform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProcessBlockActionUseCaseTest {

    private lateinit var useCase: ProcessBlockActionUseCase
    private lateinit var youtubePlatform: Platform
    private lateinit var defaultDailyLimit: DailyLimitConfig
    private lateinit var defaultCurious: CuriousStatus

    @Before
    fun setUp() {
        useCase = ProcessBlockActionUseCase()
        youtubePlatform = Platform(
            id = "youtube_shorts",
            name = "YouTube Shorts",
            category = "Video & Streaming",
            packageName = "com.google.android.youtube",
            defaultBlockMode = BlockMode.PRESS_BACK,
            currentBlockMode = BlockMode.PRESS_BACK,
            isEnabled = true
        )
        defaultDailyLimit = DailyLimitConfig(
            isEnabled = true,
            dailyLimitMinutes = 30,
            usedMinutesToday = 5,
            enforceHardLockOnExceed = true
        )
        defaultCurious = CuriousStatus(
            config = CuriousConfig(isEnabled = true, sessionDurationMinutes = 5, cooldownDurationMinutes = 25),
            activeSessionEndsAtMillis = 0L,
            cooldownEndsAtMillis = 0L
        )
    }

    @Test
    fun `BLOCK_ALL plan immediately blocks content`() {
        val decision = useCase.evaluate(
            platform = youtubePlatform,
            activePlan = DetectionPlan.BLOCK_ALL,
            dailyLimitConfig = defaultDailyLimit,
            curiousStatus = defaultCurious,
            pauseEndsAtMillis = 0L,
            nowMillis = 100_000L
        )

        assertTrue(decision is BlockDecision.Block)
        val block = decision as BlockDecision.Block
        assertEquals(BlockMode.PRESS_BACK, block.blockMode)
        assertEquals(DetectionPlan.BLOCK_ALL, block.activePlan)
    }

    @Test
    fun `ONE_REEL plan allows first reel and blocks second reel`() {
        val firstDecision = useCase.evaluate(
            platform = youtubePlatform,
            activePlan = DetectionPlan.ONE_REEL,
            dailyLimitConfig = defaultDailyLimit,
            curiousStatus = defaultCurious,
            pauseEndsAtMillis = 0L,
            nowMillis = 100_000L
        )
        assertTrue(firstDecision is BlockDecision.Allow)

        val secondDecision = useCase.evaluate(
            platform = youtubePlatform,
            activePlan = DetectionPlan.ONE_REEL,
            dailyLimitConfig = defaultDailyLimit,
            curiousStatus = defaultCurious,
            pauseEndsAtMillis = 0L,
            nowMillis = 105_000L
        )
        assertTrue(secondDecision is BlockDecision.Block)
        assertEquals(DetectionPlan.ONE_REEL, (secondDecision as BlockDecision.Block).activePlan)
    }

    @Test
    fun `PAUSED plan allows content while timer is active and blocks when expired`() {
        val now = 200_000L
        val pauseEnds = 260_000L

        val activeDecision = useCase.evaluate(
            platform = youtubePlatform,
            activePlan = DetectionPlan.PAUSED,
            dailyLimitConfig = defaultDailyLimit,
            curiousStatus = defaultCurious,
            pauseEndsAtMillis = pauseEnds,
            nowMillis = now
        )
        assertTrue(activeDecision is BlockDecision.Allow)

        val expiredDecision = useCase.evaluate(
            platform = youtubePlatform,
            activePlan = DetectionPlan.PAUSED,
            dailyLimitConfig = defaultDailyLimit,
            curiousStatus = defaultCurious,
            pauseEndsAtMillis = pauseEnds,
            nowMillis = 270_000L
        )
        assertTrue(expiredDecision is BlockDecision.Block)
    }

    @Test
    fun `CURIOUS plan allows during active session and blocks during cooldown`() {
        val now = 500_000L
        val activeCurious = defaultCurious.copy(
            activeSessionEndsAtMillis = 560_000L,
            cooldownEndsAtMillis = 800_000L
        )

        val sessionDecision = useCase.evaluate(
            platform = youtubePlatform,
            activePlan = DetectionPlan.CURIOUS,
            dailyLimitConfig = defaultDailyLimit,
            curiousStatus = activeCurious,
            pauseEndsAtMillis = 0L,
            nowMillis = now
        )
        assertTrue(sessionDecision is BlockDecision.Allow)

        val cooldownDecision = useCase.evaluate(
            platform = youtubePlatform,
            activePlan = DetectionPlan.CURIOUS,
            dailyLimitConfig = defaultDailyLimit,
            curiousStatus = activeCurious,
            pauseEndsAtMillis = 0L,
            nowMillis = 600_000L
        )
        assertTrue(cooldownDecision is BlockDecision.Block)
    }

    @Test
    fun `daily limit exceeded forces block even if otherwise permitted`() {
        val exceededLimit = DailyLimitConfig(
            isEnabled = true,
            dailyLimitMinutes = 20,
            usedMinutesToday = 25,
            enforceHardLockOnExceed = true
        )

        val decisionDuringPause = useCase.evaluate(
            platform = youtubePlatform,
            activePlan = DetectionPlan.PAUSED,
            dailyLimitConfig = exceededLimit,
            curiousStatus = defaultCurious,
            pauseEndsAtMillis = 999_999L,
            nowMillis = 100_000L
        )

        assertTrue(decisionDuringPause is BlockDecision.Block)
    }
}
