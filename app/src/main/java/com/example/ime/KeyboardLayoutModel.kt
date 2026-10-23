package com.example.ime

import com.example.data.KeyboardLanguage

enum class KeyAction {
    CHARACTER,
    SHIFT,
    BACKSPACE,
    ENTER,
    SPACE,
    SYMBOL_SWITCH,
    LANGUAGE_SWITCH,
    EMOJI_PANEL,
    CLIPBOARD_PANEL,
    CODING_PANEL,
    GAMING_PANEL,
    SETTINGS,
    INSERT_TEXT
}

enum class ShiftState {
    OFF,
    SHIFTED,
    CAPS_LOCKED
}

data class KeyDef(
    val primaryText: String,
    val secondaryText: String? = null,
    val action: KeyAction = KeyAction.CHARACTER,
    val weight: Float = 1.0f,
    val isSpecial: Boolean = false,
    val insertPayload: String? = null
)

data class KeyboardRow(
    val keys: List<KeyDef>
)

object KeyboardLayoutModel {

    /**
     * Standard Number Row (English digits 1-0 with Bangla secondary hints ১-০)
     * Matches Image 1 (English) and Image 4 (Symbols 1).
     */
    fun getNumberRow(): KeyboardRow {
        val keys = listOf(
            KeyDef(primaryText = "1", secondaryText = "১", weight = 1f),
            KeyDef(primaryText = "2", secondaryText = "২", weight = 1f),
            KeyDef(primaryText = "3", secondaryText = "৩", weight = 1f),
            KeyDef(primaryText = "4", secondaryText = "৪", weight = 1f),
            KeyDef(primaryText = "5", secondaryText = "৫", weight = 1f),
            KeyDef(primaryText = "6", secondaryText = "৬", weight = 1f),
            KeyDef(primaryText = "7", secondaryText = "৭", weight = 1f),
            KeyDef(primaryText = "8", secondaryText = "৮", weight = 1f),
            KeyDef(primaryText = "9", secondaryText = "৯", weight = 1f),
            KeyDef(primaryText = "0", secondaryText = "০", weight = 1f)
        )
        return KeyboardRow(keys)
    }

    /**
     * Bangla Number Row (Bangla digits ১-০ with English secondary hints 1-0)
     * Matches Image 2 and Image 3 (Bangla Jatiya).
     */
    fun getBanglaNumberRow(): KeyboardRow {
        val keys = listOf(
            KeyDef(primaryText = "১", secondaryText = "1", weight = 1f),
            KeyDef(primaryText = "২", secondaryText = "2", weight = 1f),
            KeyDef(primaryText = "৩", secondaryText = "3", weight = 1f),
            KeyDef(primaryText = "৪", secondaryText = "4", weight = 1f),
            KeyDef(primaryText = "৫", secondaryText = "5", weight = 1f),
            KeyDef(primaryText = "৬", secondaryText = "6", weight = 1f),
            KeyDef(primaryText = "৭", secondaryText = "7", weight = 1f),
            KeyDef(primaryText = "৮", secondaryText = "8", weight = 1f),
            KeyDef(primaryText = "৯", secondaryText = "9", weight = 1f),
            KeyDef(primaryText = "০", secondaryText = "0", weight = 1f)
        )
        return KeyboardRow(keys)
    }

