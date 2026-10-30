package com.example.ui.keyboard

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.PictureInPicture
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.VerticalAlignBottom
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
import com.example.ime.AvroPhoneticParser
import com.example.ime.DynamicKeyboardLayout
import com.example.ime.EnglishWordPredictor
import com.example.ime.KeyAction
import com.example.ime.KeyDef
import com.example.ime.KeyboardLayoutModel
import com.example.ime.KeyboardLayoutRegistry
import com.example.ime.KeyboardRow
import com.example.ime.ShiftState
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import kotlin.math.roundToInt

data class SpacePopupState(
    val leftLayout: DynamicKeyboardLayout,
    val centerLayout: DynamicKeyboardLayout,
    val rightLayout: DynamicKeyboardLayout,
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

data class DynamicLayoutTriple(
    val left: DynamicKeyboardLayout,
    val center: DynamicKeyboardLayout,
    val right: DynamicKeyboardLayout
)

fun getLayoutTriple(current: DynamicKeyboardLayout, allEnabled: List<DynamicKeyboardLayout>): DynamicLayoutTriple {
    if (allEnabled.isEmpty()) {
        return DynamicLayoutTriple(current, current, current)
    }
    val currentIdx = allEnabled.indexOfFirst { it.id.equals(current.id, ignoreCase = true) }
    val effectiveIdx = if (currentIdx == -1) 0 else currentIdx
    val size = allEnabled.size

    val leftIdx = (effectiveIdx - 1 + size) % size
    val rightIdx = (effectiveIdx + 1) % size

    return DynamicLayoutTriple(
        left = allEnabled[leftIdx],
        center = allEnabled[effectiveIdx],
        right = allEnabled[rightIdx]
    )
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
    onOpenToolbarSettings: () -> Unit = onOpenSettings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val prefRev = keyboardPreferences.preferenceRevision.intValue

    var activePage by remember { mutableStateOf(ActiveKeyboardPage.LETTERS) }
    var currentLayout by remember(keyboardPreferences.activeLayoutId, prefRev) {
        mutableStateOf(keyboardPreferences.getActiveLayout(context))
    }
    var currentLanguage by remember(currentLayout) { mutableStateOf(currentLayout.keyboardLanguage) }
    var shiftState by remember { mutableStateOf(ShiftState.OFF) }
    var isShifted by remember { mutableStateOf(false) }
    var currentTheme by remember(keyboardPreferences.theme, prefRev) {
        mutableStateOf(keyboardPreferences.theme)
    }
    var oneHandedMode by remember(keyboardPreferences.oneHandedMode, prefRev) {
        mutableStateOf(keyboardPreferences.oneHandedMode)
    }
    var isFloatingMode by remember(keyboardPreferences.isFloatingMode, prefRev) {
        mutableStateOf(keyboardPreferences.isFloatingMode)
    }
    var floatingOffsetX by remember(keyboardPreferences.floatingOffsetX, prefRev) {
        mutableFloatStateOf(keyboardPreferences.floatingOffsetX)
    }
    var floatingOffsetY by remember(keyboardPreferences.floatingOffsetY, prefRev) {
        mutableFloatStateOf(keyboardPreferences.floatingOffsetY)
    }
    var floatingScale by remember(keyboardPreferences.floatingScale, prefRev) {
        mutableFloatStateOf(keyboardPreferences.floatingScale)
    }
    var floatingOpacity by remember(keyboardPreferences.floatingOpacity, prefRev) {
        mutableFloatStateOf(keyboardPreferences.floatingOpacity)
    }
    var switchMethod by remember(keyboardPreferences.languageSwitchMethod, prefRev) {
        mutableStateOf(keyboardPreferences.languageSwitchMethod)
    }
    val showNumberRow = remember(keyboardPreferences.showNumberRow, prefRev) { keyboardPreferences.showNumberRow }
    val keyHeight = remember(keyboardPreferences.keyHeightDp, prefRev) { keyboardPreferences.keyHeightDp.dp }
    val keySpacing = remember(keyboardPreferences.keySpacingDp, prefRev) { keyboardPreferences.keySpacingDp.dp }
    val currentKeyHeight = remember(keyHeight, isFloatingMode, floatingScale) {
        if (isFloatingMode) keyHeight * floatingScale else keyHeight
    }
    val currentKeySpacing = remember(keySpacing, isFloatingMode, floatingScale) {
        if (isFloatingMode) keySpacing * floatingScale else keySpacing
    }
    val hapticEnabled = remember(keyboardPreferences.hapticFeedback, prefRev) { keyboardPreferences.hapticFeedback }
    val longPressDelayMs = remember(keyboardPreferences.keyLongPressDelayMs, prefRev) { keyboardPreferences.keyLongPressDelayMs.toLong() }
    val standardKeyboardContentHeight = remember(showNumberRow, currentKeyHeight, currentKeySpacing) {
        val rowCount = if (showNumberRow) 5 else 4
        (currentKeyHeight * rowCount) + (currentKeySpacing * (rowCount - 1)) + 4.dp
    }

    var avroComposingText by remember { mutableStateOf("") }
    val avroCandidates = remember(avroComposingText) {
        if (avroComposingText.isNotEmpty()) AvroPhoneticParser.getCandidates(avroComposingText) else emptyList()
    }

    var englishComposingWord by remember { mutableStateOf("") }
    val englishCandidates: List<String> = remember(englishComposingWord) {
        EnglishWordPredictor.predict(englishComposingWord)
    }

    val showGlobeKey = (switchMethod == LanguageSwitchMethod.GLOBE_KEY)
    val bgColor = Color(currentTheme.backgroundHex)

    val allRegisteredLayouts = remember(context) { KeyboardLayoutRegistry.getLayouts(context) }
    val enabledLayouts = remember(keyboardPreferences.enabledLayoutIds, allRegisteredLayouts) {
        allRegisteredLayouts.filter { keyboardPreferences.enabledLayoutIds.contains(it.id) }.ifEmpty { allRegisteredLayouts }
    }

    // Keyboard root coordinates for positioning popup overlays accurately
    var keyboardRootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var activeSpacePopup by remember { mutableStateOf<SpacePopupState?>(null) }
    var activeKeyPopup by remember { mutableStateOf<KeyPopupState?>(null) }
    var lastSpaceTapTime by remember { mutableLongStateOf(0L) }

    val audioManager = remember { context.getSystemService(android.content.Context.AUDIO_SERVICE) as? android.media.AudioManager }
    val soundEnabled = remember(keyboardPreferences.soundFeedback, prefRev) { keyboardPreferences.soundFeedback }

    fun performKeyFeedback(fx: Int = android.media.AudioManager.FX_KEYPRESS_STANDARD) {
        if (hapticEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
        if (soundEnabled) {
            audioManager?.playSoundEffect(fx)
        }
    }

    fun performHaptic() {
        performKeyFeedback(android.media.AudioManager.FX_KEYPRESS_STANDARD)
    }

    // Main Keyboard Layout Rows calculation - dynamically built from XML
    val rows: List<KeyboardRow> = remember(
        activePage,
        currentLayout,
        shiftState,
        isShifted,
        showNumberRow,
        showGlobeKey
    ) {
        val result = mutableListOf<KeyboardRow>()

        when (activePage) {
            ActiveKeyboardPage.LETTERS -> {
                if (showNumberRow) {
                    val numberRow = when (currentLayout.languageCode) {
                        "bn" -> KeyboardLayoutModel.getBanglaNumberRow()
                        "hi" -> KeyboardLayoutModel.getHindiNumberRow()
                        else -> KeyboardLayoutModel.getNumberRow()
                    }
                    result.add(numberRow)
                }
                result.addAll(
                    KeyboardLayoutRegistry.buildRows(
                        layout = currentLayout,
                        shiftState = shiftState,
                        isCaps = shiftState == ShiftState.CAPS_LOCKED || isShifted,
                        showGlobeKey = showGlobeKey
                    )
                )
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

    val keyboardToolbarComposable: @Composable (Boolean) -> Unit = { inFloating ->
        KeyboardToolbar(
            theme = currentTheme,
            activePage = activePage,
            keyboardPreferences = keyboardPreferences,
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
            onOpenToolbarSettings = onOpenToolbarSettings,
            onToggleOneHanded = {
                oneHandedMode = if (oneHandedMode == OneHandedMode.OFF) OneHandedMode.RIGHT else OneHandedMode.OFF
                keyboardPreferences.oneHandedMode = oneHandedMode
                performHaptic()
            },
            onToggleFloating = {
                isFloatingMode = !inFloating
                keyboardPreferences.isFloatingMode = !inFloating
                performHaptic()
            },
            onClearAll = {
                actionTarget.onClearAll()
                performHaptic()
            },
            onCycleCase = {
                actionTarget.onCycleCase()
                shiftState = when (shiftState) {
                    ShiftState.OFF -> ShiftState.SHIFTED
                    ShiftState.SHIFTED -> ShiftState.CAPS_LOCKED
                    ShiftState.CAPS_LOCKED -> ShiftState.OFF
                }
                performHaptic()
            },
            onVoiceTyping = {
                performKeyFeedback(android.media.AudioManager.FX_KEYPRESS_STANDARD)
                actionTarget.onVoiceTyping()
            }
        )
    }

    val keyboardPagesContent: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(standardKeyboardContentHeight)
        ) {
            when (activePage) {
                        ActiveKeyboardPage.TEXT_EDITING -> {
                            TextEditingPadView(
                                theme = currentTheme,
                                actionTarget = actionTarget,
                                hapticEnabled = hapticEnabled,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        ActiveKeyboardPage.NUMPAD -> {
                            NumberPadView(
                                theme = currentTheme,
                                currentLanguage = currentLanguage,
                                actionTarget = actionTarget,
                                hapticEnabled = hapticEnabled,
                                onSwitchToLetters = { activePage = ActiveKeyboardPage.LETTERS },
                                modifier = Modifier.fillMaxSize()
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
                                onClose = { activePage = ActiveKeyboardPage.LETTERS },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        ActiveKeyboardPage.CLIPBOARD -> {
                            ClipboardPanelView(
                                theme = currentTheme,
                                clipboardRepository = clipboardRepository,
                                onPasteText = { text ->
                                    actionTarget.onCommitText(text)
                                    clipboardRepository.copyToSystemClipboard(text, context)
                                    performHaptic()
                                    activePage = ActiveKeyboardPage.LETTERS
                                },
                                onClose = { activePage = ActiveKeyboardPage.LETTERS },
                                modifier = Modifier.fillMaxSize()
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
                                onClose = { activePage = ActiveKeyboardPage.LETTERS },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            // Standard Key Matrix
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 2.dp),
                                verticalArrangement = Arrangement.spacedBy(currentKeySpacing)
                            ) {
                            if (activePage == ActiveKeyboardPage.LETTERS && currentLayout.id == "bn_avro") {
                                AvroPhoneticSuggestionStrip(
                                    composingText = avroComposingText,
                                    candidates = avroCandidates,
                                    theme = currentTheme,
                                    onSelectCandidate = { chosen ->
                                        actionTarget.onCommitText(chosen + " ")
                                        avroComposingText = ""
                                        performHaptic()
                                    },
                                    onClearComposing = {
                                        avroComposingText = ""
                                        performHaptic()
                                    }
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            } else if (activePage == ActiveKeyboardPage.LETTERS && currentLayout.languageCode == "en" && englishCandidates.isNotEmpty()) {
                                EnglishWordSuggestionStrip(
                                    composingWord = englishComposingWord,
                                    candidates = englishCandidates,
                                    theme = currentTheme,
                                    onSelectCandidate = { chosen ->
                                        for (i in 0 until englishComposingWord.length) {
                                            actionTarget.onDeleteBackward()
                                        }
                                        actionTarget.onCommitText(chosen + " ")
                                        englishComposingWord = ""
                                        performHaptic()
                                    },
                                    onClearComposing = {
                                        englishComposingWord = ""
                                        performHaptic()
                                    }
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }

                            rows.forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(currentKeySpacing),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    row.keys.forEach { keyDef ->
                                        if (keyDef.action == KeyAction.SPACE) {
                                            SpaceKeyItem(
                                                keyDef = keyDef,
                                                theme = currentTheme,
                                                height = currentKeyHeight,
                                                switchMethod = switchMethod,
                                                currentLayout = currentLayout,
                                                enabledLayouts = enabledLayouts,
                                                isLanguageSpace = activePage == ActiveKeyboardPage.LETTERS,
                                                rootCoordinates = keyboardRootCoordinates,
                                                spacebarCursorControl = keyboardPreferences.spacebarCursorControl,
                                                onMoveCursor = { offset -> actionTarget.onMoveCursor(offset) },
                                                onShowPopup = { left, center, right, bounds, initialIndex ->
                                                    activeSpacePopup = SpacePopupState(
                                                        leftLayout = left,
                                                        centerLayout = center,
                                                        rightLayout = right,
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
                                                onSelectLayout = { chosen ->
                                                    if (avroComposingText.isNotEmpty()) {
                                                        val primary = avroCandidates.firstOrNull() ?: AvroPhoneticParser.parse(avroComposingText)
                                                        actionTarget.onCommitText(primary)
                                                        avroComposingText = ""
                                                    }
                                                    englishComposingWord = ""
                                                    currentLayout = chosen
                                                    currentLanguage = chosen.keyboardLanguage
                                                    keyboardPreferences.activeLayoutId = chosen.id
                                                    keyboardPreferences.activeLanguage = chosen.keyboardLanguage
                                                },
                                                onPerformHaptic = { performKeyFeedback(android.media.AudioManager.FX_KEYPRESS_SPACEBAR) },
                                                onTap = {
                                                    performKeyFeedback(android.media.AudioManager.FX_KEYPRESS_SPACEBAR)
                                                    if (currentLayout.id == "bn_avro" && avroComposingText.isNotEmpty()) {
                                                        val primary = avroCandidates.firstOrNull() ?: AvroPhoneticParser.parse(avroComposingText)
                                                        actionTarget.onCommitText(primary + " ")
                                                        avroComposingText = ""
                                                    } else {
                                                        englishComposingWord = ""
                                                        val now = System.currentTimeMillis()
                                                        val isDoubleSpace = keyboardPreferences.doubleSpacePeriod && (now - lastSpaceTapTime < 450L) && lastSpaceTapTime != 0L
                                                        if (isDoubleSpace) {
                                                            actionTarget.onDoubleSpacePeriod()
                                                            lastSpaceTapTime = 0L
                                                            if (keyboardPreferences.autoCapitalization && currentLayout.languageCode == "en") {
                                                                shiftState = ShiftState.SHIFTED
                                                                isShifted = true
                                                            }
                                                        } else {
                                                            actionTarget.onCommitText(" ")
                                                            lastSpaceTapTime = now
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.weight(keyDef.weight)
                                            )
                                        } else if (keyDef.action == KeyAction.BACKSPACE) {
                                            BackspaceKeyItem(
                                                keyDef = keyDef,
                                                theme = currentTheme,
                                                height = currentKeyHeight,
                                                keyboardPreferences = keyboardPreferences,
                                                onDelete = {
                                                    if (currentLayout.id == "bn_avro" && avroComposingText.isNotEmpty()) {
                                                        avroComposingText = avroComposingText.dropLast(1)
                                                        performHaptic()
                                                    } else {
                                                        if (englishComposingWord.isNotEmpty()) {
                                                            englishComposingWord = englishComposingWord.dropLast(1)
                                                        }
                                                        actionTarget.onDeleteBackward()
                                                    }
                                                },
                                                onDeleteWord = {
                                                    if (currentLayout.id == "bn_avro" && avroComposingText.isNotEmpty()) {
                                                        avroComposingText = ""
                                                        performHaptic()
                                                    } else {
                                                        englishComposingWord = ""
                                                        actionTarget.onDeleteWordBackward()
                                                    }
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
                                                height = currentKeyHeight,
                                                isGamingMode = activePage == ActiveKeyboardPage.GAMING,
                                                rootCoordinates = keyboardRootCoordinates,
                                                longPressDelayMs = longPressDelayMs,
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
                                                    if (avroComposingText.isNotEmpty()) {
                                                        val primary = avroCandidates.firstOrNull() ?: AvroPhoneticParser.parse(avroComposingText)
                                                        actionTarget.onCommitText(primary)
                                                        avroComposingText = ""
                                                    }
                                                    englishComposingWord = ""
                                                    actionTarget.onCommitText(text)
                                                    if (shiftState == ShiftState.SHIFTED) {
                                                        shiftState = ShiftState.OFF
                                                        isShifted = false
                                                    } else if (isShifted && currentLayout.languageCode != "en") {
                                                        isShifted = false
                                                        shiftState = ShiftState.OFF
                                                    }
                                                },
                                                onPerformHaptic = { performHaptic() },
                                                onClick = {
                                                    performHaptic()
                                                    when (keyDef.action) {
                                                        KeyAction.CHARACTER -> {
                                                            if (currentLayout.id == "bn_avro" && keyDef.primaryText.length == 1 && keyDef.primaryText[0].isLetter()) {
                                                                avroComposingText += keyDef.primaryText
                                                            } else {
                                                                if (avroComposingText.isNotEmpty()) {
                                                                    val primary = avroCandidates.firstOrNull() ?: AvroPhoneticParser.parse(avroComposingText)
                                                                    actionTarget.onCommitText(primary)
                                                                    avroComposingText = ""
                                                                }
                                                                if (currentLayout.languageCode == "en" && keyDef.primaryText.length == 1 && keyDef.primaryText[0].isLetter()) {
                                                                    englishComposingWord += keyDef.primaryText
                                                                } else {
                                                                    englishComposingWord = ""
                                                                }
                                                                actionTarget.onCommitText(keyDef.primaryText)
                                                            }
                                                            if (shiftState == ShiftState.SHIFTED) {
                                                                shiftState = ShiftState.OFF
                                                                isShifted = false
                                                            } else if (isShifted && currentLayout.languageCode != "en") {
                                                                isShifted = false
                                                                shiftState = ShiftState.OFF
                                                            }
                                                        }
                                                        KeyAction.SPACE -> {
                                                            englishComposingWord = ""
                                                            if (currentLayout.id == "bn_avro" && avroComposingText.isNotEmpty()) {
                                                                val primary = avroCandidates.firstOrNull() ?: AvroPhoneticParser.parse(avroComposingText)
                                                                actionTarget.onCommitText(primary + " ")
                                                                avroComposingText = ""
                                                            } else {
                                                                actionTarget.onCommitText(" ")
                                                            }
                                                        }
                                                        KeyAction.BACKSPACE -> {
                                                            if (currentLayout.id == "bn_avro" && avroComposingText.isNotEmpty()) {
                                                                avroComposingText = avroComposingText.dropLast(1)
                                                            } else {
                                                                if (englishComposingWord.isNotEmpty()) {
                                                                    englishComposingWord = englishComposingWord.dropLast(1)
                                                                }
                                                                actionTarget.onDeleteBackward()
                                                            }
                                                        }
                                                        KeyAction.ENTER -> {
                                                            englishComposingWord = ""
                                                            performKeyFeedback(android.media.AudioManager.FX_KEYPRESS_RETURN)
                                                            if (currentLayout.id == "bn_avro" && avroComposingText.isNotEmpty()) {
                                                                val primary = avroCandidates.firstOrNull() ?: AvroPhoneticParser.parse(avroComposingText)
                                                                actionTarget.onCommitText(primary)
                                                                avroComposingText = ""
                                                            }
                                                            actionTarget.onEnterAction()
                                                            if (keyboardPreferences.autoCapitalization && currentLayout.languageCode == "en") {
                                                                shiftState = ShiftState.SHIFTED
                                                                isShifted = true
                                                            }
                                                        }
                                                        KeyAction.SHIFT -> {
                                                            if (currentLayout.languageCode == "en") {
                                                                shiftState = when (shiftState) {
                                                                    ShiftState.OFF -> ShiftState.SHIFTED
                                                                    ShiftState.SHIFTED -> ShiftState.CAPS_LOCKED
                                                                    ShiftState.CAPS_LOCKED -> ShiftState.OFF
                                                                }
                                                                isShifted = shiftState != ShiftState.OFF
                                                            } else {
                                                                isShifted = !isShifted
                                                                shiftState = if (isShifted) ShiftState.SHIFTED else ShiftState.OFF
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
                                                            englishComposingWord = ""
                                                            if (avroComposingText.isNotEmpty()) {
                                                                val primary = avroCandidates.firstOrNull() ?: AvroPhoneticParser.parse(avroComposingText)
                                                                actionTarget.onCommitText(primary)
                                                                avroComposingText = ""
                                                            }
                                                            val nextLayout = keyboardPreferences.cycleLayout(context = context)
                                                            currentLayout = nextLayout
                                                            currentLanguage = nextLayout.keyboardLanguage
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
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(if (isFloatingMode) Color.Transparent else bgColor)
            .then(if (isFloatingMode) Modifier else Modifier.navigationBarsPadding())
            .padding(bottom = if (!isFloatingMode && isImeService) 8.dp else 4.dp)
            .onGloballyPositioned { rootCoords ->
                keyboardRootCoordinates = rootCoords
            },
        contentAlignment = if (isFloatingMode) Alignment.BottomCenter else Alignment.TopStart
    ) {
        if (isFloatingMode) {
            // Floating Keyboard Window Card (Gboard style)
            Column(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            floatingOffsetX.roundToInt(),
                            floatingOffsetY.roundToInt()
                        )
                    }
                    .fillMaxWidth(0.92f * floatingScale)
                    .widthIn(min = 280.dp, max = 520.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = Color.Black.copy(alpha = 0.5f)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor.copy(alpha = floatingOpacity))
                    .border(
                        border = BorderStroke(
                            1.dp,
                            Color(currentTheme.accentHex).copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Toolbar
                keyboardToolbarComposable(true)

                // Main keyboard pages
                keyboardPagesContent()

                // Bottom Floating Control & Drag Bar
                FloatingControlBar(
                    theme = currentTheme,
                    opacity = floatingOpacity,
                    scale = floatingScale,
                    onDrag = { dx, dy ->
                        floatingOffsetX += dx
                        floatingOffsetY += dy
                    },
                    onDragEnd = {
                        keyboardPreferences.floatingOffsetX = floatingOffsetX
                        keyboardPreferences.floatingOffsetY = floatingOffsetY
                    },
                    onDock = {
                        isFloatingMode = false
                        keyboardPreferences.isFloatingMode = false
                        performHaptic()
                    },
                    onCycleOpacity = {
                        val nextOpacity = when {
                            floatingOpacity >= 0.92f -> 0.78f
                            floatingOpacity >= 0.75f -> 0.60f
                            else -> 1.0f
                        }
                        floatingOpacity = nextOpacity
                        keyboardPreferences.floatingOpacity = nextOpacity
                        performHaptic()
                    },
                    onCycleScale = {
                        val nextScale = when {
                            floatingScale >= 0.95f -> 0.78f
                            floatingScale >= 0.85f -> 1.0f
                            else -> 0.88f
                        }
                        floatingScale = nextScale
                        keyboardPreferences.floatingScale = nextScale
                        performHaptic()
                    },
                    onResetPosition = {
                        floatingOffsetX = 0f
                        floatingOffsetY = -80f
                        keyboardPreferences.floatingOffsetX = 0f
                        keyboardPreferences.floatingOffsetY = -80f
                        performHaptic()
                    }
                )
            }
        } else {
            // Standard Docked Keyboard
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
                    keyboardToolbarComposable(false)
                    keyboardPagesContent()
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

    val popupWidthDp = 300.dp
    val popupHeightDp = 54.dp
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
            val layouts = listOf(state.leftLayout, state.centerLayout, state.rightLayout)
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

            // Text labels for the 3 layouts (Language Name on top, Layout Name on bottom)
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                layouts.forEachIndexed { index, layout ->
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
                                text = layout.languageName,
                                color = if (isSelected) Color.White else textColor.copy(alpha = 0.85f),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = layout.layoutName,
                                color = if (isSelected) Color.White.copy(alpha = 0.9f) else textColor.copy(alpha = 0.5f),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
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
    longPressDelayMs: Long = 240L,
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

                            // Wait up to 240ms for pointer release. Tap released before timeout -> standard click
                            var isQuickTap = false
                            val timeoutResult = withTimeoutOrNull(longPressDelayMs) {
                                var event = awaitPointerEvent()
                                while (event.changes.any { it.id == down.id && it.pressed }) {
                                    event = awaitPointerEvent()
                                }
                                isQuickTap = true
                                false
                            }

                            if (isQuickTap) {
                                isPressed = false
                                onClick()
                            } else if (timeoutResult == null) {
                                // Genuine long press held past 240ms -> show popup with both base key and extra key!
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
    currentLayout: DynamicKeyboardLayout,
    enabledLayouts: List<DynamicKeyboardLayout>,
    isLanguageSpace: Boolean,
    rootCoordinates: LayoutCoordinates?,
    spacebarCursorControl: Boolean = true,
    onMoveCursor: (Int) -> Unit = {},
    onShowPopup: (left: DynamicKeyboardLayout, center: DynamicKeyboardLayout, right: DynamicKeyboardLayout, bounds: Rect, initialIndex: Int) -> Unit,
    onUpdatePopupIndex: (newIndex: Int, dragDeltaX: Float) -> Unit,
    onDismissPopup: () -> Unit,
    onSelectLayout: (layout: DynamicKeyboardLayout) -> Unit,
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
            .pointerInput(currentLayout, switchMethod, isLanguageSpace, rootCoordinates, enabledLayouts, spacebarCursorControl) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    val triple = getLayoutTriple(currentLayout, enabledLayouts)
                    var currentSelectedIndex = 1
                    var isPopupActive = false

                    val allowSpaceSwipe = isLanguageSpace && switchMethod == LanguageSwitchMethod.SPACEBAR_SWIPE
                    val thresholdPx = with(density) { 30.dp.toPx() }

                    if (allowSpaceSwipe) {
                        // Wait up to 240ms for quick tap or horizontal drag
                        val upBeforeTimeout = withTimeoutOrNull(240L) {
                            var event = awaitPointerEvent()
                            while (event.changes.any { it.id == down.id && it.pressed }) {
                                val change = event.changes.first { it.id == down.id }
                                val deltaX = change.position.x - down.position.x
                                // If user is already swiping horizontally on spacebar and swipe is enabled:
                                if (abs(deltaX) >= thresholdPx) {
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
                                    onSelectLayout(chosen)
                                    onPerformHaptic()
                                    break
                                }

                                val deltaX = change.position.x - down.position.x
                                val newIndex = when {
                                    deltaX < -thresholdPx -> 0 // Left layout
                                    deltaX > thresholdPx -> 2  // Right layout
                                    else -> 1                 // Center layout
                                }

                                if (newIndex != currentSelectedIndex) {
                                    currentSelectedIndex = newIndex
                                    onPerformHaptic()
                                    onUpdatePopupIndex(currentSelectedIndex, deltaX)
                                }
                            }
                        }
                    } else if (spacebarCursorControl) {
                        // Precision Spacebar Cursor Navigation
                        var lastX = down.position.x
                        var accumulatedX = 0f
                        var hasMovedCursor = false
                        val stepPx = with(density) { 16.dp.toPx() }

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break

                            if (!change.pressed) {
                                isPressed = false
                                if (!hasMovedCursor) {
                                    onTap()
                                }
                                break
                            }

                            val dx = change.position.x - lastX
                            lastX = change.position.x
                            accumulatedX += dx

                            if (accumulatedX >= stepPx) {
                                hasMovedCursor = true
                                onMoveCursor(1)
                                onPerformHaptic()
                                accumulatedX -= stepPx
                            } else if (accumulatedX <= -stepPx) {
                                hasMovedCursor = true
                                onMoveCursor(-1)
                                onPerformHaptic()
                                accumulatedX += stepPx
                            }
                        }
                    } else {
                        // Standard tap
                        var event = awaitPointerEvent()
                        while (event.changes.any { it.id == down.id && it.pressed }) {
                            event = awaitPointerEvent()
                        }
                        isPressed = false
                        onTap()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            if (isLanguageSpace) {
                val allowSpaceSwipe = switchMethod == LanguageSwitchMethod.SPACEBAR_SWIPE && enabledLayouts.size > 1
                val showSubtitle = !currentLayout.layoutName.equals(currentLayout.languageName, ignoreCase = true) && currentLayout.languageCode != "en"
                val displayText = if (showSubtitle) "${currentLayout.languageName} (${currentLayout.layoutName})" else currentLayout.languageName

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (allowSpaceSwipe) {
                        Text(
                            text = "‹  ",
                            color = textColor.copy(alpha = 0.45f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = displayText,
                        color = textColor.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false
                    )
                    if (allowSpaceSwipe) {
                        Text(
                            text = "  ›",
                            color = textColor.copy(alpha = 0.45f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Text(
                    text = keyDef.primaryText,
                    color = textColor.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false
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
            .pointerInput(keyboardPreferences.deleteHoldMode, keyboardPreferences.deleteInitialDelayMs, keyboardPreferences.deleteRepeatIntervalMs, keyboardPreferences.backspaceSwipeDelete) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    onPerformHaptic()
                    onDelete() // Delete 1 character immediately on initial tap

                    val initialDelay = keyboardPreferences.deleteInitialDelayMs.toLong()
                    val repeatInterval = keyboardPreferences.deleteRepeatIntervalMs.toLong()
                    val mode = keyboardPreferences.deleteHoldMode
                    val hapticOnRepeat = keyboardPreferences.deleteHapticOnRepeat
                    val allowSwipeDelete = keyboardPreferences.backspaceSwipeDelete
                    val swipeThreshold = with(density) { 26.dp.toPx() }
                    var lastSwipeX = down.position.x
                    var didSwipeDelete = false

                    // Wait for initial delay or detect swipe left
                    val upBeforeTimeout = withTimeoutOrNull(initialDelay) {
                        var event = awaitPointerEvent()
                        while (event.changes.any { it.id == down.id && it.pressed }) {
                            val change = event.changes.first { it.id == down.id }
                            if (allowSwipeDelete) {
                                val deltaX = change.position.x - lastSwipeX
                                if (deltaX <= -swipeThreshold) {
                                    didSwipeDelete = true
                                    isHoldingWordDelete = true
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
                        // Quick tap release
                        isPressed = false
                        isHoldingWordDelete = false
                    } else if (!didSwipeDelete) {
                        // Hold timed out without swipe -> Continuous auto-removal begins!
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
                    } else {
                        // Swipe delete was triggered, continue tracking swipe left until finger lifts
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) {
                                isPressed = false
                                isHoldingWordDelete = false
                                break
                            }
                            val deltaX = change.position.x - lastSwipeX
                            if (deltaX <= -swipeThreshold) {
                                onDeleteWord()
                                onPerformHaptic()
                                lastSwipeX = change.position.x
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

@Composable
private fun AvroPhoneticSuggestionStrip(
    composingText: String,
    candidates: List<String>,
    theme: KeyboardThemeOption,
    onSelectCandidate: (String) -> Unit,
    onClearComposing: () -> Unit
) {
    val accentColor = Color(theme.accentHex)
    val textColor = Color(theme.textHex)
    val surfaceColor = Color(theme.surfaceHex)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(surfaceColor)
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 6.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = accentColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = if (composingText.isNotEmpty()) "[$composingText]" else "Avro",
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            Text(
                text = " ➔ ",
                color = textColor.copy(alpha = 0.45f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (candidates.isEmpty()) {
            Text(
                text = "English to Bangla (e.g. ami ➔ আমি)",
                color = textColor.copy(alpha = 0.5f),
                fontSize = 11.sp,
                maxLines = 1
            )
        } else {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                candidates.forEachIndexed { index, candidate ->
                    val isFirst = index == 0
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onSelectCandidate(candidate) },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isFirst) accentColor else surfaceColor,
                        border = if (isFirst) null else androidx.compose.foundation.BorderStroke(0.5.dp, textColor.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = candidate,
                            color = if (isFirst) Color.White else textColor,
                            fontSize = 13.5.sp,
                            fontWeight = if (isFirst) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            if (composingText.isNotEmpty()) {
                IconButton(
                    onClick = onClearComposing,
                    modifier = Modifier.size(24.dp)
                ) {
                    Text("✕", color = textColor.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


@Composable
private fun EnglishWordSuggestionStrip(
    composingWord: String,
    candidates: List<String>,
    theme: KeyboardThemeOption,
    onSelectCandidate: (String) -> Unit,
    onClearComposing: () -> Unit
) {
    val accentColor = Color(theme.accentHex)
    val textColor = Color(theme.textHex)
    val surfaceColor = Color(theme.surfaceHex)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(surfaceColor)
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = accentColor.copy(alpha = 0.15f),
            modifier = Modifier.padding(end = 6.dp)
        ) {
            Text(
                text = "⚡ ",
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            candidates.forEachIndexed { index, candidate ->
                val isFirst = index == 0
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onSelectCandidate(candidate) },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isFirst) accentColor else surfaceColor,
                    border = if (isFirst) null else androidx.compose.foundation.BorderStroke(0.5.dp, textColor.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = candidate,
                        color = if (isFirst) Color.White else textColor,
                        fontSize = 13.5.sp,
                        fontWeight = if (isFirst) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }
        }

        IconButton(
            onClick = onClearComposing,
            modifier = Modifier.size(24.dp)
        ) {
            Text("✕", color = textColor.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FloatingControlBar(
    theme: KeyboardThemeOption,
    opacity: Float,
    scale: Float,
    onDrag: (dx: Float, dy: Float) -> Unit,
    onDragEnd: () -> Unit,
    onDock: () -> Unit,
    onCycleOpacity: () -> Unit,
    onCycleScale: () -> Unit,
    onResetPosition: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = Color(theme.accentHex)
    val surfaceColor = Color(theme.surfaceHex)
    val textColor = Color(theme.textHex)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        color = surfaceColor.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Dock button
            IconButton(
                onClick = onDock,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.VerticalAlignBottom,
                    contentDescription = "Dock Keyboard",
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Central Drag Handle Pill
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
                    .padding(horizontal = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = onDragEnd
                        ) { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount.x, dragAmount.y)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DragIndicator,
                        contentDescription = "Drag to move keyboard",
                        tint = textColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Hold & Drag",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
            }

            // Quick Opacity Toggle
            IconButton(
                onClick = onCycleOpacity,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Opacity,
                    contentDescription = "Change Opacity",
                    tint = textColor.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Quick Scale / Size Toggle
            IconButton(
                onClick = onCycleScale,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AspectRatio,
                    contentDescription = "Resize Keyboard",
                    tint = textColor.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Reset Position
            IconButton(
                onClick = onResetPosition,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.RestartAlt,
                    contentDescription = "Reset Position",
                    tint = textColor.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
