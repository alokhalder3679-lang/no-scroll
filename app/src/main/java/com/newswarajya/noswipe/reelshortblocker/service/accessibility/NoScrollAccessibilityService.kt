package com.newswarajya.noswipe.reelshortblocker.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.newswarajya.noswipe.reelshortblocker.data.ServiceLocator
import com.newswarajya.noswipe.reelshortblocker.domain.model.BlockMode
import com.newswarajya.noswipe.reelshortblocker.domain.model.ViewNode
import com.newswarajya.noswipe.reelshortblocker.domain.usecase.BlockDecision
import com.newswarajya.noswipe.reelshortblocker.service.deviceadmin.NSDeviceAdminReceiver
import com.newswarajya.noswipe.reelshortblocker.ui.activities.pinblockoverlay.PinBlockOverlayActivity

/**
 * Real-time AccessibilityService that inspects the active window hierarchy, evaluates
 * short-video detection rules via DetectShortContentUseCase, and executes the user's
 * chosen BlockMode (GLOBAL_ACTION_BACK, GLOBAL_ACTION_HOME, or DevicePolicyManager.lockNow).
 */
class NoScrollAccessibilityService : AccessibilityService() {

    private var lastBlockTimestampMillis: Long = 0L
    private var lastPackageVisited: String = ""

    override fun onServiceConnected() {
        super.onServiceConnected()
        ServiceLocator.init(applicationContext)
        ServiceLocator.refreshSystemPermissions(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        ServiceLocator.init(applicationContext)

        val repo = ServiceLocator.noScrollRepository
        if (!repo.isMasterShieldEnabled.value) return

        val eventPackage = event.packageName?.toString() ?: return
        if (eventPackage == applicationContext.packageName) return

        // Reset 1-Reel grace counter when user switches between different host apps
        if (eventPackage != lastPackageVisited) {
            lastPackageVisited = eventPackage
            ServiceLocator.processBlockActionUseCase.resetOneReelGrace()
        }

        // Throttle rapid consecutive accessibility events within 900ms after a block action
        val now = System.currentTimeMillis()
        if (now - lastBlockTimestampMillis < 900L) return

        val rootNodeInfo = rootInActiveWindow ?: event.source ?: return
        val domainTree = mapNodeInfoToDomain(rootNodeInfo, eventPackage, depth = 0, maxDepth = 10)

        val detection = ServiceLocator.detectShortContentUseCase.execute(
            rootNode = domainTree,
            platforms = repo.getPlatformsSnapshot()
        )

        if (!detection.isDetected || detection.matchedPlatform == null) {
            return
        }

        val decision = ServiceLocator.processBlockActionUseCase.evaluate(
            platform = detection.matchedPlatform,
            activePlan = repo.activePlan.value,
            dailyLimitConfig = repo.dailyLimitConfig.value,
            curiousStatus = repo.curiousStatus.value,
            pauseEndsAtMillis = repo.pauseEndsAtMillis.value,
            nowMillis = now
        )

        if (decision is BlockDecision.Block) {
            lastBlockTimestampMillis = now
            executeBlockAction(decision)
        }
    }

    private fun executeBlockAction(decision: BlockDecision.Block) {
        val settings = ServiceLocator.settingsRepository.settings.value

        if (settings.isHapticFeedbackEnabled) {
            triggerHapticPulse()
        }

        ServiceLocator.recordBlockHistoryUseCase.execute(
            platform = decision.platform,
            blockMode = decision.blockMode,
            planWhenBlocked = decision.activePlan
        )

        when (decision.blockMode) {
            BlockMode.PRESS_BACK -> {
                performGlobalAction(GLOBAL_ACTION_BACK)
            }

            BlockMode.KILL_APP -> {
                performGlobalAction(GLOBAL_ACTION_HOME)
            }

            BlockMode.LOCK_SCREEN -> {
                val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
                val adminComponent = ComponentName(this, NSDeviceAdminReceiver::class.java)
                when {
                    dpm != null && dpm.isAdminActive(adminComponent) -> {
                        dpm.lockNow()
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                        performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                    }
                    else -> {
                        performGlobalAction(GLOBAL_ACTION_HOME)
                    }
                }
            }
        }

        if (settings.isPinEnabled) {
            val overlayIntent = Intent(this, PinBlockOverlayActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra(PinBlockOverlayActivity.EXTRA_PLATFORM_NAME, decision.platform.name)
                putExtra(PinBlockOverlayActivity.EXTRA_REASON, decision.reason)
            }
            startActivity(overlayIntent)
        }
    }

    private fun triggerHapticPulse() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(65L, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(65L)
            }
        } catch (_: Exception) {
        }
    }

    private fun mapNodeInfoToDomain(
        node: AccessibilityNodeInfo,
        fallbackPackage: String,
        depth: Int,
        maxDepth: Int
    ): ViewNode {
        val childrenList = mutableListOf<ViewNode>()
        if (depth < maxDepth) {
            val count = node.childCount.coerceAtMost(35)
            for (i in 0 until count) {
                val child = node.getChild(i) ?: continue
                childrenList.add(mapNodeInfoToDomain(child, fallbackPackage, depth + 1, maxDepth))
            }
        }
        return ViewNode(
            packageName = node.packageName?.toString() ?: fallbackPackage,
            viewIdResourceName = node.viewIdResourceName,
            text = node.text?.toString(),
            contentDescription = node.contentDescription?.toString(),
            className = node.className?.toString(),
            isVisibleToUser = node.isVisibleToUser,
            children = childrenList
        )
    }

    override fun onInterrupt() {
        ServiceLocator.refreshSystemPermissions(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        ServiceLocator.refreshSystemPermissions(applicationContext)
    }
}
