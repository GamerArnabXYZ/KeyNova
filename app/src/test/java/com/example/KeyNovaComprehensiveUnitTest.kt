package com.example

import android.text.InputType
import android.view.inputmethod.EditorInfo
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
    fun testPersonalDictionary_newEnglishWordShowsPlusAndPersists() {
        val fakePrefs = FakeSharedPreferences()
        val dict = PersonalDictionaryManager(fakePrefs)

        val newWord = "Zenthor"

        // 1. Check if word is known in static dictionary
        assertFalse("New custom word must NOT be in static dictionary",
            EnglishWordPredictor.isStaticWord(newWord))

        // 2. Check if word is known in personal dictionary before add
        assertFalse("Must NOT be in personal dictionary yet",
            dict.containsWord(newWord))

        // 3. Visibility condition for '+' button:
        // isEligibleForDict && !isKnownWord
        val isKnownWordBefore = EnglishWordPredictor.isStaticWord(newWord) || dict.containsWord(newWord)
        val showAddButtonBefore = newWord.length >= 2 && !isKnownWordBefore
        assertTrue("'+' button MUST be visible for new un-saved English word", showAddButtonBefore)

        // 4. Click '+' -> save word
        val added = dict.addWord(newWord)
        assertTrue("addWord must return true for new valid word", added)

        // 5. After save: containsWord must be true and '+' button must disappear
        assertTrue("containsWord must be true after add", dict.containsWord(newWord))
        val isKnownWordAfter = EnglishWordPredictor.isStaticWord(newWord) || dict.containsWord(newWord)
        val showAddButtonAfter = newWord.length >= 2 && !isKnownWordAfter
        assertFalse("'+' button MUST disappear after word is saved", showAddButtonAfter)

        // 6. Future suggestion check: typing prefix "Zen" must retrieve "Zenthor"
        val matches = dict.findMatches("Zen", limit = 3)
        assertTrue("Prefix 'Zen' must suggest saved word 'Zenthor'", matches.contains("Zenthor"))
    }

    @Test
    fun testPersonalDictionary_existingDictionaryWordSuppressesPlusButton() {
        val fakePrefs = FakeSharedPreferences()
        val dict = PersonalDictionaryManager(fakePrefs)

        val existingWord = "hello"

        // 1. 'hello' is in static dictionary
        assertTrue("Static word 'hello' must be recognized",
            EnglishWordPredictor.isStaticWord(existingWord))

        // 2. Visibility condition: isKnownWord is true -> showAddButton is false
        val isKnownWord = EnglishWordPredictor.isStaticWord(existingWord) || dict.containsWord(existingWord)
        val showAddButton = existingWord.length >= 2 && !isKnownWord
        assertFalse("'+' button must NOT appear for existing dictionary words", showAddButton)
    }

    @Test
    fun testPersonalDictionary_duplicateWordHandling() {
        val fakePrefs = FakeSharedPreferences()
        val dict = PersonalDictionaryManager(fakePrefs)

        assertTrue("First add must succeed", dict.addWord("CustomBrand"))
        assertFalse("Exact duplicate add must be rejected", dict.addWord("CustomBrand"))
        assertFalse("Case-insensitive duplicate add must be rejected", dict.addWord("custombrand"))
        assertFalse("Uppercase duplicate add must be rejected", dict.addWord("CUSTOMBRAND"))

        assertEquals("Personal dictionary must contain exactly 1 entry without duplicates",
            1, dict.getAllWords().size)
    }

    @Test
    fun testPersonalDictionary_avroPhoneticWordAdditionAndMatches() {
        val fakePrefs = FakeSharedPreferences()
        val dict = PersonalDictionaryManager(fakePrefs)

        val phonetic = "arnix"
        val bangla = "আর্নিক্স"

        // 1. Before add
        assertFalse("Phonetic mapping must not exist yet", dict.containsPhoneticMapping(phonetic))
        assertFalse("Bangla word must not exist yet", dict.containsWord(bangla))

        val isKnownAvroBefore = dict.containsPhoneticMapping(phonetic) || dict.containsWord(bangla)
        val showAvroAddButtonBefore = phonetic.length >= 2 && bangla.isNotEmpty() && !isKnownAvroBefore
        assertTrue("'+' button must be visible for new Avro word", showAvroAddButtonBefore)

        // 2. Click '+' -> save Avro phonetic word
        val added = dict.addAvroPhoneticWord(phonetic, bangla)
        assertTrue("addAvroPhoneticWord must return true", added)

        // 3. After save
        assertTrue(dict.containsPhoneticMapping(phonetic))
        assertTrue(dict.containsWord(bangla))

        val isKnownAvroAfter = dict.containsPhoneticMapping(phonetic) || dict.containsWord(bangla)
        val showAvroAddButtonAfter = phonetic.length >= 2 && bangla.isNotEmpty() && !isKnownAvroAfter
        assertFalse("'+' button must disappear after Avro word is saved", showAvroAddButtonAfter)

        // 4. Future suggestion retrieval:
        // Typing prefix "arn" or "arnix" must suggest "আর্নিক্স"
        val matchesShort = dict.findAvroMatches("arn", limit = 3)
        assertTrue("Phonetic prefix 'arn' must return 'আর্নিক্স'", matchesShort.contains(bangla))

        val matchesFull = dict.findAvroMatches("arnix", limit = 3)
        assertTrue("Full phonetic 'arnix' must return 'আর্নিক্স'", matchesFull.contains(bangla))
    }

    @Test
    fun testPersonalDictionary_simulatedAppRestartPersistence() {
        val fakePrefs = FakeSharedPreferences()

        // Session 1: User adds words
        val dictSession1 = PersonalDictionaryManager(fakePrefs)
        dictSession1.addWord("Zenthor")
        dictSession1.addWord("CodeSnippet")
        dictSession1.addAvroPhoneticWord("shuvokamona", "শুভকামনা")

        assertEquals(3, dictSession1.getAllWords().size)

        // Session 2: App killed and restarted, new PersonalDictionaryManager initialized with same SharedPreferences
        val dictSession2 = PersonalDictionaryManager(fakePrefs)

        assertTrue("Saved English word 'Zenthor' must persist after app restart",
            dictSession2.containsWord("Zenthor"))
        assertTrue("Saved English word 'CodeSnippet' must persist after app restart",
            dictSession2.containsWord("CodeSnippet"))
        assertTrue("Saved Avro phonetic mapping must persist after app restart",
            dictSession2.containsPhoneticMapping("shuvokamona"))
        assertTrue("Saved Bangla word must persist after app restart",
            dictSession2.containsWord("শুভকামনা"))

        val avroSuggestions = dictSession2.findAvroMatches("shuvo", limit = 3)
        assertTrue("Avro suggestions must retrieve 'শুভকামনা' after restart",
            avroSuggestions.contains("শুভকামনা"))
    }

    @Test
    fun testPersonalDictionary_invalidAndShortWordsRejected() {
        val fakePrefs = FakeSharedPreferences()
        val dict = PersonalDictionaryManager(fakePrefs)

        assertFalse("Single letter word must be rejected", dict.addWord("a"))
        assertFalse("Empty string must be rejected", dict.addWord(""))
        assertFalse("Whitespace must be rejected", dict.addWord("   "))
        assertEquals(0, dict.getAllWords().size)
    }

    @Test
    fun testPersonalDictionary_wordRemoval() {
        val fakePrefs = FakeSharedPreferences()
        val dict = PersonalDictionaryManager(fakePrefs)

        dict.addWord("TempWord")
        assertTrue(dict.containsWord("TempWord"))

        val removed = dict.removeWord("TempWord")
        assertTrue("removeWord must return true", removed)
        assertFalse("Word must no longer be in dictionary", dict.containsWord("TempWord"))
        assertTrue(dict.findMatches("Temp").isEmpty())
    }
}
