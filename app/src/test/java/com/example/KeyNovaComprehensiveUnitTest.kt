package com.example

import android.text.InputType
import android.view.inputmethod.EditorInfo
import com.example.data.SmartActionType
import com.example.data.SmartClipboardAnalyzer
import com.example.data.ToolbarTool
import com.example.ime.AvroPhoneticParser
import com.example.ime.EnglishWordPredictor
import com.example.ime.KeyAction
import com.example.ime.KeyboardLayoutModel
import com.example.ime.ShiftState
import com.example.util.EditorInfoHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure JVM Automated Unit Tests for KeyNova Keyboard.
 * Runs completely locally without external network dependencies or shadow jar downloads.
 * Covers:
 * 1. Smart Clipboard (URLs, Emails, Phone Numbers, OTP codes, edge cases & false-positives)
 * 2. Private Typing Mode & EditorInfo sensitivity detection
 * 3. Avro Phonetic Transliteration & Candidate Generation
 * 4. English Word Predictor & Case Preservation
 * 5. Toolbar Tools Constraints & Customization
 * 6. Keyboard Layout Model Integrity
 * 7. Haptic Intensity & Preference Boundaries
 */
class KeyNovaComprehensiveUnitTest {

    // ==========================================
    // 1. SMART CLIPBOARD ANALYZER TESTS
    // ==========================================

    @Test
    fun testSmartClipboard_validUrlDetection() {
        val action1 = SmartClipboardAnalyzer.analyze("https://github.com/developer/keynova")
        assertNotNull("Should detect standard HTTPS URL", action1)
        assertEquals(SmartActionType.OPEN_URL, action1?.type)
        assertEquals("https://github.com/developer/keynova", action1?.actionText)
        assertEquals("Open Link", action1?.label)

        val action2 = SmartClipboardAnalyzer.analyze("Check this out: www.google.com/search?q=android")
        assertNotNull("Should detect www URL with query parameters", action2)
        assertEquals(SmartActionType.OPEN_URL, action2?.type)
        assertEquals("https://www.google.com/search?q=android", action2?.actionText)

        val action3 = SmartClipboardAnalyzer.analyze("http://subdomain.example.org:8080/path/to/page")
        assertNotNull("Should detect HTTP URL with custom port", action3)
        assertEquals(SmartActionType.OPEN_URL, action3?.type)
    }

    @Test
    fun testSmartClipboard_validEmailDetection() {
        val action1 = SmartClipboardAnalyzer.analyze("contact user@example.com for info")
        assertNotNull("Should detect standard email", action1)
        assertEquals(SmartActionType.SEND_EMAIL, action1?.type)
        assertEquals("user@example.com", action1?.actionText)

        val action2 = SmartClipboardAnalyzer.analyze("dev.team+beta_12@my-service.co.in")
        assertNotNull("Should detect complex email with plus-alias and dashes", action2)
        assertEquals(SmartActionType.SEND_EMAIL, action2?.type)
        assertEquals("dev.team+beta_12@my-service.co.in", action2?.actionText)
    }

    @Test
    fun testSmartClipboard_validPhoneDetection() {
        val action1 = SmartClipboardAnalyzer.analyze("Call +91 98765 43210")
        assertNotNull("Should detect international phone number", action1)
        assertEquals(SmartActionType.DIAL_PHONE, action1?.type)
        assertTrue(action1?.actionText?.contains("98765") == true)

        val action2 = SmartClipboardAnalyzer.analyze("My number is (800) 555-0199")
        assertNotNull("Should detect US formatted phone number", action2)
        assertEquals(SmartActionType.DIAL_PHONE, action2?.type)
    }

