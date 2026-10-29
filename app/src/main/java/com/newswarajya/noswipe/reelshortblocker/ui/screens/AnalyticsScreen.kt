package com.newswarajya.noswipe.reelshortblocker.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.newswarajya.noswipe.reelshortblocker.domain.model.AnalyticsSummary
import com.newswarajya.noswipe.reelshortblocker.ui.theme.AmberCaution
import com.newswarajya.noswipe.reelshortblocker.ui.theme.CyanFocusAccent
import com.newswarajya.noswipe.reelshortblocker.ui.theme.EmeraldShieldBright
import com.newswarajya.noswipe.reelshortblocker.ui.theme.EmeraldShieldPrimary
import com.newswarajya.noswipe.reelshortblocker.ui.theme.ObsidianCardBorder
import com.newswarajya.noswipe.reelshortblocker.ui.theme.ObsidianSurface
import com.newswarajya.noswipe.reelshortblocker.ui.theme.ObsidianSurfaceElevated
import com.newswarajya.noswipe.reelshortblocker.ui.theme.VioletCurious
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(
    summary: AnalyticsSummary,
    onNavigateBack: () -> Unit
) {
    BackHandler(onBack = onNavigateBack)

    val timeFormatter = remember { SimpleDateFormat("MMM d • HH:mm:ss", Locale.US) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Behavioral Audit & Analytics",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Backed by SQLite tables shorts_block_history & device_unlock_sessions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 4 Top KPI Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnalyticsKpiCard(
                        modifier = Modifier.weight(1f),
                        label = "BLOCKED TODAY",
                        value = "${summary.totalBlockedToday}",
                        subtext = "${summary.totalBlockedAllTime} all-time blocks",
                        accent = EmeraldShieldBright
                    )
                    AnalyticsKpiCard(
                        modifier = Modifier.weight(1f),
                        label = "RECLAIMED TODAY",
                        value = "${summary.estimatedMinutesSavedToday}m",
                        subtext = "${summary.estimatedMinutesSavedWeek}m saved this week",
                        accent = CyanFocusAccent
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnalyticsKpiCard(
                        modifier = Modifier.weight(1f),
                        label = "UNLOCK SESSIONS",
                        value = "${summary.activeUnlockSessionsToday}",
                        subtext = "Tracked via USER_PRESENT",
                        accent = VioletCurious
                    )
                    AnalyticsKpiCard(
                        modifier = Modifier.weight(1f),
                        label = "MINDFUL STREAK",
                        value = "${summary.currentStreakDays} Days",
                        subtext = "Zero uncontrolled binges",
                        accent = AmberCaution
                    )
                }
            }
        }

        // Weekly Interception Bar Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekly_bar_chart_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.dp, ObsidianCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "7-Day Interception Trend",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${summary.estimatedMinutesSavedWeek} mins reclaimed",
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldShieldBright
                        )
                    }

                    val maxCount = (summary.weeklyBreakdown.maxOfOrNull { it.blockedCount } ?: 1).coerceAtLeast(1)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        summary.weeklyBreakdown.forEach { point ->
                            val heightFraction = (point.blockedCount.toFloat() / maxCount.toFloat()).coerceIn(0.08f, 1f)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "${point.blockedCount}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = EmeraldShieldBright
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height((96 * heightFraction).dp)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    EmeraldShieldBright,
                                                    EmeraldShieldPrimary.copy(alpha = 0.45f)
                                                )
                                            )
                                        )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = point.dayLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Platform Breakdown Card
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
                        text = "Interceptions by Platform",
                        style = MaterialTheme.typography.titleMedium
                    )

                    summary.topPlatforms.forEach { stat ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stat.platformName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${stat.blockedCount} blocks (${(stat.percentageOfTotal * 100).toInt()}%)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CyanFocusAccent
                                )
                            }
                            LinearProgressIndicator(
                                progress = { stat.percentageOfTotal.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp),
                                color = CyanFocusAccent,
                                trackColor = ObsidianSurfaceElevated
                            )
                        }
                    }
                }
            }
        }

        // Recent SQLite Block Audit Log
        item {
            Text(
                text = "Recent SQLite Interception Log",
                style = MaterialTheme.typography.titleMedium
            )
        }

        items(summary.recentBlocks.take(12), key = { it.id.toString() + "_" + it.timestamp }) { record ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.dp, ObsidianCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = record.platformName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${record.packageName} • Session ${record.sessionId ?: "default"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = timeFormatter.format(Date(record.timestamp)),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        color = EmeraldShieldPrimary.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${record.blockMode.shortLabel} • ${record.planWhenBlocked.badge}",
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldShieldBright,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsKpiCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    subtext: String,
    accent: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = BorderStroke(1.dp, ObsidianCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                color = accent
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
