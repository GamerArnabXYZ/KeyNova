package com.example.ui.keyboard

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ime.KeyAction
import com.example.ime.KeyDef
import com.example.ime.KeyboardLayoutModel
import com.example.ime.KeyboardRow
import com.example.ime.ShiftState
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import kotlin.math.roundToInt

data class SpacePopupState(
    val leftLanguage: KeyboardLanguage,
    val centerLanguage: KeyboardLanguage,
    val rightLanguage: KeyboardLanguage,
    val selectedIndex: Int, // 0 = left, 1 = center, 2 = right
    val spaceBarBounds: Rect,
    val dragDeltaX: Float = 0f
)

data class KeyPopupState(
    val primaryText: String,
    val displaySecondaryText: String,
    val commitSecondaryText: String,
    val selectedIndex: Int, // 0 = primaryText, 1 = secondaryText
    val keyBounds: Rect
)

data class LanguageTriple(
    val left: KeyboardLanguage,
    val center: KeyboardLanguage,
    val right: KeyboardLanguage
)

fun getLanguageTriple(current: KeyboardLanguage): LanguageTriple {
    return when (current) {
        KeyboardLanguage.ENGLISH -> LanguageTriple(
            left = KeyboardLanguage.BANGLA,
            center = KeyboardLanguage.ENGLISH,
            right = KeyboardLanguage.HINDI
        )
        KeyboardLanguage.BANGLA -> LanguageTriple(
            left = KeyboardLanguage.ENGLISH,
            center = KeyboardLanguage.BANGLA,
            right = KeyboardLanguage.HINDI
        )
        KeyboardLanguage.HINDI -> LanguageTriple(
            left = KeyboardLanguage.BANGLA,
            center = KeyboardLanguage.HINDI,
            right = KeyboardLanguage.ENGLISH
        )
    }
}

fun resolveSecondaryText(primary: String, secondary: String?): Pair<String, String> {
    if (secondary == null) return primary to primary
    return when (secondary) {
        "..." -> {
            when (primary) {
                "," -> ";" to ";"
                "." -> "..." to "..."
                "।" -> "!" to "!"
                else -> secondary to secondary
            }
        }
        else -> secondary to secondary
    }
}

