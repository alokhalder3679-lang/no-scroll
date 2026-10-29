package com.newswarajya.noswipe.reelshortblocker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.newswarajya.noswipe.reelshortblocker.domain.model.DetectionPlan
import com.newswarajya.noswipe.reelshortblocker.ui.theme.AmberCaution
import com.newswarajya.noswipe.reelshortblocker.ui.theme.CoralDanger
import com.newswarajya.noswipe.reelshortblocker.ui.theme.CyanFocusAccent
import com.newswarajya.noswipe.reelshortblocker.ui.theme.EmeraldShieldBright
import com.newswarajya.noswipe.reelshortblocker.ui.theme.EmeraldShieldPrimary
import com.newswarajya.noswipe.reelshortblocker.ui.theme.ObsidianCardBorder
import com.newswarajya.noswipe.reelshortblocker.ui.theme.ObsidianSurface
import com.newswarajya.noswipe.reelshortblocker.ui.theme.ObsidianSurfaceElevated
import com.newswarajya.noswipe.reelshortblocker.ui.theme.VioletCurious
import com.newswarajya.noswipe.reelshortblocker.ui.viewmodel.HomeUiState
import com.newswarajya.noswipe.reelshortblocker.ui.viewmodel.NoScrollDestination

@Composable
fun DashboardScreen(
    uiState: HomeUiState,
    onToggleMasterShield: (Boolean) -> Unit,
    onSelectPlan: (DetectionPlan) -> Unit,
    onNavigate: (NoScrollDestination) -> Unit,
    onTriggerLiveInterceptionTest: (String) -> Unit,
    onTogglePlatformQuick: (String, Boolean) -> Unit
) {
    var selectedTestPlatformId by remember { mutableStateOf("youtube_shorts") }

    val pulseTransition = rememberInfiniteTransition(label = "shield_pulse")
    val pulseRadiusFraction by pulseTransition.animateFloat(
        initialValue = 0.84f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_radius"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Shield Status Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("master_shield_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = if (uiState.isMasterShieldEnabled) {
                        EmeraldShieldPrimary.copy(alpha = 0.55f)
                    } else {
                        CoralDanger.copy(alpha = 0.45f)
                    }
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.radialGradient(
                                colors = if (uiState.isMasterShieldEnabled) {
                                    listOf(
                                        EmeraldShieldPrimary.copy(alpha = 0.22f),
                                        ObsidianSurface
                                    )
                                } else {
                                    listOf(
                                        CoralDanger.copy(alpha = 0.18f),
                                        ObsidianSurface
                                    )
                                }
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(64.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val ringColor = if (uiState.isMasterShieldEnabled) {
                                            EmeraldShieldBright
                                        } else {
                                            CoralDanger
                                        }
                                        drawCircle(
                                            color = ringColor.copy(alpha = 0.18f),
                                            radius = (size.minDimension / 2f) * pulseRadiusFraction
                                        )
                                        drawCircle(
                                            color = ringColor.copy(alpha = 0.5f),
                                            radius = (size.minDimension / 2f) * 0.78f,
                                            style = Stroke(width = 2.dp.toPx())
                                        )
                                    }
                                    Icon(
                                        imageVector = if (uiState.isMasterShieldEnabled) {
                                            Icons.Default.Shield
                                        } else {
                                            Icons.Default.GppBad
                                        },
                                        contentDescription = "Master Shield Icon",
                                        tint = if (uiState.isMasterShieldEnabled) {
                                            EmeraldShieldBright
                                        } else {
                                            CoralDanger
                                        },
                                        modifier = Modifier.size(30.dp)
                                    )
                                }

                                Column {
                                    Surface(
                                        color = if (uiState.isMasterShieldEnabled) {
                                            EmeraldShieldPrimary.copy(alpha = 0.18f)
                                        } else {
                                            CoralDanger.copy(alpha = 0.18f)
                                        },
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = if (uiState.isMasterShieldEnabled) {
                                                "SHIELD ARMED • ${uiState.activePlan.badge}"
                                            } else {
                                                "SHIELD DISARMED"
                                            },
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (uiState.isMasterShieldEnabled) {
                                                EmeraldShieldBright
                                            } else {
                                                CoralDanger
                                            },
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (uiState.isMasterShieldEnabled) {
                                            "Real-Time Reel Blocker"
                                        } else {
                                            "Interception Paused"
                                        },
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val enabledCount = uiState.platforms.count { it.isEnabled }
                                    Text(
                                        text = "$enabledCount of ${uiState.platforms.size} platforms monitored",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = uiState.isMasterShieldEnabled,
                                onCheckedChange = onToggleMasterShield,
                                modifier = Modifier.testTag("master_shield_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = EmeraldShieldPrimary
                                )
                            )
                        }

                        // Quick Metrics Strip inside Hero
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HeroMetricPill(
                                modifier = Modifier.weight(1f),
                                label = "BLOCKED TODAY",
                                value = "${uiState.analyticsSummary.totalBlockedToday}",
                                accentColor = EmeraldShieldBright
                            )
                            HeroMetricPill(
                                modifier = Modifier.weight(1f),
                                label = "TIME RECLAIMED",
                                value = "${uiState.analyticsSummary.estimatedMinutesSavedToday}m",
                                accentColor = CyanFocusAccent
                            )
                            HeroMetricPill(
                                modifier = Modifier.weight(1f),
                                label = "DAILY BAND",
                                value = uiState.dailyLimitConfig.emojiBand.emoji + " ${uiState.dailyLimitConfig.dailyLimitMinutes}m",
                                accentColor = AmberCaution
                            )
                        }
                    }
                }
            }
        }

        // 2. 4 Protection Plans Selector (BLOCK_ALL, ONE_REEL, CURIOUS, PAUSED)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Protection Plan",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = uiState.activePlan.badge,
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldShieldBright
                    )
                }

                DetectionPlan.entries.forEach { plan ->
                    val isSelected = uiState.activePlan == plan
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectPlan(plan) }
                            .testTag("plan_card_${plan.name.lowercase()}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                ObsidianSurfaceElevated
                            } else {
                                ObsidianSurface
                            }
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) EmeraldShieldPrimary else ObsidianCardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = plan.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (isSelected) EmeraldShieldBright else MaterialTheme.colorScheme.onSurface
                                    )
                                    Surface(
                                        color = if (isSelected) {
                                            EmeraldShieldPrimary.copy(alpha = 0.22f)
                                        } else {
                                            ObsidianCardBorder.copy(alpha = 0.5f)
                                        },
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = plan.badge,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isSelected) EmeraldShieldBright else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = plan.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (plan == DetectionPlan.PAUSED && isSelected && uiState.pauseRemainingSeconds > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Active Pause Countdown: ${uiState.pauseRemainingSeconds / 60}m ${uiState.pauseRemainingSeconds % 60}s left",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = AmberCaution
                                    )
                                }
                                if (plan == DetectionPlan.CURIOUS && isSelected) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val cStatus = uiState.curiousStatus
                                    val statusLine = when {
                                        cStatus.isInActiveSession(uiState.nowMillis) ->
                                            "Micro-Session Active: ${cStatus.remainingSessionSeconds(uiState.nowMillis)}s remaining"
                                        cStatus.isInCooldown(uiState.nowMillis) ->
                                            "Cooldown Enforced: ${cStatus.remainingCooldownSeconds(uiState.nowMillis)}s remaining"
                                        else ->
                                            "${cStatus.config.sessionDurationMinutes}m session / ${cStatus.config.cooldownDurationMinutes}m cooldown"
                                    }
                                    Text(
                                        text = statusLine,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = VioletCurious
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isSelected) EmeraldShieldBright else ObsidianCardBorder,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Quick Wellbeing Modules Row (Daily Limit, Curious Mode, Mindful Pause)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Wellbeing Modules",
                    style = MaterialTheme.typography.titleLarge
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickModuleCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_daily_limit_button"),
                        title = "Daily Limit",
                        subtitle = uiState.dailyLimitConfig.emojiBand.formattedDisplay,
                        icon = Icons.Default.Tune,
                        accent = AmberCaution,
                        onClick = { onNavigate(NoScrollDestination.LIMITS) }
                    )
                    QuickModuleCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_curious_mode_button"),
                        title = "Curious Mode",
                        subtitle = "${uiState.curiousStatus.config.sessionDurationMinutes}m / ${uiState.curiousStatus.config.cooldownDurationMinutes}m",
                        icon = Icons.Default.Timer,
                        accent = VioletCurious,
                        onClick = { onNavigate(NoScrollDestination.CURIOUS) }
                    )
                    QuickModuleCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_mindful_pause_button"),
                        title = "Mindful Breath",
                        subtitle = "${uiState.securitySettings.mindfulBreathDurationSeconds}s Cycle",
                        icon = Icons.Default.SelfImprovement,
                        accent = CyanFocusAccent,
                        onClick = { onNavigate(NoScrollDestination.MINDFUL) }
                    )
                }
            }
        }

        // 4. Interactive Accessibility Tree Detection Lab (Real UseCase Execution)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("interception_lab_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.dp, ObsidianCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = CyanFocusAccent
                        )
                        Column {
                            Text(
                                text = "Live View-Tree Interception Lab",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Dispatch a real ViewNode hierarchy through DetectShortContentUseCase & ProcessBlockActionUseCase",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    val quickTargets = listOf(
                        "youtube_shorts" to "YouTube Shorts",
                        "instagram_reels" to "Instagram Reels",
                        "tiktok" to "TikTok",
                        "facebook_reels" to "Facebook Reels",
                        "snapchat_spotlight" to "Snapchat Spotlight"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickTargets.forEach { (id, label) ->
                            FilterChip(
                                selected = selectedTestPlatformId == id,
                                onClick = { selectedTestPlatformId = id },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanFocusAccent.copy(alpha = 0.22f),
                                    selectedLabelColor = CyanFocusAccent
                                )
                            )
                        }
                    }

                    Button(
                        onClick = { onTriggerLiveInterceptionTest(selectedTestPlatformId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("simulate_interception_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldShieldPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Test Window Tree Interception Now",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    AnimatedVisibility(visible = uiState.lastSimulationFeedback != null) {
                        uiState.lastSimulationFeedback?.let { feedback ->
                            Surface(
                                color = if (feedback.wasBlocked) {
                                    EmeraldShieldPrimary.copy(alpha = 0.14f)
                                } else {
                                    AmberCaution.copy(alpha = 0.14f)
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (feedback.wasBlocked) EmeraldShieldPrimary else AmberCaution
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "${feedback.actionTaken} • ${feedback.platformName}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (feedback.wasBlocked) EmeraldShieldBright else AmberCaution
                                    )
                                    Text(
                                        text = "Matched Node: ${feedback.matchedViewId}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = feedback.reason,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Top Monitored Platforms Quick Toggles
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.dp, ObsidianCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Primary Monitored Feeds",
                            style = MaterialTheme.typography.titleMedium
                        )
                        OutlinedButton(
                            onClick = { onNavigate(NoScrollDestination.PLATFORMS) },
                            modifier = Modifier.testTag("view_all_platforms_button"),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "All 34 Apps",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    uiState.platforms.take(5).forEach { platform ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = platform.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${platform.packageName} • ${platform.currentBlockMode.shortLabel}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = platform.isEnabled,
                                onCheckedChange = { onTogglePlatformQuick(platform.id, it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroMetricPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accentColor: Color
) {
    Surface(
        modifier = modifier,
        color = ObsidianSurfaceElevated,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, ObsidianCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = accentColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun QuickModuleCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = BorderStroke(1.dp, ObsidianCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = accent,
                maxLines = 1
            )
        }
    }
}