    /**
     * English QWERTY Layout matching Image 1 exactly:
     * Row 1: Q(৳), W(%), E(_), R(;), T(<), Y(>), U(+), I(=), O([), P(])
     * Row 2: A(@), S(#), D(&), F(*), G(-), H(!), J(?), K((), L())
     * Row 3: Shift, Z("), X, C, V, B('), N(:), M(/), Backspace
     * Row 4: ?123, Emoji/Globe, ,(...), Spacebar, .(...), Enter(...)
     */
    fun getEnglishLayout(shiftState: ShiftState, isCaps: Boolean, showGlobeKey: Boolean = true): List<KeyboardRow> {
        val isUpper = shiftState != ShiftState.OFF || isCaps

        val r1 = listOf(
            KeyDef(if (isUpper) "Q" else "q", "৳", weight = 1f),
            KeyDef(if (isUpper) "W" else "w", "%", weight = 1f),
            KeyDef(if (isUpper) "E" else "e", "_", weight = 1f),
            KeyDef(if (isUpper) "R" else "r", ";", weight = 1f),
            KeyDef(if (isUpper) "T" else "t", "<", weight = 1f),
            KeyDef(if (isUpper) "Y" else "y", ">", weight = 1f),
            KeyDef(if (isUpper) "U" else "u", "+", weight = 1f),
            KeyDef(if (isUpper) "I" else "i", "=", weight = 1f),
            KeyDef(if (isUpper) "O" else "o", "[", weight = 1f),
            KeyDef(if (isUpper) "P" else "p", "]", weight = 1f)
        )

        val r2 = listOf(
            KeyDef(if (isUpper) "A" else "a", "@", weight = 1f),
            KeyDef(if (isUpper) "S" else "s", "#", weight = 1f),
            KeyDef(if (isUpper) "D" else "d", "&", weight = 1f),
            KeyDef(if (isUpper) "F" else "f", "*", weight = 1f),
            KeyDef(if (isUpper) "G" else "g", "-", weight = 1f),
            KeyDef(if (isUpper) "H" else "h", "!", weight = 1f),
            KeyDef(if (isUpper) "J" else "j", "?", weight = 1f),
            KeyDef(if (isUpper) "K" else "k", "(", weight = 1f),
            KeyDef(if (isUpper) "L" else "l", ")", weight = 1f)
        )

        val r3Keys = mutableListOf<KeyDef>()
        r3Keys.add(
            KeyDef(
                primaryText = if (shiftState == ShiftState.CAPS_LOCKED) "⇪" else "⇧",
                action = KeyAction.SHIFT,
                weight = 1.3f,
                isSpecial = true
            )
        )
        r3Keys.add(KeyDef(if (isUpper) "Z" else "z", "\"", weight = 1f))
        r3Keys.add(KeyDef(if (isUpper) "X" else "x", weight = 1f))
        r3Keys.add(KeyDef(if (isUpper) "C" else "c", weight = 1f))
        r3Keys.add(KeyDef(if (isUpper) "V" else "v", weight = 1f))
        r3Keys.add(KeyDef(if (isUpper) "B" else "b", "'", weight = 1f))
        r3Keys.add(KeyDef(if (isUpper) "N" else "n", ":", weight = 1f))
        r3Keys.add(KeyDef(if (isUpper) "M" else "m", "/", weight = 1f))
        r3Keys.add(
            KeyDef(
                primaryText = "⌫",
                action = KeyAction.BACKSPACE,
                weight = 1.3f,
                isSpecial = true
            )
        )

        val r4Keys = mutableListOf<KeyDef>()
        r4Keys.add(KeyDef(primaryText = "?123", action = KeyAction.SYMBOL_SWITCH, weight = 1.2f, isSpecial = true))
        if (showGlobeKey) {
            r4Keys.add(KeyDef(primaryText = "🌐", action = KeyAction.LANGUAGE_SWITCH, weight = 1.0f, isSpecial = true))
        } else {
            r4Keys.add(KeyDef(primaryText = "☺", action = KeyAction.EMOJI_PANEL, weight = 1.0f, isSpecial = true))
        }
        r4Keys.add(KeyDef(primaryText = ",", secondaryText = "...", weight = 0.9f))
        val spaceWeight = if (showGlobeKey) 3.6f else 4.2f
        r4Keys.add(KeyDef(primaryText = "English", action = KeyAction.SPACE, weight = spaceWeight))
        r4Keys.add(KeyDef(primaryText = ".", secondaryText = "...", weight = 0.9f))
        r4Keys.add(KeyDef(primaryText = "↵", secondaryText = "...", action = KeyAction.ENTER, weight = 1.3f, isSpecial = true))

        return listOf(KeyboardRow(r1), KeyboardRow(r2), KeyboardRow(r3Keys), KeyboardRow(r4Keys))
    }

