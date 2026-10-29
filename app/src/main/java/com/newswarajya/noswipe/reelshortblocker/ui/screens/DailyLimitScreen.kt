package com.newswarajya.noswipe.reelshortblocker.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.newswarajya.noswipe.reelshortblocker.domain.model.DailyLimitConfig
import com.newswarajya.noswipe.reelshortblocker.domain.model.EmojiBand
import com.newswarajya.noswipe.reelshortblocker.ui.theme.AmberCaution
import com.newswarajya.noswipe.reelshortblocker.ui.theme.CoralDanger
import com.newswarajya.noswipe.reelshortblocker.ui.theme.CyanFocusAccent
import com.newswarajya.noswipe.reelshortblocker.ui.theme.EmeraldShieldBright
import com.newswarajya.noswipe.reelshortblocker.ui.theme.EmeraldShieldPrimary
import com.newswarajya.noswipe.reelshortblocker.ui.theme.ObsidianCardBorder
import com.newswarajya.noswipe.reelshortblocker.ui.theme.ObsidianSurface
import com.newswarajya.noswipe.reelshortblocker.ui.theme.ObsidianSurfaceElevated
import kotlin.math.roundToInt

@Composable
fun DailyLimitScreen(
    config: DailyLimitConfig,
    onUpdateLimit: (Int, Boolean, Boolean) -> Unit,
    onSimulateMinutesUsed: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    BackHandler(onBack = onNavigateBack)

    val currentBand = config.emojiBand
    val bandColor = when (currentBand) {
        EmojiBand.MONK_MODE -> EmeraldShieldBright
        EmojiBand.LASER_FOCUS -> CyanFocusAccent
        EmojiBand.HEALTHY_BALANCE -> EmeraldShieldPrimary
        EmojiBand.SLIPPING_AWAY -> AmberCaution
        EmojiBand.DOPAMINE_OVERLOAD -> CoralDanger
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("daily_limit_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Scroll Limit",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Dynamic Emoji Band classification & hard-lock override",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = config.isEnabled,
                    onCheckedChange = { enabled ->
                        onUpdateLimit(config.dailyLimitMinutes, enabled, config.enforceHardLockOnExceed)
                    },
                    modifier = Modifier.testTag("daily_limit_enable_switch")
                )
            }
        }

        // Dynamic Emoji Band Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("emoji_band_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.5.dp, bandColor.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = currentBand.emoji,
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        text = "${config.dailyLimitMinutes} min / day",
                        style = MaterialTheme.typography.displayMedium,
                        color = bandColor
                    )
                    Surface(
                        color = bandColor.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = currentBand.formattedDisplay,
                            style = MaterialTheme.typography.titleMedium,
                            color = bandColor,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                    Text(
                        text = currentBand.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Slider(
                        value = config.dailyLimitMinutes.toFloat(),
                        onValueChange = { newVal ->
                            onUpdateLimit(
                                newVal.roundToInt(),
                                config.isEnabled,
                                config.enforceHardLockOnExceed
                            )
                        },
                        valueRange = 0f..300f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("daily_limit_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = bandColor,
                            activeTrackColor = bandColor
                        )
                    )

                    // Quick Bracket Preset Chips
                    val presets = listOf(
                        0 to "0m Monk",
                        15 to "15m Laser",
                        60 to "60m Balance",
                        150 to "150m Slipping",
                        250 to "250m Overload"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        presets.forEach { (mins, label) ->
                            FilterChip(
                                selected = config.dailyLimitMinutes == mins,
                                onClick = {
                                    onUpdateLimit(mins, config.isEnabled, config.enforceHardLockOnExceed)
                                },
                                label = {
                                    Text(label, style = MaterialTheme.typography.labelMedium)
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = bandColor.copy(alpha = 0.22f),
                                    selectedLabelColor = bandColor
                                )
                            )
                        }
                    }
                }
            }
        }

        // Today's Usage vs Allowance Progress Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.dp, ObsidianCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Scroll Exposure",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${config.usedMinutesToday}m / ${config.dailyLimitMinutes}m",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (config.isExceeded) CoralDanger else EmeraldShieldBright
                        )
                    }

                    val progress = if (config.dailyLimitMinutes <= 0) {
                        1f
                    } else {
                        (config.usedMinutesToday.toFloat() / config.dailyLimitMinutes.toFloat()).coerceIn(0f, 1f)
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        color = if (config.isExceeded) CoralDanger else bandColor,
                        trackColor = ObsidianSurfaceElevated
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onSimulateMinutesUsed(5) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("simulate_add_5m_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ObsidianSurfaceElevated,
                                contentColor = Color.White
                            )
                        ) {
                            Text("+5m Usage")
                        }
                        OutlinedButton(
                            onClick = { onSimulateMinutesUsed(-config.usedMinutesToday) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reset_daily_usage_button")
                        ) {
                            Text("Reset Today (0m)")
                        }
                    }
                }
            }
        }

        // Emoji Band Classification Reference Table
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.dp, ObsidianCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Emoji Band Brackets",
                        style = MaterialTheme.typography.titleMedium
                    )
                    EmojiBand.entries.forEach { band ->
                        val isCurrent = band == currentBand
                        Surface(
                            color = if (isCurrent) ObsidianSurfaceElevated else Color.Transparent,
                            shape = RoundedCornerShape(12.dp),
                            border = if (isCurrent) BorderStroke(1.dp, bandColor) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = band.formattedDisplay,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) bandColor else MaterialTheme.colorScheme.onSurface
                                )
                                val rangeLabel = when (band) {
                                    EmojiBand.MONK_MODE -> "0 min"
                                    EmojiBand.LASER_FOCUS -> "1–30 min"
                                    EmojiBand.HEALTHY_BALANCE -> "31–120 min"
                                    EmojiBand.SLIPPING_AWAY -> "121–240 min"
                                    EmojiBand.DOPAMINE_OVERLOAD -> "241+ min"
                                }
                                Text(
                                    text = rangeLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
