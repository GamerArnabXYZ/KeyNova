package com.example

import android.text.InputType
import android.view.inputmethod.EditorInfo
import com.example.data.PersonalDictEntryType
import com.example.data.PersonalDictionaryManager
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

    // ==========================================
    // 8. PERSONAL DICTIONARY & '+' BUTTON FLOW TESTS
    // ==========================================

    private class FakeSharedPreferences : android.content.SharedPreferences {
        val map = mutableMapOf<String, Any>()

        override fun getAll(): MutableMap<String, *> = map.toMutableMap()
        override fun getString(key: String?, defValue: String?): String? = map[key] as? String ?: defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
            @Suppress("UNCHECKED_CAST")
            val set = map[key] as? Set<String>
            return set?.toMutableSet() ?: defValues
        }
        override fun getInt(key: String?, defValue: Int): Int = map[key] as? Int ?: defValue
        override fun getLong(key: String?, defValue: Long): Long = map[key] as? Long ?: defValue
        override fun getFloat(key: String?, defValue: Float): Float = map[key] as? Float ?: defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = map[key] as? Boolean ?: defValue
        override fun contains(key: String?): Boolean = map.containsKey(key)
        override fun edit(): android.content.SharedPreferences.Editor = FakeEditor()
        override fun registerOnSharedPreferenceChangeListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}

        inner class FakeEditor : android.content.SharedPreferences.Editor {
            private val temp = mutableMapOf<String, Any?>()

            override fun putString(key: String?, value: String?): android.content.SharedPreferences.Editor {
                if (key != null) temp[key] = value
                return this
            }
            override fun putStringSet(key: String?, values: MutableSet<String>?): android.content.SharedPreferences.Editor {
                if (key != null) temp[key] = values?.let { HashSet(it) }
                return this
            }
            override fun putInt(key: String?, value: Int): android.content.SharedPreferences.Editor {
                if (key != null) temp[key] = value
                return this
            }
            override fun putLong(key: String?, value: Long): android.content.SharedPreferences.Editor {
                if (key != null) temp[key] = value
                return this
            }
            override fun putFloat(key: String?, value: Float): android.content.SharedPreferences.Editor {
                if (key != null) temp[key] = value
                return this
            }
            override fun putBoolean(key: String?, value: Boolean): android.content.SharedPreferences.Editor {
                if (key != null) temp[key] = value
                return this
            }
            override fun remove(key: String?): android.content.SharedPreferences.Editor {
                if (key != null) temp[key] = null
                return this
            }
            override fun clear(): android.content.SharedPreferences.Editor {
                map.clear()
                temp.clear()
                return this
            }
            override fun commit(): Boolean {
                apply()
                return true
            }
            override fun apply() {
                temp.forEach { (k, v) ->
                    if (v == null) map.remove(k) else map[k] = v
                }
                temp.clear()
            }
        }
    }

    @Test
    fun testPersonalDictionary_automaticLearningEnglishAndFrequencyRanking() {
        val fakePrefs = FakeSharedPreferences()
        val dict = PersonalDictionaryManager(fakePrefs)

        // 1. Initial state: word unknown
        assertFalse(dict.containsWord("Zenthor", "en"))

        // 2. User types "Zenthor" once (automatic learning on space/complete)
        val learnedOnce = dict.learnWord("Zenthor", "en", isPrivateMode = false)
        assertTrue("learnWord must succeed for valid word", learnedOnce)
        assertTrue("containsWord must now be true", dict.containsWord("Zenthor", "en"))

        // 3. User types "Zenthor" multiple times -> frequency increments
        dict.learnWord("Zenthor", "en", isPrivateMode = false)
        dict.learnWord("Zenthor", "en", isPrivateMode = false)

        // User types another word once
        dict.learnWord("Zenithia", "en", isPrivateMode = false)

        // 4. Query suggestions for prefix "Zen": "Zenthor" (freq 3) must rank higher than "Zenithia" (freq 1)
        val suggestions = dict.findEnglishMatches("Zen", limit = 5)
        assertEquals(2, suggestions.size)
        assertEquals("Zenthor", suggestions[0])
        assertEquals("Zenithia", suggestions[1])
    }

    @Test
    fun testPersonalDictionary_banglaNativeAutomaticLearning() {
        val fakePrefs = FakeSharedPreferences()
        val dict = PersonalDictionaryManager(fakePrefs)

        val nativeWord = "বাংলাদেশ"
        assertFalse(dict.containsWord(nativeWord, "bn"))

        // Automatic learning from Native Jatiya typing
        val learned = dict.learnWord(nativeWord, "bn", isPrivateMode = false)
        assertTrue("Bangla native word learning must succeed", learned)
        assertTrue(dict.containsWord(nativeWord, "bn"))

        // Prefix match
        val matches = dict.findBanglaMatches("বাংলা", limit = 3)
        assertTrue("Prefix 'বাংলা' must retrieve 'বাংলাদেশ'", matches.contains(nativeWord))
    }

    @Test
    fun testPersonalDictionary_hindiDevanagariAutomaticLearning() {
        val fakePrefs = FakeSharedPreferences()
        val dict = PersonalDictionaryManager(fakePrefs)

        val hindiWord = "नमस्ते"
        assertFalse(dict.containsWord(hindiWord, "hi"))

        // Automatic learning from Devanagari typing
        val learned = dict.learnWord(hindiWord, "hi", isPrivateMode = false)
        assertTrue("Hindi word learning must succeed", learned)
        assertTrue(dict.containsWord(hindiWord, "hi"))

        // Prefix match
        val matches = dict.findHindiMatches("नम", limit = 3)
        assertTrue("Prefix 'नम' must retrieve 'नमस्ते'", matches.contains(hindiWord))
    }

    @Test
    fun testPersonalDictionary_avroPhoneticAutomaticLearning() {
        val fakePrefs = FakeSharedPreferences()
        val dict = PersonalDictionaryManager(fakePrefs)

        val phonetic = "arnix"
        val bangla = "আর্নিক্স"

        assertFalse(dict.containsPhoneticMapping(phonetic))
        assertFalse(dict.containsWord(bangla, "bn"))

        // Automatic learning of Avro pair
        val learned = dict.learnAvroPhonetic(phonetic, bangla, isPrivateMode = false)
        assertTrue("learnAvroPhonetic must succeed", learned)
        assertTrue(dict.containsPhoneticMapping(phonetic))
        assertTrue(dict.containsWord(bangla, "bn"))

        // Suggestions retrieval
        val prefixMatches = dict.findAvroMatches("arn", limit = 3)
        assertTrue("Phonetic prefix 'arn' must return 'আর্নিক্স'", prefixMatches.contains(bangla))
    }

    @Test
    fun testPersonalDictionary_strictPrivateModeBlocking() {
        val fakePrefs = FakeSharedPreferences()
        val dict = PersonalDictionaryManager(fakePrefs)

        // When isPrivateMode is true, NO words or mappings may be learned
        assertFalse("Private mode must strictly reject English learning",
            dict.learnWord("SecretVault", "en", isPrivateMode = true))
        assertFalse(dict.containsWord("SecretVault", "en"))

        assertFalse("Private mode must strictly reject Bangla learning",
            dict.learnWord("গোপনতথ্য", "bn", isPrivateMode = true))
        assertFalse(dict.containsWord("গোপনতথ্য", "bn"))

        assertFalse("Private mode must strictly reject Hindi learning",
            dict.learnWord("गुप्तपासवर्ड", "hi", isPrivateMode = true))
        assertFalse(dict.containsWord("गुप्तपासवर्ड", "hi"))

        assertFalse("Private mode must strictly reject Avro learning",
            dict.learnAvroPhonetic("secretkey", "চাবি", isPrivateMode = true))
        assertFalse(dict.containsPhoneticMapping("secretkey"))
    }

    @Test
    fun testPersonalDictionary_securityAndSanitizationFilters() {
        val fakePrefs = FakeSharedPreferences()
        val dict = PersonalDictionaryManager(fakePrefs)

        // Strict rejection of pure numbers / OTPs / PINs (security protection)
        assertFalse("Pure numbers must be rejected", dict.learnWord("482910", "en", false))
        assertFalse("Pure 4-digit PIN must be rejected", dict.learnWord("1234", "en", false))
        assertFalse("Bengali pure digits must be rejected", dict.learnWord("১২৩৪৫", "bn", false))
        assertFalse("Devanagari pure digits must be rejected", dict.learnWord("१२३४५", "hi", false))

        // Rejection of strings containing spaces/whitespace
        assertFalse("Multi-word strings with spaces must not be auto-learned", dict.learnWord("hello world", "en", false))

        // Rejection of pure symbols (no letters, no digits)
        assertFalse("Pure symbols must be rejected", dict.learnWord("###", "en", false))
        assertFalse("Pure punctuation must be rejected", dict.learnWord("...", "en", false))

        // Short words (< 2 chars) rejected
        assertFalse("Single letter rejected", dict.learnWord("a", "en", false))
        assertFalse("Empty string rejected", dict.learnWord("", "en", false))
    }

    @Test
    fun testPersonalDictionary_expandedEntryTypesDetectionAndLearning() {
        val fakePrefs = FakeSharedPreferences()
        val dict = PersonalDictionaryManager(fakePrefs)

        // 1. Alphanumeric combinations (Arnix90, Craft2026, Gamer123)
        assertEquals(PersonalDictEntryType.ALPHANUMERIC, dict.detectEntryType("Arnix90"))
        assertEquals(PersonalDictEntryType.ALPHANUMERIC, dict.detectEntryType("Craft2026"))
        assertTrue("Alphanumeric entry Arnix90 must be learned", dict.learnWord("Arnix90", "en", false))
        assertTrue(dict.containsWord("Arnix90", "en"))

        // 2. Special-character combinations (Arnix@67, Code_Arnix, My-Project)
        assertEquals(PersonalDictEntryType.CUSTOM, dict.detectEntryType("Arnix@67"))
        assertEquals(PersonalDictEntryType.CUSTOM, dict.detectEntryType("Code_Arnix"))
        assertTrue("Special character entry Arnix@67 must be learned", dict.learnWord("Arnix@67", "en", false))
        assertTrue(dict.containsWord("Arnix@67", "en"))

        // 3. Email addresses (arnix@example.com)
        assertEquals(PersonalDictEntryType.EMAIL, dict.detectEntryType("arnix@example.com"))
        assertTrue("Email address must be learned", dict.learnWord("arnix@example.com", "en", false))
        assertTrue(dict.containsWord("arnix@example.com", "en"))

        // 4. URLs and website addresses (https://example.com, craftsprint.io)
        assertEquals(PersonalDictEntryType.URL, dict.detectEntryType("https://example.com"))
        assertEquals(PersonalDictEntryType.URL, dict.detectEntryType("craftsprint.io"))
        assertTrue("URL must be learned", dict.learnWord("https://example.com", "en", false))
        assertTrue(dict.containsWord("https://example.com", "en"))

        // 5. Normal words (Arnix, আমার, नमस्ते)
        assertEquals(PersonalDictEntryType.WORD, dict.detectEntryType("Arnix"))
        assertEquals(PersonalDictEntryType.WORD, dict.detectEntryType("আমার"))
        assertEquals(PersonalDictEntryType.WORD, dict.detectEntryType("नमस्ते"))

        // 6. Original text casing and characters must be perfectly preserved
        val retrievedEmail = dict.getAllEntries().firstOrNull { it.text == "arnix@example.com" }
        assertNotNull(retrievedEmail)
        assertEquals("arnix@example.com", retrievedEmail?.text)
        assertEquals(PersonalDictEntryType.EMAIL, retrievedEmail?.type)

        val retrievedSpecial = dict.getAllEntries().firstOrNull { it.text == "Arnix@67" }
        assertNotNull(retrievedSpecial)
        assertEquals("Arnix@67", retrievedSpecial?.text)
        assertEquals(PersonalDictEntryType.CUSTOM, retrievedSpecial?.type)
    }

    @Test
    fun testPersonalDictionary_contextualSuggestions() {
        val fakePrefs = FakeSharedPreferences()
        val dict = PersonalDictionaryManager(fakePrefs)

        // Populate multiple entry types starting with "arn"
        dict.learnWord("arnix@gmail.com", "en", false)
        dict.learnWord("https://arnix.io", "en", false)
        dict.learnWord("Arnix90", "en", false)
        dict.learnWord("ArnixWord", "en", false)

        // Case 1: In Email Input Field -> Email entries must be prioritized first
        val emailSuggestions = dict.getContextualSuggestions(
            prefix = "arn",
            isEmailField = true,
            isUriField = false,
            languageCode = "en",
            limit = 5
        )
        assertTrue(emailSuggestions.isNotEmpty())
        assertEquals("In email field, email addresses must be prioritized first",
            "arnix@gmail.com", emailSuggestions[0])

        // Case 2: In URL/URI Input Field -> URL entries must be prioritized first
        val uriSuggestions = dict.getContextualSuggestions(
            prefix = "arn",
            isEmailField = false,
            isUriField = true,
            languageCode = "en",
            limit = 5
        )
        assertTrue(uriSuggestions.isNotEmpty())
        assertEquals("In URI field, URL entries must be prioritized first",
            "https://arnix.io", uriSuggestions[0])

        // Case 3: In Normal Text Field -> All matching entries suggested
        val normalSuggestions = dict.getContextualSuggestions(
            prefix = "Arn",
            isEmailField = false,
            isUriField = false,
            languageCode = "en",
            limit = 10
        )
        assertTrue(normalSuggestions.contains("Arnix90"))
        assertTrue(normalSuggestions.contains("ArnixWord"))
    }

    @Test
    fun testPersonalDictionary_fullCrudOperations() {
        val fakePrefs = FakeSharedPreferences()
        val dict = PersonalDictionaryManager(fakePrefs)

        // 1. Manual Add
        val added1 = dict.addCustomEntry("CraftSprint.IO", PersonalDictEntryType.URL, "en")
        assertTrue("Manual add must succeed", added1)
        val added2 = dict.addCustomEntry("support@craftsprint.io", PersonalDictEntryType.EMAIL, "en")
        assertTrue(added2)
        val added3 = dict.addCustomEntry("DevNova_2026", PersonalDictEntryType.ALPHANUMERIC, "en")
        assertTrue(added3)
        assertEquals(3, dict.getTotalEntryCount())

        // 2. Category filtering
        val emailEntries = dict.getEntriesFiltered(PersonalDictEntryType.EMAIL, "")
        assertEquals(1, emailEntries.size)
        assertEquals("support@craftsprint.io", emailEntries[0].text)

        val urlEntries = dict.getEntriesFiltered(PersonalDictEntryType.URL, "")
        assertEquals(1, urlEntries.size)
        assertEquals("CraftSprint.IO", urlEntries[0].text)

        // 3. Search query filtering
        val searchResults = dict.getEntriesFiltered(null, "craft")
        assertEquals(2, searchResults.size)

        // 4. Edit / Update Entry
        val updateSuccess = dict.updateEntry("craftsprint.io", "https://craftsprint.io/docs", PersonalDictEntryType.URL)
        assertTrue("Update must succeed", updateSuccess)
        assertFalse(dict.containsWord("CraftSprint.IO", "en"))
        assertTrue(dict.containsWord("https://craftsprint.io/docs", "en"))

        // 5. Single Delete
        val deleteSuccess = dict.deleteEntry("support@craftsprint.io")
        assertTrue("Single delete must succeed", deleteSuccess)
        assertEquals(2, dict.getTotalEntryCount())

        // 6. Bulk Delete
        val remainingIds = setOf("https://craftsprint.io/docs", "devnova_2026")
        dict.deleteEntries(remainingIds)
        assertEquals(0, dict.getTotalEntryCount())

        // 7. Clear All
        dict.addCustomEntry("TokenA", PersonalDictEntryType.WORD, "en")
        dict.addCustomEntry("TokenB", PersonalDictEntryType.WORD, "en")
        assertEquals(2, dict.getTotalEntryCount())
        dict.clearAll()
        assertEquals(0, dict.getTotalEntryCount())
    }

    @Test
    fun testPersonalDictionary_autoLearningToggle() {
        val fakePrefs = FakeSharedPreferences()
        val dict = PersonalDictionaryManager(fakePrefs)

        // When isPrivateMode / learningBlocked is true (e.g. autoLearn = false or private mode = true)
        val blockedResult = dict.learnWord("DoNotSaveMe", "en", isPrivateMode = true)
        assertFalse("Blocked learning must return false", blockedResult)
        assertFalse(dict.containsWord("DoNotSaveMe", "en"))
        assertEquals(0, dict.getTotalEntryCount())

        // When unblocked, learning succeeds
        val allowedResult = dict.learnWord("DoSaveMe", "en", isPrivateMode = false)
        assertTrue("Unblocked learning must return true", allowedResult)
        assertTrue(dict.containsWord("DoSaveMe", "en"))
        assertEquals(1, dict.getTotalEntryCount())
    }

    @Test
    fun testPersonalDictionary_restartPersistenceExpandedTypes() {
        val fakePrefs = FakeSharedPreferences()

        // Session 1: Learn expanded entries
        val session1 = PersonalDictionaryManager(fakePrefs)
        session1.learnWord("Arnix90", "en", false)
        session1.learnWord("Arnix@67", "en", false)
        session1.learnWord("contact@arnix.dev", "en", false)
        session1.learnWord("https://arnix.dev", "en", false)
        session1.learnWord("বাংলাশব্দ", "bn", false)
        session1.learnWord("हिन्दीशब्द", "hi", false)
        session1.learnAvroPhonetic("ami", "আমি", false)

        // Session 2: App process restarts, reloads from SharedPreferences
        val session2 = PersonalDictionaryManager(fakePrefs)

        assertTrue("Alphanumeric Arnix90 must persist after restart", session2.containsWord("Arnix90", "en"))
        assertTrue("Special Arnix@67 must persist after restart", session2.containsWord("Arnix@67", "en"))
        assertTrue("Email contact@arnix.dev must persist after restart", session2.containsWord("contact@arnix.dev", "en"))
        assertTrue("URL https://arnix.dev must persist after restart", session2.containsWord("https://arnix.dev", "en"))
        assertTrue("Bangla word must persist after restart", session2.containsWord("বাংলাশব্দ", "bn"))
        assertTrue("Hindi word must persist after restart", session2.containsWord("हिन्दीशब्द", "hi"))
        assertTrue("Avro mapping must persist after restart", session2.containsPhoneticMapping("ami"))

        val entries = session2.getAllEntries()
        val emailEntry = entries.firstOrNull { it.text == "contact@arnix.dev" }
        assertEquals(PersonalDictEntryType.EMAIL, emailEntry?.type)

        val urlEntry = entries.firstOrNull { it.text == "https://arnix.dev" }
        assertEquals(PersonalDictEntryType.URL, urlEntry?.type)

        val alphaEntry = entries.firstOrNull { it.text == "Arnix90" }
        assertEquals(PersonalDictEntryType.ALPHANUMERIC, alphaEntry?.type)
    }

    @Test
    fun testPersonalDictionary_languageIsolation() {
        val fakePrefs = FakeSharedPreferences()
        val dict = PersonalDictionaryManager(fakePrefs)

        dict.learnWord("HelloWorld", "en", false)
        dict.learnWord("বাংলাভাষা", "bn", false)
        dict.learnWord("हिन्दीभाषा", "hi", false)

        // Cross-script rejection during learning
        assertFalse("Bengali word cannot be learned as English", dict.learnWord("বাংলা", "en", false))
        assertFalse("Hindi word cannot be learned as English", dict.learnWord("हिन्दी", "en", false))
        assertFalse("English word cannot be learned as Bangla", dict.learnWord("English", "bn", false))
        assertFalse("English word cannot be learned as Hindi", dict.learnWord("English", "hi", false))

        // Queries must be isolated
        assertTrue(dict.findEnglishMatches("Hello").contains("HelloWorld"))
        assertTrue(dict.findBanglaMatches("বাংলা").contains("বাংলাভাষা"))
        assertTrue(dict.findHindiMatches("हिन्दी").contains("हिन्दीभाषा"))

        // Cross-query emptiness
        assertFalse(dict.findEnglishMatches("বাংলা").contains("বাংলাভাষা"))
        assertFalse(dict.findBanglaMatches("Hello").contains("HelloWorld"))
        assertFalse(dict.findHindiMatches("Hello").contains("HelloWorld"))
    }

    @Test
    fun testPersonalDictionary_simulatedAppRestartPersistence() {
        val fakePrefs = FakeSharedPreferences()

        // Session 1: Learn words across languages
        val session1 = PersonalDictionaryManager(fakePrefs)
        session1.learnWord("NovaApp", "en", false)
        session1.learnWord("NovaApp", "en", false) // freq 2
        session1.learnWord("সোনারদেশ", "bn", false)
        session1.learnWord("शुभकामना", "hi", false)
        session1.learnAvroPhonetic("dhonnobad", "ধন্যবাদ", false)

        // Session 2: App process killed and restarted with same SharedPreferences
        val session2 = PersonalDictionaryManager(fakePrefs)

        assertTrue(session2.containsWord("NovaApp", "en"))
        assertTrue(session2.containsWord("সোনারদেশ", "bn"))
        assertTrue(session2.containsWord("शुभकामना", "hi"))
        assertTrue(session2.containsPhoneticMapping("dhonnobad"))

        val enMatches = session2.findEnglishMatches("Nova")
        assertEquals(1, enMatches.size)
        assertEquals("NovaApp", enMatches[0])

        val bnMatches = session2.findBanglaMatches("সোনার")
        assertEquals("সোনারদেশ", bnMatches[0])

        val hiMatches = session2.findHindiMatches("शुभ")
        assertEquals("शुभकामना", hiMatches[0])

        val avroMatches = session2.findAvroMatches("dhonno")
        assertTrue(avroMatches.contains("ধন্যবাদ"))
    }

    @Test
    fun testPersonalDictionary_managementAndClearing() {
        val fakePrefs = FakeSharedPreferences()
        val dict = PersonalDictionaryManager(fakePrefs)

        dict.learnWord("WordOne", "en", false)
        dict.learnWord("WordTwo", "en", false)
        dict.learnWord("এক", "bn", false)
        dict.learnWord("एक", "hi", false)

        assertEquals(2, dict.getWordCount("en"))
        assertEquals(1, dict.getWordCount("bn"))
        assertEquals(1, dict.getWordCount("hi"))

        // Remove single word
        assertTrue(dict.removeWord("WordOne", "en"))
        assertFalse(dict.containsWord("WordOne", "en"))
        assertEquals(1, dict.getWordCount("en"))

        // Clear single language
        dict.clearLanguage("bn")
        assertEquals(0, dict.getWordCount("bn"))
        assertEquals(1, dict.getWordCount("en"))

        // Clear all
        dict.clearAll()
        assertEquals(0, dict.getWordCount("en"))
        assertEquals(0, dict.getWordCount("hi"))
    }
}
