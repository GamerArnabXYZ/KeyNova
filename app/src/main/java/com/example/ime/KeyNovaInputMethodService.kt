package com.example.ime

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.data.ClipboardRepository
import com.example.data.KeyboardPreferences
import com.example.data.SnippetsRepository
import com.example.ui.keyboard.KeyNovaKeyboardView
import com.example.ui.keyboard.KeyboardActionTarget

class KeyNovaInputMethodService : InputMethodService(),
    LifecycleOwner,
    SavedStateRegistryOwner,
    ViewModelStoreOwner,
    KeyboardActionTarget {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val vmStore = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = vmStore

    private lateinit var prefs: KeyboardPreferences
    private lateinit var clipboardRepo: ClipboardRepository
    private lateinit var snippetsRepo: SnippetsRepository
    private var clipboardManager: ClipboardManager? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val clipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
        syncPrimaryClip()
    }

    private fun syncPrimaryClip() {
        try {
            clipboardRepo.syncFromSystemClipboard(this)
        } catch (_: Exception) {}
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        prefs = KeyboardPreferences(this)
        clipboardRepo = ClipboardRepository(this)
        snippetsRepo = SnippetsRepository(this)
        KeyboardLayoutModel.init(this)

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        try {
            clipboardManager?.addPrimaryClipChangedListener(clipChangedListener)
        } catch (_: Exception) {}
        syncPrimaryClip()
    }

    override fun onEvaluateInputViewShown(): Boolean {
        return true
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        return false
    }

    override fun onShowInputRequested(flags: Int, configChange: Boolean): Boolean {
        return true
    }

    override fun onCreateInputView(): View {
        // Ensure decorView on the dialog window has full Jetpack lifecycle awareness
        window?.window?.let { win ->
            val bg = prefs.theme.backgroundHex.toInt()
            win.navigationBarColor = bg
            win.decorView.setBackgroundColor(bg)
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(win, false)
            win.decorView.setViewTreeLifecycleOwner(this)
            win.decorView.setViewTreeSavedStateRegistryOwner(this)
            win.decorView.setViewTreeViewModelStoreOwner(this)
        }

        val composeView = ComposeView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(prefs.theme.backgroundHex.toInt())

            // CRITICAL: Keep composition alive across hide/show cycles in other apps
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@KeyNovaInputMethodService))

            setViewTreeLifecycleOwner(this@KeyNovaInputMethodService)
            setViewTreeSavedStateRegistryOwner(this@KeyNovaInputMethodService)
            setViewTreeViewModelStoreOwner(this@KeyNovaInputMethodService)

            setContent {
                KeyNovaKeyboardView(
                    keyboardPreferences = prefs,
                    clipboardRepository = clipboardRepo,
                    snippetsRepository = snippetsRepo,
                    actionTarget = this@KeyNovaInputMethodService,
                    isImeService = true,
                    onOpenSettings = {
                        val intent = Intent(this@KeyNovaInputMethodService, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
        return composeView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)

        prefs.notifyPreferencesChanged()
        syncPrimaryClip()

        window?.window?.let { win ->
            val bg = prefs.theme.backgroundHex.toInt()
            win.navigationBarColor = bg
            win.decorView.setBackgroundColor(bg)
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(win, false)
            win.decorView.setViewTreeLifecycleOwner(this)
            win.decorView.setViewTreeSavedStateRegistryOwner(this)
            win.decorView.setViewTreeViewModelStoreOwner(this)
        }

        if (lifecycleRegistry.currentState != Lifecycle.State.RESUMED) {
            if (lifecycleRegistry.currentState == Lifecycle.State.CREATED) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            }
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        if (lifecycleRegistry.currentState == Lifecycle.State.RESUMED) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            clipboardManager?.removePrimaryClipChangedListener(clipChangedListener)
        } catch (_: Exception) {}
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        vmStore.clear()
    }

    // KeyboardActionTarget Implementation
    override fun onCommitText(text: String) {
        val ic = currentInputConnection ?: return
        ic.finishComposingText()
        ic.commitText(text, 1)
    }

    override fun onDeleteBackward() {
        val ic = currentInputConnection ?: return
        val selectedText = ic.getSelectedText(0)
        if (selectedText.isNullOrEmpty()) {
            ic.deleteSurroundingText(1, 0)
        } else {
            ic.commitText("", 1)
        }
    }

    override fun onDeleteWordBackward() {
        val ic = currentInputConnection ?: return
        val selectedText = ic.getSelectedText(0)
        if (!selectedText.isNullOrEmpty()) {
            ic.commitText("", 1)
            return
        }

        // Look back up to 120 characters before cursor to find the word boundary
        val textBefore = ic.getTextBeforeCursor(120, 0)?.toString()
        if (textBefore.isNullOrEmpty()) {
            ic.deleteSurroundingText(1, 0)
            return
        }

        var lenToDelete = 0
        var i = textBefore.length - 1

        // Skip any trailing spaces/newlines
        while (i >= 0 && textBefore[i].isWhitespace()) {
            lenToDelete++
            i--
        }

        // Delete previous word characters
        while (i >= 0 && !textBefore[i].isWhitespace()) {
            lenToDelete++
            i--
        }

        if (lenToDelete == 0) {
            lenToDelete = 1
        }

        ic.deleteSurroundingText(lenToDelete, 0)
    }

    override fun onEnterAction() {
        val ic = currentInputConnection ?: return
        val info = currentInputEditorInfo
        val imeAction = info?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE
        if (imeAction != EditorInfo.IME_ACTION_NONE && imeAction != EditorInfo.IME_ACTION_UNSPECIFIED) {
            ic.performEditorAction(imeAction)
        } else {
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }
    }

    private var isSelecting = false

    override fun onToggleSelectMode(): Boolean {
        isSelecting = !isSelecting
        return isSelecting
    }

    override fun onMoveCursor(offset: Int) {
        val ic = currentInputConnection ?: return
        val keyCode = if (offset < 0) KeyEvent.KEYCODE_DPAD_LEFT else KeyEvent.KEYCODE_DPAD_RIGHT
        val count = kotlin.math.abs(offset).coerceAtLeast(1)
        for (i in 0 until count) {
            if (isSelecting) {
                ic.sendKeyEvent(KeyEvent(0, 0, KeyEvent.ACTION_DOWN, keyCode, 0, KeyEvent.META_SHIFT_ON))
                ic.sendKeyEvent(KeyEvent(0, 0, KeyEvent.ACTION_UP, keyCode, 0, KeyEvent.META_SHIFT_ON))
            } else {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
            }
        }
    }

    override fun onMoveCursorUp() {
        val ic = currentInputConnection ?: return
        if (isSelecting) {
            ic.sendKeyEvent(KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP, 0, KeyEvent.META_SHIFT_ON))
            ic.sendKeyEvent(KeyEvent(0, 0, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_UP, 0, KeyEvent.META_SHIFT_ON))
        } else {
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP))
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_UP))
        }
    }

    override fun onMoveCursorDown() {
        val ic = currentInputConnection ?: return
        if (isSelecting) {
            ic.sendKeyEvent(KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN, 0, KeyEvent.META_SHIFT_ON))
            ic.sendKeyEvent(KeyEvent(0, 0, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_DOWN, 0, KeyEvent.META_SHIFT_ON))
        } else {
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN))
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_DOWN))
        }
    }

    override fun onMoveCursorToStart() {
        val ic = currentInputConnection ?: return
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_HOME))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MOVE_HOME))
    }

    override fun onMoveCursorToEnd() {
        val ic = currentInputConnection ?: return
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_END))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MOVE_END))
    }

    override fun onSelectAll() {
        val ic = currentInputConnection ?: return
        ic.performContextMenuAction(android.R.id.selectAll)
    }

    override fun onCopy() {
        val ic = currentInputConnection ?: return
        ic.performContextMenuAction(android.R.id.copy)
        mainHandler.postDelayed({
            syncPrimaryClip()
        }, 150)
    }

    override fun onPaste() {
        val ic = currentInputConnection ?: return
        val cm = clipboardManager ?: getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clipText = if (cm != null && cm.hasPrimaryClip()) {
            cm.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString()
        } else null

        if (!clipText.isNullOrEmpty()) {
            ic.finishComposingText()
            ic.commitText(clipText, 1)
            clipboardRepo.addItem(clipText)
        } else {
            ic.performContextMenuAction(android.R.id.paste)
        }
    }

    override fun onClearAll() {
        val ic = currentInputConnection ?: return
        ic.performContextMenuAction(android.R.id.selectAll)
        ic.commitText("", 1)
    }

    override fun onCycleCase() {
        val ic = currentInputConnection ?: return
        val selected = ic.getSelectedText(0)?.toString()
        if (!selected.isNullOrEmpty()) {
            val cycled = when {
                selected.all { it.isUpperCase() || !it.isLetter() } -> selected.lowercase()
                selected.all { it.isLowerCase() || !it.isLetter() } -> selected.replaceFirstChar { it.uppercase() }
                else -> selected.uppercase()
            }
            ic.commitText(cycled, 1)
        }
    }

    override fun onVoiceTyping() {
        try {
            val intent = Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Speak now to type...")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (_: Exception) {}
    }

    override fun onDoubleSpacePeriod() {
        val ic = currentInputConnection ?: return
        val textBefore = ic.getTextBeforeCursor(2, 0)?.toString()
        if (textBefore?.endsWith(" ") == true) {
            ic.deleteSurroundingText(1, 0)
            ic.commitText(". ", 1)
        } else {
            ic.commitText(". ", 1)
        }
    }
}