    /**
     * Bangla National / Jatiya (জাতীয়) Layout matching Image 2 (unshifted) and Image 3 (shifted).
     * Unshifted:
     * Row 1: ঙ(ং), য(য়), ড(ঢ), প(ফ), ট(ঠ), চ(ছ), জ(ঝ), হ(ঞ), গ(ঘ), ড়(ঢ়)
     * Row 2: ৃ(ঋ), ু(উ), ি(ই), ব(ভ), ্(Hasanta), া(আ), ক(খ), ত(ৎ), দ(ধ)
     * Row 3: Shift, ঁ(ঃ), ো(ও), ে(এ), র(ল), ন(ণ), স(ষ), ম(শ), Backspace
     * Row 4: ?১২৩, Emoji/Globe, ,(...), Spacebar(জাতীয়), ।(...), Enter(...)
     *
     * Shifted:
     * Row 1: ং, ্য, ঢ, ফ, ঠ, ছ, ঝ, ঞ, ঘ, ঢ়
     * Row 2: ঋ, ূ(ঊ), ী(ঈ), ভ, ’, অ, খ, থ, ধ
     * Row 3: Shift(active), ঃ, ৌ(ঔ), ৈ(ঐ), ্র, ণ, ষ, শ, Backspace
     * Row 4: ?১২৩, Emoji/Globe, ,(...), Spacebar(জাতীয়), ।(...), Enter(...)
     */
    fun getBanglaLayout(shifted: Boolean, showGlobeKey: Boolean = true): List<KeyboardRow> {
        return if (!shifted) {
            // Unshifted Jatiya (Image 2)
            val r1 = listOf(
                KeyDef("ঙ", "ং", weight = 1f),
                KeyDef("য", "য়", weight = 1f),
                KeyDef("ড", "ঢ", weight = 1f),
                KeyDef("প", "ফ", weight = 1f),
                KeyDef("ট", "ঠ", weight = 1f),
                KeyDef("চ", "ছ", weight = 1f),
                KeyDef("জ", "ঝ", weight = 1f),
                KeyDef("হ", "ঞ", weight = 1f),
                KeyDef("গ", "ঘ", weight = 1f),
                KeyDef("ড়", "ঢ়", weight = 1f)
            )

            val r2 = listOf(
                KeyDef("ৃ", "ঋ", weight = 1f),
                KeyDef("ু", "উ", weight = 1f),
                KeyDef("ি", "ই", weight = 1f),
                KeyDef("ব", "ভ", weight = 1f),
                KeyDef("্", null, weight = 1f), // Virama / Hasanta for conjuncts
                KeyDef("া", "আ", weight = 1f),
                KeyDef("ক", "খ", weight = 1f),
                KeyDef("ত", "ৎ", weight = 1f),
                KeyDef("দ", "ধ", weight = 1f)
            )

            val r3 = mutableListOf<KeyDef>()
            r3.add(KeyDef(primaryText = "⇧", action = KeyAction.SHIFT, weight = 1.3f, isSpecial = true))
            r3.add(KeyDef("ঁ", "ঃ", weight = 1f))
            r3.add(KeyDef("ো", "ও", weight = 1f))
            r3.add(KeyDef("ে", "এ", weight = 1f))
            r3.add(KeyDef("র", "ল", weight = 1f))
            r3.add(KeyDef("ন", "ণ", weight = 1f))
            r3.add(KeyDef("স", "ষ", weight = 1f))
            r3.add(KeyDef("ম", "শ", weight = 1f))
            r3.add(KeyDef(primaryText = "⌫", action = KeyAction.BACKSPACE, weight = 1.3f, isSpecial = true))

            val r4Keys = mutableListOf<KeyDef>()
            r4Keys.add(KeyDef(primaryText = "?১২৩", action = KeyAction.SYMBOL_SWITCH, weight = 1.2f, isSpecial = true))
            if (showGlobeKey) {
                r4Keys.add(KeyDef(primaryText = "🌐", action = KeyAction.LANGUAGE_SWITCH, weight = 1.0f, isSpecial = true))
            } else {
                r4Keys.add(KeyDef(primaryText = "☺", action = KeyAction.EMOJI_PANEL, weight = 1.0f, isSpecial = true))
            }
            r4Keys.add(KeyDef(primaryText = ",", secondaryText = "...", weight = 0.9f))
            val spaceWeight = if (showGlobeKey) 3.6f else 4.2f
            r4Keys.add(KeyDef(primaryText = "জাতীয়", action = KeyAction.SPACE, weight = spaceWeight))
            r4Keys.add(KeyDef(primaryText = "।", secondaryText = "...", weight = 0.9f))
            r4Keys.add(KeyDef(primaryText = "↵", secondaryText = "...", action = KeyAction.ENTER, weight = 1.3f, isSpecial = true))

            listOf(KeyboardRow(r1), KeyboardRow(r2), KeyboardRow(r3), KeyboardRow(r4Keys))
        } else {
            // Shifted Jatiya (Image 3)
            val r1 = listOf(
                KeyDef("ং", null, weight = 1f),
                KeyDef("্য", null, weight = 1f),
                KeyDef("ঢ", null, weight = 1f),
                KeyDef("ফ", null, weight = 1f),
                KeyDef("ঠ", null, weight = 1f),
                KeyDef("ছ", null, weight = 1f),
                KeyDef("ঝ", null, weight = 1f),
                KeyDef("ঞ", null, weight = 1f),
                KeyDef("ঘ", null, weight = 1f),
                KeyDef("ঢ়", null, weight = 1f)
            )

            val r2 = listOf(
                KeyDef("ঋ", null, weight = 1f),
                KeyDef("ূ", "ঊ", weight = 1f),
                KeyDef("ী", "ঈ", weight = 1f),
                KeyDef("ভ", null, weight = 1f),
                KeyDef("’", null, weight = 1f),
                KeyDef("অ", null, weight = 1f),
                KeyDef("খ", null, weight = 1f),
                KeyDef("থ", null, weight = 1f),
                KeyDef("ধ", null, weight = 1f)
            )

            val r3 = mutableListOf<KeyDef>()
            r3.add(KeyDef(primaryText = "⬆", action = KeyAction.SHIFT, weight = 1.3f, isSpecial = true))
            r3.add(KeyDef("ঃ", null, weight = 1f))
            r3.add(KeyDef("ৌ", "ঔ", weight = 1f))
            r3.add(KeyDef("ৈ", "ঐ", weight = 1f))
            r3.add(KeyDef("্র", null, weight = 1f))
            r3.add(KeyDef("ণ", null, weight = 1f))
            r3.add(KeyDef("ষ", null, weight = 1f))
            r3.add(KeyDef("শ", null, weight = 1f))
            r3.add(KeyDef(primaryText = "⌫", action = KeyAction.BACKSPACE, weight = 1.3f, isSpecial = true))

            val r4Keys = mutableListOf<KeyDef>()
            r4Keys.add(KeyDef(primaryText = "?১২৩", action = KeyAction.SYMBOL_SWITCH, weight = 1.2f, isSpecial = true))
            if (showGlobeKey) {
                r4Keys.add(KeyDef(primaryText = "🌐", action = KeyAction.LANGUAGE_SWITCH, weight = 1.0f, isSpecial = true))
            } else {
                r4Keys.add(KeyDef(primaryText = "☺", action = KeyAction.EMOJI_PANEL, weight = 1.0f, isSpecial = true))
            }
            r4Keys.add(KeyDef(primaryText = ",", secondaryText = "...", weight = 0.9f))
            val spaceWeight = if (showGlobeKey) 3.6f else 4.2f
            r4Keys.add(KeyDef(primaryText = "জাতীয়", action = KeyAction.SPACE, weight = spaceWeight))
            r4Keys.add(KeyDef(primaryText = "।", secondaryText = "...", weight = 0.9f))
            r4Keys.add(KeyDef(primaryText = "↵", secondaryText = "...", action = KeyAction.ENTER, weight = 1.3f, isSpecial = true))

            listOf(KeyboardRow(r1), KeyboardRow(r2), KeyboardRow(r3), KeyboardRow(r4Keys))
        }
    }

