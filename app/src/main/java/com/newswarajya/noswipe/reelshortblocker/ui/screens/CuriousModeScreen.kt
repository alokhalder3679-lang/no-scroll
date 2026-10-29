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
import com.newswarajya.noswipe.reelshortblocker.domain.model.CuriousStatus
import com.newswarajya.noswipe.reelshortblocker.ui.theme.AmberCaution
import com.newswarajya.noswipe.reelshortblocker.ui.theme.EmeraldShieldBright
import com.newswarajya.noswipe.reelshortblocker.ui.theme.ObsidianCardBorder
import com.newswarajya.noswipe.reelshortblocker.ui.theme.ObsidianSurface
import com.newswarajya.noswipe.reelshortblocker.ui.theme.VioletCurious
import kotlin.math.roundToInt

@Composable
fun CuriousModeScreen(
    curiousStatus: CuriousStatus,
    nowMillis: Long,
    onUpdateConfig: (Int, Int, Int, Boolean) -> Unit,
    onStartSession: () -> Unit,
    onEndSessionIntoCooldown: () -> Unit,
    onNavigateBack: () -> Unit
) {
    BackHandler(onBack = onNavigateBack)

    val config = curiousStatus.config
    val inSession = curiousStatus.isInActiveSession(nowMillis)
    val inCooldown = curiousStatus.isInCooldown(nowMillis)
    val sessionRemSec = curiousStatus.remainingSessionSeconds(nowMillis)
    val cooldownRemSec = curiousStatus.remainingCooldownSeconds(nowMillis)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("curious_mode_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Curious Micro-Sessions",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Intentional bounded windows followed by mandatory dopamine cooldowns",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Live Session / Cooldown Status Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("curious_status_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(
                    1.5.dp,
                    when {
                        inSession -> EmeraldShieldBright
                        inCooldown -> AmberCaution
                        else -> VioletCurious
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = VioletCurious.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = when {
                                inSession -> "ACTIVE MICRO-SESSION"
                                inCooldown -> "MANDATORY COOLDOWN LOCK"
                                else -> "READY FOR INTENTIONAL SESSION"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = when {
                                inSession -> EmeraldShieldBright
                                inCooldown -> AmberCaution
                                else -> VioletCurious
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = when {
                            inSession -> "${sessionRemSec / 60}m ${sessionRemSec % 60}s left in session"
                            inCooldown -> "${cooldownRemSec / 60}m ${cooldownRemSec % 60}s cooldown remaining"
                            else -> "${config.sessionDurationMinutes}m Window • ${config.cooldownDurationMinutes}m Cooldown"
                        },
                        style = MaterialTheme.typography.headlineLarge
                    )

                    Text(
                        text = "Sessions completed today: ${curiousStatus.sessionsCompletedToday} / ${config.maxDailySessions}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onStartSession,
                            enabled = !inSession && !inCooldown,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("start_curious_session_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VioletCurious,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Start Micro-Session", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onEndSessionIntoCooldown,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("enter_cooldown_button")
                        ) {
                            Text("Force Cooldown Now")
                        }
                    }
                }
            }
        }

        // Configuration Sliders
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.dp, ObsidianCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Session Parameters",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Micro-Session Length", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "${config.sessionDurationMinutes} min",
                                style = MaterialTheme.typography.labelLarge,
                                color = VioletCurious
                            )
                        }
                        Slider(
                            value = config.sessionDurationMinutes.toFloat(),
                            onValueChange = { v ->
                                onUpdateConfig(
                                    v.roundToInt(),
                                    config.cooldownDurationMinutes,
                                    config.maxDailySessions,
                                    config.requireMindfulBreathBeforeStart
                                )
                            },
                            valueRange = 1f..20f,
                            colors = SliderDefaults.colors(
                                thumbColor = VioletCurious,
                                activeTrackColor = VioletCurious
                            )
                        )
                    }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Mandatory Cooldown Duration", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "${config.cooldownDurationMinutes} min",
                                style = MaterialTheme.typography.labelLarge,
                                color = AmberCaution
                            )
                        }
                        Slider(
                            value = config.cooldownDurationMinutes.toFloat(),
                            onValueChange = { v ->
                                onUpdateConfig(
                                    config.sessionDurationMinutes,
                                    v.roundToInt(),
                                    config.maxDailySessions,
                                    config.requireMindfulBreathBeforeStart
                                )
                            },
                            valueRange = 5f..120f,
                            colors = SliderDefaults.colors(
                                thumbColor = AmberCaution,
                                activeTrackColor = AmberCaution
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Require Mindful Breath Before Session",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Prompt a 12-second breathing cycle before unlocking Curious Mode",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = config.requireMindfulBreathBeforeStart,
                            onCheckedChange = { req ->
                                onUpdateConfig(
                                    config.sessionDurationMinutes,
                                    config.cooldownDurationMinutes,
                                    config.maxDailySessions,
                                    req
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
