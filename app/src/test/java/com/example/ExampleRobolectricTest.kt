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
@Config(sdk = [36])
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
        val context = ApplicationProvider.getApplicationContext<Context>()
        val layouts = com.example.ime.KeyboardLayoutRegistry.getLayouts(context)
        val enLayout = layouts.first { it.id == "en_qwerty" }

        val enTriple = com.example.ui.keyboard.getLayoutTriple(enLayout, layouts)
        assertEquals(enLayout.id, enTriple.center.id)
        assertNotNull(enTriple.left)
        assertNotNull(enTriple.right)

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

    @Test
    fun testToolbarCustomizationAndMaxConstraint() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = KeyboardPreferences(context)
        prefs.resetToolbarTools()

        // 1. Initial defaults: 7 tools active
        val defaultTools = prefs.enabledToolbarTools
        assertEquals(7, defaultTools.size)
        assertEquals(7, com.example.data.ToolbarTool.MAX_ENABLED_TOOLS)

        // 2. Attempting to add an 8th tool should fail (return false)
        val unusedTool = com.example.data.ToolbarTool.entries.first { !defaultTools.contains(it) }
        val addedResult = prefs.toggleToolbarTool(unusedTool, true)
        org.junit.Assert.assertFalse("Adding 8th tool should be rejected", addedResult)
        assertEquals(7, prefs.enabledToolbarTools.size)

        // 3. Turning off one tool allows adding a different tool
        val firstTool = defaultTools.first()
        val removeResult = prefs.toggleToolbarTool(firstTool, false)
        assertTrue(removeResult)
        assertEquals(6, prefs.enabledToolbarTools.size)

        // Now adding the new tool succeeds
        val addedAfterRemove = prefs.toggleToolbarTool(unusedTool, true)
        assertTrue(addedAfterRemove)
        assertEquals(7, prefs.enabledToolbarTools.size)
        assertTrue(prefs.isToolbarToolEnabled(unusedTool))

        // 4. Test reorder
        val toolAtZero = prefs.enabledToolbarTools[0]
        val toolAtOne = prefs.enabledToolbarTools[1]
        prefs.moveToolbarTool(0, 1)
        assertEquals(toolAtZero, prefs.enabledToolbarTools[1])
        assertEquals(toolAtOne, prefs.enabledToolbarTools[0])

        // 5. Test reset
        prefs.resetToolbarTools()
        assertEquals(7, prefs.enabledToolbarTools.size)
    }

    @Test
    fun testXmlResourceBasedKeyboardLoading() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        KeyboardLayoutModel.init(context)

        // 1. Verify XML array resources exist and are non-empty
        val enRow1 = context.resources.getStringArray(R.array.keyboard_en_row1_lower)
        assertEquals(10, enRow1.size)
        assertEquals("q", enRow1[0])
        assertEquals("p", enRow1[9])

        val bnRow1 = context.resources.getStringArray(R.array.keyboard_bn_row1_unshifted)
        assertEquals(10, bnRow1.size)
        assertEquals("ঙ", bnRow1[0])

        val hiRow1 = context.resources.getStringArray(R.array.keyboard_hi_row1_consonants)
        assertEquals(10, hiRow1.size)
        assertEquals("क", hiRow1[0])

        // 2. Verify KeyboardLayoutModel reads from XML
        val englishLayout = KeyboardLayoutModel.getEnglishLayout(ShiftState.OFF, isCaps = false)
        assertEquals("q", englishLayout[0].keys[0].primaryText)
        assertEquals("৳", englishLayout[0].keys[0].secondaryText)

        val banglaLayout = KeyboardLayoutModel.getBanglaLayout(shifted = false)
        assertEquals("ঙ", banglaLayout[0].keys[0].primaryText)
        assertEquals("ং", banglaLayout[0].keys[0].secondaryText)

        val hindiLayout = KeyboardLayoutModel.getHindiLayout(shifted = false)
        assertEquals("क", hindiLayout[0].keys[0].primaryText)

        // 3. Verify Toolbar Tools localized from XML
        val emojiTool = com.example.data.ToolbarTool.EMOJI
        val localizedTitle = emojiTool.getLocalizedTitle(context)
        assertEquals("Emoji & Emoticons", localizedTitle)
    }

    @Test
    fun testDynamicXmlKeyboardRegistry() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val layouts = com.example.ime.KeyboardLayoutRegistry.getLayouts(context)
        assertTrue("Layouts should be loaded from XML registry", layouts.isNotEmpty())

        val layoutIds = layouts.map { it.id }
        assertTrue("en_qwerty must be registered in XML", layoutIds.contains("en_qwerty"))
        assertTrue("bn_jatiya must be registered in XML", layoutIds.contains("bn_jatiya"))
        assertTrue("bn_avro must be registered in XML", layoutIds.contains("bn_avro"))
        assertTrue("hi_devanagari must be registered in XML", layoutIds.contains("hi_devanagari"))

        // Test Bangla Avro Layout properties (Avro Phonetic with English QWERTY letters and Bangla phonetic hints)
        val avro = layouts.first { it.id == "bn_avro" }
        assertEquals("বাংলা", avro.languageName)
        assertEquals("Avro (Phonetic)", avro.layoutName)
        assertTrue(avro.row1.isNotEmpty())
        assertEquals("q", avro.row1[0])
        assertEquals("p", avro.row1[9])
        assertTrue("Row 1 must have Bangla phonetic hints", avro.row1Sec.isNotEmpty())

        // Verify buildRows converts DynamicKeyboardLayout into KeyboardRow objects
        val rows = com.example.ime.KeyboardLayoutRegistry.buildRows(
            layout = avro,
            shiftState = ShiftState.OFF,
            isCaps = false,
            showGlobeKey = true
        )
        assertEquals(4, rows.size)
        assertEquals("q", rows[0].keys[0].primaryText)

        // Verify Space Key on Row 4
        val spaceKey = rows[3].keys.firstOrNull { it.action == com.example.ime.KeyAction.SPACE }
        assertNotNull("Row 4 must contain space key", spaceKey)

        // Verify DynamicLayoutTriple for spacebar swipe switcher
        val triple = com.example.ui.keyboard.getLayoutTriple(avro, layouts)
        assertEquals(avro.id, triple.center.id)
        assertNotNull(triple.left)
        assertNotNull(triple.right)

        // Verify KeyboardPreferences active layout and cycle
        val prefs = KeyboardPreferences(context)
        prefs.activeLayoutId = "bn_avro"
        assertEquals("bn_avro", prefs.activeLayoutId)
        val active = prefs.getActiveLayout(context)
        assertEquals("Avro (Phonetic)", active.layoutName)
        assertEquals("বাংলা", active.languageName)

        val cycled = prefs.cycleLayout(context = context)
        assertNotNull(cycled)
        assertTrue(cycled.id != "bn_avro" || layouts.size == 1)
    }

    @Test
    fun testAvroPhoneticParser() {
        val parser = com.example.ime.AvroPhoneticParser

        // Common Bengali conversational words typed in English
        assertEquals("আমি", parser.parse("ami"))
        assertEquals("তুমি", parser.parse("tumi"))
        assertEquals("কেমন", parser.parse("kemon"))
        assertEquals("আছো", parser.parse("acho"))
        assertEquals("ভালো", parser.parse("bhalo"))
        assertEquals("বাংলা", parser.parse("bangla"))
        assertEquals("ধন্যবাদ", parser.parse("dhonnobad"))
        assertEquals("দেশ", parser.parse("desh"))
        assertEquals("সুন্দর", parser.parse("shundor"))

        // Consonant and kar rule conversions
        assertEquals("ক", parser.parse("k"))
        assertEquals("খ", parser.parse("kh"))
        assertEquals("গ", parser.parse("g"))
        assertEquals("ঘ", parser.parse("gh"))
        assertEquals("কা", parser.parse("ka"))
        assertEquals("কি", parser.parse("ki"))
        assertEquals("কু", parser.parse("ku"))
        assertEquals("কে", parser.parse("ke"))
        assertEquals("কো", parser.parse("ko"))

        // Candidate prediction list
        val candidates = parser.getCandidates("ami")
        assertTrue("Candidates must include 'আমি'", candidates.contains("আমি"))
        assertTrue("Candidates list should have suggestions", candidates.isNotEmpty())
    }
}

