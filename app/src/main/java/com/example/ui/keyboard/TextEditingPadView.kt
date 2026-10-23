package com.example.ui.keyboard

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import com.example.data.KeyboardThemeOption

@Composable
fun TextEditingPadView(
    theme: KeyboardThemeOption,
    actionTarget: KeyboardActionTarget,
    hapticEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var isSelectMode by remember { mutableStateOf(false) }

    fun vibrate() {
        if (hapticEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
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
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Main 3-row grid with tall side arrows
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(174.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Left Tall Arrow (<)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .background(keyColor)
                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                    .clickable {
                        vibrate()
                        actionTarget.onMoveCursor(-1)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Move Left",
                    tint = textColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Middle Column: Up (^), Select, Down (v)
            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Up
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(keyColor)
                        .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                        .clickable {
                            vibrate()
                            actionTarget.onMoveCursorUp()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Move Up",
                        tint = textColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Select
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelectMode) accentColor.copy(alpha = 0.35f) else keyColor)
                        .border(
                            1.dp,
                            if (isSelectMode) accentColor else borderColor,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            vibrate()
                            isSelectMode = actionTarget.onToggleSelectMode()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Select",
                        color = if (isSelectMode) accentColor else textColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Down
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(keyColor)
                        .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                        .clickable {
                            vibrate()
                            actionTarget.onMoveCursorDown()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Move Down",
                        tint = textColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Right Tall Arrow (>)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .background(keyColor)
                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                    .clickable {
                        vibrate()
                        actionTarget.onMoveCursor(1)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Move Right",
                    tint = textColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Far-Right Column: Select All, Copy, Paste
            Column(
                modifier = Modifier
                    .weight(2.3f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Select All
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(keyColor)
                        .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                        .clickable {
                            vibrate()
                            actionTarget.onSelectAll()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Select All",
                        color = textColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Copy
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(keyColor)
                        .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                        .clickable {
                            vibrate()
                            actionTarget.onCopy()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Copy",
                        color = textColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Paste
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(keyColor)
                        .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                        .clickable {
                            vibrate()
                            actionTarget.onPaste()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Paste",
                        color = textColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Bottom Row: |< , >| , Backspace
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Start of text (|<)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .background(keyColor)
                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                    .clickable {
                        vibrate()
                        actionTarget.onMoveCursorToStart()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "|<",
                    color = textColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // End of text (>|)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .background(keyColor)
                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                    .clickable {
                        vibrate()
                        actionTarget.onMoveCursorToEnd()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ">|",
                    color = textColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Backspace ⌫
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .background(keyColor)
                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
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
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
