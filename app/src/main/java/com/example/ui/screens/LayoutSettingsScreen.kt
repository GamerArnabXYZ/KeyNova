package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.DeleteHoldMode
import com.example.data.KeyboardPreferences
import com.example.data.OneHandedMode
import kotlinx.coroutines.withTimeoutOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayoutSettingsScreen(
    keyboardPreferences: KeyboardPreferences,
    onNavigateBack: () -> Unit
) {
    var showNumberRow by remember { mutableStateOf(keyboardPreferences.showNumberRow) }
    var keyHeight by remember { mutableFloatStateOf(keyboardPreferences.keyHeightDp.toFloat()) }
    var keySpacing by remember { mutableFloatStateOf(keyboardPreferences.keySpacingDp.toFloat()) }
    var haptic by remember { mutableStateOf(keyboardPreferences.hapticFeedback) }
    var sound by remember { mutableStateOf(keyboardPreferences.soundFeedback) }
    var oneHanded by remember { mutableStateOf(keyboardPreferences.oneHandedMode) }
    var deleteMode by remember { mutableStateOf(keyboardPreferences.deleteHoldMode) }
    var deleteInitialDelay by remember { mutableFloatStateOf(keyboardPreferences.deleteInitialDelayMs.toFloat()) }
    var deleteRepeatInterval by remember { mutableFloatStateOf(keyboardPreferences.deleteRepeatIntervalMs.toFloat()) }
    var deleteHaptic by remember { mutableStateOf(keyboardPreferences.deleteHapticOnRepeat) }
    var sampleTestText by remember { mutableStateOf("The quick brown fox jumps over the lazy dog") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.layout_settings_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF0F172A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Dedicated Number Row",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Display 1 2 3 4 5 6 7 8 9 0 at the top of QWERTY",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Switch(
                                checked = showNumberRow,
                                onCheckedChange = {
                                    showNumberRow = it
                                    keyboardPreferences.showNumberRow = it
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2563EB))
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Key Height",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "${keyHeight.toInt()} dp",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                        }

                        Slider(
                            value = keyHeight,
                            onValueChange = {
                                keyHeight = it
                                keyboardPreferences.keyHeightDp = it.toInt()
                            },
                            valueRange = 38f..64f,
                            steps = 12,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF2563EB),
                                activeTrackColor = Color(0xFF2563EB)
                            )
                        )

                        // Height preview box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(keyHeight.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Preview Key Height (${keyHeight.toInt()} dp)",
                                color = Color(0xFF2563EB),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Key Spacing",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "${keySpacing.toInt()} dp",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                        }

                        Slider(
                            value = keySpacing,
                            onValueChange = {
                                keySpacing = it
                                keyboardPreferences.keySpacingDp = it.toInt()
                            },
                            valueRange = 2f..8f,
                            steps = 5,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF2563EB),
                                activeTrackColor = Color(0xFF2563EB)
                            )
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Vibration / Haptic Feedback",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Gentle tactile click on each keypress",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Switch(
                                checked = haptic,
                                onCheckedChange = {
                                    haptic = it
                                    keyboardPreferences.hapticFeedback = it
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2563EB))
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "One-Handed Mode",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Shrink keyboard towards thumb reach",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                FilterChip(
                                    selected = oneHanded == OneHandedMode.OFF,
                                    onClick = {
                                        oneHanded = OneHandedMode.OFF
                                        keyboardPreferences.oneHandedMode = OneHandedMode.OFF
                                    },
                                    label = { Text("Off", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = oneHanded == OneHandedMode.LEFT,
                                    onClick = {
                                        oneHanded = OneHandedMode.LEFT
                                        keyboardPreferences.oneHandedMode = OneHandedMode.LEFT
                                    },
                                    label = { Text("Left", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = oneHanded == OneHandedMode.RIGHT,
                                    onClick = {
                                        oneHanded = OneHandedMode.RIGHT
                                        keyboardPreferences.oneHandedMode = OneHandedMode.RIGHT
                                    },
                                    label = { Text("Right", fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Delete Button Auto-Remove",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Customize hold behavior: auto-remove words one-by-one, trigger delay, and speed",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )

                        // Mode Selector: Word by Word vs Character by Character
                        Text(
                            text = "Hold Action Mode",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = deleteMode == DeleteHoldMode.WORD_BY_WORD,
                                onClick = {
                                    deleteMode = DeleteHoldMode.WORD_BY_WORD
                                    keyboardPreferences.deleteHoldMode = DeleteHoldMode.WORD_BY_WORD
                                },
                                label = {
                                    Text("Word by Word (Auto-remove words)", fontSize = 12.sp)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = deleteMode == DeleteHoldMode.CHARACTER_BY_CHARACTER,
                                onClick = {
                                    deleteMode = DeleteHoldMode.CHARACTER_BY_CHARACTER
                                    keyboardPreferences.deleteHoldMode = DeleteHoldMode.CHARACTER_BY_CHARACTER
                                },
                                label = {
                                    Text("Character by Character", fontSize = 12.sp)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

                        // Trigger Delay Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Hold Delay Before Trigger",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "How long to press before continuous deletion begins",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Text(
                                text = "${deleteInitialDelay.toInt()} ms",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                        }

                        Slider(
                            value = deleteInitialDelay,
                            onValueChange = {
                                deleteInitialDelay = it
                                keyboardPreferences.deleteInitialDelayMs = it.toInt()
                            },
                            valueRange = 150f..750f,
                            steps = 11,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF2563EB),
                                activeTrackColor = Color(0xFF2563EB)
                            )
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

                        // Repeat Speed / Interval Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-Remove Speed (Interval)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Lower ms = Faster auto-removal of words/chars",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Text(
                                text = "${deleteRepeatInterval.toInt()} ms (${if (deleteRepeatInterval < 80) "Very Fast" else if (deleteRepeatInterval < 130) "Fast" else if (deleteRepeatInterval < 200) "Normal" else "Slow"})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                        }

                        Slider(
                            value = deleteRepeatInterval,
                            onValueChange = {
                                deleteRepeatInterval = it
                                keyboardPreferences.deleteRepeatIntervalMs = it.toInt()
                            },
                            valueRange = 40f..300f,
                            steps = 12,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF2563EB),
                                activeTrackColor = Color(0xFF2563EB)
                            )
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

                        // Vibration Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Haptic Click on Auto-Remove",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Tactile vibration pulse on each deleted word/character",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Switch(
                                checked = deleteHaptic,
                                onCheckedChange = {
                                    deleteHaptic = it
                                    keyboardPreferences.deleteHapticOnRepeat = it
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2563EB))
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

                        // Interactive Test Playground Box
                        Text(
                            text = "Interactive Live Test",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = if (sampleTestText.isEmpty()) "[Empty - click Reset to test again]" else sampleTestText,
                                fontSize = 13.5.sp,
                                color = if (sampleTestText.isEmpty()) Color(0xFF94A3B8) else Color(0xFF0F172A),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        var isTestDeletePressed by remember { mutableStateOf(false) }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Hold-to-delete Test Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isTestDeletePressed) Color(0xFFDC2626) else Color(0xFFEF4444))
                                    .pointerInput(deleteMode, deleteInitialDelay, deleteRepeatInterval) {
                                        awaitEachGesture {
                                            val down = awaitFirstDown(requireUnconsumed = false)
                                            isTestDeletePressed = true

                                            // Initial 1-char deletion
                                            if (sampleTestText.isNotEmpty()) {
                                                sampleTestText = sampleTestText.dropLast(1)
                                            }

                                            // Wait initial delay
                                            val upEarly = withTimeoutOrNull(deleteInitialDelay.toLong()) {
                                                var event = awaitPointerEvent()
                                                while (event.changes.any { it.id == down.id && it.pressed }) {
                                                    event = awaitPointerEvent()
                                                }
                                                event.changes.firstOrNull { it.id == down.id && !it.pressed }
                                            }

                                            if (upEarly != null) {
                                                isTestDeletePressed = false
                                            } else {
                                                // Auto-delete loop
                                                while (true) {
                                                    if (deleteMode == DeleteHoldMode.WORD_BY_WORD) {
                                                        // Word deletion
                                                        var trimmed = sampleTestText
                                                        while (trimmed.isNotEmpty() && trimmed.last().isWhitespace()) {
                                                            trimmed = trimmed.dropLast(1)
                                                        }
                                                        while (trimmed.isNotEmpty() && !trimmed.last().isWhitespace()) {
                                                            trimmed = trimmed.dropLast(1)
                                                        }
                                                        sampleTestText = trimmed
                                                    } else {
                                                        if (sampleTestText.isNotEmpty()) {
                                                            sampleTestText = sampleTestText.dropLast(1)
                                                        }
                                                    }

                                                    val released = withTimeoutOrNull(deleteRepeatInterval.toLong()) {
                                                        var event = awaitPointerEvent()
                                                        while (event.changes.any { it.id == down.id && it.pressed }) {
                                                            event = awaitPointerEvent()
                                                        }
                                                        true
                                                    }

                                                    if (released == true) {
                                                        isTestDeletePressed = false
                                                        break
                                                    }
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Hold to Test Delete ⌫",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Reset text button
                            OutlinedButton(
                                onClick = {
                                    sampleTestText = "The quick brown fox jumps over the lazy dog"
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Text("Reset Text", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
