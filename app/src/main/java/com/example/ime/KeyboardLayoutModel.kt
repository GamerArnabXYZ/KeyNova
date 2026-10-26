package com.example.ime

import android.content.Context
import android.content.res.Resources
import com.example.R

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

    @Volatile
    private var resources: Resources? = null

    fun init(context: Context) {
        resources = context.applicationContext.resources
    }

    private fun getArray(resId: Int, fallback: List<String>): List<String> {
        return runCatching {
            resources?.getStringArray(resId)?.toList()
        }.getOrNull()?.takeIf { it.isNotEmpty() } ?: fallback
    }

    private fun getStr(resId: Int, fallback: String): String {
        return runCatching {
            resources?.getString(resId)
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: fallback
    }

    /**
     * Standard Number Row (English digits 1-0 with Bangla secondary hints ১-০)
     * Loaded from keyboard_symbols.xml
     */
    fun getNumberRow(): KeyboardRow {
        val enDigits = getArray(
            R.array.keyboard_numbers_en,
            listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        )
        val bnDigits = getArray(
            R.array.keyboard_numbers_bn,
            listOf("১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯", "০")
        )

        val keys = enDigits.mapIndexed { index, en ->
            KeyDef(
                primaryText = en,
                secondaryText = bnDigits.getOrNull(index),
                weight = 1f
            )
        }
        return KeyboardRow(keys)
    }

    /**
     * Bangla Number Row (Bangla digits ১-০ with English secondary hints 1-0)
     * Loaded from keyboard_symbols.xml
     */
    fun getBanglaNumberRow(): KeyboardRow {
        val bnDigits = getArray(
            R.array.keyboard_numbers_bn,
            listOf("১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯", "০")
        )
        val enDigits = getArray(
            R.array.keyboard_numbers_en,
            listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        )

        val keys = bnDigits.mapIndexed { index, bn ->
            KeyDef(
                primaryText = bn,
                secondaryText = enDigits.getOrNull(index),
                weight = 1f
            )
        }
        return KeyboardRow(keys)
    }

    /**
     * English QWERTY Layout:
     * Loaded dynamically from keyboard_en.xml
     */
    fun getEnglishLayout(shiftState: ShiftState, isCaps: Boolean, showGlobeKey: Boolean = true): List<KeyboardRow> {
        val isUpper = shiftState != ShiftState.OFF || isCaps

        val r1Lower = getArray(R.array.keyboard_en_row1_lower, listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"))
        val r1Upper = getArray(R.array.keyboard_en_row1_upper, listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"))
        val r1Sec = getArray(R.array.keyboard_en_row1_secondary, listOf("৳", "%", "_", ";", "<", ">", "+", "=", "[", "]"))

        val r1 = (if (isUpper) r1Upper else r1Lower).mapIndexed { i, char ->
            val sec = r1Sec.getOrNull(i)?.takeIf { it.isNotBlank() }
            KeyDef(char, sec, weight = 1f)
        }

        val r2Lower = getArray(R.array.keyboard_en_row2_lower, listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"))
        val r2Upper = getArray(R.array.keyboard_en_row2_upper, listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"))
        val r2Sec = getArray(R.array.keyboard_en_row2_secondary, listOf("@", "#", "&", "*", "-", "!", "?", "(", ")"))

        val r2 = (if (isUpper) r2Upper else r2Lower).mapIndexed { i, char ->
            val sec = r2Sec.getOrNull(i)?.takeIf { it.isNotBlank() }
            KeyDef(char, sec, weight = 1f)
        }

        val r3Lower = getArray(R.array.keyboard_en_row3_lower, listOf("z", "x", "c", "v", "b", "n", "m"))
        val r3Upper = getArray(R.array.keyboard_en_row3_upper, listOf("Z", "X", "C", "V", "B", "N", "M"))
        val r3Sec = getArray(R.array.keyboard_en_row3_secondary, listOf("\"", "", "", "", "'", ":", "/"))

        val shiftLabel = if (shiftState == ShiftState.CAPS_LOCKED) {
            getStr(R.string.key_en_shift_caps, "⇪")
        } else {
            getStr(R.string.key_en_shift_off, "⇧")
        }
        val backspaceLabel = getStr(R.string.key_en_backspace, "⌫")

        val r3Keys = mutableListOf<KeyDef>()
        r3Keys.add(KeyDef(primaryText = shiftLabel, action = KeyAction.SHIFT, weight = 1.3f, isSpecial = true))
        (if (isUpper) r3Upper else r3Lower).forEachIndexed { i, char ->
            val sec = r3Sec.getOrNull(i)?.takeIf { it.isNotBlank() }
            r3Keys.add(KeyDef(char, sec, weight = 1f))
        }
        r3Keys.add(KeyDef(primaryText = backspaceLabel, action = KeyAction.BACKSPACE, weight = 1.3f, isSpecial = true))

        val symbolSwitchLabel = getStr(R.string.key_en_symbol_switch, "?123")
        val globeLabel = getStr(R.string.key_en_globe, "🌐")
        val emojiLabel = getStr(R.string.key_en_emoji, "☺")
        val spaceLabel = getStr(R.string.key_en_space_label, "English")
        val commaLabel = getStr(R.string.key_en_comma, ",")
        val periodLabel = getStr(R.string.key_en_period, ".")
        val enterLabel = getStr(R.string.key_en_enter, "↵")

        val r4Keys = mutableListOf<KeyDef>()
        r4Keys.add(KeyDef(primaryText = symbolSwitchLabel, action = KeyAction.SYMBOL_SWITCH, weight = 1.2f, isSpecial = true))
        if (showGlobeKey) {
            r4Keys.add(KeyDef(primaryText = globeLabel, action = KeyAction.LANGUAGE_SWITCH, weight = 1.0f, isSpecial = true))
        } else {
            r4Keys.add(KeyDef(primaryText = emojiLabel, action = KeyAction.EMOJI_PANEL, weight = 1.0f, isSpecial = true))
        }
        r4Keys.add(KeyDef(primaryText = commaLabel, secondaryText = "...", weight = 0.9f))
        val spaceWeight = if (showGlobeKey) 3.6f else 4.2f
        r4Keys.add(KeyDef(primaryText = spaceLabel, action = KeyAction.SPACE, weight = spaceWeight))
        r4Keys.add(KeyDef(primaryText = periodLabel, secondaryText = "...", weight = 0.9f))
        r4Keys.add(KeyDef(primaryText = enterLabel, action = KeyAction.ENTER, weight = 1.3f, isSpecial = true))

        return listOf(KeyboardRow(r1), KeyboardRow(r2), KeyboardRow(r3Keys), KeyboardRow(r4Keys))
    }

    /**
     * Bangla National / Jatiya (জাতীয়) Layout:
     * Loaded dynamically from keyboard_bn.xml
     */
    fun getBanglaLayout(shifted: Boolean, showGlobeKey: Boolean = true): List<KeyboardRow> {
        val symbolSwitch = getStr(R.string.key_bn_symbol_switch, "?১২৩")
        val spaceLabel = getStr(R.string.key_bn_space_label, "জাতীয়")
        val dariyaLabel = getStr(R.string.key_bn_dariya, "।")
        val enterLabel = getStr(R.string.key_en_enter, "↵")

        return if (!shifted) {
            val r1Prim = getArray(R.array.keyboard_bn_row1_unshifted, listOf("ঙ", "য", "ড", "প", "ট", "চ", "জ", "হ", "গ", "ড়"))
            val r1Sec = getArray(R.array.keyboard_bn_row1_secondary, listOf("ং", "য়", "ঢ", "ফ", "ঠ", "ছ", "ঝ", "ঞ", "ঘ", "ঢ়"))
            val r1 = r1Prim.mapIndexed { i, char ->
                KeyDef(char, r1Sec.getOrNull(i)?.takeIf { it.isNotBlank() }, weight = 1f)
            }

            val r2Prim = getArray(R.array.keyboard_bn_row2_unshifted, listOf("ৃ", "ু", "ি", "ব", "্", "া", "ক", "ত", "দ"))
            val r2Sec = getArray(R.array.keyboard_bn_row2_secondary, listOf("ঋ", "উ", "ই", "ভ", "", "আ", "খ", "ৎ", "ধ"))
            val r2 = r2Prim.mapIndexed { i, char ->
                KeyDef(char, r2Sec.getOrNull(i)?.takeIf { it.isNotBlank() }, weight = 1f)
            }

            val r3Prim = getArray(R.array.keyboard_bn_row3_unshifted, listOf("ঁ", "ো", "ে", "র", "ন", "স", "ম"))
            val r3Sec = getArray(R.array.keyboard_bn_row3_secondary, listOf("ঃ", "ও", "এ", "ল", "ণ", "ষ", "শ"))

            val r3 = mutableListOf<KeyDef>()
            r3.add(KeyDef(primaryText = "⇧", action = KeyAction.SHIFT, weight = 1.3f, isSpecial = true))
            r3Prim.forEachIndexed { i, char ->
                r3.add(KeyDef(char, r3Sec.getOrNull(i)?.takeIf { it.isNotBlank() }, weight = 1f))
            }
            r3.add(KeyDef(primaryText = "⌫", action = KeyAction.BACKSPACE, weight = 1.3f, isSpecial = true))

            val r4Keys = mutableListOf<KeyDef>()
            r4Keys.add(KeyDef(primaryText = symbolSwitch, action = KeyAction.SYMBOL_SWITCH, weight = 1.2f, isSpecial = true))
            if (showGlobeKey) {
                r4Keys.add(KeyDef(primaryText = "🌐", action = KeyAction.LANGUAGE_SWITCH, weight = 1.0f, isSpecial = true))
            } else {
                r4Keys.add(KeyDef(primaryText = "☺", action = KeyAction.EMOJI_PANEL, weight = 1.0f, isSpecial = true))
            }
            r4Keys.add(KeyDef(primaryText = ",", secondaryText = "...", weight = 0.9f))
            val spaceWeight = if (showGlobeKey) 3.6f else 4.2f
            r4Keys.add(KeyDef(primaryText = spaceLabel, action = KeyAction.SPACE, weight = spaceWeight))
            r4Keys.add(KeyDef(primaryText = dariyaLabel, secondaryText = "...", weight = 0.9f))
            r4Keys.add(KeyDef(primaryText = enterLabel, action = KeyAction.ENTER, weight = 1.3f, isSpecial = true))

            listOf(KeyboardRow(r1), KeyboardRow(r2), KeyboardRow(r3), KeyboardRow(r4Keys))
        } else {
            val r1Prim = getArray(R.array.keyboard_bn_row1_shifted, listOf("ং", "্য", "ঢ", "ফ", "ঠ", "ছ", "ঝ", "ঞ", "ঘ", "ঢ়"))
            val r1 = r1Prim.map { KeyDef(it, null, weight = 1f) }

            val r2Prim = getArray(R.array.keyboard_bn_row2_shifted, listOf("ঋ", "ূ", "ী", "ভ", "’", "অ", "খ", "থ", "ধ"))
            val r2Sec = getArray(R.array.keyboard_bn_row2_shifted_secondary, listOf("", "ঊ", "ঈ", "", "", "", "", "", ""))
            val r2 = r2Prim.mapIndexed { i, char ->
                KeyDef(char, r2Sec.getOrNull(i)?.takeIf { it.isNotBlank() }, weight = 1f)
            }

            val r3Prim = getArray(R.array.keyboard_bn_row3_shifted, listOf("ঃ", "ৌ", "ৈ", "্র", "ণ", "ষ", "শ"))
            val r3Sec = getArray(R.array.keyboard_bn_row3_shifted_secondary, listOf("", "ঔ", "ঐ", "", "", "", ""))

            val r3 = mutableListOf<KeyDef>()
            r3.add(KeyDef(primaryText = "⬆", action = KeyAction.SHIFT, weight = 1.3f, isSpecial = true))
            r3Prim.forEachIndexed { i, char ->
                r3.add(KeyDef(char, r3Sec.getOrNull(i)?.takeIf { it.isNotBlank() }, weight = 1f))
            }
            r3.add(KeyDef(primaryText = "⌫", action = KeyAction.BACKSPACE, weight = 1.3f, isSpecial = true))

            val r4Keys = mutableListOf<KeyDef>()
            r4Keys.add(KeyDef(primaryText = symbolSwitch, action = KeyAction.SYMBOL_SWITCH, weight = 1.2f, isSpecial = true))
            if (showGlobeKey) {
                r4Keys.add(KeyDef(primaryText = "🌐", action = KeyAction.LANGUAGE_SWITCH, weight = 1.0f, isSpecial = true))
            } else {
                r4Keys.add(KeyDef(primaryText = "☺", action = KeyAction.EMOJI_PANEL, weight = 1.0f, isSpecial = true))
            }
            r4Keys.add(KeyDef(primaryText = ",", secondaryText = "...", weight = 0.9f))
            val spaceWeight = if (showGlobeKey) 3.6f else 4.2f
            r4Keys.add(KeyDef(primaryText = spaceLabel, action = KeyAction.SPACE, weight = spaceWeight))
            r4Keys.add(KeyDef(primaryText = dariyaLabel, secondaryText = "...", weight = 0.9f))
            r4Keys.add(KeyDef(primaryText = enterLabel, action = KeyAction.ENTER, weight = 1.3f, isSpecial = true))

            listOf(KeyboardRow(r1), KeyboardRow(r2), KeyboardRow(r3), KeyboardRow(r4Keys))
        }
    }

    /**
     * Hindi Devanagari (हिन्दी) Layout:
     * Loaded dynamically from keyboard_hi.xml
     */
    fun getHindiLayout(shifted: Boolean, showGlobeKey: Boolean = true): List<KeyboardRow> {
        val spaceLabel = getStr(R.string.key_hi_space_label, "हिन्दी")
        val enterLabel = getStr(R.string.key_en_enter, "↵")

        return if (!shifted) {
            val r1Keys = getArray(R.array.keyboard_hi_row1_consonants, listOf("क", "ख", "ग", "घ", "ङ", "च", "छ", "ज", "झ", "ञ"))
            val r2Keys = getArray(R.array.keyboard_hi_row2_consonants, listOf("ट", "ठ", "ड", "ढ", "ण", "त", "थ", "द", "ध", "न"))
            val r3Keys = getArray(R.array.keyboard_hi_row3_consonants, listOf("प", "फ", "ब", "भ", "म", "य", "र", "ल", "व"))

            val r1 = r1Keys.map { KeyDef(primaryText = it, weight = 1f) }
            val r2 = r2Keys.map { KeyDef(primaryText = it, weight = 1f) }

            val r3 = mutableListOf<KeyDef>()
            val shiftSwar = getStr(R.string.key_hi_shift_swar, "स्वर")
            r3.add(KeyDef(primaryText = shiftSwar, action = KeyAction.SHIFT, weight = 1.3f, isSpecial = true))
            r3Keys.forEach {
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
            r4Keys.add(KeyDef(primaryText = spaceLabel, action = KeyAction.SPACE, weight = spaceWeight))
            r4Keys.add(KeyDef(primaryText = "।", secondaryText = "...", weight = 0.9f))
            r4Keys.add(KeyDef(primaryText = enterLabel, action = KeyAction.ENTER, weight = 1.3f, isSpecial = true))

            listOf(KeyboardRow(r1), KeyboardRow(r2), KeyboardRow(r3), KeyboardRow(r4Keys))
        } else {
            val r1Keys = getArray(R.array.keyboard_hi_row1_vowels, listOf("अ", "आ", "इ", "ई", "उ", "ऊ", "ऋ", "ए", "ऐ", "ओ"))
            val r2Keys = getArray(R.array.keyboard_hi_row2_matras, listOf("ा", "ि", "ी", "ु", "ू", "ृ", "े", "ै", "ो", "ौ"))
            val r3Keys = getArray(R.array.keyboard_hi_row3_special, listOf("्", "ं", "ः", "ँ", "स", "ह", "ड़", "ढ़", "औ"))

            val r1 = r1Keys.map { KeyDef(primaryText = it, weight = 1f) }
            val r2 = r2Keys.map { KeyDef(primaryText = it, weight = 1f) }

            val r3 = mutableListOf<KeyDef>()
            val shiftVyanjan = getStr(R.string.key_hi_shift_vyanjan, "व्यं")
            r3.add(KeyDef(primaryText = shiftVyanjan, action = KeyAction.SHIFT, weight = 1.3f, isSpecial = true))
            r3Keys.forEach {
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
            r4Keys.add(KeyDef(primaryText = spaceLabel, action = KeyAction.SPACE, weight = spaceWeight))
            r4Keys.add(KeyDef(primaryText = "।", secondaryText = "...", weight = 0.9f))
            r4Keys.add(KeyDef(primaryText = enterLabel, action = KeyAction.ENTER, weight = 1.3f, isSpecial = true))

            listOf(KeyboardRow(r1), KeyboardRow(r2), KeyboardRow(r3), KeyboardRow(r4Keys))
        }
    }

    /**
     * Symbols Page 1 (?123):
     * Loaded dynamically from keyboard_symbols.xml
     */
    fun getSymbolsPage1(): List<KeyboardRow> {
        val r0 = getNumberRow()

        val r1Prim = getArray(R.array.keyboard_symbols1_row1, listOf("~", "\\", "<", ">", "_", "=", "[", "]"))
        val r1 = mutableListOf<KeyDef>()
        r1.add(KeyDef("⇥", action = KeyAction.INSERT_TEXT, insertPayload = "\t", weight = 1.2f, isSpecial = true))
        r1Prim.forEach { r1.add(KeyDef(it, weight = 1.0f)) }

        val r2Prim = getArray(R.array.keyboard_symbols1_row2, listOf("@", "#", "৳", "%", "&", "*", "-", "+", "(", ")"))
        val r2Sec = getArray(R.array.keyboard_symbols1_row2_secondary, listOf("", "", "$", "", "", "★", "_", "", "<", ">"))
        val r2 = r2Prim.mapIndexed { i, char ->
            KeyDef(char, r2Sec.getOrNull(i)?.takeIf { it.isNotBlank() }, weight = 1.0f)
        }

        val r3Prim = getArray(R.array.keyboard_symbols1_row3, listOf("!", "\"", "'", ":", ";", "/", "?"))
        val r3Sec = getArray(R.array.keyboard_symbols1_row3_secondary, listOf("", "", "", "°", "", "", ""))
        val switchPage2 = getStr(R.string.key_symbols_switch_page2, "= \\ <")

        val r3 = mutableListOf<KeyDef>()
        r3.add(KeyDef(switchPage2, action = KeyAction.SYMBOL_SWITCH, weight = 1.3f, isSpecial = true))
        r3Prim.forEachIndexed { i, char ->
            r3.add(KeyDef(char, r3Sec.getOrNull(i)?.takeIf { it.isNotBlank() }, weight = 1.0f))
        }
        r3.add(KeyDef("⌫", action = KeyAction.BACKSPACE, weight = 1.3f, isSpecial = true))

        val switchAbc = getStr(R.string.key_symbols_switch_abc, "ABC")
        val enterLabel = getStr(R.string.key_en_enter, "↵")

        val r4 = listOf(
            KeyDef(switchAbc, action = KeyAction.SYMBOL_SWITCH, weight = 1.3f, isSpecial = true),
            KeyDef(",", secondaryText = "...", weight = 0.9f),
            KeyDef("English", action = KeyAction.SPACE, weight = 4.2f),
            KeyDef(".", secondaryText = "...", weight = 0.9f),
            KeyDef(enterLabel, action = KeyAction.ENTER, weight = 1.4f, isSpecial = true)
        )
        return listOf(r0, KeyboardRow(r1), KeyboardRow(r2), KeyboardRow(r3), KeyboardRow(r4))
    }

    /**
     * Symbols Page 2 (= \ <):
     * Loaded dynamically from keyboard_symbols.xml
     */
    fun getSymbolsPage2(): List<KeyboardRow> {
        val r0Prim = getArray(R.array.keyboard_symbols2_row0, listOf("☞", "◐", "⊕", "✖", "✔", "✪", "∞", "⇒", "➤", "✆"))
        val r0Sec = getArray(R.array.keyboard_symbols2_row0_secondary, listOf("", "", "", "", "", "★", "", "", "", "✉"))
        val r0 = r0Prim.mapIndexed { i, char ->
            KeyDef(char, r0Sec.getOrNull(i)?.takeIf { it.isNotBlank() }, weight = 1.0f)
        }

        val r1Prim = getArray(R.array.keyboard_symbols2_row1, listOf("~", "`", "|", "₹", "√", "π", "÷", "×", "{", "}"))
        val r1Sec = getArray(R.array.keyboard_symbols2_row1_secondary, listOf("", "", "", "৹", "", "Π", "", "", "", ""))
        val r1 = r1Prim.mapIndexed { i, char ->
            KeyDef(char, r1Sec.getOrNull(i)?.takeIf { it.isNotBlank() }, weight = 1.0f)
        }

        val r2Prim = getArray(R.array.keyboard_symbols2_row2, listOf("£", "$", "%", "^", "°", "_", "=", "[", "]"))
        val r2Sec = getArray(R.array.keyboard_symbols2_row2_secondary, listOf("", "", "", "→", "•", "", "≠", "", ""))
        val r2 = mutableListOf<KeyDef>()
        r2.add(KeyDef("⇥", action = KeyAction.INSERT_TEXT, insertPayload = "\t", weight = 1.2f, isSpecial = true))
        r2Prim.forEachIndexed { i, char ->
            r2.add(KeyDef(char, r2Sec.getOrNull(i)?.takeIf { it.isNotBlank() }, weight = 1.0f))
        }

        val r3Prim = getArray(R.array.keyboard_symbols2_row3, listOf("✥", "®", "©", "™", "\\", "<", ">"))
        val r3Sec = getArray(R.array.keyboard_symbols2_row3_secondary, listOf("র‍্য", "", "", "", "", "", ""))
        val r3 = mutableListOf<KeyDef>()
        r3.add(KeyDef("?123", action = KeyAction.SYMBOL_SWITCH, weight = 1.3f, isSpecial = true))
        r3Prim.forEachIndexed { i, char ->
            r3.add(KeyDef(char, r3Sec.getOrNull(i)?.takeIf { it.isNotBlank() }, weight = 1.0f))
        }
        r3.add(KeyDef("⌫", action = KeyAction.BACKSPACE, weight = 1.3f, isSpecial = true))

        val switchAbc = getStr(R.string.key_symbols_switch_abc, "ABC")
        val enterLabel = getStr(R.string.key_en_enter, "↵")

        val r4 = listOf(
            KeyDef(switchAbc, action = KeyAction.SYMBOL_SWITCH, weight = 1.3f, isSpecial = true),
            KeyDef(",", secondaryText = "...", weight = 0.9f),
            KeyDef("English", action = KeyAction.SPACE, weight = 4.2f),
            KeyDef(".", secondaryText = "...", weight = 0.9f),
            KeyDef(enterLabel, action = KeyAction.ENTER, weight = 1.4f, isSpecial = true)
        )
        return listOf(KeyboardRow(r0), KeyboardRow(r1), KeyboardRow(r2), KeyboardRow(r3), KeyboardRow(r4))
    }

    /**
     * Coding Layout:
     * Loaded dynamically from keyboard_tools.xml
     */
    fun getCodingLayout(): List<KeyboardRow> {
        val r1Keys = getArray(R.array.keyboard_coding_row1, listOf("{", "}", "[", "]", "(", ")", "<", ">", "/", "\\"))
        val r2Keys = getArray(R.array.keyboard_coding_row2, listOf("@", "#", "$", "%", "^", "&", "*", "-", "+", "="))
        val r3Keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        val r4Keys = getArray(R.array.keyboard_coding_row4, listOf(";", ":", "'", "\"", "_", "|", "!", "?"))

        val r1 = r1Keys.map { KeyDef(primaryText = it, weight = 1f) }
        val r2 = r2Keys.map { KeyDef(primaryText = it, weight = 1f) }
        val r3 = r3Keys.map { KeyDef(primaryText = it, weight = 1f) }

        val r4 = mutableListOf<KeyDef>()
        r4.add(KeyDef(primaryText = "ABC", action = KeyAction.SYMBOL_SWITCH, weight = 1.3f, isSpecial = true))
        r4Keys.forEach {
            r4.add(KeyDef(primaryText = it, weight = 1f))
        }
        r4.add(KeyDef(primaryText = "⌫", action = KeyAction.BACKSPACE, weight = 1.3f, isSpecial = true))

        val snippetsLabel = getStr(R.string.key_coding_snippets, "</> Snippets")
        val tabLabel = getStr(R.string.key_coding_tab, "Tab")
        val spaceLabel = getStr(R.string.key_coding_space, "Code Space")
        val enterLabel = getStr(R.string.key_en_enter, "↵")

        val r5 = listOf(
            KeyDef(primaryText = snippetsLabel, action = KeyAction.CODING_PANEL, weight = 2.2f, isSpecial = true),
            KeyDef(primaryText = tabLabel, action = KeyAction.INSERT_TEXT, insertPayload = "    ", weight = 1.2f, isSpecial = true),
            KeyDef(primaryText = spaceLabel, action = KeyAction.SPACE, weight = 3.6f),
            KeyDef(primaryText = enterLabel, action = KeyAction.ENTER, weight = 1.5f, isSpecial = true)
        )
        return listOf(KeyboardRow(r1), KeyboardRow(r2), KeyboardRow(r3), KeyboardRow(r4), KeyboardRow(r5))
    }

    /**
     * Gaming WASD Layout:
     * Loaded dynamically from keyboard_tools.xml
     */
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

        val ggLabel = getStr(R.string.key_gaming_gg, "GG")
        val glhfLabel = getStr(R.string.key_gaming_glhf, "glhf")
        val afkLabel = getStr(R.string.key_gaming_afk, "AFK")
        val gameSpaceLabel = getStr(R.string.key_gaming_space, "Game Space")
        val enterLabel = getStr(R.string.key_en_enter, "↵")

        val r5 = listOf(
            KeyDef(primaryText = "ABC", action = KeyAction.SYMBOL_SWITCH, weight = 1.2f, isSpecial = true),
            KeyDef(primaryText = ggLabel, action = KeyAction.INSERT_TEXT, insertPayload = "GG ", weight = 1.1f, isSpecial = true),
            KeyDef(primaryText = glhfLabel, action = KeyAction.INSERT_TEXT, insertPayload = "glhf! ", weight = 1.1f, isSpecial = true),
            KeyDef(primaryText = afkLabel, action = KeyAction.INSERT_TEXT, insertPayload = "AFK ", weight = 1.1f, isSpecial = true),
            KeyDef(primaryText = gameSpaceLabel, action = KeyAction.SPACE, weight = 2.8f),
            KeyDef(primaryText = enterLabel, action = KeyAction.ENTER, weight = 1.5f, isSpecial = true)
        )
        return listOf(KeyboardRow(r1), KeyboardRow(r2), KeyboardRow(r3), KeyboardRow(r4), KeyboardRow(r5))
    }
}