    fun getHindiLayout(shifted: Boolean, showGlobeKey: Boolean = true): List<KeyboardRow> {
        return if (!shifted) {
            val r1 = listOf("क", "ख", "ग", "घ", "ङ", "च", "छ", "ज", "झ", "ञ")
                .map { KeyDef(primaryText = it, weight = 1f) }
            val r2 = listOf("ट", "ठ", "ड", "ढ", "ण", "त", "थ", "द", "ध", "न")
                .map { KeyDef(primaryText = it, weight = 1f) }
            val r3 = mutableListOf<KeyDef>()
            r3.add(KeyDef(primaryText = "स्वर", action = KeyAction.SHIFT, weight = 1.3f, isSpecial = true))
            listOf("प", "फ", "ब", "भ", "म", "य", "र", "ल", "व").forEach {
                r3.add(KeyDef(primaryText = it, weight = 1f))
            }
            r3.add(KeyDef(primaryText = "⌫", action = KeyAction.BACKSPACE, weight = 1.3f, isSpecial = true))

            val r4Keys = mutableListOf<KeyDef>()
            r4Keys.add(KeyDef(primaryText = "?123", action = KeyAction.SYMBOL_SWITCH, weight = 1.2f, isSpecial = true))
            if (showGlobeKey) {
                r4Keys.add(KeyDef(primaryText = "🌐", action = KeyAction.LANGUAGE_SWITCH, weight = 1.0f, isSpecial = true))
            } else {
                r4Keys.add(KeyDef(primaryText = "☺", action = KeyAction.EMOJI_PANEL, weight = 1.0f, isSpecial = true))
            }
            r4Keys.add(KeyDef(primaryText = ",", secondaryText = "...", weight = 0.9f))
            val spaceWeight = if (showGlobeKey) 3.6f else 4.2f
            r4Keys.add(KeyDef(primaryText = "हिन्दी", action = KeyAction.SPACE, weight = spaceWeight))
            r4Keys.add(KeyDef(primaryText = "।", secondaryText = "...", weight = 0.9f))
            r4Keys.add(KeyDef(primaryText = "↵", secondaryText = "...", action = KeyAction.ENTER, weight = 1.3f, isSpecial = true))

            listOf(KeyboardRow(r1), KeyboardRow(r2), KeyboardRow(r3), KeyboardRow(r4Keys))
        } else {
            val r1 = listOf("अ", "आ", "इ", "ई", "उ", "ऊ", "ऋ", "ए", "ऐ", "ओ")
                .map { KeyDef(primaryText = it, weight = 1f) }
            val r2 = listOf("ा", "ि", "ी", "ु", "ू", "ृ", "े", "ै", "ो", "ौ")
                .map { KeyDef(primaryText = it, weight = 1f) }
            val r3 = mutableListOf<KeyDef>()
            r3.add(KeyDef(primaryText = "व्यं", action = KeyAction.SHIFT, weight = 1.3f, isSpecial = true))
            listOf("্", "ं", "ः", "ँ", "स", "ह", "ड़", "ढ़", "औ").forEach {
                r3.add(KeyDef(primaryText = it, weight = 1f))
            }
            r3.add(KeyDef(primaryText = "⌫", action = KeyAction.BACKSPACE, weight = 1.3f, isSpecial = true))

            val r4Keys = mutableListOf<KeyDef>()
            r4Keys.add(KeyDef(primaryText = "?123", action = KeyAction.SYMBOL_SWITCH, weight = 1.2f, isSpecial = true))
            if (showGlobeKey) {
                r4Keys.add(KeyDef(primaryText = "🌐", action = KeyAction.LANGUAGE_SWITCH, weight = 1.0f, isSpecial = true))
            } else {
                r4Keys.add(KeyDef(primaryText = "☺", action = KeyAction.EMOJI_PANEL, weight = 1.0f, isSpecial = true))
            }
            r4Keys.add(KeyDef(primaryText = ",", secondaryText = "...", weight = 0.9f))
            val spaceWeight = if (showGlobeKey) 3.6f else 4.2f
            r4Keys.add(KeyDef(primaryText = "हिन्दी", action = KeyAction.SPACE, weight = spaceWeight))
            r4Keys.add(KeyDef(primaryText = "।", secondaryText = "...", weight = 0.9f))
            r4Keys.add(KeyDef(primaryText = "↵", secondaryText = "...", action = KeyAction.ENTER, weight = 1.3f, isSpecial = true))

            listOf(KeyboardRow(r1), KeyboardRow(r2), KeyboardRow(r3), KeyboardRow(r4Keys))
        }
    }

