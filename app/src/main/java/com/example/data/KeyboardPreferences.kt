package com.example.data

import android.content.Context
import android.content.SharedPreferences

enum class KeyboardThemeOption(
    val id: String,
    val displayName: String,
    val backgroundHex: Long,
    val surfaceHex: Long,
    val keyHex: Long,
    val textHex: Long,
    val accentHex: Long,
    val secondaryTextHex: Long
) {
    LIGHT(
        id = "light",
        displayName = "Light (Default)",
        backgroundHex = 0xFFECEFF1,
        surfaceHex = 0xFFFFFFFF,
        keyHex = 0xFFFFFFFF,
        textHex = 0xFF1E293B,
        accentHex = 0xFF2563EB,
        secondaryTextHex = 0xFF64748B
    ),
    DARK(
        id = "dark",
        displayName = "Dark",
        backgroundHex = 0xFF121212,
        surfaceHex = 0xFF1E1E1E,
        keyHex = 0xFF2C2C2C,
        textHex = 0xFFF8FAFC,
        accentHex = 0xFF3B82F6,
        secondaryTextHex = 0xFF94A3B8
    ),
    BLUE(
        id = "blue",
        displayName = "Blue",
        backgroundHex = 0xFF0F172A,
        surfaceHex = 0xFF1E293B,
        keyHex = 0xFF1D4ED8,
        textHex = 0xFFFFFFFF,
        accentHex = 0xFF38BDF8,
        secondaryTextHex = 0xFF93C5FD
    ),
    GREEN(
        id = "green",
        displayName = "Green",
        backgroundHex = 0xFF064E3B,
        surfaceHex = 0xFF065F46,
        keyHex = 0xFF047857,
        textHex = 0xFFFFFFFF,
        accentHex = 0xFF34D399,
        secondaryTextHex = 0xFFA7F3D0
    ),
    PURPLE(
        id = "purple",
        displayName = "Purple",
        backgroundHex = 0xFF2E1065,
        surfaceHex = 0xFF3B0764,
        keyHex = 0xFF6D28D9,
        textHex = 0xFFFFFFFF,
        accentHex = 0xFFC084FC,
        secondaryTextHex = 0xFFDDD6FE
    ),
    RED(
        id = "red",
        displayName = "Red",
        backgroundHex = 0xFF450A0A,
        surfaceHex = 0xFF7F1D1D,
        keyHex = 0xFFB91C1C,
        textHex = 0xFFFFFFFF,
        accentHex = 0xFFF87171,
        secondaryTextHex = 0xFFFECACA
    ),
    CUSTOM(
        id = "custom",
        displayName = "Custom Theme",
        backgroundHex = 0xFF121212,
        surfaceHex = 0xFF1E1E1E,
        keyHex = 0xFF2C2C2C,
        textHex = 0xFFF8FAFC,
        accentHex = 0xFF22C55E,
        secondaryTextHex = 0xFF94A3B8
    );

    companion object {
        fun fromId(id: String): KeyboardThemeOption {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: LIGHT
        }
    }
}

enum class KeyboardLanguage(val code: String, val nativeName: String, val scriptName: String) {
    ENGLISH("en", "English", "Latin"),
    BANGLA("bn", "জাতীয়", "Bengali"),
    HINDI("hi", "हिन्दी", "Devanagari");

    companion object {
        fun fromCode(code: String): KeyboardLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}

enum class OneHandedMode {
    OFF, LEFT, RIGHT
}

enum class LanguageSwitchMethod(val id: String, val displayName: String, val description: String) {
    GLOBE_KEY(
        "globe_key",
        "Globe Key (🌐)",
        "Show dedicated globe key next to spacebar"
    ),
    SPACEBAR_SWIPE(
        "spacebar_swipe",
        "Swipe / Hold Spacebar",
        "Hold and swipe horizontally on spacebar to switch"
    );