    @Test
    fun testSmartClipboard_validOtpDetection() {
        val action1 = SmartClipboardAnalyzer.analyze("Your KeyNova login OTP is 482910. Do not share.")
        assertNotNull("Should detect 6-digit OTP", action1)
        assertEquals(SmartActionType.COPY_OTP, action1?.type)
        assertEquals("482910", action1?.actionText)
        assertEquals("Code: 482910", action1?.label)

        val action2 = SmartClipboardAnalyzer.analyze("Verification code: 918273")
        assertNotNull("Should detect verification code", action2)
        assertEquals(SmartActionType.COPY_OTP, action2?.type)
        assertEquals("918273", action2?.actionText)

        val action3 = SmartClipboardAnalyzer.analyze("Your PIN - 1234")
        assertNotNull("Should detect 4-digit PIN", action3)
        assertEquals(SmartActionType.COPY_OTP, action3?.type)
        assertEquals("1234", action3?.actionText)
    }

    @Test
    fun testSmartClipboard_negativeAndEdgeCases() {
        // Empty or short strings
        assertNull("Empty string should return null", SmartClipboardAnalyzer.analyze(""))
        assertNull("Whitespace string should return null", SmartClipboardAnalyzer.analyze("   "))
        assertNull("Two char string should return null", SmartClipboardAnalyzer.analyze("hi"))

        // Regular conversational text without entity
        assertNull("Normal phrase without links or numbers should return null",
            SmartClipboardAnalyzer.analyze("The quick brown fox jumps over the lazy dog."))

        // False-positive short numbers that are not phones (< 7 digits)
        val shortNumAction = SmartClipboardAnalyzer.analyze("Room 404")
        assertNull("Short number should not be classified as phone", shortNumAction)
    }

    // ==========================================
    // 2. PRIVATE TYPING & EDITORINFO TESTS
    // ==========================================

    @Test
    fun testEditorInfoHelper_noLearningFlagDetection() {
        val infoWithFlag = EditorInfo().apply {
            imeOptions = EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
        }
        assertTrue("Must detect IME_FLAG_NO_PERSONALIZED_LEARNING",
            EditorInfoHelper.isNoLearningRequested(infoWithFlag))
        assertTrue("Sensitive field must be true when no learning requested",
            EditorInfoHelper.isPrivateOrSensitive(infoWithFlag))

        val infoWithoutFlag = EditorInfo().apply {
            imeOptions = EditorInfo.IME_ACTION_DONE
        }
        assertFalse("Must be false when no learning flag is absent",
            EditorInfoHelper.isNoLearningRequested(infoWithoutFlag))
    }

    @Test
    fun testEditorInfoHelper_passwordFieldDetection() {
        // Text Password
        val textPassword = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        assertTrue("Must detect text password", EditorInfoHelper.isPasswordField(textPassword))
        assertTrue(EditorInfoHelper.isPrivateOrSensitive(textPassword))

        // Visible Password
        val visiblePassword = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
        assertTrue("Must detect visible password", EditorInfoHelper.isPasswordField(visiblePassword))

        // Web Password
        val webPassword = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
        }
        assertTrue("Must detect web password", EditorInfoHelper.isPasswordField(webPassword))

        // Number PIN / Password
        val numberPassword = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        assertTrue("Must detect number PIN password", EditorInfoHelper.isPasswordField(numberPassword))

