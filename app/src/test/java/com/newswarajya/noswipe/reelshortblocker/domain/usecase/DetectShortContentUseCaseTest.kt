package com.newswarajya.noswipe.reelshortblocker.domain.usecase

import com.newswarajya.noswipe.reelshortblocker.domain.model.BlockMode
import com.newswarajya.noswipe.reelshortblocker.domain.model.DetectorType
import com.newswarajya.noswipe.reelshortblocker.domain.model.Platform
import com.newswarajya.noswipe.reelshortblocker.domain.model.PlatformDetector
import com.newswarajya.noswipe.reelshortblocker.domain.model.ViewNode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DetectShortContentUseCaseTest {

    private lateinit var useCase: DetectShortContentUseCase
    private lateinit var samplePlatforms: List<Platform>

    @Before
    fun setUp() {
        useCase = DetectShortContentUseCase()
        samplePlatforms = listOf(
            Platform(
                id = "youtube_shorts",
                name = "YouTube Shorts",
                category = "Video & Streaming",
                packageName = "com.google.android.youtube",
                defaultBlockMode = BlockMode.PRESS_BACK,
                isEnabled = true,
                detectors = listOf(
                    PlatformDetector(
                        type = DetectorType.FINDBYID,
                        value = "com.google.android.youtube:id/reel_player_underlay"
                    ),
                    PlatformDetector(
                        type = DetectorType.FINDBYID,
                        value = "com.google.android.youtube:id/progress"
                    )
                )
            ),
            Platform(
                id = "facebook_reels",
                name = "Facebook Reels / Video",
                category = "Social Networks",
                packageName = "com.facebook.katana",
                defaultBlockMode = BlockMode.PRESS_BACK,
                isEnabled = true,
                detectors = listOf(
                    PlatformDetector(
                        type = DetectorType.CONT_DESC,
                        value = "Add a comment…"
                    )
                )
            )
        )
    }

    @Test
    fun `detects YouTube Shorts when reel_player_underlay is present`() {
        val rootNode = ViewNode(
            packageName = "com.google.android.youtube",
            className = "android.widget.FrameLayout",
            children = listOf(
                ViewNode(
                    packageName = "com.google.android.youtube",
                    className = "android.view.ViewGroup",
                    children = listOf(
                        ViewNode(
                            packageName = "com.google.android.youtube",
                            viewIdResourceName = "com.google.android.youtube:id/reel_player_underlay",
                            isVisibleToUser = true
                        )
                    )
                )
            )
        )

        val result = useCase.execute(rootNode, samplePlatforms)
        assertTrue(result.isDetected)
        assertNotNull(result.matchedPlatform)
        assertEquals("youtube_shorts", result.matchedPlatform?.id)
    }

    @Test
    fun `detects Facebook Reels via content description`() {
        val rootNode = ViewNode(
            packageName = "com.facebook.katana",
            className = "android.widget.FrameLayout",
            children = listOf(
                ViewNode(
                    packageName = "com.facebook.katana",
                    contentDescription = "Add a comment…",
                    isVisibleToUser = true
                )
            )
        )

        val result = useCase.execute(rootNode, samplePlatforms)
        assertTrue(result.isDetected)
        assertEquals("facebook_reels", result.matchedPlatform?.id)
    }

    @Test
    fun `does not detect when platform is disabled`() {
        val disabledPlatforms = samplePlatforms.map { it.copy(isEnabled = false) }
        val rootNode = ViewNode(
            packageName = "com.google.android.youtube",
            viewIdResourceName = "com.google.android.youtube:id/reel_player_underlay",
            isVisibleToUser = true
        )

        val result = useCase.execute(rootNode, disabledPlatforms)
        assertFalse(result.isDetected)
        assertNull(result.matchedPlatform)
    }

    @Test
    fun `does not detect on ordinary non-short views`() {
        val rootNode = ViewNode(
            packageName = "com.google.android.youtube",
            className = "android.widget.FrameLayout",
            children = listOf(
                ViewNode(
                    packageName = "com.google.android.youtube",
                    viewIdResourceName = "com.google.android.youtube:id/watch_player",
                    text = "Long-form Documentary Lecture",
                    isVisibleToUser = true
                )
            )
        )

        val result = useCase.execute(rootNode, samplePlatforms)
        assertFalse(result.isDetected)
        assertNull(result.matchedPlatform)
    }
}
