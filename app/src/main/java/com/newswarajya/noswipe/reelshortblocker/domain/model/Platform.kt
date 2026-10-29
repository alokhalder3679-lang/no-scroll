package com.newswarajya.noswipe.reelshortblocker.domain.model

enum class DetectorType {
    FINDBYID,
    VIEWID_RES_NAME,
    CONT_DESC,
    TEXT_CONTAINS;

    companion object {
        fun fromName(value: String?): DetectorType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: FINDBYID
        }
    }
}

data class PlatformDetector(
    val type: DetectorType,
    val value: String
)

data class Platform(
    val id: String,
    val name: String,
    val category: String,
    val packageName: String,
    val defaultBlockMode: BlockMode,
    val currentBlockMode: BlockMode = defaultBlockMode,
    val isEnabled: Boolean = true,
    val detectors: List<PlatformDetector> = emptyList(),
    val blockedCount: Int = 0
)

/**
 * Pure Kotlin representation of an Accessibility view hierarchy node.
 * Keeps the domain layer 100% free of Android SDK dependencies for fast unit testing.
 */
data class ViewNode(
    val packageName: String,
    val viewIdResourceName: String? = null,
    val text: String? = null,
    val contentDescription: String? = null,
    val className: String? = null,
    val isVisibleToUser: Boolean = true,
    val children: List<ViewNode> = emptyList()
)

data class DetectionResult(
    val isDetected: Boolean,
    val matchedPlatform: Platform? = null,
    val matchedDetector: PlatformDetector? = null,
    val matchedNodeId: String? = null
)
