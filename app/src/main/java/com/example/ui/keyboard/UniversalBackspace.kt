package com.example.ui.keyboard

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Universal Hold-to-Repeat Backspace Modifier.
 * Provides consistent, rapid character deletion on hold and optional swipe left to delete words
 * across every tool, panel, and screen of the keyboard.
 */
fun Modifier.universalBackspacePressAndHold(
    onDelete: () -> Unit,
    onDeleteWord: (() -> Unit)? = null,
    initialDelayMs: Long = 350L,
    repeatIntervalMs: Long = 50L,
    allowSwipeDelete: Boolean = true,
    swipeThresholdDp: Float = 24f,
    onPerformHaptic: () -> Unit = {},
    onPressedChange: (Boolean) -> Unit = {},
    onWordModeChange: (Boolean) -> Unit = {}
): Modifier = this.pointerInput(initialDelayMs, repeatIntervalMs, allowSwipeDelete) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        onPressedChange(true)
        onPerformHaptic()
        onDelete() // Delete 1 character immediately on initial tap

        val swipeThreshold = with(density) { swipeThresholdDp.dp.toPx() }
        var lastSwipeX = down.position.x
        var didSwipeDelete = false

        // Wait for initial delay or detect swipe left
        val upBeforeTimeout = withTimeoutOrNull(initialDelayMs) {
            var event = awaitPointerEvent()
            while (event.changes.any { it.id == down.id && it.pressed }) {
                val change = event.changes.first { it.id == down.id }
                if (allowSwipeDelete && onDeleteWord != null) {
                    val deltaX = change.position.x - lastSwipeX
                    if (deltaX <= -swipeThreshold) {
                        didSwipeDelete = true
                        onWordModeChange(true)
                        onDeleteWord()
                        onPerformHaptic()
                        lastSwipeX = change.position.x
                    }
                }
                event = awaitPointerEvent()
            }
            event.changes.firstOrNull { it.id == down.id && !it.pressed }
        }

        if (upBeforeTimeout != null) {
            // Released before initial delay (quick single tap)
            onPressedChange(false)
            onWordModeChange(false)
        } else if (!didSwipeDelete) {
            // Held down past initial delay -> Accelerated continuous auto-removal begins!
            while (true) {
                onDelete()
                onPerformHaptic()

                val upDuringInterval = withTimeoutOrNull(repeatIntervalMs) {
                    var event = awaitPointerEvent()
                    while (event.changes.any { it.id == down.id && it.pressed }) {
                        event = awaitPointerEvent()
                    }
                    true
                }

                if (upDuringInterval == true) {
                    onPressedChange(false)
                    onWordModeChange(false)
                    break
                }
            }
        } else {
            // Swipe delete was triggered, continue tracking swipes until finger releases
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                if (!change.pressed) {
                    onPressedChange(false)
                    onWordModeChange(false)
                    break
                }
                if (allowSwipeDelete && onDeleteWord != null) {
                    val deltaX = change.position.x - lastSwipeX
                    if (deltaX <= -swipeThreshold) {
                        onDeleteWord()
                        onPerformHaptic()
                        lastSwipeX = change.position.x
                    }
                }
            }
        }
    }
}

/**
 * Reusable Universal Backspace Key Composable with built-in hold-to-repeat removal,
 * visual press-state elevation, and haptic feedback.
 */
@Composable
fun UniversalBackspaceKey(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    iconColor: Color,
    accentColor: Color,
    border: BorderStroke? = null,
    shape: Shape = RoundedCornerShape(8.dp),
    iconSize: Dp = 20.dp,
    elevation: Dp = 1.dp,
    initialDelayMs: Long = 350L,
    repeatIntervalMs: Long = 50L,
    allowSwipeDelete: Boolean = true,
    onDelete: () -> Unit,
    onDeleteWord: (() -> Unit)? = null,
    onPerformHaptic: () -> Unit = {}
) {
    var isPressed by remember { mutableStateOf(false) }
    var isHoldingWordDelete by remember { mutableStateOf(false) }

    val effectiveBg by animateColorAsState(
        targetValue = if (isPressed) accentColor.copy(alpha = 0.35f) else backgroundColor,
        animationSpec = tween(durationMillis = 80),
        label = "universal_backspace_bg"
    )

    Box(
        modifier = modifier
            .shadow(if (isPressed) 0.dp else elevation, shape)
            .clip(shape)
            .background(effectiveBg)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .universalBackspacePressAndHold(
                onDelete = onDelete,
                onDeleteWord = onDeleteWord,
                initialDelayMs = initialDelayMs,
                repeatIntervalMs = repeatIntervalMs,
                allowSwipeDelete = allowSwipeDelete,
                onPerformHaptic = onPerformHaptic,
                onPressedChange = { isPressed = it },
                onWordModeChange = { isHoldingWordDelete = it }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Backspace",
                tint = iconColor,
                modifier = Modifier.size(iconSize)
            )
            if (isHoldingWordDelete) {
                Text(
                    text = "WORD",
                    color = accentColor,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 7.5.sp
                )
            }
        }
    }
}
