package com.example.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableIntStateOf
import com.example.ime.AvroPhoneticParser
import com.example.ime.EnglishWordPredictor

/**
 * Entry type classifications for Personal Dictionary.
 */
enum class PersonalDictEntryType(val id: String, val displayName: String) {
    WORD("word", "Word"),
    EMAIL("email", "Email"),
    URL("url", "URL"),
    ALPHANUMERIC("alphanumeric", "Alphanumeric"),
    CUSTOM("custom", "Custom");

    companion object {
        fun fromId(id: String): PersonalDictEntryType {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: WORD
        }
    }
}

/**
 * Model representing a saved personal dictionary entry.
 * Preserves exact original text casing, symbols, and provides usage frequency & type.
 */
data class PersonalDictEntry(
    val id: String,                // Normalized lookup key (lowercase text)
    val text: String,              // Preserved original display text
    val type: PersonalDictEntryType,
    val languageCode: String,      // "en", "bn", "hi", "all"
    var frequency: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Advanced Personal Dictionary & Vocabulary Learning Engine for KeyNova.
 * Completely offline, privacy-first, on-device storage in SharedPreferences.
 *
 * Supports:
 * - Normal words: "Arnix", "Hello", "আমার", "नमस्ते"
 * - Alphanumeric combinations: "Arnix90", "Craft2026", "Gamer123"
 * - Special-character combinations: "Arnix@67", "Code_Arnix", "My-Project", "CraftSprint.IO"
 * - Email addresses: "arnix@example.com"
 * - URLs and website addresses: "https://example.com", "example.com/site"
 * - Usernames and custom identifiers
 * - Bangla Avro phonetic mappings
 *
 * Features:
 * - Automatic background learning on word completion (Space, Enter, Punctuation, Candidate selection).
 * - Full CRUD: view, search, filter, manual add, edit, single delete, bulk delete, clear all.
 * - Context-aware suggestions (Email fields prioritize emails, URL fields prioritize URLs).
 * - Frequency ranking with duplicate prevention.
 * - Strict Privacy: Blocked in password/PIN/OTP fields, private/incognito mode, or when auto-learning disabled.
 * - Process restart persistence and instant live UI reactivity via Compose mutable revision state.
 */
class PersonalDictionaryManager internal constructor(
    private val prefs: SharedPreferences
) {
    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences("keynova_personal_dict", Context.MODE_PRIVATE)
    )

    companion object {
        // Storage keys
        private const val KEY_ENTRIES_V3 = "key_entries_v3"
        private const val KEY_WORDS_EN_V2 = "key_words_en_v2"
        private const val KEY_WORDS_BN_V2 = "key_words_bn_v2"
        private const val KEY_WORDS_HI_V2 = "key_words_hi_v2"
        private const val KEY_PHONETIC_BN_V2 = "key_phonetic_bn_v2"

        // Legacy keys for seamless migration
        private const val KEY_LEGACY_CUSTOM_WORDS = "key_custom_words"
        private const val KEY_LEGACY_PHONETIC = "key_phonetic_mappings"

        @Volatile
        private var instance: PersonalDictionaryManager? = null

        fun getInstance(context: Context): PersonalDictionaryManager {
            return instance ?: synchronized(this) {
                instance ?: PersonalDictionaryManager(context).also { instance = it }
            }
        }

        fun detectEntryType(rawText: String): PersonalDictEntryType {
            val text = rawText.trim()
            if (text.isEmpty()) return PersonalDictEntryType.WORD

            // 1. Email address detection (e.g. arnix@example.com)
            if (text.contains('@') && text.contains('.') && !text.contains(' ') &&
                text.indexOf('@') in 1 until text.lastIndexOf('.') &&
                text.lastIndexOf('.') < text.length - 1
            ) {
                return PersonalDictEntryType.EMAIL
            }

            // 2. URL detection (e.g. https://example.com, www.site.org, craftsprint.io)
            if (text.startsWith("http://", ignoreCase = true) ||
                text.startsWith("https://", ignoreCase = true) ||
                text.startsWith("www.", ignoreCase = true) ||
                (text.contains('.') && !text.contains(' ') && (
                    text.endsWith(".com", ignoreCase = true) ||
                    text.endsWith(".org", ignoreCase = true) ||
                    text.endsWith(".net", ignoreCase = true) ||
                    text.endsWith(".io", ignoreCase = true) ||
                    text.endsWith(".in", ignoreCase = true) ||
                    text.endsWith(".co", ignoreCase = true) ||
                    text.endsWith(".app", ignoreCase = true) ||
                    text.endsWith(".dev", ignoreCase = true) ||
                    text.endsWith(".ai", ignoreCase = true) ||
                    text.endsWith(".xyz", ignoreCase = true) ||
                    text.endsWith(".bd", ignoreCase = true)
                ))
            ) {
                return PersonalDictEntryType.URL
            }

            // 3. Alphanumeric combinations (e.g. Arnix90, Gamer123, Craft2026)
            val hasLetters = text.any { it.isLetter() || it in '\u0980'..'\u09FF' || it in '\u0900'..'\u097F' }
            val hasDigits = text.any { it.isDigit() || it in '\u09E6'..'\u09EF' || it in '\u0966'..'\u096F' }
            val hasSymbols = text.any { !it.isLetterOrDigit() && it !in '\u0980'..'\u09FF' && it !in '\u0900'..'\u097F' && it != '-' && it != '\'' }

            if (hasLetters && hasDigits && !hasSymbols) {
                return PersonalDictEntryType.ALPHANUMERIC
            }

            // 4. Custom Special-character combinations (e.g. Arnix@67, Code_Arnix, My-Project)
            if (hasSymbols || text.contains('_') || text.contains('@')) {
                return PersonalDictEntryType.CUSTOM
            }

            // 5. Standard word
            return PersonalDictEntryType.WORD
        }
    }

    // Reactive revision state so Jetpack Compose UI immediately recomposes on dictionary updates
    val revision = mutableIntStateOf(0)

    // Primary rich entries map: id (lowercase) -> PersonalDictEntry
    private val entriesMap: MutableMap<String, PersonalDictEntry> = mutableMapOf()

    // Avro phonetic mappings: lowercase phonetic -> (banglaWord, frequency)
    private val avroMap: MutableMap<String, AvroEntry> = mutableMapOf()

    data class AvroEntry(val phonetic: String, val bangla: String, var frequency: Int)

    init {
        loadFromPreferences()
    }

    private fun loadFromPreferences() {
        synchronized(this) {
            entriesMap.clear()
            avroMap.clear()

            // 1. Load V3 rich entries: format "text\ttype\tlang\tfreq\ttimestamp"
            val v3Saved = prefs.getStringSet(KEY_ENTRIES_V3, emptySet()) ?: emptySet()
            for (entryRaw in v3Saved) {
                parseV3Entry(entryRaw)?.let {
                    entriesMap[it.id] = it
                }
            }

            // 2. Load and migrate English V2 words
            val enSaved = prefs.getStringSet(KEY_WORDS_EN_V2, emptySet()) ?: emptySet()
            for (entry in enSaved) {
                val colonIdx = entry.lastIndexOf(':')
                val text = if (colonIdx > 0) entry.substring(0, colonIdx).trim() else entry.trim()
                val freq = if (colonIdx > 0) entry.substring(colonIdx + 1).toIntOrNull() ?: 1 else 1
                if (text.isNotEmpty()) {
                    val key = text.lowercase()
                    if (!entriesMap.containsKey(key)) {
                        val detected = detectEntryType(text)
                        entriesMap[key] = PersonalDictEntry(
                            id = key,
                            text = text,
                            type = detected,
                            languageCode = "en",
                            frequency = freq.coerceAtLeast(1)
                        )
                    }
                }
            }

            // 3. Load and migrate Bangla V2 words
            val bnSaved = prefs.getStringSet(KEY_WORDS_BN_V2, emptySet()) ?: emptySet()
            for (entry in bnSaved) {
                val colonIdx = entry.lastIndexOf(':')
                val text = if (colonIdx > 0) entry.substring(0, colonIdx).trim() else entry.trim()
                val freq = if (colonIdx > 0) entry.substring(colonIdx + 1).toIntOrNull() ?: 1 else 1
                if (text.isNotEmpty() && !entriesMap.containsKey(text)) {
                    entriesMap[text] = PersonalDictEntry(
                        id = text,
                        text = text,
                        type = PersonalDictEntryType.WORD,
                        languageCode = "bn",
                        frequency = freq.coerceAtLeast(1)
                    )
                }
            }

            // 4. Load and migrate Hindi V2 words
            val hiSaved = prefs.getStringSet(KEY_WORDS_HI_V2, emptySet()) ?: emptySet()
            for (entry in hiSaved) {
                val colonIdx = entry.lastIndexOf(':')
                val text = if (colonIdx > 0) entry.substring(0, colonIdx).trim() else entry.trim()
                val freq = if (colonIdx > 0) entry.substring(colonIdx + 1).toIntOrNull() ?: 1 else 1
                if (text.isNotEmpty() && !entriesMap.containsKey(text)) {
                    entriesMap[text] = PersonalDictEntry(
                        id = text,
                        text = text,
                        type = PersonalDictEntryType.WORD,
                        languageCode = "hi",
                        frequency = freq.coerceAtLeast(1)
                    )
                }
            }

            // 5. Load Avro phonetic mappings
            val avroSaved = prefs.getStringSet(KEY_PHONETIC_BN_V2, emptySet()) ?: emptySet()
            for (entry in avroSaved) {
                parseAvroEntry(entry)?.let { avroMap[it.phonetic.lowercase()] = it }
            }

            // 6. Migrate legacy data if present
            migrateLegacyData()
        }
    }

    private fun parseV3Entry(raw: String): PersonalDictEntry? {
        val parts = raw.split("\t")
        if (parts.size >= 4) {
            val text = parts[0].trim()
            val type = PersonalDictEntryType.fromId(parts[1])
            val lang = parts[2].trim()
            val freq = parts[3].toIntOrNull() ?: 1
            val timestamp = if (parts.size >= 5) parts[4].toLongOrNull() ?: System.currentTimeMillis() else System.currentTimeMillis()
            if (text.isNotEmpty()) {
                val id = if (lang == "bn" || lang == "hi") text else text.lowercase()
                return PersonalDictEntry(
                    id = id,
                    text = text,
                    type = type,
                    languageCode = lang,
                    frequency = freq.coerceAtLeast(1),
                    timestamp = timestamp
                )
            }
        }
        return null
    }

    private fun parseAvroEntry(raw: String): AvroEntry? {
        val colonIdx = raw.lastIndexOf(':')
        val mappingPart = if (colonIdx > 0) raw.substring(0, colonIdx) else raw
        val freq = if (colonIdx > 0) raw.substring(colonIdx + 1).toIntOrNull() ?: 1 else 1

        val eqIdx = mappingPart.indexOf('=')
        if (eqIdx > 0 && eqIdx < mappingPart.length - 1) {
            val p = mappingPart.substring(0, eqIdx).trim().lowercase()
            val b = mappingPart.substring(eqIdx + 1).trim()
            if (p.isNotEmpty() && b.isNotEmpty()) {
                return AvroEntry(p, b, freq.coerceAtLeast(1))
            }
        }
        return null
    }

    private fun migrateLegacyData() {
        var needsPersist = false
        val legacyWords = prefs.getStringSet(KEY_LEGACY_CUSTOM_WORDS, emptySet()) ?: emptySet()
        if (legacyWords.isNotEmpty()) {
            for (word in legacyWords) {
                val trimmed = word.trim()
                if (trimmed.length < 2) continue
                val key = if (trimmed.any { it in '\u0980'..'\u09FF' || it in '\u0900'..'\u097F' }) trimmed else trimmed.lowercase()
                if (!entriesMap.containsKey(key)) {
                    val lang = when {
                        trimmed.any { it in '\u0980'..'\u09FF' } -> "bn"
                        trimmed.any { it in '\u0900'..'\u097F' } -> "hi"
                        else -> "en"
                    }
                    entriesMap[key] = PersonalDictEntry(
                        id = key,
                        text = trimmed,
                        type = detectEntryType(trimmed),
                        languageCode = lang,
                        frequency = 1
                    )
                    needsPersist = true
                }
            }
        }

        val legacyPhonetic = prefs.getStringSet(KEY_LEGACY_PHONETIC, emptySet()) ?: emptySet()
        if (legacyPhonetic.isNotEmpty()) {
            for (entry in legacyPhonetic) {
                val eqIdx = entry.indexOf('=')
                if (eqIdx > 0 && eqIdx < entry.length - 1) {
                    val p = entry.substring(0, eqIdx).trim().lowercase()
                    val b = entry.substring(eqIdx + 1).trim()
                    if (p.isNotEmpty() && b.isNotEmpty() && !avroMap.containsKey(p)) {
                        avroMap[p] = AvroEntry(p, b, 1)
                        needsPersist = true
                    }
                }
            }
        }

        if (needsPersist) {
            persistAll()
        }
    }

    private fun persistAll() {
        val editor = prefs.edit()
        val v3Set = entriesMap.values.map {
            "${it.text}\t${it.type.id}\t${it.languageCode}\t${it.frequency}\t${it.timestamp}"
        }.toSet()
        editor.putStringSet(KEY_ENTRIES_V3, HashSet(v3Set))

        // Backward compatibility mirrors
        val enSet = entriesMap.values.filter { it.languageCode == "en" || it.languageCode == "all" }
            .map { "${it.text}:${it.frequency}" }.toSet()
        editor.putStringSet(KEY_WORDS_EN_V2, HashSet(enSet))

        val bnSet = entriesMap.values.filter { it.languageCode == "bn" }
            .map { "${it.text}:${it.frequency}" }.toSet()
        editor.putStringSet(KEY_WORDS_BN_V2, HashSet(bnSet))

        val hiSet = entriesMap.values.filter { it.languageCode == "hi" }
            .map { "${it.text}:${it.frequency}" }.toSet()
        editor.putStringSet(KEY_WORDS_HI_V2, HashSet(hiSet))

        editor.putStringSet(KEY_PHONETIC_BN_V2, HashSet(avroMap.values.map { "${it.phonetic}=${it.bangla}:${it.frequency}" }))
        editor.apply()
    }

    // =========================================================================
    // SMART PATTERN DETECTION & VALIDATION
    // =========================================================================

    /**
     * Automatically classifies the type of a custom text entry.
     */
    fun detectEntryType(rawText: String): PersonalDictEntryType = Companion.detectEntryType(rawText)

    /**
     * Validates whether a candidate string is safe and valid for automatic learning.
     * Allows words, alphanumeric (Arnix90), special combinations (Arnix@67), emails, URLs.
     * Strictly blocks pure numbers/OTPs/PINs, pure symbols, whitespaces, and passwords.
     */
    fun isEligibleForLearning(rawWord: String, languageCode: String): Boolean {
        val trimmed = rawWord.trim()
        if (trimmed.length !in 2..100) return false

        // 1. Must NOT contain whitespaces or line breaks
        if (trimmed.any { it.isWhitespace() }) return false

        // 2. Strict rejection of pure numbers / OTPs / PINs (e.g. 482910, 1234, ১২৩৪৫, १२३४५)
        // Must contain at least one letter or script character if digits are present
        val hasLetters = trimmed.any { it.isLetter() || it in '\u0980'..'\u09FF' || it in '\u0900'..'\u097F' }
        val allDigits = trimmed.all { it.isDigit() || it in '\u09E6'..'\u09EF' || it in '\u0966'..'\u096F' }
        if (allDigits) return false

        // 3. Strict rejection of pure symbols (no letters, no digits)
        val hasLetterOrDigit = trimmed.any {
            it.isLetterOrDigit() || it in '\u0980'..'\u09FF' || it in '\u0900'..'\u097F'
        }
        if (!hasLetterOrDigit) return false

        // 4. Language & Script checks for native Indic words
        val detected = detectEntryType(trimmed)
        if (detected == PersonalDictEntryType.WORD) {
            if (languageCode == "en") {
                val hasIndic = trimmed.any { it in '\u0980'..'\u09FF' || it in '\u0900'..'\u097F' }
                if (hasIndic) return false
            } else if (languageCode == "bn") {
                val hasBengali = trimmed.any { it in '\u0980'..'\u09FF' }
                val hasOther = trimmed.any { (it in 'a'..'z') || (it in 'A'..'Z') || (it in '\u0900'..'\u097F') }
                if (!hasBengali || hasOther) return false
            } else if (languageCode == "hi") {
                val hasHindi = trimmed.any { it in '\u0900'..'\u097F' }
                val hasOther = trimmed.any { (it in 'a'..'z') || (it in 'A'..'Z') || (it in '\u0980'..'\u09FF') }
                if (!hasHindi || hasOther) return false
            }
        }

        return true
    }

    // =========================================================================
    // AUTOMATIC LEARNING CORE
    // =========================================================================

    /**
     * Automatically learns a word, alphanumeric string, email, URL, or custom text from normal typing.
     * Preserves exact original text formatting and increments frequency.
     * Strictly blocked if isPrivateMode is true or validation fails.
     */
    fun learnWord(rawWord: String, languageCode: String, isPrivateMode: Boolean): Boolean {
        if (isPrivateMode) return false
        val clean = rawWord.trim()
        if (!isEligibleForLearning(clean, languageCode)) return false

        val detectedType = detectEntryType(clean)
        val normalizedId = if (languageCode == "bn" || languageCode == "hi") clean else clean.lowercase()

        synchronized(this) {
            val existing = entriesMap[normalizedId]
            if (existing != null) {
                existing.frequency = (existing.frequency + 1).coerceAtMost(9999)
            } else {
                entriesMap[normalizedId] = PersonalDictEntry(
                    id = normalizedId,
                    text = clean,
                    type = detectedType,
                    languageCode = languageCode,
                    frequency = 1
                )
            }
            persistAll()
        }

        EnglishWordPredictor.clearCache()
        revision.intValue++
        return true
    }

    /**
     * Automatically learns a Bangla Avro phonetic mapping (e.g. "arnix" -> "আর্নিক্স").
     * Phonetic key must be Latin letters, and resulting word must be Bengali script.
     */
    fun learnAvroPhonetic(rawPhonetic: String, rawBangla: String, isPrivateMode: Boolean): Boolean {
        if (isPrivateMode) return false
        val p = rawPhonetic.trim().lowercase()
        val b = rawBangla.trim()

        if (p.length < 2 || b.isEmpty()) return false
        if (!p.all { it in 'a'..'z' }) return false
        if (!isEligibleForLearning(b, "bn")) return false

        synchronized(this) {
            val existing = avroMap[p]
            if (existing != null && existing.bangla == b) {
                existing.frequency = (existing.frequency + 1).coerceAtMost(9999)
            } else {
                avroMap[p] = AvroEntry(p, b, 1)
            }

            // Also learn Bangla word in entriesMap
            val bnExisting = entriesMap[b]
            if (bnExisting != null) {
                bnExisting.frequency = (bnExisting.frequency + 1).coerceAtMost(9999)
            } else {
                entriesMap[b] = PersonalDictEntry(
                    id = b,
                    text = b,
                    type = PersonalDictEntryType.WORD,
                    languageCode = "bn",
                    frequency = 1
                )
            }
            persistAll()
        }

        EnglishWordPredictor.clearCache()
        revision.intValue++
        return true
    }

    fun addWord(word: String): Boolean {
        return learnWord(word, "en", isPrivateMode = false)
    }

    fun addAvroPhoneticWord(phonetic: String, bangla: String): Boolean {
        return learnAvroPhonetic(phonetic, bangla, isPrivateMode = false)
    }

    // =========================================================================
    // LOOKUP & CONTEXT-AWARE SUGGESTIONS (FREQUENCY RANKED)
    // =========================================================================

    /**
     * Finds context-aware suggestions based on prefix and active input type (Email, URL, Normal).
     * - Email fields: prioritizes saved email addresses.
     * - URL fields: prioritizes saved URLs.
     * - Normal fields: matches words, alphanumeric tokens (Arnix90), and custom entries (Arnix@67).
     * Sorted by:
     * 1. Exact match / exact prefix match
     * 2. Frequency descending
     * 3. Shorter length
     */
    fun getContextualSuggestions(
        prefix: String,
        isEmailField: Boolean = false,
        isUriField: Boolean = false,
        languageCode: String = "en",
        limit: Int = 6
    ): List<String> {
        val clean = prefix.trim()
        val lower = clean.lowercase()

        return synchronized(this) {
            val allList = entriesMap.values.toList()

            // If empty prefix in specialized fields, show top suggestions for that field
            if (clean.isEmpty()) {
                return@synchronized when {
                    isEmailField -> allList.filter { it.type == PersonalDictEntryType.EMAIL }
                        .sortedByDescending { it.frequency }.map { it.text }.take(limit)
                    isUriField -> allList.filter { it.type == PersonalDictEntryType.URL }
                        .sortedByDescending { it.frequency }.map { it.text }.take(limit)
                    else -> emptyList()
                }
            }

            allList
                .filter { entry ->
                    val textLower = entry.text.lowercase()
                    val langMatch = entry.languageCode == languageCode || entry.languageCode == "all" ||
                            (languageCode == "en" && (entry.type == PersonalDictEntryType.EMAIL || entry.type == PersonalDictEntryType.URL || entry.type == PersonalDictEntryType.ALPHANUMERIC || entry.type == PersonalDictEntryType.CUSTOM))
                    langMatch && (textLower.startsWith(lower) || (clean.length >= 2 && textLower.contains(lower)))
                }
                .sortedWith(
                    compareByDescending<PersonalDictEntry> {
                        // Priority 1: Match field context type
                        when {
                            isEmailField && it.type == PersonalDictEntryType.EMAIL -> 100
                            isUriField && it.type == PersonalDictEntryType.URL -> 100
                            else -> 0
                        }
                    }
                    .thenByDescending { it.text.lowercase().startsWith(lower) } // Exact prefix first
                    .thenByDescending { it.frequency }                           // Higher frequency first
                    .thenBy { it.text.length }                                   // Shorter text first
                )
                .map { it.text }
                .distinct()
                .take(limit)
        }
    }

    fun containsWord(word: String, languageCode: String? = null): Boolean {
        val trimmed = word.trim()
        if (trimmed.isEmpty()) return false
        val key = trimmed.lowercase()
        synchronized(this) {
            val entry = entriesMap[key] ?: entriesMap[trimmed]
            if (entry != null) {
                if (languageCode == null || entry.languageCode == languageCode || entry.languageCode == "all") {
                    return true
                }
            }
            return false
        }
    }

    fun containsPhoneticMapping(phonetic: String): Boolean {
        val p = phonetic.trim().lowercase()
        if (p.isEmpty()) return false
        synchronized(this) {
            return avroMap.containsKey(p)
        }
    }

    fun findEnglishMatches(prefix: String, limit: Int = 3): List<String> {
        return getContextualSuggestions(prefix, isEmailField = false, isUriField = false, languageCode = "en", limit = limit)
    }

    fun findBanglaMatches(prefix: String, limit: Int = 5): List<String> {
        val clean = prefix.trim()
        if (clean.length < 2) return emptyList()

        return synchronized(this) {
            entriesMap.values
                .filter { it.languageCode == "bn" && it.text.startsWith(clean) }
                .sortedWith(compareByDescending<PersonalDictEntry> { it.frequency }
                    .thenBy { it.text.length }
                )
                .map { it.text }
                .take(limit)
        }
    }

    fun findHindiMatches(prefix: String, limit: Int = 5): List<String> {
        val clean = prefix.trim()
        if (clean.length < 2) return emptyList()

        return synchronized(this) {
            entriesMap.values
                .filter { it.languageCode == "hi" && it.text.startsWith(clean) }
                .sortedWith(compareByDescending<PersonalDictEntry> { it.frequency }
                    .thenBy { it.text.length }
                )
                .map { it.text }
                .take(limit)
        }
    }

    fun findAvroMatches(phoneticPrefix: String, limit: Int = 3): List<String> {
        val clean = phoneticPrefix.trim().lowercase()
        if (clean.length < 2) return emptyList()

        val results = mutableListOf<String>()

        synchronized(this) {
            // Direct phonetic map matches
            val directMatches = avroMap.values
                .filter { it.phonetic.startsWith(clean) }
                .sortedWith(compareByDescending<AvroEntry> { it.phonetic == clean }
                    .thenByDescending { it.frequency }
                )
                .map { it.bangla }
            results.addAll(directMatches)

            // Bangla vocabulary matching parsed Bangla prefix
            val parsed = try {
                AvroPhoneticParser.parse(clean)
            } catch (_: Exception) { "" }

            if (parsed.length >= 2) {
                val bnVocabMatches = entriesMap.values
                    .filter { it.languageCode == "bn" && it.text.startsWith(parsed) }
                    .sortedByDescending { it.frequency }
                    .map { it.text }
                results.addAll(bnVocabMatches)
            }
        }

        return results.distinct().take(limit)
    }

    fun findMatches(prefix: String, limit: Int = 3): List<String> {
        return findEnglishMatches(prefix, limit)
    }

    // =========================================================================
    // FULL CRUD & DICTIONARY MANAGEMENT FOR MAIN APP
    // =========================================================================

    /**
     * Returns all saved entries sorted by frequency descending.
     */
    fun getAllEntries(): List<PersonalDictEntry> = synchronized(this) {
        entriesMap.values.sortedWith(
            compareByDescending<PersonalDictEntry> { it.frequency }
                .thenByDescending { it.timestamp }
        )
    }

    /**
     * Filters entries by search query and optional type filter.
     */
    fun getEntriesFiltered(
        query: String = "",
        typeFilter: PersonalDictEntryType? = null
    ): List<PersonalDictEntry> = synchronized(this) {
        val lowerQuery = query.trim().lowercase()
        entriesMap.values
            .filter { entry ->
                val matchesQuery = lowerQuery.isEmpty() || entry.text.lowercase().contains(lowerQuery)
                val matchesType = typeFilter == null || entry.type == typeFilter
                matchesQuery && matchesType
            }
            .sortedWith(
                compareByDescending<PersonalDictEntry> { it.frequency }
                    .thenByDescending { it.timestamp }
            )
    }

    fun getEntriesFiltered(
        typeFilter: PersonalDictEntryType?,
        query: String = ""
    ): List<PersonalDictEntry> = getEntriesFiltered(query, typeFilter)

    /**
     * Manually adds a new custom entry to the dictionary.
     */
    fun addCustomEntry(
        text: String,
        type: PersonalDictEntryType? = null,
        languageCode: String = "all"
    ): Boolean {
        val clean = text.trim()
        if (clean.length !in 2..100) return false
        if (clean.any { it.isWhitespace() }) return false

        val detectedType = type ?: detectEntryType(clean)
        val id = if (languageCode == "bn" || languageCode == "hi") clean else clean.lowercase()

        synchronized(this) {
            val existing = entriesMap[id]
            if (existing != null) {
                existing.frequency = (existing.frequency + 1).coerceAtMost(9999)
            } else {
                entriesMap[id] = PersonalDictEntry(
                    id = id,
                    text = clean,
                    type = detectedType,
                    languageCode = languageCode,
                    frequency = 1
                )
            }
            persistAll()
        }

        EnglishWordPredictor.clearCache()
        revision.intValue++
        return true
    }

    /**
     * Updates an existing entry's text and/or type.
     */
    fun updateEntry(
        originalText: String,
        newText: String,
        newType: PersonalDictEntryType? = null
    ): Boolean {
        val cleanOriginal = originalText.trim()
        val cleanNew = newText.trim()
        if (cleanNew.length !in 2..100) return false
        if (cleanNew.any { it.isWhitespace() }) return false

        val origKey = cleanOriginal.lowercase()
        val newKey = cleanNew.lowercase()

        synchronized(this) {
            val existing = entriesMap[origKey] ?: entriesMap[cleanOriginal] ?: return false
            val updatedType = newType ?: detectEntryType(cleanNew)

            entriesMap.remove(origKey)
            entriesMap.remove(cleanOriginal)

            entriesMap[newKey] = PersonalDictEntry(
                id = newKey,
                text = cleanNew,
                type = updatedType,
                languageCode = existing.languageCode,
                frequency = existing.frequency,
                timestamp = System.currentTimeMillis()
            )
            persistAll()
        }

        EnglishWordPredictor.clearCache()
        revision.intValue++
        return true
    }

    /**
     * Deletes a single entry by text or normalized key.
     */
    fun deleteEntry(text: String): Boolean {
        val clean = text.trim()
        val key = clean.lowercase()
        var removed = false

        synchronized(this) {
            if (entriesMap.remove(key) != null || entriesMap.remove(clean) != null) {
                removed = true
            }
            // Clean up any matching Avro mappings
            val avroKeys = avroMap.filter { it.key == key || it.value.bangla == clean }.keys.toList()
            if (avroKeys.isNotEmpty()) {
                avroKeys.forEach { avroMap.remove(it) }
                removed = true
            }
            if (removed) {
                persistAll()
            }
        }

        if (removed) {
            EnglishWordPredictor.clearCache()
            revision.intValue++
        }
        return removed
    }

    /**
     * Bulk deletes multiple entries.
     */
    fun deleteEntries(texts: Collection<String>): Int {
        var count = 0
        synchronized(this) {
            for (text in texts) {
                val clean = text.trim()
                val key = clean.lowercase()
                if (entriesMap.remove(key) != null || entriesMap.remove(clean) != null) {
                    count++
                }
                val avroKeys = avroMap.filter { it.key == key || it.value.bangla == clean }.keys.toList()
                if (avroKeys.isNotEmpty()) {
                    avroKeys.forEach { avroMap.remove(it) }
                    count++
                }
            }
            if (count > 0) {
                persistAll()
            }
        }

        if (count > 0) {
            EnglishWordPredictor.clearCache()
            revision.intValue++
        }
        return count
    }

    fun getTotalEntryCount(): Int = synchronized(this) {
        entriesMap.size
    }

    fun getWordCount(languageCode: String): Int = synchronized(this) {
        when (languageCode) {
            "en" -> entriesMap.values.count { it.languageCode == "en" || it.languageCode == "all" }
            "bn" -> entriesMap.values.count { it.languageCode == "bn" }
            "hi" -> entriesMap.values.count { it.languageCode == "hi" }
            "bn_avro" -> avroMap.size
            else -> entriesMap.size
        }
    }

    fun getAllWords(): List<String> = synchronized(this) {
        entriesMap.values.map { it.text }.sorted()
    }

    fun getAllWords(languageCode: String): List<String> = synchronized(this) {
        entriesMap.values
            .filter { it.languageCode == languageCode || it.languageCode == "all" }
            .sortedByDescending { it.frequency }
            .map { it.text }
    }

    fun getAllPhoneticMappings(): Map<String, String> = synchronized(this) {
        avroMap.mapValues { it.value.bangla }
    }

    fun removeWord(word: String, languageCode: String? = null): Boolean {
        return deleteEntry(word)
    }

    fun clearLanguage(languageCode: String) {
        synchronized(this) {
            when (languageCode) {
                "en" -> entriesMap.entries.removeAll { it.value.languageCode == "en" }
                "bn" -> {
                    entriesMap.entries.removeAll { it.value.languageCode == "bn" }
                    avroMap.clear()
                }
                "hi" -> entriesMap.entries.removeAll { it.value.languageCode == "hi" }
            }
            persistAll()
        }
        EnglishWordPredictor.clearCache()
        revision.intValue++
    }

    fun clearAll() {
        synchronized(this) {
            entriesMap.clear()
            avroMap.clear()
            prefs.edit().clear().apply()
        }
        EnglishWordPredictor.clearCache()
        revision.intValue++
    }
}
