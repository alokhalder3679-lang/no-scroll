package com.newswarajya.noswipe.reelshortblocker.ui.activities.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.newswarajya.noswipe.reelshortblocker.service.monitor.AppMonitorService
import com.newswarajya.noswipe.reelshortblocker.ui.screens.AnalyticsScreen
import com.newswarajya.noswipe.reelshortblocker.ui.screens.CuriousModeScreen
import com.newswarajya.noswipe.reelshortblocker.ui.screens.DailyLimitScreen
import com.newswarajya.noswipe.reelshortblocker.ui.screens.DashboardScreen
import com.newswarajya.noswipe.reelshortblocker.ui.screens.MindfulPauseScreen
import com.newswarajya.noswipe.reelshortblocker.ui.screens.PlatformsScreen
import com.newswarajya.noswipe.reelshortblocker.ui.screens.SettingsScreen
import com.newswarajya.noswipe.reelshortblocker.ui.theme.CoralDanger
import com.newswarajya.noswipe.reelshortblocker.ui.theme.EmeraldShieldBright
import com.newswarajya.noswipe.reelshortblocker.ui.theme.EmeraldShieldPrimary
import com.newswarajya.noswipe.reelshortblocker.ui.theme.NoScrollTheme
import com.newswarajya.noswipe.reelshortblocker.ui.theme.ObsidianBackground
import com.newswarajya.noswipe.reelshortblocker.ui.theme.ObsidianSurface
import com.newswarajya.noswipe.reelshortblocker.ui.viewmodel.HomeViewModel
import com.newswarajya.noswipe.reelshortblocker.ui.viewmodel.NoScrollDestination

class HomeActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            startService(Intent(this, AppMonitorService::class.java))
        } catch (_: Exception) {
        }

        setContent {
            NoScrollTheme {
                NoScrollRootScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions()
    }
}