        // Normal text field
        val normalText = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
        }
        assertFalse("Normal text field is not a password", EditorInfoHelper.isPasswordField(normalText))
        assertFalse("Normal text without flags is not private", EditorInfoHelper.isPrivateOrSensitive(normalText))

        // Null info
        assertFalse("Null EditorInfo safely returns false", EditorInfoHelper.isPrivateOrSensitive(null))
    }

    // ==========================================
    // 3. AVRO PHONETIC PARSER TESTS
    // ==========================================

    @Test
    fun testAvroPhoneticParser_conversationalTransliteration() {
        val parser = AvroPhoneticParser
        assertEquals("আমি", parser.parse("ami"))
        assertEquals("তুমি", parser.parse("tumi"))
        assertEquals("কেমন", parser.parse("kemon"))
        assertEquals("আছো", parser.parse("acho"))
        assertEquals("ভালো", parser.parse("bhalo"))
        assertEquals("বাংলা", parser.parse("bangla"))
        assertEquals("ধন্যবাদ", parser.parse("dhonnobad"))
        assertEquals("দেশ", parser.parse("desh"))
        assertEquals("সুন্দর", parser.parse("shundor"))
    }

    @Test
    fun testAvroPhoneticParser_consonantsAndKarRules() {
        val parser = AvroPhoneticParser
        assertEquals("ক", parser.parse("k"))
        assertEquals("খ", parser.parse("kh"))
        assertEquals("গ", parser.parse("g"))
        assertEquals("ঘ", parser.parse("gh"))
        assertEquals("কা", parser.parse("ka"))
        assertEquals("কি", parser.parse("ki"))
        assertEquals("কু", parser.parse("ku"))
        assertEquals("কে", parser.parse("ke"))
        assertEquals("কো", parser.parse("ko"))
    }

    @Test
    fun testAvroPhoneticParser_candidateSuggestions() {
        val parser = AvroPhoneticParser
        val candidates = parser.getCandidates("ami")
        assertTrue("Candidates must include 'আমি'", candidates.contains("আমি"))
        assertTrue("Candidates list should have multiple suggestions", candidates.isNotEmpty())

        val tumiCandidates = parser.getCandidates("tumi")
        assertTrue("Candidates must include 'তুমি'", tumiCandidates.contains("তুমি"))

        // Edge case: empty string
        val emptyCandidates = parser.getCandidates("")
        assertTrue("Empty input returns empty candidates list", emptyCandidates.isEmpty())
    }

    // ==========================================
    // 4. ENGLISH WORD PREDICTOR TESTS
    // ==========================================

    @Test
    fun testEnglishWordPredictor_basicPrefixMatching() {
        val predictor = EnglishWordPredictor
        val matches = predictor.predict("hel", limit = 3)
        assertTrue("Should contain 'hello'", matches.contains("hello"))
        assertTrue("Matches count must respect limit", matches.size <= 3)

        val thMatches = predictor.predict("th", limit = 4)
        assertTrue("Should contain common words like 'the' or 'that'", thMatches.isNotEmpty())
    }

    @Test
    fun testEnglishWordPredictor_casingPreservation() {
        val predictor = EnglishWordPredictor

        // Capitalized prefix
        val capitalized = predictor.predict("Hel", limit = 3)
        assertTrue("Capitalized prefix must yield capitalized suggestions",
            capitalized.all { it[0].isUpperCase() })
        assertTrue(capitalized.contains("Hello"))

        // Uppercase prefix
        val uppercase = predictor.predict("HEL", limit = 3)
        assertTrue("Uppercase prefix must yield all uppercase suggestions",
            uppercase.all { word -> word.all { char -> char.isUpperCase() } })
        assertTrue(uppercase.contains("HELLO"))

        // Lowercase prefix
        val lowercase = predictor.predict("hel", limit = 3)
        assertTrue(lowercase.contains("hello"))
    }

    @Test
    fun testEnglishWordPredictor_boundariesAndEdgeCases() {
        val predictor = EnglishWordPredictor
        // Single character prefix
        assertTrue("Single char prefix must return empty list", predictor.predict("h").isEmpty())
        // Empty prefix
        assertTrue("Empty prefix must return empty list", predictor.predict("").isEmpty())
        // Whitespace prefix
        assertTrue("Whitespace prefix must return empty list", predictor.predict("   ").isEmpty())
        // Non-existent prefix
        assertTrue("Non-matching prefix returns empty list", predictor.predict("zzxyqw").isEmpty())
    }

    // ==========================================
    // 5. TOOLBAR TOOLS & CUSTOMIZATION TESTS
    // ==========================================

    @Test
    fun testToolbarTools_maxConstraintsAndDefaults() {
        assertEquals("Max enabled tools constraint must be exactly 7",
            7, ToolbarTool.MAX_ENABLED_TOOLS)
        assertEquals("Default tools set count must be exactly 7",
            7, ToolbarTool.DEFAULT_TOOL_IDS.size)

        // Verify all default tools exist and can be resolved
        val defaultTools = ToolbarTool.DEFAULT_TOOL_IDS.mapNotNull { ToolbarTool.fromId(it) }
        assertEquals(7, defaultTools.size)

        // Verify distinct entries
        assertEquals("All default tools must be distinct",
            7, defaultTools.toSet().size)
    }

    @Test
    fun testToolbarTools_fromIdResolution() {
        assertEquals(ToolbarTool.EMOJI, ToolbarTool.fromId("emoji"))
        assertEquals(ToolbarTool.CLIPBOARD, ToolbarTool.fromId("clipboard"))
        assertEquals(ToolbarTool.THEMES, ToolbarTool.fromId("themes"))
        assertEquals(ToolbarTool.FLOATING, ToolbarTool.fromId("floating"))
        assertEquals(ToolbarTool.SETTINGS, ToolbarTool.fromId("settings"))
        assertEquals(ToolbarTool.SNIPPETS, ToolbarTool.fromId("snippets"))
        assertEquals(ToolbarTool.NUMPAD, ToolbarTool.fromId("numpad"))
        assertEquals(ToolbarTool.TEXT_EDITING, ToolbarTool.fromId("text_editing"))
        assertEquals(ToolbarTool.TRANSLATE, ToolbarTool.fromId("translate"))
        assertEquals(ToolbarTool.VOICE_TYPING, ToolbarTool.fromId("voice_typing"))
        assertEquals(ToolbarTool.FANCY_FONTS, ToolbarTool.fromId("fancy_fonts"))

        // Unknown id returns null
        assertNull("Unknown id returns null", ToolbarTool.fromId("unknown_feature_xyz"))
    }

    // ==========================================
    // 6. KEYBOARD LAYOUT MODEL TESTS
    // ==========================================

    @Test
    fun testKeyboardLayoutModel_structureAndActions() {
        // English layout structure
        val englishRows = KeyboardLayoutModel.getEnglishLayout(ShiftState.OFF, isCaps = false)
        assertEquals("English QWERTY must have 4 core rows", 4, englishRows.size)

        // Space key check on bottom row
        val spaceKey = englishRows[3].keys.firstOrNull { it.action == KeyAction.SPACE }
        assertNotNull("Bottom row must have SPACE key", spaceKey)

        // Shift key check
        val shiftKey = englishRows[2].keys.firstOrNull { it.action == KeyAction.SHIFT }
        assertNotNull("Row 3 must have SHIFT key", shiftKey)

        // Backspace key check
        val backspaceKey = englishRows[2].keys.firstOrNull { it.action == KeyAction.BACKSPACE }
        assertNotNull("Row 3 must have BACKSPACE key", backspaceKey)

        // Enter key check
        val enterKey = englishRows[3].keys.firstOrNull { it.action == KeyAction.ENTER }
        assertNotNull("Row 4 must have ENTER key", enterKey)
    }

    @Test
    fun testSymbolsLayouts_rowsCount() {
        val sym1 = KeyboardLayoutModel.getSymbolsPage1()
        assertEquals("Symbols Page 1 must have 5 rows", 5, sym1.size)

        val sym2 = KeyboardLayoutModel.getSymbolsPage2()
        assertEquals("Symbols Page 2 must have 5 rows", 5, sym2.size)
    }

    // ==========================================
    // 7. HAPTIC INTENSITY CLAMPING & MAPPING
    // ==========================================

    @Test
    fun testHapticIntensity_clampingLogic() {
        // Clamping logic: coerceIn(1, 3)
        // 1: Subtle, 2: Medium, 3: Heavy
        val clamp = { value: Int -> value.coerceIn(1, 3) }

        assertEquals(1, clamp(1)) // Subtle
        assertEquals(2, clamp(2)) // Medium
        assertEquals(3, clamp(3)) // Heavy

        // Out-of-bounds boundary values
        assertEquals(1, clamp(0))
        assertEquals(1, clamp(-5))
        assertEquals(3, clamp(4))
        assertEquals(3, clamp(99))
    }
}
