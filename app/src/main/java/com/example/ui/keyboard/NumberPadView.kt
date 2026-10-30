package com.example.ui.keyboard

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardLanguage
import com.example.data.KeyboardThemeOption

@Composable
fun NumberPadView(
    theme: KeyboardThemeOption,
    currentLanguage: KeyboardLanguage,
    actionTarget: KeyboardActionTarget,
    hapticEnabled: Boolean,
    onSwitchToLetters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var useNativeDigits by remember {
        mutableStateOf(currentLanguage == KeyboardLanguage.BANGLA || currentLanguage == KeyboardLanguage.HINDI)
    }

    fun vibrate() {
        if (hapticEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    val banglaDigits = listOf("০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯")
    val hindiDigits = listOf("०", "१", "२", "३", "४", "५", "६", "७", "८", "९")
    val standardDigits = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")

    val activeDigits = remember(useNativeDigits, currentLanguage) {
        if (useNativeDigits) {
            when (currentLanguage) {
                KeyboardLanguage.BANGLA -> banglaDigits
                KeyboardLanguage.HINDI -> hindiDigits
                KeyboardLanguage.ENGLISH -> standardDigits
            }
        } else {
            standardDigits
        }
    }

    val bgColor = Color(theme.backgroundHex)
    val keyColor = Color(theme.keyHex)
    val textColor = Color(theme.textHex)
    val accentColor = Color(theme.accentHex)
    val borderColor = if (theme == KeyboardThemeOption.LIGHT) Color(0xFFD1D5DB) else Color(0xFF374151)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // Top Row: 1 2 3 4 5 6 7 8 9 0
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").forEach { num ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(keyColor)
                        .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                        .clickable {
                            vibrate()
                            actionTarget.onCommitText(num)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = num,
                        color = textColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Row 2: + | 1 | 2 | 3 | %
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            NumKey(text = "+", modifier = Modifier.weight(1.1f), keyColor = keyColor, textColor = textColor, borderColor = borderColor) {
                vibrate()
                actionTarget.onCommitText("+")
            }
            NumKey(text = activeDigits[1], modifier = Modifier.weight(1.3f), keyColor = keyColor, textColor = textColor, borderColor = borderColor, isDigit = true) {
                vibrate()
                actionTarget.onCommitText(activeDigits[1])
            }
            NumKey(text = activeDigits[2], modifier = Modifier.weight(1.3f), keyColor = keyColor, textColor = textColor, borderColor = borderColor, isDigit = true) {
                vibrate()
                actionTarget.onCommitText(activeDigits[2])
            }
            NumKey(text = activeDigits[3], modifier = Modifier.weight(1.3f), keyColor = keyColor, textColor = textColor, borderColor = borderColor, isDigit = true) {
                vibrate()
                actionTarget.onCommitText(activeDigits[3])
            }
            NumKey(text = "%", modifier = Modifier.weight(1.1f), keyColor = keyColor, textColor = textColor, borderColor = borderColor) {
                vibrate()
                actionTarget.onCommitText("%")
            }
        }

        // Row 3: - | 4 | 5 | 6 | 123 (toggle digits)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            NumKey(text = "-", modifier = Modifier.weight(1.1f), keyColor = keyColor, textColor = textColor, borderColor = borderColor) {
                vibrate()
                actionTarget.onCommitText("-")
            }
            NumKey(text = activeDigits[4], modifier = Modifier.weight(1.3f), keyColor = keyColor, textColor = textColor, borderColor = borderColor, isDigit = true) {
                vibrate()
                actionTarget.onCommitText(activeDigits[4])
            }
            NumKey(text = activeDigits[5], modifier = Modifier.weight(1.3f), keyColor = keyColor, textColor = textColor, borderColor = borderColor, isDigit = true) {
                vibrate()
                actionTarget.onCommitText(activeDigits[5])
            }
            NumKey(text = activeDigits[6], modifier = Modifier.weight(1.3f), keyColor = keyColor, textColor = textColor, borderColor = borderColor, isDigit = true) {
                vibrate()
                actionTarget.onCommitText(activeDigits[6])
            }
            NumKey(text = if (useNativeDigits) "123" else "১/१", modifier = Modifier.weight(1.1f), keyColor = keyColor, textColor = accentColor, borderColor = borderColor) {
                vibrate()
                useNativeDigits = !useNativeDigits
            }
        }

        // Row 4: * | 7 | 8 | 9 | ⌫
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            NumKey(text = "*", modifier = Modifier.weight(1.1f), keyColor = keyColor, textColor = textColor, borderColor = borderColor) {
                vibrate()
                actionTarget.onCommitText("*")
            }
            NumKey(text = activeDigits[7], modifier = Modifier.weight(1.3f), keyColor = keyColor, textColor = textColor, borderColor = borderColor, isDigit = true) {
                vibrate()
                actionTarget.onCommitText(activeDigits[7])
            }
            NumKey(text = activeDigits[8], modifier = Modifier.weight(1.3f), keyColor = keyColor, textColor = textColor, borderColor = borderColor, isDigit = true) {
                vibrate()
                actionTarget.onCommitText(activeDigits[8])
            }
            NumKey(text = activeDigits[9], modifier = Modifier.weight(1.3f), keyColor = keyColor, textColor = textColor, borderColor = borderColor, isDigit = true) {
                vibrate()
                actionTarget.onCommitText(activeDigits[9])
            }
            Box(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(keyColor)
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                    .clickable {
                        vibrate()
                        actionTarget.onDeleteBackward()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = textColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Row 5: Switch Language/Letters ("অকখ" or "ABC") | . | 0 | ␣ | 🔍/↵
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Switch back to letters
            val letterLabel = when (currentLanguage) {
                KeyboardLanguage.BANGLA -> "অকখ"
                KeyboardLanguage.HINDI -> "अकख"
                KeyboardLanguage.ENGLISH -> "ABC"
            }
            NumKey(
                text = letterLabel,
                modifier = Modifier.weight(1.1f),
                keyColor = keyColor,
                textColor = textColor,
                borderColor = borderColor
            ) {
                vibrate()
                onSwitchToLetters()
            }

            NumKey(text = ".", modifier = Modifier.weight(1.3f), keyColor = keyColor, textColor = textColor, borderColor = borderColor) {
                vibrate()
                actionTarget.onCommitText(".")
            }

            NumKey(text = activeDigits[0], modifier = Modifier.weight(1.3f), keyColor = keyColor, textColor = textColor, borderColor = borderColor, isDigit = true) {
                vibrate()
                actionTarget.onCommitText(activeDigits[0])
            }

            // Space
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(keyColor)
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                    .clickable {
                        vibrate()
                        actionTarget.onCommitText(" ")
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "␣",
                    color = textColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Search / Enter action
            Box(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor)
                    .clickable {
                        vibrate()
                        actionTarget.onEnterAction()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search / Action",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun NumKey(
    text: String,
    modifier: Modifier = Modifier,
    keyColor: Color,
    textColor: Color,
    borderColor: Color,
    isDigit: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(keyColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = if (isDigit) 21.sp else 16.sp,
            fontWeight = if (isDigit) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}