private data class NavTabItem(
    val destination: NoScrollDestination,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

private val primaryTabs = listOf(
    NavTabItem(NoScrollDestination.DASHBOARD, "Shield", Icons.Default.Shield, "nav_tab_dashboard"),
    NavTabItem(NoScrollDestination.PLATFORMS, "Platforms", Icons.Default.Apps, "nav_tab_platforms"),
    NavTabItem(NoScrollDestination.LIMITS, "Limits", Icons.Default.Tune, "nav_tab_limits"),
    NavTabItem(NoScrollDestination.MINDFUL, "Mindful", Icons.Default.SelfImprovement, "nav_tab_mindful"),
    NavTabItem(NoScrollDestination.ANALYTICS, "Analytics", Icons.Default.Analytics, "nav_tab_analytics"),
    NavTabItem(NoScrollDestination.SETTINGS, "Settings", Icons.Default.Settings, "nav_tab_settings")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoScrollRootScreen(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
    ) {
        val isExpandedScreen = maxWidth >= 640.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = ObsidianBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "NoScroll Logo",
                                tint = EmeraldShieldBright,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "NoScroll",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = "v2.1.2 • Clean-Room Reel Blocker",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        Surface(
                            color = if (uiState.isMasterShieldEnabled) {
                                EmeraldShieldPrimary.copy(alpha = 0.2f)
                            } else {
                                CoralDanger.copy(alpha = 0.2f)
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(
                                text = if (uiState.isMasterShieldEnabled) {
                                    "● ${uiState.activePlan.badge}"
                                } else {
                                    "○ OFF"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (uiState.isMasterShieldEnabled) {
                                    EmeraldShieldBright
                                } else {
                                    CoralDanger
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ObsidianSurface
                    )
                )
            },
            bottomBar = {
                if (!isExpandedScreen) {
                    NavigationBar(
                        containerColor = ObsidianSurface,
                        tonalElevation = 8.dp
                    ) {
                        primaryTabs.forEach { item ->
                            val isSelected = currentDestination == item.destination ||
                                (currentDestination == NoScrollDestination.CURIOUS && item.destination == NoScrollDestination.LIMITS)
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.navigateTo(item.destination) },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1
                                    )
                                },
                                modifier = Modifier.testTag(item.testTag),
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = EmeraldShieldBright,
                                    selectedTextColor = EmeraldShieldBright,
                                    indicatorColor = EmeraldShieldPrimary.copy(alpha = 0.2f)
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isExpandedScreen) {
                    NavigationRail(
                        containerColor = ObsidianSurface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        primaryTabs.forEach { item ->
                            val isSelected = currentDestination == item.destination ||
                                (currentDestination == NoScrollDestination.CURIOUS && item.destination == NoScrollDestination.LIMITS)
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { viewModel.navigateTo(item.destination) },
                                icon = {
                                    Icon(imageVector = item.icon, contentDescription = item.label)
                                },
                                label = { Text(item.label) },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentDestination) {
                        NoScrollDestination.DASHBOARD -> DashboardScreen(
                            uiState = uiState,
                            onToggleMasterShield = viewModel::setMasterShield,
                            onSelectPlan = { plan -> viewModel.selectDetectionPlan(plan) },
                            onNavigate = viewModel::navigateTo,
                            onTriggerLiveInterceptionTest = viewModel::triggerLiveTreeInterceptionTest,
                            onTogglePlatformQuick = viewModel::togglePlatform
                        )

                        NoScrollDestination.PLATFORMS -> PlatformsScreen(
                            platforms = uiState.platforms,
                            searchQuery = uiState.searchQuery,
                            selectedCategory = uiState.selectedCategory,
                            onSearchQueryChange = viewModel::setSearchQuery,
                            onCategoryChange = viewModel::setSelectedCategory,
                            onTogglePlatform = viewModel::togglePlatform,
                            onBlockModeChange = viewModel::updatePlatformBlockMode,
                            onEnableAll = viewModel::enableAllPlatforms,
                            onNavigateBack = { viewModel.navigateTo(NoScrollDestination.DASHBOARD) }
                        )

                        NoScrollDestination.LIMITS -> DailyLimitScreen(
                            config = uiState.dailyLimitConfig,
                            onUpdateLimit = viewModel::updateDailyLimit,
                            onSimulateMinutesUsed = viewModel::addSimulatedScrollMinutes,
                            onNavigateBack = { viewModel.navigateTo(NoScrollDestination.DASHBOARD) }
                        )

                        NoScrollDestination.CURIOUS -> CuriousModeScreen(
                            curiousStatus = uiState.curiousStatus,
                            nowMillis = uiState.nowMillis,
                            onUpdateConfig = viewModel::updateCuriousConfiguration,
                            onStartSession = viewModel::startCuriousMicroSession,
                            onEndSessionIntoCooldown = viewModel::endCuriousSessionEarly,
                            onNavigateBack = { viewModel.navigateTo(NoScrollDestination.DASHBOARD) }
                        )

                        NoScrollDestination.MINDFUL -> MindfulPauseScreen(
                            activePlan = uiState.activePlan,
                            pauseRemainingSeconds = uiState.pauseRemainingSeconds,
                            quotes = uiState.mindfulQuotes,
                            activeQuoteIndex = uiState.activeQuoteIndex,
                            onNextQuote = viewModel::nextMindfulQuote,
                            onStartMindfulPause = viewModel::startMindfulPauseTimer,
                            onResumeStrictProtection = {
                                viewModel.selectDetectionPlan(
                                    com.newswarajya.noswipe.reelshortblocker.domain.model.DetectionPlan.BLOCK_ALL
                                )
                            },
                            onNavigateBack = { viewModel.navigateTo(NoScrollDestination.DASHBOARD) }
                        )

                        NoScrollDestination.ANALYTICS -> AnalyticsScreen(
                            summary = uiState.analyticsSummary,
                            onNavigateBack = { viewModel.navigateTo(NoScrollDestination.DASHBOARD) }
                        )

                        NoScrollDestination.SETTINGS -> SettingsScreen(
                            settings = uiState.securitySettings,
                            onUpdatePin = viewModel::updatePinSettings,
                            onToggleBiometric = viewModel::setBiometricUnlock,
                            onToggleHaptics = viewModel::setHapticFeedback,
                            onTogglePersistentNotification = viewModel::setPersistentNotification,
                            onRefreshPermissions = viewModel::refreshPermissions,
                            onNavigateBack = { viewModel.navigateTo(NoScrollDestination.DASHBOARD) }
                        )
                    }
                }
            }
        }
    }
}
