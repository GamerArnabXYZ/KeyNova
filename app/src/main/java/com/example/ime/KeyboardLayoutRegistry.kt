package com.example.ime

import android.content.Context
import android.content.res.Resources
import com.example.data.KeyboardLanguage

/**
 * Data model for dynamically registered keyboard layouts loaded purely from XML.
 * Users can add any language/layout in keyboard_registry.xml without modifying Kotlin code.
 */
data class DynamicKeyboardLayout(
    val id: String,
    val languageCode: String,
    val languageName: String,
    val layoutName: String,
    val scriptName: String = "Script",
    val spaceLabel: String = languageName,
    val symbolSwitch: String = "?123",
    val periodKey: String = ".",
    val row1: List<String>,
    val row1Upper: List<String> = emptyList(),
    val row1Sec: List<String> = emptyList(),
    val row2: List<String>,
    val row2Upper: List<String> = emptyList(),
    val row2Sec: List<String> = emptyList(),
    val row3: List<String>,
    val row3Upper: List<String> = emptyList(),
    val row3Sec: List<String> = emptyList(),
    val shiftRow1: List<String> = emptyList(),
    val shiftRow1Sec: List<String> = emptyList(),
    val shiftRow2: List<String> = emptyList(),
    val shiftRow2Sec: List<String> = emptyList(),
    val shiftRow3: List<String> = emptyList(),
    val shiftRow3Sec: List<String> = emptyList()
) {
    val keyboardLanguage: KeyboardLanguage
        get() = KeyboardLanguage.fromCode(languageCode)
}

object KeyboardLayoutRegistry {

    @Volatile
    private var cachedLayouts: List<DynamicKeyboardLayout>? = null

    fun getLayouts(context: Context?): List<DynamicKeyboardLayout> {
        val cached = cachedLayouts
        if (cached != null) return cached
        if (context == null) return getDefaultFallbackLayouts()

        val loaded = loadFromResources(context)
        val finalLayouts = if (loaded.isNotEmpty()) loaded else getDefaultFallbackLayouts()
        cachedLayouts = finalLayouts
        return finalLayouts
    }

    fun getLayoutById(id: String, context: Context? = null): DynamicKeyboardLayout {
        val all = getLayouts(context)
        return all.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: all.first()
    }

    fun reload(context: Context): List<DynamicKeyboardLayout> {
        val loaded = loadFromResources(context)
        val finalLayouts = if (loaded.isNotEmpty()) loaded else getDefaultFallbackLayouts()
        cachedLayouts = finalLayouts
        return finalLayouts
    }

    private fun loadFromResources(context: Context): List<DynamicKeyboardLayout> {
        val res = context.resources
        val pkg = context.packageName

        val registryResId = res.getIdentifier("registered_keyboard_layouts", "array", pkg)
        if (registryResId == 0) return getDefaultFallbackLayouts()

        val layoutIds = runCatching { res.getStringArray(registryResId) }.getOrNull() ?: return getDefaultFallbackLayouts()

        return layoutIds.mapNotNull { layoutId ->
            loadSingleLayout(res, pkg, layoutId)
        }
    }

