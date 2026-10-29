package com.newswarajya.noswipe.reelshortblocker.ui.activities.pinblockoverlay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.newswarajya.noswipe.reelshortblocker.data.ServiceLocator
import com.newswarajya.noswipe.reelshortblocker.ui.theme.CoralDanger
import com.newswarajya.noswipe.reelshortblocker.ui.theme.EmeraldShieldBright
import com.newswarajya.noswipe.reelshortblocker.ui.theme.EmeraldShieldPrimary
import com.newswarajya.noswipe.reelshortblocker.ui.theme.NoScrollTheme
import com.newswarajya.noswipe.reelshortblocker.ui.theme.ObsidianBackground
import com.newswarajya.noswipe.reelshortblocker.ui.theme.ObsidianCardBorder
import com.newswarajya.noswipe.reelshortblocker.ui.theme.ObsidianSurface
import com.newswarajya.noswipe.reelshortblocker.ui.theme.ObsidianSurfaceElevated

/**
 * 4-Digit PIN security barrier and full-screen lock activity launched when short-form feeds
 * are intercepted while PIN security is enabled.
 */
class PinBlockOverlayActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ServiceLocator.init(applicationContext)
        enableEdgeToEdge()

        val platformName = intent?.getStringExtra(EXTRA_PLATFORM_NAME) ?: "Short-Video Feed"
        val reason = intent?.getStringExtra(EXTRA_REASON) ?: "Strict Shield Interception"

        setContent {
            NoScrollTheme {
                PinBlockOverlayScreen(
                    platformName = platformName,
                    reason = reason,
                    onVerifyPin = { enteredPin ->
                        ServiceLocator.settingsRepository.verifyPin(enteredPin)
                    },
                    onDismissUnlocked = { finish() }
                )
            }
        }
    }

    companion object {
        const val EXTRA_PLATFORM_NAME = "extra_platform_name"
        const val EXTRA_REASON = "extra_block_reason"
    }
}

@Composable
fun PinBlockOverlayScreen(
    platformName: String,
    reason: String,
    onVerifyPin: (String) -> Boolean,
    onDismissUnlocked: () -> Unit
) {
    var enteredDigits by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun appendDigit(digit: String) {
        if (enteredDigits.length < 4) {
            val next = enteredDigits + digit
            enteredDigits = next
            errorMessage = null
            if (next.length == 4) {
                if (onVerifyPin(next)) {
                    onDismissUnlocked()
                } else {
                    errorMessage = "Incorrect 4-digit PIN (Default: 2580)"
                    enteredDigits = ""
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(20.dp)
            .testTag("pin_block_overlay_screen"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
            border = BorderStroke(1.5.dp, EmeraldShieldPrimary.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(EmeraldShieldPrimary.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "PIN Barrier",
                        tint = EmeraldShieldBright,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Text(
                    text = "$platformName Blocked",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // 4 PIN Dots Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val filled = index < enteredDigits.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    if (filled) EmeraldShieldBright else ObsidianSurfaceElevated
                                )
                        )
                    }
                }

                errorMessage?.let { msg ->
                    Surface(
                        color = CoralDanger.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.labelMedium,
                            color = CoralDanger,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Numeric Keypad
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9")
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    rows.forEach { rowDigits ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            rowDigits.forEach { digit ->
                                OutlinedButton(
                                    onClick = { appendDigit(digit) },
                                    modifier = Modifier
                                        .size(68.dp)
                                        .testTag("pin_key_$digit"),
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, ObsidianCardBorder)
                                ) {
                                    Text(
                                        text = digit,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.size(68.dp))
                        OutlinedButton(
                            onClick = { appendDigit("0") },
                            modifier = Modifier
                                .size(68.dp)
                                .testTag("pin_key_0"),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, ObsidianCardBorder)
                        ) {
                            Text(
                                text = "0",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                if (enteredDigits.isNotEmpty()) {
                                    enteredDigits = enteredDigits.dropLast(1)
                                }
                            },
                            modifier = Modifier
                                .size(68.dp)
                                .testTag("pin_key_backspace"),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, ObsidianCardBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Backspace,
                                contentDescription = "Backspace",
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onDismissUnlocked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("return_to_focus_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldShieldPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "I Choose Focus — Close Feed",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
