package com.example.data

import org.json.JSONObject

/**
 * Configuration for user-created custom keyboard themes.
 * Supports custom background colors, gallery background images, brightness,
 * key colors, suggestion/toolbar styling, and functional key styling.
 */
data class CustomThemeConfig(
    val id: String = "custom_theme",
    val name: String = "Custom Theme",
    // Background tab
    val backgroundColor: Long = 0xFF121212,
    val backgroundImagePath: String? = null,
    val backgroundBrightness: Float = 0.50f,
    // Keys tab
    val keyBackgroundColor: Long = 0xFF2C2C2C,
    val keyTextColor: Long = 0xFFF8FAFC,
    val keyHintColor: Long = 0xFF94A3B8,
    val keyBrightness: Float = 1.0f,
    // Suggestion tab
    val expandImageBehindSuggestion: Boolean = false,
    val suggestionBackgroundColor: Long = 0xFF1E1E1E,
    val suggestionTextColor: Long = 0xFFF8FAFC,
    val suggestionBrightness: Float = 0.50f,
    // Functional Keys tab
    val funcKeyBackgroundColor: Long = 0xFF3B82F6,
    val funcKeyTextColor: Long = 0xFFFFFFFF,
    val funcKeyBrightness: Float = 1.0f
) {
    fun toJson(): String {
        val json = JSONObject()
        json.put("id", id)
        json.put("name", name)
        json.put("backgroundColor", backgroundColor)
        json.put("backgroundImagePath", backgroundImagePath ?: "")
        json.put("backgroundBrightness", backgroundBrightness.toDouble())
        json.put("keyBackgroundColor", keyBackgroundColor)
        json.put("keyTextColor", keyTextColor)
        json.put("keyHintColor", keyHintColor)
        json.put("keyBrightness", keyBrightness.toDouble())
        json.put("expandImageBehindSuggestion", expandImageBehindSuggestion)
        json.put("suggestionBackgroundColor", suggestionBackgroundColor)
        json.put("suggestionTextColor", suggestionTextColor)
        json.put("suggestionBrightness", suggestionBrightness.toDouble())
        json.put("funcKeyBackgroundColor", funcKeyBackgroundColor)
        json.put("funcKeyTextColor", funcKeyTextColor)
        json.put("funcKeyBrightness", funcKeyBrightness.toDouble())
        return json.toString()
    }

    companion object {
        fun fromJson(jsonStr: String?): CustomThemeConfig {
            if (jsonStr.isNullOrEmpty()) return defaultTheme()
            return try {
                val obj = JSONObject(jsonStr)
                CustomThemeConfig(
                    id = obj.optString("id", "custom_theme"),
                    name = obj.optString("name", "Custom Theme"),
                    backgroundColor = obj.optLong("backgroundColor", 0xFF121212),
                    backgroundImagePath = obj.optString("backgroundImagePath", "").takeIf { it.isNotEmpty() },
                    backgroundBrightness = obj.optDouble("backgroundBrightness", 0.50).toFloat(),
                    keyBackgroundColor = obj.optLong("keyBackgroundColor", 0xFF2C2C2C),
                    keyTextColor = obj.optLong("keyTextColor", 0xFFF8FAFC),
                    keyHintColor = obj.optLong("keyHintColor", 0xFF94A3B8),
                    keyBrightness = obj.optDouble("keyBrightness", 1.0).toFloat(),
                    expandImageBehindSuggestion = obj.optBoolean("expandImageBehindSuggestion", false),
                    suggestionBackgroundColor = obj.optLong("suggestionBackgroundColor", 0xFF1E1E1E),
                    suggestionTextColor = obj.optLong("suggestionTextColor", 0xFFF8FAFC),
                    suggestionBrightness = obj.optDouble("suggestionBrightness", 0.50).toFloat(),
                    funcKeyBackgroundColor = obj.optLong("funcKeyBackgroundColor", 0xFF3B82F6),
                    funcKeyTextColor = obj.optLong("funcKeyTextColor", 0xFFFFFFFF),
                    funcKeyBrightness = obj.optDouble("funcKeyBrightness", 1.0).toFloat()
                )
            } catch (_: Exception) {
                defaultTheme()
            }
        }

        fun defaultTheme(): CustomThemeConfig = CustomThemeConfig()

        /**
         * Presets matching the screenshot's "Color Presets" cards
         */
        val PRESETS = listOf(
            ThemePreset(
                name = "Obsidian Dark",
                previewColors = listOf(0xFF121212, 0xFF2C2C2C, 0xFFF8FAFC, 0xFF3B82F6),
                config = CustomThemeConfig(
                    name = "Obsidian Dark",
                    backgroundColor = 0xFF121212,
                    backgroundBrightness = 0.50f,
                    keyBackgroundColor = 0xFF2C2C2C,
                    keyTextColor = 0xFFF8FAFC,
                    keyHintColor = 0xFF94A3B8,
                    keyBrightness = 1.0f,
                    suggestionBackgroundColor = 0xFF1E1E1E,
                    suggestionTextColor = 0xFFF8FAFC,
                    suggestionBrightness = 0.50f,
                    funcKeyBackgroundColor = 0xFF3B82F6,
                    funcKeyTextColor = 0xFFFFFFFF,
                    funcKeyBrightness = 1.0f
                )
            ),
            ThemePreset(
                name = "Cyber Matrix",
                previewColors = listOf(0xFF0A0F0D, 0xFF1E2923, 0xFF34D399, 0xFF10B981),
                config = CustomThemeConfig(
                    name = "Cyber Matrix",
                    backgroundColor = 0xFF0A0F0D,
                    backgroundBrightness = 0.50f,
                    keyBackgroundColor = 0xFF16231B,
                    keyTextColor = 0xFFECFDF5,
                    keyHintColor = 0xFF6EE7B7,
                    keyBrightness = 1.0f,
                    suggestionBackgroundColor = 0xFF132018,
                    suggestionTextColor = 0xFF34D399,
                    suggestionBrightness = 0.60f,
                    funcKeyBackgroundColor = 0xFF059669,
                    funcKeyTextColor = 0xFFFFFFFF,
                    funcKeyBrightness = 1.0f
                )
            ),
            ThemePreset(
                name = "Arctic Sky",
                previewColors = listOf(0xFFE0F2FE, 0xFFFFFFFF, 0xFF0284C7, 0xFF0284C7),
                config = CustomThemeConfig(
                    name = "Arctic Sky",
                    backgroundColor = 0xFFE0F2FE,
                    backgroundBrightness = 0.70f,
                    keyBackgroundColor = 0xFFFFFFFF,
                    keyTextColor = 0xFF0F172A,
                    keyHintColor = 0xFF0284C7,
                    keyBrightness = 1.0f,
                    suggestionBackgroundColor = 0xFFBAE6FD,
                    suggestionTextColor = 0xFF0369A1,
                    suggestionBrightness = 0.70f,
                    funcKeyBackgroundColor = 0xFF0284C7,
                    funcKeyTextColor = 0xFFFFFFFF,
                    funcKeyBrightness = 1.0f
                )
            ),
            ThemePreset(
                name = "Cyan Aqua",
                previewColors = listOf(0xFF0F2B36, 0xFF164E63, 0xFF67E8F9, 0xFF06B6D4),
                config = CustomThemeConfig(
                    name = "Cyan Aqua",
                    backgroundColor = 0xFF082F49,
                    backgroundBrightness = 0.50f,
                    keyBackgroundColor = 0xFF155E75,
                    keyTextColor = 0xFFF0FDF4,
                    keyHintColor = 0xFF67E8F9,
                    keyBrightness = 1.0f,
                    suggestionBackgroundColor = 0xFF0E4A5E,
                    suggestionTextColor = 0xFF22D3EE,
                    suggestionBrightness = 0.60f,
                    funcKeyBackgroundColor = 0xFF0891B2,
                    funcKeyTextColor = 0xFFFFFFFF,
                    funcKeyBrightness = 1.0f
                )
            ),
            ThemePreset(
                name = "Mint Forest",
                previewColors = listOf(0xFFECFDF5, 0xFFFFFFFF, 0xFF065F46, 0xFF059669),
                config = CustomThemeConfig(
                    name = "Mint Forest",
                    backgroundColor = 0xFFD1FAE5,
                    backgroundBrightness = 0.70f,
                    keyBackgroundColor = 0xFFFFFFFF,
                    keyTextColor = 0xFF064E3B,
                    keyHintColor = 0xFF059669,
                    keyBrightness = 1.0f,
                    suggestionBackgroundColor = 0xFFA7F3D0,
                    suggestionTextColor = 0xFF065F46,
                    suggestionBrightness = 0.70f,
                    funcKeyBackgroundColor = 0xFF059669,
                    funcKeyTextColor = 0xFFFFFFFF,
                    funcKeyBrightness = 1.0f
                )
            ),
            ThemePreset(
                name = "Rose Velvet",
                previewColors = listOf(0xFFFFF1F2, 0xFFFFFFFF, 0xFFBE123C, 0xFFE11D48),
                config = CustomThemeConfig(
                    name = "Rose Velvet",
                    backgroundColor = 0xFFFFE4E6,
                    backgroundBrightness = 0.70f,
                    keyBackgroundColor = 0xFFFFFFFF,
                    keyTextColor = 0xFF881337,
                    keyHintColor = 0xFFE11D48,
                    keyBrightness = 1.0f,
                    suggestionBackgroundColor = 0xFFFECDD3,
                    suggestionTextColor = 0xFF9F1239,
                    suggestionBrightness = 0.70f,
                    funcKeyBackgroundColor = 0xFFE11D48,
                    funcKeyTextColor = 0xFFFFFFFF,
                    funcKeyBrightness = 1.0f
                )
            ),
            ThemePreset(
                name = "Royal Violet",
                previewColors = listOf(0xFF1E1035, 0xFF3B1E68, 0xFFE9D5FF, 0xFF8B5CF6),
                config = CustomThemeConfig(
                    name = "Royal Violet",
                    backgroundColor = 0xFF1E1035,
                    backgroundBrightness = 0.50f,
                    keyBackgroundColor = 0xFF35195D,
                    keyTextColor = 0xFFFAF5FF,
                    keyHintColor = 0xFFC084FC,
                    keyBrightness = 1.0f,
                    suggestionBackgroundColor = 0xFF2A1349,
                    suggestionTextColor = 0xFFDDD6FE,
                    suggestionBrightness = 0.60f,
                    funcKeyBackgroundColor = 0xFF7C3AED,
                    funcKeyTextColor = 0xFFFFFFFF,
                    funcKeyBrightness = 1.0f
                )
            ),
            ThemePreset(
                name = "Sunset Amber",
                previewColors = listOf(0xFF1C1917, 0xFF292524, 0xFFFEF3C7, 0xFFEA580C),
                config = CustomThemeConfig(
                    name = "Sunset Amber",
                    backgroundColor = 0xFF1C1917,
                    backgroundBrightness = 0.50f,
                    keyBackgroundColor = 0xFF292524,
                    keyTextColor = 0xFFFFFBEB,
                    keyHintColor = 0xFFF59E0B,
                    keyBrightness = 1.0f,
                    suggestionBackgroundColor = 0xFF231F1D,
                    suggestionTextColor = 0xFFFBBF24,
                    suggestionBrightness = 0.60f,
                    funcKeyBackgroundColor = 0xFFEA580C,
                    funcKeyTextColor = 0xFFFFFFFF,
                    funcKeyBrightness = 1.0f
                )
            )
        )
    }
}

data class ThemePreset(
    val name: String,
    val previewColors: List<Long>,
    val config: CustomThemeConfig
)
