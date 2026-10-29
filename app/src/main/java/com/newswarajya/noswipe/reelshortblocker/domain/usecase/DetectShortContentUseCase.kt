package com.newswarajya.noswipe.reelshortblocker.domain.usecase

import com.newswarajya.noswipe.reelshortblocker.domain.model.DetectionResult
import com.newswarajya.noswipe.reelshortblocker.domain.model.DetectorType
import com.newswarajya.noswipe.reelshortblocker.domain.model.Platform
import com.newswarajya.noswipe.reelshortblocker.domain.model.PlatformDetector
import com.newswarajya.noswipe.reelshortblocker.domain.model.ViewNode

/**
 * Deep Module: Inspects a pure-Kotlin ViewNode hierarchy against the catalog of monitored
 * platforms and their view detectors (FINDBYID, VIEWID_RES_NAME, CONT_DESC, TEXT_CONTAINS).
 */
class DetectShortContentUseCase {

    fun execute(
        rootNode: ViewNode?,
        platforms: List<Platform>
    ): DetectionResult {
        if (rootNode == null) {
            return DetectionResult(isDetected = false)
        }

        val activePackage = rootNode.packageName
        if (activePackage.isBlank()) {
            return DetectionResult(isDetected = false)
        }

        // Filter to enabled platforms matching the active window's package name
        val candidatePlatforms = platforms.filter { platform ->
            platform.isEnabled && platform.packageName.equals(activePackage, ignoreCase = true)
        }

        if (candidatePlatforms.isEmpty()) {
            return DetectionResult(isDetected = false)
        }

        for (platform in candidatePlatforms) {
            for (detector in platform.detectors) {
                val matchedNode = findMatchingNode(rootNode, detector)
                if (matchedNode != null) {
                    return DetectionResult(
                        isDetected = true,
                        matchedPlatform = platform,
                        matchedDetector = detector,
                        matchedNodeId = matchedNode.viewIdResourceName ?: matchedNode.contentDescription
                    )
                }
            }
        }

        return DetectionResult(isDetected = false)
    }

    private fun findMatchingNode(
        node: ViewNode,
        detector: PlatformDetector
    ): ViewNode? {
        if (node.isVisibleToUser && matchesDetector(node, detector)) {
            return node
        }
        for (child in node.children) {
            val found = findMatchingNode(child, detector)
            if (found != null) {
                return found
            }
        }
        return null
    }

    private fun matchesDetector(
        node: ViewNode,
        detector: PlatformDetector
    ): Boolean {
        val target = detector.value.trim()
        if (target.isEmpty()) return false

        return when (detector.type) {
            DetectorType.FINDBYID -> {
                val viewId = node.viewIdResourceName ?: return false
                viewId.equals(target, ignoreCase = true) ||
                    viewId.endsWith(target, ignoreCase = true) ||
                    (target.contains(":id/") && viewId.substringAfter(":id/", "")
                        .equals(target.substringAfter(":id/", ""), ignoreCase = true))
            }

            DetectorType.VIEWID_RES_NAME -> {
                val viewId = node.viewIdResourceName ?: return false
                val resourceIdPart = viewId.substringAfter(":id/", viewId)
                resourceIdPart.equals(target, ignoreCase = true) ||
                    viewId.contains(target, ignoreCase = true)
            }

            DetectorType.CONT_DESC -> {
                val desc = node.contentDescription ?: return false
                desc.equals(target, ignoreCase = true) ||
                    desc.contains(target, ignoreCase = true)
            }

            DetectorType.TEXT_CONTAINS -> {
                val text = node.text ?: ""
                val desc = node.contentDescription ?: ""
                text.contains(target, ignoreCase = true) ||
                    desc.contains(target, ignoreCase = true)
            }
        }
    }
}
