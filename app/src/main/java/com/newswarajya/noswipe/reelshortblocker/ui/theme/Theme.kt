package com.newswarajya.noswipe.reelshortblocker.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val NoScrollDarkColorScheme = darkColorScheme(
    primary = EmeraldShieldPrimary,
    onPrimary = ObsidianBackground,
    primaryContainer = EmeraldShieldContainer,
    onPrimaryContainer = EmeraldShieldBright,
    secondary = CyanFocusAccent,
    onSecondary = ObsidianBackground,
    secondaryContainer = ObsidianSurfaceElevated,
    onSecondaryContainer = CyanFocusSoft,
    tertiary = VioletCurious,
    onTertiary = TextPrimaryDark,
    tertiaryContainer = VioletCuriousContainer,
    onTertiaryContainer = TextPrimaryDark,
    error = CoralDanger,
    onError = TextPrimaryDark,
    errorContainer = CoralDangerContainer,
    onErrorContainer = TextPrimaryDark,
    background = ObsidianBackground,
    onBackground = TextPrimaryDark,
    surface = ObsidianSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = ObsidianSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = ObsidianCardBorder
)

val NoScrollShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun NoScrollTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NoScrollDarkColorScheme,
        typography = NoScrollTypography,
        shapes = NoScrollShapes,
        content = content
    )
}