    private fun loadSingleLayout(res: Resources, pkg: String, layoutId: String): DynamicKeyboardLayout? {
        val langCode = getString(res, "layout_${layoutId}_lang_code", pkg, "en")
        val langName = getString(res, "layout_${layoutId}_lang_name", pkg, layoutId)
        val layoutName = getString(res, "layout_${layoutId}_layout_name", pkg, "Default")
        val script = getString(res, "layout_${layoutId}_script", pkg, "Default")
        val spaceLabel = getString(res, "layout_${layoutId}_space_label", pkg, langName)
        val symbolSwitch = getString(res, "layout_${layoutId}_symbol_switch", pkg, "?123")
        val periodKey = getString(res, "layout_${layoutId}_period", pkg, ".")

        val r1 = getStringArray(res, "layout_${layoutId}_row1", pkg)
        val r2 = getStringArray(res, "layout_${layoutId}_row2", pkg)
        val r3 = getStringArray(res, "layout_${layoutId}_row3", pkg)

        if (r1.isEmpty() || r2.isEmpty() || r3.isEmpty()) return null

        return DynamicKeyboardLayout(
            id = layoutId,
            languageCode = langCode,
            languageName = langName,
            layoutName = layoutName,
            scriptName = script,
            spaceLabel = spaceLabel,
            symbolSwitch = symbolSwitch,
            periodKey = periodKey,
            row1 = r1,
            row1Upper = getStringArray(res, "layout_${layoutId}_row1_upper", pkg),
            row1Sec = getStringArray(res, "layout_${layoutId}_row1_sec", pkg),
            row2 = r2,
            row2Upper = getStringArray(res, "layout_${layoutId}_row2_upper", pkg),
            row2Sec = getStringArray(res, "layout_${layoutId}_row2_sec", pkg),
            row3 = r3,
            row3Upper = getStringArray(res, "layout_${layoutId}_row3_upper", pkg),
            row3Sec = getStringArray(res, "layout_${layoutId}_row3_sec", pkg),
            shiftRow1 = getStringArray(res, "layout_${layoutId}_shift_row1", pkg),
            shiftRow1Sec = getStringArray(res, "layout_${layoutId}_shift_row1_sec", pkg),
            shiftRow2 = getStringArray(res, "layout_${layoutId}_shift_row2", pkg),
            shiftRow2Sec = getStringArray(res, "layout_${layoutId}_shift_row2_sec", pkg),
            shiftRow3 = getStringArray(res, "layout_${layoutId}_shift_row3", pkg),
            shiftRow3Sec = getStringArray(res, "layout_${layoutId}_shift_row3_sec", pkg)
        )
    }

    private fun getString(res: Resources, name: String, pkg: String, default: String): String {
        val id = res.getIdentifier(name, "string", pkg)
        return if (id != 0) runCatching { res.getString(id) }.getOrDefault(default) else default
    }

    private fun getStringArray(res: Resources, name: String, pkg: String): List<String> {
        val id = res.getIdentifier(name, "array", pkg)
        return if (id != 0) runCatching { res.getStringArray(id).toList() }.getOrDefault(emptyList()) else emptyList()
    }

