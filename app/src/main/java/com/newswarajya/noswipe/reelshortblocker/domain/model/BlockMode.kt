package com.newswarajya.noswipe.reelshortblocker.domain.model

enum class BlockMode(
    val title: String,
    val shortLabel: String,
    val description: String
) {
    PRESS_BACK(
        title = "Navigate Back",
        shortLabel = "Press Back",
        description = "Performs a soft back action to exit the Reels/Shorts player while keeping the host app open."
    ),
    KILL_APP(
        title = "Exit to Home",
        shortLabel = "Kill App",
        description = "Immediately returns to the home launcher screen to break the dopamine loop."
    ),
    LOCK_SCREEN(
        title = "Lock Device Screen",
        shortLabel = "Lock Screen",
        description = "Locks the device immediately via Device Admin or shows the PIN barrier overlay."
    );

    companion object {
        fun fromName(value: String?): BlockMode {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PRESS_BACK
        }
    }
}