    companion object {
        fun fromId(id: String): LanguageSwitchMethod {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: GLOBE_KEY
        }
    }
}

enum class ActiveKeyboardPage {
    LETTERS,
    SYMBOLS_1,
    SYMBOLS_2,
    CODING,
    GAMING,
    EMOJI,
    CLIPBOARD,
    SNIPPETS,
    TEXT_EDITING,
    NUMPAD
}

enum class ToolbarTool(
    val id: String,
    val titleRes: Int,
    val descriptionRes: Int,
    val title: String,
    val description: String,
    val defaultEnabled: Boolean
) {
    EMOJI(
        id = "emoji",
        titleRes = com.example.R.string.tool_emoji_title,
        descriptionRes = com.example.R.string.tool_emoji_desc,
        title = "Emoji & Emoticons",
        description = "Quick access to smileys, emojis, and emotional reactions",
        defaultEnabled = true
    ),
    CLIPBOARD(
        id = "clipboard",
        titleRes = com.example.R.string.tool_clipboard_title,
        descriptionRes = com.example.R.string.tool_clipboard_desc,
        title = "Clipboard Manager",
        description = "Access recent copied text and pinned quick clips",
        defaultEnabled = true
    ),
    TEXT_EDITING(
        id = "text_editing",
        titleRes = com.example.R.string.tool_text_editing_title,
        descriptionRes = com.example.R.string.tool_text_editing_desc,
        title = "Text Editor & D-Pad",
        description = "Precision cursor navigation, text selection, copy, cut, and paste",
        defaultEnabled = true
    ),
    NUMPAD(
        id = "numpad",
        titleRes = com.example.R.string.tool_numpad_title,
        descriptionRes = com.example.R.string.tool_numpad_desc,
        title = "Numeric Keypad",
        description = "Calculator-style 10-key numpad with mathematical operators",
        defaultEnabled = true
    ),
    SNIPPETS(
        id = "snippets",
        titleRes = com.example.R.string.tool_snippets_title,
        descriptionRes = com.example.R.string.tool_snippets_desc,
        title = "Quick Snippets",
        description = "Pre-saved code snippets, email templates, and fast replies",
        defaultEnabled = false
    ),
    CODING(
        id = "coding",
        titleRes = com.example.R.string.tool_coding_title,
        descriptionRes = com.example.R.string.tool_coding_desc,
        title = "Coding Symbols",
        description = "Programming brackets {}, [], <>, logic operators, and symbols",
        defaultEnabled = true
    ),
    GAMING(
        id = "gaming",
        titleRes = com.example.R.string.tool_gaming_title,
        descriptionRes = com.example.R.string.tool_gaming_desc,
        title = "Gaming Keypad",
        description = "WASD layout, dedicated arrow keys, and gaming hotkeys",
        defaultEnabled = false
    ),
    THEMES(
        id = "themes",
        titleRes = com.example.R.string.tool_themes_title,
        descriptionRes = com.example.R.string.tool_themes_desc,
        title = "Theme Switcher",
        description = "Quickly cycle through keyboard color themes on the fly",
        defaultEnabled = true
    ),
    ONE_HANDED(
        id = "one_handed",
        titleRes = com.example.R.string.tool_one_handed_title,
        descriptionRes = com.example.R.string.tool_one_handed_desc,
        title = "One-Handed Mode",
        description = "Dock keyboard to the left or right for easier single-hand thumb typing",
        defaultEnabled = false
    ),
    CASE_CONVERTER(
        id = "case_converter",
        titleRes = com.example.R.string.tool_case_converter_title,
        descriptionRes = com.example.R.string.tool_case_converter_desc,
        title = "Case Converter",
        description = "Cycle case of text between lowercase, Title Case, and UPPERCASE",
        defaultEnabled = false
    ),
    CLEAR_TEXT(
        id = "clear_text",
        titleRes = com.example.R.string.tool_clear_text_title,
        descriptionRes = com.example.R.string.tool_clear_text_desc,
        title = "Clear All Text",
        description = "Instant single-tap wipe of the current input field",
        defaultEnabled = false
    ),
    VOICE_TYPING(
        id = "voice_typing",
        titleRes = com.example.R.string.tool_voice_typing_title,
        descriptionRes = com.example.R.string.tool_voice_typing_desc,
        title = "Voice Typing",
        description = "Dictate text using Google Speech Recognition",
        defaultEnabled = false
    ),
    FLOATING(
        id = "floating",
        titleRes = com.example.R.string.tool_floating_title,
        descriptionRes = com.example.R.string.tool_floating_desc,
        title = "Floating Keyboard",
        description = "Move, resize, and position keyboard anywhere on the screen",
        defaultEnabled = false
    ),
    SETTINGS(
        id = "settings",
        titleRes = com.example.R.string.tool_settings_title,
        descriptionRes = com.example.R.string.tool_settings_desc,
        title = "Keyboard Settings",
        description = "Direct shortcut to customize KeyNova preferences",
        defaultEnabled = true
    );

    fun getLocalizedTitle(context: Context): String =
        runCatching { context.getString(titleRes) }.getOrDefault(title)

    fun getLocalizedDesc(context: Context): String =
        runCatching { context.getString(descriptionRes) }.getOrDefault(description)

    companion object {
        const val MAX_ENABLED_TOOLS = 7

        val DEFAULT_TOOL_IDS = listOf(
            EMOJI.id,
            CLIPBOARD.id,
            TEXT_EDITING.id,
            NUMPAD.id,
            CODING.id,
            THEMES.id,
            SETTINGS.id
        )

        fun fromId(id: String): ToolbarTool? {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) }
        }
    }
}