    /**
     * Converts a DynamicKeyboardLayout into KeyboardRow matrix for Jetpack Compose UI.
     */
    fun buildRows(
        layout: DynamicKeyboardLayout,
        shiftState: ShiftState,
        isCaps: Boolean,
        showGlobeKey: Boolean = true
    ): List<KeyboardRow> {
        val isShifted = shiftState != ShiftState.OFF || isCaps

        // Row 1
        val r1Prim = when {
            isShifted && layout.shiftRow1.isNotEmpty() -> layout.shiftRow1
            isShifted && layout.row1Upper.isNotEmpty() -> layout.row1Upper
            else -> layout.row1
        }
        val r1Sec = when {
            isShifted && layout.shiftRow1Sec.isNotEmpty() -> layout.shiftRow1Sec
            else -> layout.row1Sec
        }
        val r1 = r1Prim.mapIndexed { i, char ->
            KeyDef(char, r1Sec.getOrNull(i)?.takeIf { it.isNotBlank() }, weight = 1f)
        }

        // Row 2
        val r2Prim = when {
            isShifted && layout.shiftRow2.isNotEmpty() -> layout.shiftRow2
            isShifted && layout.row2Upper.isNotEmpty() -> layout.row2Upper
            else -> layout.row2
        }
        val r2Sec = when {
            isShifted && layout.shiftRow2Sec.isNotEmpty() -> layout.shiftRow2Sec
            else -> layout.row2Sec
        }
        val r2 = r2Prim.mapIndexed { i, char ->
            KeyDef(char, r2Sec.getOrNull(i)?.takeIf { it.isNotBlank() }, weight = 1f)
        }

        // Row 3
        val r3Prim = when {
            isShifted && layout.shiftRow3.isNotEmpty() -> layout.shiftRow3
            isShifted && layout.row3Upper.isNotEmpty() -> layout.row3Upper
            else -> layout.row3
        }
        val r3Sec = when {
            isShifted && layout.shiftRow3Sec.isNotEmpty() -> layout.shiftRow3Sec
            else -> layout.row3Sec
        }

        val shiftLabel = if (shiftState == ShiftState.CAPS_LOCKED) "⇪" else "⇧"
        val r3 = mutableListOf<KeyDef>()
        r3.add(KeyDef(primaryText = shiftLabel, action = KeyAction.SHIFT, weight = 1.3f, isSpecial = true))
        r3Prim.forEachIndexed { i, char ->
            r3.add(KeyDef(char, r3Sec.getOrNull(i)?.takeIf { it.isNotBlank() }, weight = 1f))
        }
        r3.add(KeyDef(primaryText = "⌫", action = KeyAction.BACKSPACE, weight = 1.3f, isSpecial = true))

        // Row 4
        val r4 = mutableListOf<KeyDef>()
        r4.add(KeyDef(primaryText = layout.symbolSwitch, action = KeyAction.SYMBOL_SWITCH, weight = 1.2f, isSpecial = true))
        if (showGlobeKey) {
            r4.add(KeyDef(primaryText = "🌐", action = KeyAction.LANGUAGE_SWITCH, weight = 1.0f, isSpecial = true))
        } else {
            r4.add(KeyDef(primaryText = "☺", action = KeyAction.EMOJI_PANEL, weight = 1.0f, isSpecial = true))
        }
        r4.add(KeyDef(primaryText = ",", secondaryText = "...", weight = 0.9f))
        val spaceWeight = if (showGlobeKey) 3.6f else 4.2f
        r4.add(KeyDef(primaryText = layout.spaceLabel, action = KeyAction.SPACE, weight = spaceWeight))
        r4.add(KeyDef(primaryText = layout.periodKey, secondaryText = "...", weight = 0.9f))
        r4.add(KeyDef(primaryText = "↵", secondaryText = "...", action = KeyAction.ENTER, weight = 1.3f, isSpecial = true))

        return listOf(KeyboardRow(r1), KeyboardRow(r2), KeyboardRow(r3), KeyboardRow(r4))
    }