    /**
     * Symbols Page 1 (?123) matching Image 4 exactly:
     * Row 0: 1(১), 2(২), 3(৩), 4(৪), 5(৫), 6(৬), 7(৭), 8(৮), 9(৯), 0(০)
     * Row 1: Tab(⇥), ~, \, <, >, _, =, [, ]
     * Row 2: @, #, ৳($), %, &, *(★), -(_), +, (<), >)
     * Row 3: = \ <, !, ", ', :(°), ;, /, ?, Backspace
     * Row 4: ABC, ,(...), Spacebar, .(...), Enter(...)
     */
    fun getSymbolsPage1(): List<KeyboardRow> {
        val r0 = getNumberRow()
        val r1 = listOf(
            KeyDef("⇥", action = KeyAction.INSERT_TEXT, insertPayload = "\t", weight = 1.2f, isSpecial = true),
            KeyDef("~", weight = 1.0f),
            KeyDef("\\", weight = 1.0f),
            KeyDef("<", weight = 1.0f),
            KeyDef(">", weight = 1.0f),
            KeyDef("_", weight = 1.0f),
            KeyDef("=", weight = 1.0f),
            KeyDef("[", weight = 1.0f),
            KeyDef("]", weight = 1.0f)
        )
        val r2 = listOf(
            KeyDef("@", weight = 1.0f),
            KeyDef("#", weight = 1.0f),
            KeyDef("৳", secondaryText = "$", weight = 1.0f),
            KeyDef("%", weight = 1.0f),
            KeyDef("&", weight = 1.0f),
            KeyDef("*", secondaryText = "★", weight = 1.0f),
            KeyDef("-", secondaryText = "_", weight = 1.0f),
            KeyDef("+", weight = 1.0f),
            KeyDef("(", secondaryText = "<", weight = 1.0f),
            KeyDef(")", secondaryText = ">", weight = 1.0f)
        )
        val r3 = listOf(
            KeyDef("= \\ <", action = KeyAction.SYMBOL_SWITCH, weight = 1.3f, isSpecial = true),
            KeyDef("!", weight = 1.0f),
            KeyDef("\"", weight = 1.0f),
            KeyDef("'", weight = 1.0f),
            KeyDef(":", secondaryText = "°", weight = 1.0f),
            KeyDef(";", weight = 1.0f),
            KeyDef("/", weight = 1.0f),
            KeyDef("?", weight = 1.0f),
            KeyDef("⌫", action = KeyAction.BACKSPACE, weight = 1.3f, isSpecial = true)
        )
        val r4 = listOf(
            KeyDef("ABC", action = KeyAction.SYMBOL_SWITCH, weight = 1.3f, isSpecial = true),
            KeyDef(",", secondaryText = "...", weight = 0.9f),
            KeyDef("English", action = KeyAction.SPACE, weight = 4.2f),
            KeyDef(".", secondaryText = "...", weight = 0.9f),
            KeyDef("↵", secondaryText = "...", action = KeyAction.ENTER, weight = 1.4f, isSpecial = true)
        )
        return listOf(r0, KeyboardRow(r1), KeyboardRow(r2), KeyboardRow(r3), KeyboardRow(r4))
    }

