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

    var theme: KeyboardThemeOption
        get() = KeyboardThemeOption.fromId(prefs.getString(KEY_THEME, KeyboardThemeOption.LIGHT.id) ?: "light")
        set(value) = prefs.edit().putString(KEY_THEME, value.id).apply()

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
        return nextLang
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_THEME = "key_theme"
        private const val KEY_ACTIVE_LANG = "key_active_lang"
        private const val KEY_ENABLED_LANGS = "key_enabled_langs"
        private const val KEY_SHOW_NUMBER_ROW = "key_show_number_row"
        private const val KEY_HEIGHT_DP = "key_height_dp"
        private const val KEY_SPACING_DP = "key_spacing_dp"
        private const val KEY_HAPTIC = "key_haptic"
        private const val KEY_SOUND = "key_sound"
        private const val KEY_ONE_HANDED = "key_one_handed"
        private const val KEY_ONBOARDING = "key_onboarding"
        private const val KEY_SINGLE_TAP_SWITCH = "key_single_tap_switch"
        private const val KEY_LANG_SWITCH_METHOD = "key_lang_switch_method"
        private const val KEY_DELETE_HOLD_MODE = "key_delete_hold_mode"
        private const val KEY_DELETE_INITIAL_DELAY = "key_delete_initial_delay"
        private const val KEY_DELETE_REPEAT_INTERVAL = "key_delete_repeat_interval"
        private const val KEY_DELETE_HAPTIC = "key_delete_haptic"
    }
}