enum class DeleteHoldMode(val id: String, val displayName: String, val description: String) {
    WORD_BY_WORD(
        "word_by_word",
        "Word by Word",
        "Deletes whole words one-by-one when held"
    ),
    CHARACTER_BY_CHARACTER(
        "char_by_char",
        "Character by Character",
        "Continuously deletes individual characters when held"
    );

    companion object {
        fun fromId(id: String): DeleteHoldMode {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: WORD_BY_WORD
        }
    }
}


class KeyboardPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("keynova_preferences", Context.MODE_PRIVATE)

    val preferenceRevision = androidx.compose.runtime.mutableIntStateOf(0)

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        preferenceRevision.intValue++
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    fun notifyPreferencesChanged() {
        preferenceRevision.intValue++
    }

    var theme: KeyboardThemeOption
        get() = KeyboardThemeOption.fromId(prefs.getString(KEY_THEME, KeyboardThemeOption.LIGHT.id) ?: "light")
        set(value) {
            prefs.edit().putString(KEY_THEME, value.id).apply()
            notifyPreferencesChanged()
        }

    var customThemeConfig: CustomThemeConfig
        get() = CustomThemeConfig.fromJson(prefs.getString(KEY_CUSTOM_THEME_CONFIG, null))
        set(value) {
            prefs.edit().putString(KEY_CUSTOM_THEME_CONFIG, value.toJson()).apply()
            notifyPreferencesChanged()
        }

    var isCustomThemeActive: Boolean
        get() = prefs.getBoolean(KEY_IS_CUSTOM_THEME, false)
        set(value) {
            prefs.edit().putBoolean(KEY_IS_CUSTOM_THEME, value).apply()
            notifyPreferencesChanged()
        }

    var activeLanguage: KeyboardLanguage
        get() = KeyboardLanguage.fromCode(prefs.getString(KEY_ACTIVE_LANG, KeyboardLanguage.ENGLISH.code) ?: "en")
        set(value) = prefs.edit().putString(KEY_ACTIVE_LANG, value.code).apply()

    var enabledLanguages: Set<KeyboardLanguage>
        get() {
            val raw = prefs.getStringSet(KEY_ENABLED_LANGS, setOf("en", "bn", "hi")) ?: setOf("en", "bn", "hi")
            return raw.map { KeyboardLanguage.fromCode(it) }.toSet().ifEmpty { setOf(KeyboardLanguage.ENGLISH) }
        }
        set(value) {
            val codes = value.map { it.code }.toSet()
            prefs.edit().putStringSet(KEY_ENABLED_LANGS, codes).apply()
        }

    var showNumberRow: Boolean
        get() = prefs.getBoolean(KEY_SHOW_NUMBER_ROW, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_NUMBER_ROW, value).apply()

    var keyHeightDp: Int
        get() = prefs.getInt(KEY_HEIGHT_DP, 48)
        set(value) = prefs.edit().putInt(KEY_HEIGHT_DP, value.coerceIn(38, 64)).apply()

    var keySpacingDp: Int
        get() = prefs.getInt(KEY_SPACING_DP, 4)
        set(value) = prefs.edit().putInt(KEY_SPACING_DP, value.coerceIn(2, 8)).apply()

    var keyLongPressDelayMs: Int
        get() = prefs.getInt(KEY_LONG_PRESS_DELAY, 240)
        set(value) = prefs.edit().putInt(KEY_LONG_PRESS_DELAY, value.coerceIn(150, 600)).apply()

    var hapticFeedback: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC, value).apply()

    var soundFeedback: Boolean
        get() = prefs.getBoolean(KEY_SOUND, false)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var oneHandedMode: OneHandedMode
        get() {
            val raw = prefs.getString(KEY_ONE_HANDED, OneHandedMode.OFF.name) ?: OneHandedMode.OFF.name
            return runCatching { OneHandedMode.valueOf(raw) }.getOrDefault(OneHandedMode.OFF)
        }
        set(value) = prefs.edit().putString(KEY_ONE_HANDED, value.name).apply()

    var isFloatingMode: Boolean
        get() = prefs.getBoolean(KEY_IS_FLOATING_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_FLOATING_MODE, value).apply()

    var floatingOffsetX: Float
        get() = prefs.getFloat(KEY_FLOATING_OFFSET_X, 0f)
        set(value) = prefs.edit().putFloat(KEY_FLOATING_OFFSET_X, value).apply()

    var floatingOffsetY: Float
        get() = prefs.getFloat(KEY_FLOATING_OFFSET_Y, -80f)
        set(value) = prefs.edit().putFloat(KEY_FLOATING_OFFSET_Y, value).apply()

    var floatingScaleX: Float
        get() = prefs.getFloat(KEY_FLOATING_SCALE_X, prefs.getFloat(KEY_FLOATING_SCALE, 0.88f)).coerceIn(0.55f, 1.35f)
        set(value) = prefs.edit().putFloat(KEY_FLOATING_SCALE_X, value.coerceIn(0.55f, 1.35f)).apply()

    var floatingScaleY: Float
        get() = prefs.getFloat(KEY_FLOATING_SCALE_Y, prefs.getFloat(KEY_FLOATING_SCALE, 0.88f)).coerceIn(0.55f, 1.35f)
        set(value) = prefs.edit().putFloat(KEY_FLOATING_SCALE_Y, value.coerceIn(0.55f, 1.35f)).apply()

    var floatingScale: Float
        get() = prefs.getFloat(KEY_FLOATING_SCALE, 0.88f).coerceIn(0.55f, 1.35f)
        set(value) {
            val clamped = value.coerceIn(0.55f, 1.35f)
            prefs.edit()
                .putFloat(KEY_FLOATING_SCALE, clamped)
                .putFloat(KEY_FLOATING_SCALE_X, clamped)
                .putFloat(KEY_FLOATING_SCALE_Y, clamped)
                .apply()
        }

    var floatingOpacity: Float
        get() = prefs.getFloat(KEY_FLOATING_OPACITY, 0.95f)
        set(value) = prefs.edit().putFloat(KEY_FLOATING_OPACITY, value.coerceIn(0.5f, 1.0f)).apply()

    var hasCompletedOnboarding: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING, value).apply()

    var singleTapSwitch: Boolean
        get() = prefs.getBoolean(KEY_SINGLE_TAP_SWITCH, true)
        set(value) = prefs.edit().putBoolean(KEY_SINGLE_TAP_SWITCH, value).apply()

    var languageSwitchMethod: LanguageSwitchMethod
        get() = LanguageSwitchMethod.fromId(prefs.getString(KEY_LANG_SWITCH_METHOD, LanguageSwitchMethod.GLOBE_KEY.id) ?: "globe_key")
        set(value) = prefs.edit().putString(KEY_LANG_SWITCH_METHOD, value.id).apply()

    var deleteHoldMode: DeleteHoldMode
        get() {
            val raw = prefs.getString(KEY_DELETE_HOLD_MODE, DeleteHoldMode.WORD_BY_WORD.id) ?: DeleteHoldMode.WORD_BY_WORD.id
            return DeleteHoldMode.fromId(raw)
        }
        set(value) = prefs.edit().putString(KEY_DELETE_HOLD_MODE, value.id).apply()

    var deleteInitialDelayMs: Int
        get() = prefs.getInt(KEY_DELETE_INITIAL_DELAY, 350)
        set(value) = prefs.edit().putInt(KEY_DELETE_INITIAL_DELAY, value.coerceIn(150, 800)).apply()

    var deleteRepeatIntervalMs: Int
        get() = prefs.getInt(KEY_DELETE_REPEAT_INTERVAL, 100)
        set(value) = prefs.edit().putInt(KEY_DELETE_REPEAT_INTERVAL, value.coerceIn(40, 350)).apply()

    var deleteHapticOnRepeat: Boolean
        get() = prefs.getBoolean(KEY_DELETE_HAPTIC, true)
        set(value) = prefs.edit().putBoolean(KEY_DELETE_HAPTIC, value).apply()

    var autoCapitalization: Boolean
        get() = prefs.getBoolean(KEY_AUTO_CAPITALIZATION, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_CAPITALIZATION, value).apply()

    var doubleSpacePeriod: Boolean
        get() = prefs.getBoolean(KEY_DOUBLE_SPACE_PERIOD, true)
        set(value) = prefs.edit().putBoolean(KEY_DOUBLE_SPACE_PERIOD, value).apply()

    var spacebarCursorControl: Boolean
        get() = prefs.getBoolean(KEY_SPACEBAR_CURSOR_CONTROL, true)
        set(value) = prefs.edit().putBoolean(KEY_SPACEBAR_CURSOR_CONTROL, value).apply()

    var backspaceSwipeDelete: Boolean
        get() = prefs.getBoolean(KEY_BACKSPACE_SWIPE_DELETE, true)
        set(value) = prefs.edit().putBoolean(KEY_BACKSPACE_SWIPE_DELETE, value).apply()

    var enabledToolbarTools: List<ToolbarTool>
        get() {
            val raw = prefs.getString(KEY_ENABLED_TOOLBAR_TOOLS, null)
            val ids = if (raw != null) {
                raw.split(",").map { it.trim() }.filter { it.isNotBlank() }
            } else {
                ToolbarTool.DEFAULT_TOOL_IDS
            }
            val tools = ids.mapNotNull { ToolbarTool.fromId(it) }.distinct()
            return if (tools.isNotEmpty()) tools.take(ToolbarTool.MAX_ENABLED_TOOLS) else ToolbarTool.DEFAULT_TOOL_IDS.mapNotNull { ToolbarTool.fromId(it) }
        }
        set(value) {
            val limited = value.take(ToolbarTool.MAX_ENABLED_TOOLS)
            val raw = limited.joinToString(",") { it.id }
            prefs.edit().putString(KEY_ENABLED_TOOLBAR_TOOLS, raw).apply()
        }

    fun isToolbarToolEnabled(tool: ToolbarTool): Boolean {
        return enabledToolbarTools.contains(tool)
    }

    /**
     * Toggles tool on or off.
     * Returns true if applied, false if max limit (7) is reached and toggle-on was rejected.
     */
    fun toggleToolbarTool(tool: ToolbarTool, enabled: Boolean): Boolean {
        val current = enabledToolbarTools.toMutableList()
        if (enabled) {
            if (current.contains(tool)) return true
            if (current.size >= ToolbarTool.MAX_ENABLED_TOOLS) {
                return false
            }
            current.add(tool)
            enabledToolbarTools = current
            return true
        } else {
            current.remove(tool)
            if (current.isEmpty()) {
                current.add(ToolbarTool.SETTINGS)
            }
            enabledToolbarTools = current
            return true
        }
    }

    fun moveToolbarTool(fromIndex: Int, toIndex: Int) {
        val current = enabledToolbarTools.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            enabledToolbarTools = current
        }
    }

    fun resetToolbarTools() {
        enabledToolbarTools = ToolbarTool.DEFAULT_TOOL_IDS.mapNotNull { ToolbarTool.fromId(it) }
    }

    var activeLayoutId: String
        get() = prefs.getString(KEY_ACTIVE_LAYOUT_ID, "en_qwerty") ?: "en_qwerty"
        set(value) {
            prefs.edit().putString(KEY_ACTIVE_LAYOUT_ID, value).apply()
            val layout = com.example.ime.KeyboardLayoutRegistry.getLayoutById(value)
            prefs.edit().putString(KEY_ACTIVE_LANG, layout.languageCode).apply()
        }

    var enabledLayoutIds: Set<String>
        get() {
            val defaultSet = setOf("en_qwerty", "bn_jatiya", "bn_avro", "hi_devanagari")
            val raw = prefs.getStringSet(KEY_ENABLED_LAYOUT_IDS, defaultSet) ?: defaultSet
            return raw.ifEmpty { setOf("en_qwerty") }
        }
        set(value) {
            prefs.edit().putStringSet(KEY_ENABLED_LAYOUT_IDS, value).apply()
        }

    fun getActiveLayout(context: Context? = null): com.example.ime.DynamicKeyboardLayout {
        return com.example.ime.KeyboardLayoutRegistry.getLayoutById(activeLayoutId, context)
    }

    fun cycleLayout(reverse: Boolean = false, context: Context? = null): com.example.ime.DynamicKeyboardLayout {
        val allLayouts = com.example.ime.KeyboardLayoutRegistry.getLayouts(context)
        val enabledList = allLayouts.filter { enabledLayoutIds.contains(it.id) }.ifEmpty { allLayouts }
        val currentIdx = enabledList.indexOfFirst { it.id.equals(activeLayoutId, ignoreCase = true) }
        val nextIdx = if (reverse) {
            if (currentIdx <= 0) enabledList.size - 1 else currentIdx - 1
        } else {
            if (currentIdx == -1 || currentIdx >= enabledList.size - 1) 0 else currentIdx + 1
        }
        val nextLayout = enabledList[nextIdx]
        activeLayoutId = nextLayout.id
        return nextLayout
    }

    fun cycleLanguage(reverse: Boolean = false): KeyboardLanguage {
        val enabled = enabledLanguages.toList()
        if (enabled.isEmpty()) return KeyboardLanguage.ENGLISH
        val currentIndex = enabled.indexOf(activeLanguage)
        val nextIndex = if (reverse) {
            if (currentIndex <= 0) enabled.size - 1 else currentIndex - 1
        } else {
            if (currentIndex == -1 || currentIndex >= enabled.size - 1) 0 else currentIndex + 1
        }
        val nextLang = enabled[nextIndex]
        activeLanguage = nextLang
        // Also update activeLayoutId if matching
        val matchingLayout = enabledLayoutIds.firstOrNull { it.startsWith(nextLang.code) }
        if (matchingLayout != null) {
            activeLayoutId = matchingLayout
        }
        return nextLang
    }

    fun resetLayoutToDefaults() {
        showNumberRow = true
        keyHeightDp = 48
        keySpacingDp = 4
        keyLongPressDelayMs = 240
        hapticFeedback = true
        soundFeedback = false
        oneHandedMode = OneHandedMode.OFF
        autoCapitalization = true
        doubleSpacePeriod = true
        spacebarCursorControl = true
        backspaceSwipeDelete = true
        deleteHoldMode = DeleteHoldMode.WORD_BY_WORD
        deleteInitialDelayMs = 350
        deleteRepeatIntervalMs = 100
        deleteHapticOnRepeat = true
        isFloatingMode = false
        floatingOffsetX = 0f
        floatingOffsetY = -80f
        floatingScale = 0.88f
        floatingScaleX = 0.88f
        floatingScaleY = 0.88f
        floatingOpacity = 0.95f
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_THEME = "key_theme"
        private const val KEY_CUSTOM_THEME_CONFIG = "key_custom_theme_config"
        private const val KEY_IS_CUSTOM_THEME = "key_is_custom_theme"
        private const val KEY_ACTIVE_LANG = "key_active_lang"
        private const val KEY_ENABLED_LANGS = "key_enabled_langs"
        private const val KEY_SHOW_NUMBER_ROW = "key_show_number_row"
        private const val KEY_HEIGHT_DP = "key_height_dp"
        private const val KEY_SPACING_DP = "key_spacing_dp"
        private const val KEY_LONG_PRESS_DELAY = "key_long_press_delay"
        private const val KEY_HAPTIC = "key_haptic"
        private const val KEY_SOUND = "key_sound"
        private const val KEY_ONE_HANDED = "key_one_handed"
        private const val KEY_IS_FLOATING_MODE = "key_is_floating_mode"
        private const val KEY_FLOATING_OFFSET_X = "key_floating_offset_x"
        private const val KEY_FLOATING_OFFSET_Y = "key_floating_offset_y"
        private const val KEY_FLOATING_SCALE = "key_floating_scale"
        private const val KEY_FLOATING_SCALE_X = "key_floating_scale_x"
        private const val KEY_FLOATING_SCALE_Y = "key_floating_scale_y"
        private const val KEY_FLOATING_OPACITY = "key_floating_opacity"
        private const val KEY_ONBOARDING = "key_onboarding"
        private const val KEY_SINGLE_TAP_SWITCH = "key_single_tap_switch"
        private const val KEY_LANG_SWITCH_METHOD = "key_lang_switch_method"
        private const val KEY_DELETE_HOLD_MODE = "key_delete_hold_mode"
        private const val KEY_DELETE_INITIAL_DELAY = "key_delete_initial_delay"
        private const val KEY_DELETE_REPEAT_INTERVAL = "key_delete_repeat_interval"
        private const val KEY_DELETE_HAPTIC = "key_delete_haptic"
        private const val KEY_ENABLED_TOOLBAR_TOOLS = "key_enabled_toolbar_tools"
        private const val KEY_ACTIVE_LAYOUT_ID = "key_active_layout_id"
        private const val KEY_ENABLED_LAYOUT_IDS = "key_enabled_layout_ids"
        private const val KEY_AUTO_CAPITALIZATION = "key_auto_capitalization"
        private const val KEY_DOUBLE_SPACE_PERIOD = "key_double_space_period"
        private const val KEY_SPACEBAR_CURSOR_CONTROL = "key_spacebar_cursor_control"
        private const val KEY_BACKSPACE_SWIPE_DELETE = "key_backspace_swipe_delete"
    }
}
