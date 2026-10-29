package com.newswarajya.noswipe.reelshortblocker.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.newswarajya.noswipe.reelshortblocker.domain.repository.SecuritySettings
import com.newswarajya.noswipe.reelshortblocker.service.deviceadmin.NSDeviceAdminReceiver
import com.newswarajya.noswipe.reelshortblocker.ui.activities.pinblockoverlay.PinBlockOverlayActivity
import com.newswarajya.noswipe.reelshortblocker.ui.theme.AmberCaution
import com.newswarajya.noswipe.reelshortblocker.ui.theme.CyanFocusAccent
import com.newswarajya.noswipe.reelshortblocker.ui.theme.EmeraldShieldBright
import com.newswarajya.noswipe.reelshortblocker.ui.theme.EmeraldShieldPrimary
import com.newswarajya.noswipe.reelshortblocker.ui.theme.ObsidianCardBorder
import com.newswarajya.noswipe.reelshortblocker.ui.theme.ObsidianSurface

@Composable
fun SettingsScreen(
    settings: SecuritySettings,
    onUpdatePin: (Boolean, String) -> Unit,
    onToggleBiometric: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onTogglePersistentNotification: (Boolean) -> Unit,
    onRefreshPermissions: () -> Unit,
    onNavigateBack: () -> Unit
) {
    BackHandler(onBack = onNavigateBack)

    val context = LocalContext.current
    var pinInput by remember(settings.pinCode) { mutableStateOf(settings.pinCode) }
    var pinSavedBanner by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
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
                        text = "System Permissions & Security",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Accessibility Service, Device Admin, 4-Digit PIN & Haptics",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = onRefreshPermissions,
                    modifier = Modifier.testTag("refresh_permissions_button"),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Refresh", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // 1. OS Capability & Permission Cards
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
                        text = "Declared OS Capabilities",
                        style = MaterialTheme.typography.titleMedium
                    )

                    PermissionActionRow(
                        title = "Accessibility Service (NoScrollAccessibilityService)",
                        subtitle = "Inspects window view trees for Reels/Shorts view IDs in real time",
                        isGranted = settings.isAccessibilityServiceActive,
                        actionLabel = "Configure",
                        onClick = {
                            try {
                                context.startActivity(
                                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                )
                            } catch (_: Exception) {
                            }
                        }
                    )

                    PermissionActionRow(
                        title = "Anti-Uninstall Device Admin (NSDeviceAdminReceiver)",
                        subtitle = "Enables immediate screen lock action & prevents impulsive removal",
                        isGranted = settings.isAntiTamperAdminEnabled,
                        actionLabel = "Activate",
                        onClick = {
                            try {
                                val comp = ComponentName(context, NSDeviceAdminReceiver::class.java)
                                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, comp)
                                    putExtra(
                                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                        "NoScroll uses Device Admin to lock the screen when LOCK_SCREEN block mode is selected."
                                    )
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                            }
                        }
                    )

                    PermissionActionRow(
                        title = "System Alert Overlay (SYSTEM_ALERT_WINDOW)",
                        subtitle = "Displays the 4-digit PIN lock barrier over blocked short-video apps",
                        isGranted = settings.isOverlayPermissionGranted,
                        actionLabel = "Grant",
                        onClick = {
                            try {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            } catch (_: Exception) {
                            }
                        }
                    )

                    PermissionActionRow(
                        title = "Unrestricted Battery (IGNORE_BATTERY_OPTIMIZATIONS)",
                        subtitle = "Prevents OEM background killers from suspending real-time detection",
                        isGranted = settings.isBatteryOptimizationIgnored,
                        actionLabel = "Allow",
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                            }
                        }
                    )
                }
            }
        }

        // 2. 4-Digit PIN Security & Overlay Test Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pin_security_card"),
                shape = RoundedCornerShape(20.dp),
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "4-Digit PIN Security Barrier",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Require PIN verification on PinBlockOverlayActivity before changing plans",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.isPinEnabled,
                            onCheckedChange = { enabled ->
                                onUpdatePin(enabled, pinInput)
                            },
                            modifier = Modifier.testTag("pin_enable_switch")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { value ->
                                pinInput = value.filter { it.isDigit() }.take(4)
                                pinSavedBanner = false
                            },
                            label = { Text("4-Digit PIN") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pin_code_input")
                        )

                        Button(
                            onClick = {
                                if (pinInput.length == 4) {
                                    onUpdatePin(true, pinInput)
                                    pinSavedBanner = true
                                }
                            },
                            modifier = Modifier.testTag("save_pin_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldShieldPrimary,
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Save PIN", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (pinSavedBanner) {
                        Text(
                            text = "PIN code updated (${pinInput}) and armed.",
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldShieldBright
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(context, PinBlockOverlayActivity::class.java).apply {
                                putExtra(PinBlockOverlayActivity.EXTRA_PLATFORM_NAME, "YouTube Shorts")
                                putExtra(PinBlockOverlayActivity.EXTRA_REASON, "Strict Shield Interception Verification")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("launch_pin_overlay_button")
                    ) {
                        Text("Preview Full-Screen PIN Block Overlay")
                    }
                }
            }
        }

        // 3. Feedback & Foreground Service Preferences
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
                        text = "Feedback & Telemetry",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Haptic Interception Pulse", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Vibrate (65ms) whenever a short-form feed is intercepted",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.isHapticFeedbackEnabled,
                            onCheckedChange = onToggleHaptics,
                            modifier = Modifier.testTag("haptic_feedback_switch")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Biometric Fingerprint Fallback", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Allow USE_BIOMETRIC verification on PIN overlay screen",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.isBiometricUnlockEnabled,
                            onCheckedChange = onToggleBiometric
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Persistent Shield Notification", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Run AppMonitorService foreground status notification",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.isPersistentNotificationEnabled,
                            onCheckedChange = onTogglePersistentNotification
                        )
                    }
                }
            }
        }

        // 4. Clean-Room Build Provenance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.dp, ObsidianCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "NoScroll Clean-Room Architecture v2.1.2 (Build 116)",
                        style = MaterialTheme.typography.titleMedium,
                        color = CyanFocusAccent
                    )
                    Text(
                        text = "Package: com.newswarajya.noswipe.reelshortblocker",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "34 Monitored Platforms • Deep-Module Domain Layer • SQLite Audit Tables",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionActionRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    actionLabel: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Surface(
                    color = if (isGranted) {
                        EmeraldShieldPrimary.copy(alpha = 0.2f)
                    } else {
                        AmberCaution.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (isGranted) "ACTIVE" else "READY",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isGranted) EmeraldShieldBright else AmberCaution,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedButton(
            onClick = onClick,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(actionLabel, style = MaterialTheme.typography.labelMedium)
        }
    }
}
