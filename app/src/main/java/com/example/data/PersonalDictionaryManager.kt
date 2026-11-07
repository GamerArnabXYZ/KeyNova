package com.example.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages user's custom dictionary words for smart prediction & auto-correction.
 * Device-local, secure, and fast.
 */
class PersonalDictionaryManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("keynova_personal_dict", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CUSTOM_WORDS = "key_custom_words"
        @Volatile
        private var instance: PersonalDictionaryManager? = null

        fun getInstance(context: Context): PersonalDictionaryManager {
            return instance ?: synchronized(this) {
                instance ?: PersonalDictionaryManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val wordsSet: MutableSet<String> = run {
        val saved = prefs.getStringSet(KEY_CUSTOM_WORDS, emptySet()) ?: emptySet()
        saved.toMutableSet()
    }

    fun getAllWords(): List<String> = synchronized(wordsSet) {
        wordsSet.toList().sorted()
    }

    fun addWord(word: String) {
        val trimmed = word.trim()
        if (trimmed.length < 2) return
        synchronized(wordsSet) {
            wordsSet.add(trimmed)
            prefs.edit().putStringSet(KEY_CUSTOM_WORDS, wordsSet).apply()
        }
    }

    fun removeWord(word: String) {
        synchronized(wordsSet) {
            if (wordsSet.remove(word)) {
                prefs.edit().putStringSet(KEY_CUSTOM_WORDS, wordsSet).apply()
            }
        }
    }

    fun findMatches(prefix: String, limit: Int = 3): List<String> {
        val trimmed = prefix.trim()
        if (trimmed.length < 2) return emptyList()
        val lower = trimmed.lowercase()

        return synchronized(wordsSet) {
            wordsSet.filter { it.lowercase().startsWith(lower) && !it.equals(trimmed, ignoreCase = true) }
                .take(limit)
        }
    }
}
