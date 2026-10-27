package com.example.ui.screens

import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    var showResetDialog by remember { mutableStateOf(false) }

    var showNumberRow by remember { mutableStateOf(keyboardPreferences.showNumberRow) }
    var keyHeight by remember { mutableFloatStateOf(keyboardPreferences.keyHeightDp.toFloat()) }
    var keySpacing by remember { mutableFloatStateOf(keyboardPreferences.keySpacingDp.toFloat()) }
    var haptic by remember { mutableStateOf(keyboardPreferences.hapticFeedback) }
    var sound by remember { mutableStateOf(keyboardPreferences.soundFeedback) }
    var autoCap by remember { mutableStateOf(keyboardPreferences.autoCapitalization) }
    var doubleSpace by remember { mutableStateOf(keyboardPreferences.doubleSpacePeriod) }
    var spaceCursor by remember { mutableStateOf(keyboardPreferences.spacebarCursorControl) }
    var backspaceSwipe by remember { mutableStateOf(keyboardPreferences.backspaceSwipeDelete) }
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
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SettingsSectionHeader(title = stringResource(R.string.layout_settings_appearance_section))
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
                SettingsSectionHeader(title = stringResource(R.string.layout_settings_feedback_section))
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
                                    text = "Sound on Keypress",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Play audio click effect on each key tap",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Switch(
                                checked = sound,
                                onCheckedChange = {
                                    sound = it
                                    keyboardPreferences.soundFeedback = it
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2563EB))
                            )
                        }

                        if (sound) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 1.0f)
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.align(Alignment.End),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Outlined.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.layout_settings_sound_test), fontSize = 12.sp)
                            }
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
                SettingsSectionHeader(title = stringResource(R.string.layout_settings_smart_gestures_section))
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
                            text = "Smart Typing & Gestures",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Cursor sliding, quick periods, word deletions, and auto-casing",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )

                        // 1. Spacebar Cursor Control
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Spacebar Cursor Navigation",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Slide thumb left or right along spacebar to move cursor",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Switch(
                                checked = spaceCursor,
                                onCheckedChange = {
                                    spaceCursor = it
                                    keyboardPreferences.spacebarCursorControl = it
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2563EB))
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

                        // 2. Double Space Period
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Double-Space Period (\". \")",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Double-tap spacebar to quickly insert a period and space",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Switch(
                                checked = doubleSpace,
                                onCheckedChange = {
                                    doubleSpace = it
                                    keyboardPreferences.doubleSpacePeriod = it
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2563EB))
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

                        // 3. Auto-Capitalization
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-Capitalization",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Capitalize the first letter of each sentence automatically",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Switch(
                                checked = autoCap,
                                onCheckedChange = {
                                    autoCap = it
                                    keyboardPreferences.autoCapitalization = it
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2563EB))
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

                        // 4. Backspace Swipe Delete
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Swipe Backspace to Delete Word",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Swipe left from Backspace to delete the entire preceding word",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Switch(
                                checked = backspaceSwipe,
                                onCheckedChange = {
                                    backspaceSwipe = it
                                    keyboardPreferences.backspaceSwipeDelete = it
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2563EB))
                            )
                        }
                    }
                }
            }

            item {
                SettingsSectionHeader(title = stringResource(R.string.layout_settings_delete_section))
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
                                    .defaultMinSize(minHeight = 48.dp)
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
                                    }
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Hold to Test Delete ⌫",
                                    color = Color.White,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                            }

                            // Reset text button
                            OutlinedButton(
                                onClick = {
                                    sampleTestText = "The quick brown fox jumps over the lazy dog"
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text("Reset Text", fontSize = 12.5.sp, maxLines = 1, softWrap = false)
                            }
                        }
                    }
                }
            }

            // ==========================================
            // RESTORE DEFAULTS
            // ==========================================
            item {
                SettingsSectionHeader(title = stringResource(R.string.layout_settings_reset_section))
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
                            text = stringResource(R.string.layout_settings_btn_reset),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFDC2626)
                        )
                        Text(
                            text = stringResource(R.string.layout_settings_btn_reset_desc),
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )
                        OutlinedButton(
                            onClick = { showResetDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Restore Layout Defaults", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.layout_settings_reset_confirm_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.layout_settings_reset_confirm_msg),
                    fontSize = 13.5.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        keyboardPreferences.resetLayoutToDefaults()
                        showNumberRow = true
                        keyHeight = 48f
                        keySpacing = 4f
                        haptic = true
                        sound = false
                        autoCap = true
                        doubleSpace = true
                        spaceCursor = true
                        backspaceSwipe = true
                        oneHanded = OneHandedMode.OFF
                        deleteMode = DeleteHoldMode.WORD_BY_WORD
                        deleteInitialDelay = 350f
                        deleteRepeatInterval = 100f
                        deleteHaptic = true
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Reset", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
