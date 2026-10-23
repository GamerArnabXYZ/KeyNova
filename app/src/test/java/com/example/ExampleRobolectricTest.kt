package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ClipboardRepository
import com.example.data.KeyboardLanguage
import com.example.data.KeyboardPreferences
import com.example.data.KeyboardThemeOption
import com.example.ime.KeyboardLayoutModel
import com.example.ime.ShiftState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun testAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("KeyNova", appName)
    }

    @Test
    fun testKeyboardLayoutModels() {
        // English
        val englishRows = KeyboardLayoutModel.getEnglishLayout(ShiftState.OFF, isCaps = false)
        assertEquals(4, englishRows.size)
        assertEquals("q", englishRows[0].keys[0].primaryText)

        // Bangla (Jatiya / National)
        val banglaRows = KeyboardLayoutModel.getBanglaLayout(shifted = false)
        assertEquals(4, banglaRows.size)
        assertEquals("ঙ", banglaRows[0].keys[0].primaryText)
        assertEquals("ং", banglaRows[0].keys[0].secondaryText)

        // Symbols Page 1 (?123) & Symbols Page 2 (= \ <)
        val sym1 = KeyboardLayoutModel.getSymbolsPage1()
        assertEquals(5, sym1.size)
        val sym2 = KeyboardLayoutModel.getSymbolsPage2()
        assertEquals(5, sym2.size)
        assertEquals("☞", sym2[0].keys[0].primaryText)

        // Hindi
        val hindiRows = KeyboardLayoutModel.getHindiLayout(shifted = false)
        assertEquals(4, hindiRows.size)
        assertEquals("क", hindiRows[0].keys[0].primaryText)

        // Coding
        val codingRows = KeyboardLayoutModel.getCodingLayout()
        assertTrue(codingRows.isNotEmpty())
        assertEquals("{", codingRows[0].keys[0].primaryText)

        // Gaming
        val gamingRows = KeyboardLayoutModel.getGamingLayout()
        assertTrue(gamingRows.isNotEmpty())
        assertEquals("1", gamingRows[0].keys[0].primaryText)
    }

    @Test
    fun testClipboardRepository() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ClipboardRepository(context)

        repo.addItem("Testing KeyNova Clipboard")
        val items = repo.getItems()
        assertTrue(items.any { it.text == "Testing KeyNova Clipboard" })
    }

    @Test
    fun testKeyboardPreferences() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = KeyboardPreferences(context)

        prefs.theme = KeyboardThemeOption.BLUE
        assertEquals(KeyboardThemeOption.BLUE, prefs.theme)

        prefs.activeLanguage = KeyboardLanguage.ENGLISH
        prefs.cycleLanguage()
        assertNotNull(prefs.activeLanguage)

        // Delete hold settings tests
        assertEquals(com.example.data.DeleteHoldMode.WORD_BY_WORD, prefs.deleteHoldMode)
        prefs.deleteHoldMode = com.example.data.DeleteHoldMode.CHARACTER_BY_CHARACTER
        assertEquals(com.example.data.DeleteHoldMode.CHARACTER_BY_CHARACTER, prefs.deleteHoldMode)

        prefs.deleteInitialDelayMs = 400
        assertEquals(400, prefs.deleteInitialDelayMs)

        prefs.deleteRepeatIntervalMs = 80
        assertEquals(80, prefs.deleteRepeatIntervalMs)

        prefs.deleteHapticOnRepeat = true
        assertTrue(prefs.deleteHapticOnRepeat)
    }

    @Test
    fun testNewToolbarPages() {
        val pages = com.example.data.ActiveKeyboardPage.entries
        assertTrue(pages.contains(com.example.data.ActiveKeyboardPage.TEXT_EDITING))
        assertTrue(pages.contains(com.example.data.ActiveKeyboardPage.NUMPAD))
        assertTrue(pages.contains(com.example.data.ActiveKeyboardPage.CLIPBOARD))
    }

    @Test
    fun testInputMethodService() {
        val controller = org.robolectric.Robolectric.buildService(com.example.ime.KeyNovaInputMethodService::class.java)
        val service = controller.create().get()
        assertNotNull(service)
        val view = service.onCreateInputView()
        assertNotNull(view)
        service.onStartInputView(android.view.inputmethod.EditorInfo(), false)
    }

    @Test
    fun testLanguageTripleAndSecondaryResolution() {
        val enTriple = com.example.ui.keyboard.getLanguageTriple(KeyboardLanguage.ENGLISH)
        assertEquals(KeyboardLanguage.ENGLISH, enTriple.center)
        assertEquals(KeyboardLanguage.BANGLA, enTriple.left)
        assertEquals(KeyboardLanguage.HINDI, enTriple.right)

        val bnTriple = com.example.ui.keyboard.getLanguageTriple(KeyboardLanguage.BANGLA)
        assertEquals(KeyboardLanguage.BANGLA, bnTriple.center)
        assertEquals(KeyboardLanguage.ENGLISH, bnTriple.left)
        assertEquals(KeyboardLanguage.HINDI, bnTriple.right)

        val hiTriple = com.example.ui.keyboard.getLanguageTriple(KeyboardLanguage.HINDI)
        assertEquals(KeyboardLanguage.HINDI, hiTriple.center)
        assertEquals(KeyboardLanguage.BANGLA, hiTriple.left)
        assertEquals(KeyboardLanguage.ENGLISH, hiTriple.right)

        // Secondary text resolution
        val (dispQ, commitQ) = com.example.ui.keyboard.resolveSecondaryText("q", "1")
        assertEquals("1", dispQ)
        assertEquals("1", commitQ)

        val (dispComma, commitComma) = com.example.ui.keyboard.resolveSecondaryText(",", "...")
        assertEquals(";", dispComma)
        assertEquals(";", commitComma)

        val (dispPeriod, commitPeriod) = com.example.ui.keyboard.resolveSecondaryText(".", "...")
        assertEquals("...", dispPeriod)
        assertEquals("...", commitPeriod)

        val (dispDari, commitDari) = com.example.ui.keyboard.resolveSecondaryText("।", "...")
        assertEquals("!", dispDari)
        assertEquals("!", commitDari)
    }
}