    /**
     * Symbols Page 2 (= \ <) matching Image 5 exactly:
     * Row 0: ☞, ◐, ⊕, ✖, ✔, ✪(★), ∞, ⇒, ➤, ✆(✉)
     * Row 1: ~, `, |, ₹(৹), √, π(Π), ÷, ×, {, }
     * Row 2: Tab(⇥), £, $, %, ^(→), °(•), _, =(≠), [, ]
     * Row 3: ?123, ✥(র‍্য), ®, ©, ™, \, <, >, Backspace
     * Row 4: ABC, ,(...), Spacebar, .(...), Enter(...)
     */
    fun getSymbolsPage2(): List<KeyboardRow> {
        val r0 = listOf(
            KeyDef("☞", weight = 1.0f),
            KeyDef("◐", weight = 1.0f),
            KeyDef("⊕", weight = 1.0f),
            KeyDef("✖", weight = 1.0f),
            KeyDef("✔", weight = 1.0f),
            KeyDef("✪", secondaryText = "★", weight = 1.0f),
            KeyDef("∞", weight = 1.0f),
            KeyDef("⇒", weight = 1.0f),
            KeyDef("➤", weight = 1.0f),
            KeyDef("✆", secondaryText = "✉", weight = 1.0f)
        )
        val r1 = listOf(
            KeyDef("~", weight = 1.0f),
            KeyDef("`", weight = 1.0f),
            KeyDef("|", weight = 1.0f),
            KeyDef("₹", secondaryText = "৹", weight = 1.0f),
            KeyDef("√", weight = 1.0f),
            KeyDef("π", secondaryText = "Π", weight = 1.0f),
            KeyDef("÷", weight = 1.0f),
            KeyDef("×", weight = 1.0f),
            KeyDef("{", weight = 1.0f),
            KeyDef("}", weight = 1.0f)
        )
        val r2 = listOf(
            KeyDef("⇥", action = KeyAction.INSERT_TEXT, insertPayload = "\t", weight = 1.2f, isSpecial = true),
            KeyDef("£", weight = 1.0f),
            KeyDef("$", weight = 1.0f),
            KeyDef("%", weight = 1.0f),
            KeyDef("^", secondaryText = "→", weight = 1.0f),
            KeyDef("°", secondaryText = "•", weight = 1.0f),
            KeyDef("_", weight = 1.0f),
            KeyDef("=", secondaryText = "≠", weight = 1.0f),
            KeyDef("[", weight = 1.0f),
            KeyDef("]", weight = 1.0f)
        )
        val r3 = listOf(
            KeyDef("?123", action = KeyAction.SYMBOL_SWITCH, weight = 1.3f, isSpecial = true),
            KeyDef("✥", secondaryText = "র‍্য", weight = 1.0f),
            KeyDef("®", weight = 1.0f),
            KeyDef("©", weight = 1.0f),
            KeyDef("™", weight = 1.0f),
            KeyDef("\\", weight = 1.0f),
            KeyDef("<", weight = 1.0f),
            KeyDef(">", weight = 1.0f),
            KeyDef("⌫", action = KeyAction.BACKSPACE, weight = 1.3f, isSpecial = true)
        )
        val r4 = listOf(
            KeyDef("ABC", action = KeyAction.SYMBOL_SWITCH, weight = 1.3f, isSpecial = true),
            KeyDef(",", secondaryText = "...", weight = 0.9f),
            KeyDef("English", action = KeyAction.SPACE, weight = 4.2f),
            KeyDef(".", secondaryText = "...", weight = 0.9f),
            KeyDef("↵", secondaryText = "...", action = KeyAction.ENTER, weight = 1.4f, isSpecial = true)
        )
        return listOf(KeyboardRow(r0), KeyboardRow(r1), KeyboardRow(r2), KeyboardRow(r3), KeyboardRow(r4))
    }

