package com.newswarajya.noswipe.reelshortblocker.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.newswarajya.noswipe.reelshortblocker.data.datasource.MindfulQuote
import com.newswarajya.noswipe.reelshortblocker.domain.model.DetectionPlan
import com.newswarajya.noswipe.reelshortblocker.ui.theme.AmberCaution
import com.newswarajya.noswipe.reelshortblocker.ui.theme.CyanFocusAccent
import com.newswarajya.noswipe.reelshortblocker.ui.theme.CyanFocusSoft
import com.newswarajya.noswipe.reelshortblocker.ui.theme.EmeraldShieldBright
import com.newswarajya.noswipe.reelshortblocker.ui.theme.EmeraldShieldPrimary
import com.newswarajya.noswipe.reelshortblocker.ui.theme.ObsidianCardBorder
import com.newswarajya.noswipe.reelshortblocker.ui.theme.ObsidianSurface

@Composable
fun MindfulPauseScreen(
    activePlan: DetectionPlan,
    pauseRemainingSeconds: Long,
    quotes: List<MindfulQuote>,
    activeQuoteIndex: Int,
    onNextQuote: () -> Unit,
    onStartMindfulPause: (Int) -> Unit,
    onResumeStrictProtection: () -> Unit,
    onNavigateBack: () -> Unit
) {
    BackHandler(onBack = onNavigateBack)

    var selectedPauseMinutes by remember { mutableIntStateOf(10) }

    val infiniteTransition = rememberInfiniteTransition(label = "mindful_breathing")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.48f,
        targetValue = 0.96f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )

    val phaseText = when {
        breathScale < 0.64f -> "Inhale Slowly..."
        breathScale > 0.84f -> "Hold Gently..."
        else -> "Release & Soften..."
    }

    val currentQuote = quotes.getOrNull(activeQuoteIndex) ?: MindfulQuote(
        quote = "Between stimulus and response there is a space. In that space is our power to choose our response.",
        author = "Viktor E. Frankl",
        category = "Self-Mastery"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("mindful_pause_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Mindful Breathing & Pause",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Interrupt unconscious scrolling impulses with deliberate physiological calm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Animated Breathing Sanctuary Circle
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("breathing_circle_card"),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.dp, CyanFocusAccent.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier.size(210.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val maxRadius = size.minDimension / 2f
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        CyanFocusAccent.copy(alpha = 0.32f),
                                        EmeraldShieldPrimary.copy(alpha = 0.08f),
                                        Color.Transparent
                                    )
                                ),
                                radius = maxRadius * breathScale
                            )
                            drawCircle(
                                color = CyanFocusSoft.copy(alpha = 0.65f),
                                radius = maxRadius * breathScale * 0.78f,
                                style = Stroke(width = 2.5.dp.toPx())
                            )
                            drawCircle(
                                color = EmeraldShieldBright.copy(alpha = 0.35f),
                                radius = maxRadius * 0.92f,
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = phaseText,
                                style = MaterialTheme.typography.titleLarge,
                                color = CyanFocusSoft
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "4-4-4 Box Rhythm",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (activePlan == DetectionPlan.PAUSED && pauseRemainingSeconds > 0) {
                        Surface(
                            color = AmberCaution.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, AmberCaution)
                        ) {
                            Text(
                                text = "Pause Active: ${pauseRemainingSeconds / 60}m ${pauseRemainingSeconds % 60}s remaining",
                                style = MaterialTheme.typography.labelLarge,
                                color = AmberCaution,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Rotating Quote Card from mindful_timer_quotes.json
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mindful_quote_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.dp, ObsidianCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = EmeraldShieldPrimary.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = currentQuote.category.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = EmeraldShieldBright,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        OutlinedButton(
                            onClick = onNextQuote,
                            modifier = Modifier.testTag("next_quote_button"),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Next Quote",
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = " Next Wisdom",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    Text(
                        text = "“${currentQuote.quote}”",
                        style = MaterialTheme.typography.titleLarge,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Start
                    )

                    Text(
                        text = "— ${currentQuote.author}",
                        style = MaterialTheme.typography.labelLarge,
                        color = CyanFocusSoft
                    )
                }
            }
        }

        // Bounded Pause Timer Controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.dp, ObsidianCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Intentional Pause Window",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Temporarily suspend short-video interception for a strict countdown window.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val durationOptions = listOf(5, 10, 15, 30)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        durationOptions.forEach { mins ->
                            FilterChip(
                                selected = selectedPauseMinutes == mins,
                                onClick = { selectedPauseMinutes = mins },
                                label = { Text("${mins}m") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanFocusAccent.copy(alpha = 0.22f),
                                    selectedLabelColor = CyanFocusSoft
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onStartMindfulPause(selectedPauseMinutes) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("start_pause_timer_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanFocusAccent,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = "Start ${selectedPauseMinutes}m Pause",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = onResumeStrictProtection,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("resume_strict_shield_button")
                        ) {
                            Text("Resume Strict Shield")
                        }
                    }
                }
            }
        }
    }
}