@Composable
fun KeyNovaKeyboardView(
    keyboardPreferences: KeyboardPreferences,
    clipboardRepository: ClipboardRepository,
    snippetsRepository: SnippetsRepository,
    actionTarget: KeyboardActionTarget,
    isImeService: Boolean = false,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current

    var activePage by remember { mutableStateOf(ActiveKeyboardPage.LETTERS) }
    var currentLanguage by remember { mutableStateOf(keyboardPreferences.activeLanguage) }
    var shiftState by remember { mutableStateOf(ShiftState.OFF) }
    var isBanglaShifted by remember { mutableStateOf(false) }
    var isHindiShifted by remember { mutableStateOf(false) }
    var currentTheme by remember { mutableStateOf(keyboardPreferences.theme) }
    var oneHandedMode by remember { mutableStateOf(keyboardPreferences.oneHandedMode) }
    var switchMethod by remember { mutableStateOf(keyboardPreferences.languageSwitchMethod) }
    val showNumberRow = keyboardPreferences.showNumberRow
    val keyHeight = keyboardPreferences.keyHeightDp.dp
    val keySpacing = keyboardPreferences.keySpacingDp.dp
    val hapticEnabled = keyboardPreferences.hapticFeedback

    val showGlobeKey = (switchMethod == LanguageSwitchMethod.GLOBE_KEY)
    val bgColor = Color(currentTheme.backgroundHex)

    // Keyboard root coordinates for positioning popup overlays accurately
    var keyboardRootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var activeSpacePopup by remember { mutableStateOf<SpacePopupState?>(null) }
    var activeKeyPopup by remember { mutableStateOf<KeyPopupState?>(null) }

    fun performHaptic() {
        if (hapticEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    // Main Keyboard Layout Rows calculation
    val rows: List<KeyboardRow> = remember(
        activePage,
        currentLanguage,
        shiftState,
        isBanglaShifted,
        isHindiShifted,
        showNumberRow,
        showGlobeKey
    ) {
        val result = mutableListOf<KeyboardRow>()

        when (activePage) {
            ActiveKeyboardPage.LETTERS -> {
                if (showNumberRow) {
                    val numberRow = when (currentLanguage) {
                        KeyboardLanguage.BANGLA -> KeyboardLayoutModel.getBanglaNumberRow()
                        else -> KeyboardLayoutModel.getNumberRow()
                    }
                    result.add(numberRow)
                }
                when (currentLanguage) {
                    KeyboardLanguage.ENGLISH -> {
                        result.addAll(KeyboardLayoutModel.getEnglishLayout(shiftState, shiftState == ShiftState.CAPS_LOCKED, showGlobeKey))
                    }
                    KeyboardLanguage.BANGLA -> {
                        result.addAll(KeyboardLayoutModel.getBanglaLayout(isBanglaShifted, showGlobeKey))
                    }
                    KeyboardLanguage.HINDI -> {
                        result.addAll(KeyboardLayoutModel.getHindiLayout(isHindiShifted, showGlobeKey))
                    }
                }
            }
            ActiveKeyboardPage.SYMBOLS_1 -> {
                result.addAll(KeyboardLayoutModel.getSymbolsPage1())
            }
            ActiveKeyboardPage.SYMBOLS_2 -> {
                result.addAll(KeyboardLayoutModel.getSymbolsPage2())
            }
            ActiveKeyboardPage.CODING -> {
                result.addAll(KeyboardLayoutModel.getCodingLayout())
            }
            ActiveKeyboardPage.GAMING -> {
                result.addAll(KeyboardLayoutModel.getGamingLayout())
            }
            else -> {
                // EMOJI, CLIPBOARD, SNIPPETS handled as separate panel
            }
        }
        result
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(bgColor)
            .then(if (!isImeService) Modifier.navigationBarsPadding() else Modifier)
            .onGloballyPositioned { rootCoords ->
                keyboardRootCoordinates = rootCoords
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            // One-handed Left spacer/controls
            if (oneHandedMode == OneHandedMode.RIGHT) {
                OneHandedSidebar(
                    theme = currentTheme,
                    onMoveLeft = {
                        oneHandedMode = OneHandedMode.LEFT
                        keyboardPreferences.oneHandedMode = OneHandedMode.LEFT
                    },
                    onRestore = {
                        oneHandedMode = OneHandedMode.OFF
                        keyboardPreferences.oneHandedMode = OneHandedMode.OFF
                    },
                    modifier = Modifier.width(54.dp)
                )
            }

            // Keyboard Core Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp, vertical = 2.dp)
            ) {
                // Top Toolbar
                KeyboardToolbar(
                    theme = currentTheme,
                    activePage = activePage,
                    onPageSelected = { newPage ->
                        activePage = newPage
                        performHaptic()
                    },
                    onCycleTheme = {
                        val allThemes = KeyboardThemeOption.entries
                        val curIdx = allThemes.indexOf(currentTheme)
                        val nextTheme = allThemes[(curIdx + 1) % allThemes.size]
                        currentTheme = nextTheme
                        keyboardPreferences.theme = nextTheme
                        performHaptic()
                    },
                    onOpenSettings = onOpenSettings,
                    onToggleOneHanded = {
                        oneHandedMode = if (oneHandedMode == OneHandedMode.OFF) OneHandedMode.RIGHT else OneHandedMode.OFF
                        keyboardPreferences.oneHandedMode = oneHandedMode
                        performHaptic()
                    }
                )

                // Sub-panels (Emoji, Clipboard, Snippets, Text Editing, Number Pad) or Key rows
                when (activePage) {
                    ActiveKeyboardPage.TEXT_EDITING -> {
                        TextEditingPadView(
                            theme = currentTheme,
                            actionTarget = actionTarget,
                            hapticEnabled = hapticEnabled
                        )
                    }
                    ActiveKeyboardPage.NUMPAD -> {
                        NumberPadView(
                            theme = currentTheme,
                            currentLanguage = currentLanguage,
                            actionTarget = actionTarget,
                            hapticEnabled = hapticEnabled,
                            onSwitchToLetters = { activePage = ActiveKeyboardPage.LETTERS }
                        )
                    }
                    ActiveKeyboardPage.EMOJI -> {
                        EmojiPickerView(
                            theme = currentTheme,
                            onEmojiSelected = { emoji ->
                                actionTarget.onCommitText(emoji)
                                performHaptic()
                            },
                            onBackspace = {
                                actionTarget.onDeleteBackward()
                                performHaptic()
                            },
                            onClose = { activePage = ActiveKeyboardPage.LETTERS }
                        )
                    }
                    ActiveKeyboardPage.CLIPBOARD -> {
                        ClipboardPanelView(
                            theme = currentTheme,
                            clipboardRepository = clipboardRepository,
                            onPasteText = { text ->
                                actionTarget.onCommitText(text)
                                performHaptic()
                            },
                            onClose = { activePage = ActiveKeyboardPage.LETTERS }
                        )
                    }
                    ActiveKeyboardPage.SNIPPETS -> {
                        CodingSnippetsView(
                            theme = currentTheme,
                            snippetsRepository = snippetsRepository,
                            onInsertSnippet = { code ->
                                actionTarget.onCommitText(code)
                                performHaptic()
                            },
                            onClose = { activePage = ActiveKeyboardPage.LETTERS }
                        )
                    }
                    else -> {
                        // Standard Key Matrix
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalArrangement = Arrangement.spacedBy(keySpacing)
                        ) {
                            rows.forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(keySpacing),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    row.keys.forEach { keyDef ->
                                        if (keyDef.action == KeyAction.SPACE) {
                                            SpaceKeyItem(
                                                keyDef = keyDef,
                                                theme = currentTheme,
                                                height = keyHeight,
                                                switchMethod = switchMethod,
                                                currentLanguage = currentLanguage,
                                                isLanguageSpace = activePage == ActiveKeyboardPage.LETTERS,
                                                rootCoordinates = keyboardRootCoordinates,
                                                onShowPopup = { left, center, right, bounds, initialIndex ->
                                                    activeSpacePopup = SpacePopupState(
                                                        leftLanguage = left,
                                                        centerLanguage = center,
                                                        rightLanguage = right,
                                                        selectedIndex = initialIndex,
                                                        spaceBarBounds = bounds
                                                    )
                                                },
                                                onUpdatePopupIndex = { newIndex, deltaX ->
                                                    activeSpacePopup = activeSpacePopup?.copy(
                                                        selectedIndex = newIndex,
                                                        dragDeltaX = deltaX
                                                    )
                                                },
                                                onDismissPopup = {
                                                    activeSpacePopup = null
                                                },
                                                onSelectLanguage = { chosen ->
                                                    currentLanguage = chosen
                                                    keyboardPreferences.activeLanguage = chosen
                                                },
                                                onPerformHaptic = { performHaptic() },
                                                onTap = {
                                                    performHaptic()
                                                    actionTarget.onCommitText(" ")
                                                },
                                                modifier = Modifier.weight(keyDef.weight)
                                            )
                                        } else if (keyDef.action == KeyAction.BACKSPACE) {
                                            BackspaceKeyItem(
                                                keyDef = keyDef,
                                                theme = currentTheme,
                                                height = keyHeight,
                                                keyboardPreferences = keyboardPreferences,
                                                onDelete = {
                                                    actionTarget.onDeleteBackward()
                                                },
                                                onDeleteWord = {
                                                    actionTarget.onDeleteWordBackward()
                                                },
                                                onPerformHaptic = {
                                                    performHaptic()
                                                },
                                                modifier = Modifier.weight(keyDef.weight)
                                            )
                                        } else {
                                            KeyItem(
                                                keyDef = keyDef,
                                                theme = currentTheme,
                                                height = keyHeight,
                                                isGamingMode = activePage == ActiveKeyboardPage.GAMING,
                                                rootCoordinates = keyboardRootCoordinates,
                                                onShowPopup = { targetKey, bounds, initialIndex ->
                                                    val (disp, commit) = resolveSecondaryText(targetKey.primaryText, targetKey.secondaryText)
                                                    activeKeyPopup = KeyPopupState(
                                                        primaryText = targetKey.primaryText,
                                                        displaySecondaryText = disp,
                                                        commitSecondaryText = commit,
                                                        selectedIndex = initialIndex,
                                                        keyBounds = bounds
                                                    )
                                                },
                                                onUpdatePopupIndex = { newIndex ->
                                                    activeKeyPopup = activeKeyPopup?.copy(selectedIndex = newIndex)
                                                },
                                                onDismissPopup = {
                                                    activeKeyPopup = null
                                                },
                                                onCommitAlternate = { text ->
                                                    actionTarget.onCommitText(text)
                                                    if (shiftState == ShiftState.SHIFTED) {
                                                        shiftState = ShiftState.OFF
                                                    }
                                                    if (isBanglaShifted) {
                                                        isBanglaShifted = false
                                                    }
                                                    if (isHindiShifted) {
                                                        isHindiShifted = false
                                                    }
                                                },
                                                onPerformHaptic = { performHaptic() },
                                                onClick = {
                                                    performHaptic()
                                                    when (keyDef.action) {
                                                        KeyAction.CHARACTER -> {
                                                            actionTarget.onCommitText(keyDef.primaryText)
                                                            if (shiftState == ShiftState.SHIFTED) {
                                                                shiftState = ShiftState.OFF
                                                            }
                                                            if (isBanglaShifted) {
                                                                isBanglaShifted = false
                                                            }
                                                            if (isHindiShifted) {
                                                                isHindiShifted = false
                                                            }
                                                        }
                                                        KeyAction.SPACE -> {
                                                            actionTarget.onCommitText(" ")
                                                        }
                                                        KeyAction.BACKSPACE -> {
                                                            actionTarget.onDeleteBackward()
                                                        }
                                                        KeyAction.ENTER -> {
                                                            actionTarget.onEnterAction()
                                                        }
                                                        KeyAction.SHIFT -> {
                                                            when (currentLanguage) {
                                                                KeyboardLanguage.ENGLISH -> {
                                                                    shiftState = when (shiftState) {
                                                                        ShiftState.OFF -> ShiftState.SHIFTED
                                                                        ShiftState.SHIFTED -> ShiftState.CAPS_LOCKED
                                                                        ShiftState.CAPS_LOCKED -> ShiftState.OFF
                                                                    }
                                                                }
                                                                KeyboardLanguage.BANGLA -> {
                                                                    isBanglaShifted = !isBanglaShifted
                                                                }
                                                                KeyboardLanguage.HINDI -> {
                                                                    isHindiShifted = !isHindiShifted
                                                                }
                                                            }
                                                        }
                                                        KeyAction.SYMBOL_SWITCH -> {
                                                            activePage = when {
                                                                keyDef.primaryText == "?123" || keyDef.primaryText == "?১২৩" -> ActiveKeyboardPage.SYMBOLS_1
                                                                keyDef.primaryText == "= \\ <" || keyDef.primaryText == "=/ <" || keyDef.primaryText == "=/<" -> ActiveKeyboardPage.SYMBOLS_2
                                                                keyDef.primaryText == "ABC" -> ActiveKeyboardPage.LETTERS
                                                                else -> ActiveKeyboardPage.SYMBOLS_1
                                                            }
                                                        }
                                                        KeyAction.LANGUAGE_SWITCH -> {
                                                            val next = keyboardPreferences.cycleLanguage()
                                                            currentLanguage = next
                                                        }
                                                        KeyAction.CODING_PANEL -> {
                                                            activePage = ActiveKeyboardPage.SNIPPETS
                                                        }
                                                        KeyAction.GAMING_PANEL -> {
                                                            activePage = ActiveKeyboardPage.GAMING
                                                        }
                                                        KeyAction.EMOJI_PANEL -> {
                                                            activePage = ActiveKeyboardPage.EMOJI
                                                        }
                                                        KeyAction.CLIPBOARD_PANEL -> {
                                                            activePage = ActiveKeyboardPage.CLIPBOARD
                                                        }
                                                        KeyAction.INSERT_TEXT -> {
                                                            val payload = keyDef.insertPayload ?: keyDef.primaryText
                                                            actionTarget.onCommitText(payload)
                                                        }
                                                        KeyAction.SETTINGS -> {
                                                            onOpenSettings()
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.weight(keyDef.weight)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // One-handed Right spacer/controls
            if (oneHandedMode == OneHandedMode.LEFT) {
                OneHandedSidebar(
                    theme = currentTheme,
                    onMoveLeft = {
                        oneHandedMode = OneHandedMode.RIGHT
                        keyboardPreferences.oneHandedMode = OneHandedMode.RIGHT
                    },
                    onRestore = {
                        oneHandedMode = OneHandedMode.OFF
                        keyboardPreferences.oneHandedMode = OneHandedMode.OFF
                    },
                    modifier = Modifier.width(54.dp)
                )
            }
        }

        // OVERLAY 1: Spacebar Language Switcher Floating Popup
        activeSpacePopup?.let { popupState ->
            SpaceLanguageSwitcherPopup(
                state = popupState,
                theme = currentTheme,
                rootCoordinates = keyboardRootCoordinates
            )
        }

        // OVERLAY 2: Character Key Extra Alternate Floating Popup
        activeKeyPopup?.let { popupState ->
            KeyAlternatePopup(
                state = popupState,
                theme = currentTheme,
                rootCoordinates = keyboardRootCoordinates
            )
        }
    }
}

@Composable
private fun SpaceLanguageSwitcherPopup(
    state: SpacePopupState,
    theme: KeyboardThemeOption,
    rootCoordinates: LayoutCoordinates?
) {
    val density = LocalDensity.current
    val accentColor = Color(theme.accentHex)
    val surfaceColor = Color(theme.surfaceHex)
    val textColor = Color(theme.textHex)

    val popupWidthDp = 276.dp
    val popupHeightDp = 50.dp
    val popupWidthPx = with(density) { popupWidthDp.toPx() }
    val popupHeightPx = with(density) { popupHeightDp.toPx() }

    val rootWidthPx = rootCoordinates?.size?.width?.toFloat() ?: 1000f
    val popupX = ((state.spaceBarBounds.left + state.spaceBarBounds.right) / 2f - popupWidthPx / 2f)
        .coerceIn(with(density) { 6.dp.toPx() }, rootWidthPx - popupWidthPx - with(density) { 6.dp.toPx() })
    val popupY = (state.spaceBarBounds.top - popupHeightPx - with(density) { 10.dp.toPx() })
        .coerceAtLeast(with(density) { 4.dp.toPx() })

    // Animated position for selection indicator (smooth spring swipe animation)
    val animatedIndex by animateFloatAsState(
        targetValue = state.selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "SpaceLangPopupIndicator"
    )

    Box(
        modifier = Modifier
            .offset { IntOffset(popupX.roundToInt(), popupY.roundToInt()) }
            .width(popupWidthDp)
            .height(popupHeightDp + 8.dp)
    ) {
        // Downward Pointer Notch
        val notchX = ((state.spaceBarBounds.left + state.spaceBarBounds.right) / 2f - popupX)
            .coerceIn(with(density) { 20.dp.toPx() }, popupWidthPx - with(density) { 20.dp.toPx() })

        Box(
            modifier = Modifier
                .offset { IntOffset((notchX - with(density) { 6.dp.toPx() }).roundToInt(), with(density) { (popupHeightDp - 6.dp).toPx() }.roundToInt()) }
                .size(12.dp)
                .rotate(45f)
                .background(surfaceColor)
                .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(2.dp))
        )

        // Main Bubble
        Box(
            modifier = Modifier
                .width(popupWidthDp)
                .height(popupHeightDp)
                .shadow(10.dp, RoundedCornerShape(25.dp))
                .clip(RoundedCornerShape(25.dp))
                .background(surfaceColor)
                .border(1.5.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(25.dp))
                .padding(4.dp)
        ) {
            val languages = listOf(state.leftLanguage, state.centerLanguage, state.rightLanguage)
            val pillWidthDp = (popupWidthDp - 8.dp) / 3

            // Sliding selection indicator pill with smooth spring animation
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset((animatedIndex * with(density) { pillWidthDp.toPx() }).roundToInt(), 0)
                    }
                    .width(pillWidthDp)
                    .fillMaxHeight()
                    .shadow(3.dp, RoundedCornerShape(21.dp))
                    .clip(RoundedCornerShape(21.dp))
                    .background(accentColor)
            )

            // Text labels for the 3 languages
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                languages.forEachIndexed { index, lang ->
                    val isSelected = state.selectedIndex == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = lang.nativeName,
                                color = if (isSelected) Color.White else textColor.copy(alpha = 0.7f),
                                fontSize = 13.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                            if (index == 1) {
                                Text(
                                    text = "Current",
                                    color = if (isSelected) Color.White.copy(alpha = 0.85f) else textColor.copy(alpha = 0.45f),
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyAlternatePopup(
    state: KeyPopupState,
    theme: KeyboardThemeOption,
    rootCoordinates: LayoutCoordinates?
) {
    val density = LocalDensity.current
    val accentColor = Color(theme.accentHex)
    val surfaceColor = Color(theme.surfaceHex)
    val textColor = Color(theme.textHex)

    val popupWidthDp = 98.dp
    val popupHeightDp = 50.dp
    val popupWidthPx = with(density) { popupWidthDp.toPx() }
    val popupHeightPx = with(density) { popupHeightDp.toPx() }

    val rootWidthPx = rootCoordinates?.size?.width?.toFloat() ?: 1000f
    val popupX = (state.keyBounds.center.x - popupWidthPx / 2f)
        .coerceIn(with(density) { 4.dp.toPx() }, rootWidthPx - popupWidthPx - with(density) { 4.dp.toPx() })
    val popupY = (state.keyBounds.top - popupHeightPx - with(density) { 8.dp.toPx() })
        .coerceAtLeast(with(density) { 4.dp.toPx() })

    val animatedIndex by animateFloatAsState(
        targetValue = state.selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "KeyAltPopupIndicator"
    )

    Box(
        modifier = Modifier
            .offset { IntOffset(popupX.roundToInt(), popupY.roundToInt()) }
            .width(popupWidthDp)
            .height(popupHeightDp + 8.dp)
    ) {
        // Downward Pointer Notch pointing at the key
        val notchX = (state.keyBounds.center.x - popupX)
            .coerceIn(with(density) { 14.dp.toPx() }, popupWidthPx - with(density) { 14.dp.toPx() })

        Box(
            modifier = Modifier
                .offset { IntOffset((notchX - with(density) { 6.dp.toPx() }).roundToInt(), with(density) { (popupHeightDp - 6.dp).toPx() }.roundToInt()) }
                .size(12.dp)
                .rotate(45f)
                .background(surfaceColor)
                .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(2.dp))
        )

        // Main Bubble
        Box(
            modifier = Modifier
                .width(popupWidthDp)
                .height(popupHeightDp)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(surfaceColor)
                .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(3.dp)
        ) {
            val options = listOf(state.primaryText, state.displaySecondaryText)
            val pillWidthDp = (popupWidthDp - 6.dp) / 2

            // Animated sliding selection pill
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset((animatedIndex * with(density) { pillWidthDp.toPx() }).roundToInt(), 0)
                    }
                    .width(pillWidthDp)
                    .fillMaxHeight()
                    .shadow(2.dp, RoundedCornerShape(13.dp))
                    .clip(RoundedCornerShape(13.dp))
                    .background(accentColor)
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                options.forEachIndexed { index, text ->
                    val isSelected = state.selectedIndex == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = text,
                                color = if (isSelected) Color.White else textColor.copy(alpha = 0.75f),
                                fontSize = if (text.length > 2) 13.sp else 18.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = if (index == 0) "Key" else "Extra",
                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else textColor.copy(alpha = 0.4f),
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KeyItem(
    keyDef: KeyDef,
    theme: KeyboardThemeOption,
    height: androidx.compose.ui.unit.Dp,
    isGamingMode: Boolean,
    rootCoordinates: LayoutCoordinates?,
    onShowPopup: (keyDef: KeyDef, bounds: Rect, initialIndex: Int) -> Unit,
    onUpdatePopupIndex: (newIndex: Int) -> Unit,
    onDismissPopup: () -> Unit,
    onCommitAlternate: (text: String) -> Unit,
    onPerformHaptic: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var keyCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val density = LocalDensity.current

    val textColor = Color(theme.textHex)
    val accentColor = Color(theme.accentHex)
    val keyColor = Color(theme.keyHex)
    val surfaceColor = Color(theme.surfaceHex)

    // Gaming highlight for WASD
    val isWasdKey = isGamingMode && keyDef.primaryText in listOf("W", "A", "S", "D")

    val containerColor = when {
        isPressed -> accentColor.copy(alpha = 0.4f)
        keyDef.action == KeyAction.ENTER -> accentColor
        keyDef.isSpecial -> surfaceColor
        else -> keyColor
    }

    val contentColor = when {
        keyDef.action == KeyAction.ENTER -> Color.White
        keyDef.isSpecial -> textColor.copy(alpha = 0.9f)
        else -> textColor
    }

    val hasSecondary = keyDef.secondaryText != null

    Box(
        modifier = modifier
            .height(height)
            .shadow(if (isPressed) 0.dp else 1.5.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .then(
                if (isWasdKey) Modifier.border(1.5.dp, accentColor, RoundedCornerShape(8.dp))
                else if (theme == KeyboardThemeOption.LIGHT) Modifier.border(0.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                else Modifier
            )
            .onGloballyPositioned { coords ->
                keyCoordinates = coords
            }
            .then(
                if (hasSecondary) {
                    Modifier.pointerInput(keyDef, rootCoordinates) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            isPressed = true
                            var currentSelectedIndex = 1
                            var isPopupActive = false

                            // Wait up to 220ms for pointer release or timeout to trigger hold
                            val upBeforeTimeout = withTimeoutOrNull(220L) {
                                var event = awaitPointerEvent()
                                while (event.changes.any { it.id == down.id && it.pressed }) {
                                    val change = event.changes.first { it.id == down.id }
                                    if ((change.position - down.position).getDistance() > with(density) { 14.dp.toPx() }) {
                                        break
                                    }
                                    event = awaitPointerEvent()
                                }
                                event.changes.firstOrNull { it.id == down.id && !it.pressed }
                            }

                            if (upBeforeTimeout != null) {
                                // Tap released before 220ms -> standard click
                                isPressed = false
                                onClick()
                            } else {
                                // Long press held past 220ms -> show popup with both base key and extra key!
                                isPopupActive = true
                                onPerformHaptic()

                                val root = rootCoordinates
                                val key = keyCoordinates
                                val bounds = if (root != null && root.isAttached && key != null && key.isAttached) {
                                    val offset = root.localPositionOf(key, Offset.Zero)
                                    Rect(offset.x, offset.y, offset.x + key.size.width, offset.y + key.size.height)
                                } else Rect.Zero

                                // Initially select extra key (index 1), swipe left to select base key (index 0)
                                currentSelectedIndex = 1
                                onShowPopup(keyDef, bounds, currentSelectedIndex)

                                // Track drag / swipe between base key and extra key until release
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break

                                    if (!change.pressed) {
                                        // Finger released -> commit the chosen character
                                        isPressed = false
                                        change.consume()
                                        onDismissPopup()
                                        val (_, commitSec) = resolveSecondaryText(keyDef.primaryText, keyDef.secondaryText)
                                        val chosenChar = if (currentSelectedIndex == 0) keyDef.primaryText else commitSec
                                        onCommitAlternate(chosenChar)
                                        break
                                    }

                                    val deltaX = change.position.x - down.position.x
                                    val deltaY = change.position.y - down.position.y

                                    // Swipe left or downwards towards base key selects base key (0)
                                    // Swipe right or upwards towards extra bubble selects extra key (1)
                                    val newIndex = if (deltaX < -with(density) { 10.dp.toPx() } || (deltaY > with(density) { 12.dp.toPx() } && deltaX < with(density) { 10.dp.toPx() })) {
                                        0
                                    } else {
                                        1
                                    }

                                    if (newIndex != currentSelectedIndex) {
                                        currentSelectedIndex = newIndex
                                        onPerformHaptic()
                                        onUpdatePopupIndex(currentSelectedIndex)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Modifier.combinedClickable(
                        onClick = onClick,
                        onLongClick = null
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Centered primary text
        Text(
            text = keyDef.primaryText,
            color = contentColor,
            fontSize = when {
                keyDef.primaryText.length > 4 -> 11.sp
                keyDef.primaryText.length > 2 -> 13.sp
                keyDef.primaryText == "↵" || keyDef.primaryText == "⇥" -> 18.sp
                else -> 17.sp
            },
            fontWeight = if (keyDef.isSpecial || isWasdKey) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        // Secondary hint placed at top-right or bottom-right corner exactly as in screenshots
        if (keyDef.secondaryText != null) {
            val isEllipsis = keyDef.secondaryText == "..."
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        end = 4.dp,
                        top = if (isEllipsis) 0.dp else 2.dp,
                        bottom = if (isEllipsis) 2.dp else 0.dp
                    ),
                contentAlignment = if (isEllipsis) Alignment.BottomEnd else Alignment.TopEnd
            ) {
                Text(
                    text = keyDef.secondaryText,
                    color = contentColor.copy(alpha = if (theme == KeyboardThemeOption.LIGHT) 0.6f else 0.45f),
                    fontSize = if (isEllipsis) 10.sp else 8.5.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 9.sp
                )
            }
        }
    }
}

@Composable
private fun SpaceKeyItem(
    keyDef: KeyDef,
    theme: KeyboardThemeOption,
    height: androidx.compose.ui.unit.Dp,
    switchMethod: LanguageSwitchMethod,
    currentLanguage: KeyboardLanguage,
    isLanguageSpace: Boolean,
    rootCoordinates: LayoutCoordinates?,
    onShowPopup: (left: KeyboardLanguage, center: KeyboardLanguage, right: KeyboardLanguage, bounds: Rect, initialIndex: Int) -> Unit,
    onUpdatePopupIndex: (newIndex: Int, dragDeltaX: Float) -> Unit,
    onDismissPopup: () -> Unit,
    onSelectLanguage: (language: KeyboardLanguage) -> Unit,
    onPerformHaptic: () -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var spaceCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val density = LocalDensity.current

    val textColor = Color(theme.textHex)
    val accentColor = Color(theme.accentHex)
    val keyColor = Color(theme.keyHex)

    val containerColor = when {
        isPressed -> accentColor.copy(alpha = 0.35f)
        else -> keyColor
    }

    Box(
        modifier = modifier
            .height(height)
            .shadow(if (isPressed) 0.dp else 1.5.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .then(
                if (theme == KeyboardThemeOption.LIGHT) Modifier.border(0.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                else Modifier
            )
            .onGloballyPositioned { coords ->
                spaceCoordinates = coords
            }
            .pointerInput(currentLanguage, switchMethod, isLanguageSpace, rootCoordinates) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    val triple = getLanguageTriple(currentLanguage)
                    var currentSelectedIndex = 1
                    var isPopupActive = false

                    val thresholdPx = with(density) { 30.dp.toPx() }

                    // Wait up to 230ms for quick tap or horizontal drag
                    val upBeforeTimeout = withTimeoutOrNull(230L) {
                        var event = awaitPointerEvent()
                        while (event.changes.any { it.id == down.id && it.pressed }) {
                            val change = event.changes.first { it.id == down.id }
                            val deltaX = change.position.x - down.position.x
                            // If user is already swiping horizontally on spacebar:
                            if (isLanguageSpace && abs(deltaX) >= thresholdPx) {
                                break
                            }
                            event = awaitPointerEvent()
                        }
                        event.changes.firstOrNull { it.id == down.id && !it.pressed }
                    }

                    if (upBeforeTimeout != null) {
                        // Quick tap on spacebar
                        isPressed = false
                        onTap()
                    } else {
                        // Spacebar held or swiped -> activate floating language switcher popup!
                        isPopupActive = true
                        onPerformHaptic()

                        val root = rootCoordinates
                        val space = spaceCoordinates
                        val bounds = if (root != null && root.isAttached && space != null && space.isAttached) {
                            val offset = root.localPositionOf(space, Offset.Zero)
                            Rect(offset.x, offset.y, offset.x + space.size.width, offset.y + space.size.height)
                        } else Rect.Zero

                        onShowPopup(triple.left, triple.center, triple.right, bounds, currentSelectedIndex)

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break

                            if (!change.pressed) {
                                isPressed = false
                                change.consume()
                                onDismissPopup()
                                val chosen = when (currentSelectedIndex) {
                                    0 -> triple.left
                                    1 -> triple.center
                                    2 -> triple.right
                                    else -> triple.center
                                }
                                onSelectLanguage(chosen)
                                onPerformHaptic()
                                break
                            }

                            val deltaX = change.position.x - down.position.x
                            val newIndex = when {
                                deltaX < -thresholdPx -> 0 // Left language
                                deltaX > thresholdPx -> 2  // Right language
                                else -> 1                 // Center language
                            }

                            if (newIndex != currentSelectedIndex) {
                                currentSelectedIndex = newIndex
                                onPerformHaptic()
                                onUpdatePopupIndex(currentSelectedIndex, deltaX)
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            val label = if (isLanguageSpace) {
                when (currentLanguage) {
                    KeyboardLanguage.ENGLISH -> "English"
                    KeyboardLanguage.BANGLA -> "জাতীয়"
                    KeyboardLanguage.HINDI -> "हिन्दी"
                }
            } else {
                keyDef.primaryText
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "◀  ",
                    color = textColor.copy(alpha = 0.45f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    color = textColor.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "  ▶",
                    color = textColor.copy(alpha = 0.45f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isLanguageSpace) {
                Text(
                    text = "Hold & swipe to switch",
                    color = textColor.copy(alpha = 0.45f),
                    fontSize = 8.5.sp,
                    lineHeight = 9.sp
                )
            }
        }
    }
}

@Composable
private fun OneHandedSidebar(
    theme: KeyboardThemeOption,
    onMoveLeft: () -> Unit,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = Color(theme.textHex)
    val surfaceColor = Color(theme.surfaceHex)

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(surfaceColor)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onMoveLeft) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Switch Side",
                tint = textColor
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        IconButton(onClick = onRestore) {
            Icon(
                imageVector = Icons.Default.Fullscreen,
                contentDescription = "Restore Full Width",
                tint = textColor
            )
        }
    }
}

@Composable
private fun BackspaceKeyItem(
    keyDef: KeyDef,
    theme: KeyboardThemeOption,
    height: androidx.compose.ui.unit.Dp,
    keyboardPreferences: KeyboardPreferences,
    onDelete: () -> Unit,
    onDeleteWord: () -> Unit,
    onPerformHaptic: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var isHoldingWordDelete by remember { mutableStateOf(false) }

    val textColor = Color(theme.textHex)
    val accentColor = Color(theme.accentHex)
    val surfaceColor = Color(theme.surfaceHex)

    val containerColor = when {
        isPressed -> accentColor.copy(alpha = 0.35f)
        else -> surfaceColor
    }

    Box(
        modifier = modifier
            .height(height)
            .shadow(if (isPressed) 0.dp else 1.5.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .then(
                if (theme == KeyboardThemeOption.LIGHT) Modifier.border(0.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                else Modifier
            )
            .pointerInput(keyboardPreferences.deleteHoldMode, keyboardPreferences.deleteInitialDelayMs, keyboardPreferences.deleteRepeatIntervalMs) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    onPerformHaptic()
                    onDelete() // Delete 1 character immediately on initial tap

                    val initialDelay = keyboardPreferences.deleteInitialDelayMs.toLong()
                    val repeatInterval = keyboardPreferences.deleteRepeatIntervalMs.toLong()
                    val mode = keyboardPreferences.deleteHoldMode
                    val hapticOnRepeat = keyboardPreferences.deleteHapticOnRepeat

                    // Wait for initial delay before continuous auto-delete begins
                    val upBeforeTimeout = withTimeoutOrNull(initialDelay) {
                        var event = awaitPointerEvent()
                        while (event.changes.any { it.id == down.id && it.pressed }) {
                            event = awaitPointerEvent()
                        }
                        event.changes.firstOrNull { it.id == down.id && !it.pressed }
                    }

                    if (upBeforeTimeout != null) {
                        // Quick tap release
                        isPressed = false
                    } else {
                        // Hold timed out -> Continuous auto-removal begins!
                        if (mode == DeleteHoldMode.WORD_BY_WORD) {
                            isHoldingWordDelete = true
                        }

                        while (true) {
                            if (mode == DeleteHoldMode.WORD_BY_WORD) {
                                onDeleteWord()
                            } else {
                                onDelete()
                            }
                            if (hapticOnRepeat) {
                                onPerformHaptic()
                            }

                            val upDuringInterval = withTimeoutOrNull(repeatInterval) {
                                var event = awaitPointerEvent()
                                while (event.changes.any { it.id == down.id && it.pressed }) {
                                    event = awaitPointerEvent()
                                }
                                true
                            }

                            if (upDuringInterval == true) {
                                isPressed = false
                                isHoldingWordDelete = false
                                break
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = keyDef.primaryText,
                color = textColor.copy(alpha = 0.95f),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
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