    private fun getDefaultFallbackLayouts(): List<DynamicKeyboardLayout> {
        return listOf(
            DynamicKeyboardLayout(
                id = "en_qwerty",
                languageCode = "en",
                languageName = "English",
                layoutName = "QWERTY",
                scriptName = "Latin",
                spaceLabel = "English",
                symbolSwitch = "?123",
                periodKey = ".",
                row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
                row1Upper = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
                row1Sec = listOf("৳", "%", "_", ";", "<", ">", "+", "=", "[", "]"),
                row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
                row2Upper = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
                row2Sec = listOf("@", "#", "&", "*", "-", "!", "?", "(", ")"),
                row3 = listOf("z", "x", "c", "v", "b", "n", "m"),
                row3Upper = listOf("Z", "X", "C", "V", "B", "N", "M"),
                row3Sec = listOf("\"", "", "", "", "'", ":", "/")
            ),
            DynamicKeyboardLayout(
                id = "bn_jatiya",
                languageCode = "bn",
                languageName = "বাংলা",
                layoutName = "জাতীয় (National)",
                scriptName = "Bengali",
                spaceLabel = "জাতীয়",
                symbolSwitch = "?১২৩",
                periodKey = "।",
                row1 = listOf("ঙ", "য", "ড", "প", "ট", "চ", "জ", "হ", "গ", "ড়"),
                row1Sec = listOf("ং", "য়", "ঢ", "ফ", "ঠ", "ছ", "ঝ", "ঞ", "ঘ", "ঢ়"),
                row2 = listOf("ৃ", "ু", "ি", "ব", "্", "া", "ক", "ত", "দ"),
                row2Sec = listOf("ঋ", "উ", "ই", "ভ", "", "আ", "খ", "ৎ", "ধ"),
                row3 = listOf("ঁ", "ো", "ে", "র", "ন", "স", "ম"),
                row3Sec = listOf("ঃ", "ও", "এ", "ল", "ণ", "ষ", "শ"),
                shiftRow1 = listOf("ং", "্য", "ঢ", "ফ", "ঠ", "ছ", "ঝ", "ঞ", "ঘ", "ঢ়"),
                shiftRow2 = listOf("ঋ", "ূ", "ী", "ভ", "’", "অ", "খ", "থ", "ধ"),
                shiftRow2Sec = listOf("", "ঊ", "ঈ", "", "", "", "", "", ""),
                shiftRow3 = listOf("ः", "ৌ", "ৈ", "्র", "ণ", "ষ", "শ"),
                shiftRow3Sec = listOf("", "ঔ", "ঐ", "", "", "", "")
            ),
            DynamicKeyboardLayout(
                id = "bn_avro",
                languageCode = "bn",
                languageName = "বাংলা",
                layoutName = "Avro (Phonetic)",
                scriptName = "Bengali Phonetic",
                spaceLabel = "Avro",
                symbolSwitch = "?123",
                periodKey = ".",
                row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
                row1Upper = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
                row1Sec = listOf("ং", "ও", "এ", "র", "ট", "য়", "ু", "ি", "অ", "প"),
                row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
                row2Upper = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
                row2Sec = listOf("আ", "স", "ড", "ফ", "গ", "হ", "জ", "ক", "ল"),
                row3 = listOf("z", "x", "c", "v", "b", "n", "m"),
                row3Upper = listOf("Z", "X", "C", "V", "B", "N", "M"),
                row3Sec = listOf("য", "ক্স", "চ", "ভ", "ব", "ন", "ম"),
                shiftRow1 = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
                shiftRow1Sec = listOf("ং", "ঔ", "ঐ", "ড়", "ঠ", "য়", "ূ", "ী", "ও", "ফ"),
                shiftRow2 = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
                shiftRow2Sec = listOf("অ", "শ", "ঢ", "ফ", "ঘ", "ঃ", "ঝ", "খ", "ল"),
                shiftRow3 = listOf("Z", "X", "C", "V", "B", "N", "M"),
                shiftRow3Sec = listOf("য", "ক्ষ", "ছ", "ভ", "ব", "ণ", "ম")
            ),
            DynamicKeyboardLayout(
                id = "hi_devanagari",
                languageCode = "hi",
                languageName = "हिन्दी",
                layoutName = "Devanagari",
                scriptName = "Devanagari",
                spaceLabel = "हिन्दी",
                symbolSwitch = "?123",
                periodKey = "।",
                row1 = listOf("क", "ख", "ग", "घ", "ङ", "च", "छ", "ज", "झ", "ञ"),
                row2 = listOf("ट", "ठ", "ड", "ढ", "ण", "त", "थ", "द", "ध", "न"),
                row3 = listOf("प", "फ", "ब", "भ", "म", "य", "र", "ल", "व"),
                shiftRow1 = listOf("अ", "आ", "इ", "ई", "उ", "ऊ", "ऋ", "ए", "ऐ", "ओ"),
                shiftRow2 = listOf("ा", "ि", "ी", "ु", "ू", "ृ", "े", "ै", "ो", "ौ"),
                shiftRow3 = listOf("्", "ं", "ः", "ँ", "स", "ह", "ड़", "ढ़", "औ")
            )
        )
    }
}