    fun getCodingLayout(): List<KeyboardRow> {
        val r1 = listOf("{", "}", "[", "]", "(", ")", "<", ">", "/", "\\")
            .map { KeyDef(primaryText = it, weight = 1f) }
        val r2 = listOf("@", "#", "$", "%", "^", "&", "*", "-", "+", "=")
            .map { KeyDef(primaryText = it, weight = 1f) }
        val r3 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
            .map { KeyDef(primaryText = it, weight = 1f) }
        val r4 = mutableListOf<KeyDef>()
        r4.add(KeyDef(primaryText = "ABC", action = KeyAction.SYMBOL_SWITCH, weight = 1.3f, isSpecial = true))
        listOf(";", ":", "'", "\"", "_", "|", "!", "?").forEach {
            r4.add(KeyDef(primaryText = it, weight = 1f))
        }
        r4.add(KeyDef(primaryText = "⌫", action = KeyAction.BACKSPACE, weight = 1.3f, isSpecial = true))

        val r5 = listOf(
            KeyDef(primaryText = "</> Snippets", action = KeyAction.CODING_PANEL, weight = 2.2f, isSpecial = true),
            KeyDef(primaryText = "Tab", action = KeyAction.INSERT_TEXT, insertPayload = "    ", weight = 1.2f, isSpecial = true),
            KeyDef(primaryText = "Code Space", action = KeyAction.SPACE, weight = 3.6f),
            KeyDef(primaryText = "↵", action = KeyAction.ENTER, weight = 1.5f, isSpecial = true)
        )
        return listOf(KeyboardRow(r1), KeyboardRow(r2), KeyboardRow(r3), KeyboardRow(r4), KeyboardRow(r5))
    }

