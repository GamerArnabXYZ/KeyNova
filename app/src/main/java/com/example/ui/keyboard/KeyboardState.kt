package com.example.ui.keyboard

import com.example.data.ActiveKeyboardPage
import com.example.data.KeyboardLanguage
import com.example.data.KeyboardThemeOption
import com.example.data.OneHandedMode
import com.example.ime.ShiftState

data class KeyboardUiState(
    val activePage: ActiveKeyboardPage = ActiveKeyboardPage.LETTERS,
    val language: KeyboardLanguage = KeyboardLanguage.ENGLISH,
    val shiftState: ShiftState = ShiftState.OFF,
    val theme: KeyboardThemeOption = KeyboardThemeOption.LIGHT,
    val showNumberRow: Boolean = true,
    val keyHeightDp: Int = 48,
    val keySpacingDp: Int = 4,
    val oneHandedMode: OneHandedMode = OneHandedMode.OFF,
    val hapticFeedback: Boolean = true,
    val soundFeedback: Boolean = false,
    val isBanglaShifted: Boolean = false,
    val isHindiShifted: Boolean = false
)

interface KeyboardActionTarget {
    fun onCommitText(text: String)
    fun onDeleteBackward()
    fun onDeleteWordBackward() {
        onDeleteBackward()
    }
    fun onEnterAction()
    fun onMoveCursor(offset: Int) {}
    fun onMoveCursorUp() {}
    fun onMoveCursorDown() {}
    fun onMoveCursorToStart() {}
    fun onMoveCursorToEnd() {}
    fun onToggleSelectMode(): Boolean { return false }
    fun onSelectAll() {}
    fun onCopy() {}
    fun onPaste() {}
    fun onClearAll() {}
    fun onCycleCase() {}
    fun onVoiceTyping() {}
    fun onDoubleSpacePeriod() {}
}
