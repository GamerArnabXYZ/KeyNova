package com.example.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableIntStateOf
import com.example.ime.EnglishWordPredictor

/**
 * Manages user's custom dictionary words for smart prediction & auto-correction.
 * Supports English custom words and Bangla Avro phonetic custom words.
 * Device-local, secure, persistent, and fast.
 */
class PersonalDictionaryManager internal constructor(
    private val prefs: SharedPreferences
) {
    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences("keynova_personal_dict", Context.MODE_PRIVATE)
    )

    companion object {
        private const val KEY_CUSTOM_WORDS = "key_custom_words"
        private const val KEY_PHONETIC_MAPPINGS = "key_phonetic_mappings"

        @Volatile
        private var instance: PersonalDictionaryManager? = null

        fun getInstance(context: Context): PersonalDictionaryManager {
            return instance ?: synchronized(this) {
                instance ?: PersonalDictionaryManager(context).also { instance = it }
            }
        }
    }

    // Reactive revision state so Jetpack Compose UI immediately recomposes on dictionary updates
    val revision = mutableIntStateOf(0)

    private val wordsSet: MutableSet<String> = run {
        val saved = prefs.getStringSet(KEY_CUSTOM_WORDS, emptySet()) ?: emptySet()
        saved.toMutableSet()
    }

    // Stores phonetic Latin to Bengali mappings, e.g. "shuvokamona" -> "শুভকামনা"
    private val phoneticMap: MutableMap<String, String> = run {
        val saved = prefs.getStringSet(KEY_PHONETIC_MAPPINGS, emptySet()) ?: emptySet()
        val map = mutableMapOf<String, String>()
        for (entry in saved) {
            val idx = entry.indexOf('=')
            if (idx > 0 && idx < entry.length - 1) {
                val key = entry.substring(0, idx).trim().lowercase()
                val value = entry.substring(idx + 1).trim()
                if (key.isNotEmpty() && value.isNotEmpty()) {
                    map[key] = value
                }
            }
        }
        map
    }

    /**
     * Checks if a word is already in the personal dictionary (case-insensitive).
     */
    fun containsWord(word: String): Boolean {
        val trimmed = word.trim()
        if (trimmed.isEmpty()) return false
        return synchronized(wordsSet) {
            wordsSet.any { it.equals(trimmed, ignoreCase = true) }
        }
    }

    /**
     * Checks if a phonetic key is already mapped for Bangla Avro.
     */
    fun containsPhoneticMapping(phonetic: String): Boolean {
        val lower = phonetic.trim().lowercase()
        if (lower.isEmpty()) return false
        return synchronized(phoneticMap) {
            phoneticMap.containsKey(lower)
        }
    }

    /**
     * Adds an English (or general) custom word to the dictionary.
     * Prevents duplicates (case-insensitive).
     * Persists a new HashSet copy to SharedPreferences to prevent Android reference equality bug.
     */
    fun addWord(word: String): Boolean {
        val trimmed = word.trim()
        if (trimmed.length < 2) return false

        val added = synchronized(wordsSet) {
            if (wordsSet.any { it.equals(trimmed, ignoreCase = true) }) {
                return false
            }
            wordsSet.add(trimmed)
            // CRITICAL: Always create a fresh HashSet copy so Android SharedPreferences detects
            // the new set reference and reliably writes to disk across process restarts!
            prefs.edit().putStringSet(KEY_CUSTOM_WORDS, HashSet(wordsSet)).apply()
            true
        }

        if (added) {
            EnglishWordPredictor.clearCache()
            revision.intValue++
        }
        return added
    }

    /**
     * Adds a Bangla Avro phonetic mapping, e.g. "arnix" -> "আর্নিক্স".
     * Persists both the phonetic mapping and the Bengali word to disk.
     */
    fun addAvroPhoneticWord(phonetic: String, bangla: String): Boolean {
        val pTrim = phonetic.trim().lowercase()
        val bTrim = bangla.trim()
        if (pTrim.length < 2 || bTrim.isEmpty()) return false

        synchronized(phoneticMap) {
            phoneticMap[pTrim] = bTrim
            val set = phoneticMap.entries.map { "${it.key}=${it.value}" }.toSet()
            prefs.edit().putStringSet(KEY_PHONETIC_MAPPINGS, HashSet(set)).apply()
        }

        synchronized(wordsSet) {
            if (!wordsSet.any { it.equals(bTrim, ignoreCase = true) }) {
                wordsSet.add(bTrim)
                prefs.edit().putStringSet(KEY_CUSTOM_WORDS, HashSet(wordsSet)).apply()
            }
        }

        EnglishWordPredictor.clearCache()
        revision.intValue++
        return true
    }

    /**
     * Removes a word from the dictionary and cleans up any related phonetic mappings.
     */
    fun removeWord(word: String): Boolean {
        val trimmed = word.trim()
        var removed = false
        synchronized(wordsSet) {
            val toRemove = wordsSet.filter { it.equals(trimmed, ignoreCase = true) }
            if (toRemove.isNotEmpty()) {
                wordsSet.removeAll(toRemove.toSet())
                prefs.edit().putStringSet(KEY_CUSTOM_WORDS, HashSet(wordsSet)).apply()
                removed = true
            }
        }
        synchronized(phoneticMap) {
            val keysToRemove = phoneticMap.filter { it.key.equals(trimmed, ignoreCase = true) || it.value.equals(trimmed, ignoreCase = true) }.keys
            if (keysToRemove.isNotEmpty()) {
                keysToRemove.forEach { phoneticMap.remove(it) }
                val set = phoneticMap.entries.map { "${it.key}=${it.value}" }.toSet()
                prefs.edit().putStringSet(KEY_PHONETIC_MAPPINGS, HashSet(set)).apply()
                removed = true
            }
        }
        if (removed) {
            EnglishWordPredictor.clearCache()
            revision.intValue++
        }
        return removed
    }

    fun getAllWords(): List<String> = synchronized(wordsSet) {
        wordsSet.toList().sorted()
    }

    fun getAllPhoneticMappings(): Map<String, String> = synchronized(phoneticMap) {
        phoneticMap.toMap()
    }

    /**
     * Finds matches in the custom dictionary for a given prefix.
     * Sorts exact prefix matches first, then shorter words.
     */
    fun findMatches(prefix: String, limit: Int = 3): List<String> {
        val trimmed = prefix.trim()
        if (trimmed.length < 2) return emptyList()
        val lower = trimmed.lowercase()

        return synchronized(wordsSet) {
            wordsSet.filter { it.lowercase().startsWith(lower) }
                .sortedWith(compareBy(
                    { !it.equals(trimmed, ignoreCase = true) }, // exact match first
                    { it.length }                               // then shorter words
                ))
                .take(limit)
        }
    }

    /**
     * Finds Bangla candidates for Avro phonetic input.
     * Matches against saved phonetic keys (e.g. "arn" -> "আর্নিক্স") and saved Bangla words.
     */
    fun findAvroMatches(phoneticPrefix: String, limit: Int = 3): List<String> {
        val trimmed = phoneticPrefix.trim().lowercase()
        if (trimmed.length < 2) return emptyList()

        val results = mutableListOf<String>()

        // 1. Direct phonetic map matches
        synchronized(phoneticMap) {
            // Exact phonetic match first
            phoneticMap[trimmed]?.let { results.add(it) }
            // Prefix phonetic matches
            phoneticMap.filter { it.key.startsWith(trimmed) && it.key != trimmed }
                .values.forEach { results.add(it) }
        }

        // 2. Bangla words in wordsSet that match parsed Bangla prefix
        val parsed = try {
            com.example.ime.AvroPhoneticParser.parse(trimmed)
        } catch (_: Exception) { "" }

        if (parsed.length >= 2) {
            synchronized(wordsSet) {
                wordsSet.filter { it.startsWith(parsed) }
                    .forEach { results.add(it) }
            }
        }

        return results.distinct().take(limit)
    }

    fun clearAll() {
        synchronized(wordsSet) { wordsSet.clear() }
        synchronized(phoneticMap) { phoneticMap.clear() }
        prefs.edit().clear().apply()
        EnglishWordPredictor.clearCache()
        revision.intValue++
    }
}