    fun getGamingLayout(): List<KeyboardRow> {
        val r1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
            .map { KeyDef(primaryText = it, weight = 1f) }
        val r2 = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P")
            .map { KeyDef(primaryText = it, weight = 1f) }
        val r3 = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L")
            .map { KeyDef(primaryText = it, weight = 1f) }
        val r4 = mutableListOf<KeyDef>()
        r4.add(KeyDef(primaryText = "⇧", action = KeyAction.SHIFT, weight = 1.3f, isSpecial = true))
        listOf("Z", "X", "C", "V", "B", "N", "M").forEach {
            r4.add(KeyDef(primaryText = it, weight = 1f))
        }
        r4.add(KeyDef(primaryText = "⌫", action = KeyAction.BACKSPACE, weight = 1.3f, isSpecial = true))

        val r5 = listOf(
            KeyDef(primaryText = "ABC", action = KeyAction.SYMBOL_SWITCH, weight = 1.2f, isSpecial = true),
            KeyDef(primaryText = "GG", action = KeyAction.INSERT_TEXT, insertPayload = "GG ", weight = 1.1f, isSpecial = true),
            KeyDef(primaryText = "glhf", action = KeyAction.INSERT_TEXT, insertPayload = "glhf! ", weight = 1.1f, isSpecial = true),
            KeyDef(primaryText = "AFK", action = KeyAction.INSERT_TEXT, insertPayload = "AFK ", weight = 1.1f, isSpecial = true),
            KeyDef(primaryText = "Game Space", action = KeyAction.SPACE, weight = 2.8f),
            KeyDef(primaryText = "↵", action = KeyAction.ENTER, weight = 1.5f, isSpecial = true)
        )
        return listOf(KeyboardRow(r1), KeyboardRow(r2), KeyboardRow(r3), KeyboardRow(r4), KeyboardRow(r5))
    }
}
