package com.example.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class CodeSnippet(
    val id: String,
    val title: String,
    val code: String,
    val category: String, // "Coding", "Gaming", "General"
    val isCustom: Boolean = false
)

class SnippetsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("keynova_snippets", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CUSTOM_SNIPPETS = "custom_snippets_json"
    }

    private val defaultSnippets = listOf(
        CodeSnippet("c1", "Console Log", "console.log()", "Coding"),
        CodeSnippet("c2", "Main Function", "fun main() {\n    \n}", "Coding"),
        CodeSnippet("c3", "If Statement", "if () {\n    \n}", "Coding"),
        CodeSnippet("c4", "Arrow Function", "() => {\n    \n}", "Coding"),
        CodeSnippet("c5", "Try-Catch", "try {\n    \n} catch (e) {\n    \n}", "Coding"),
        CodeSnippet("c6", "HTML Div", "<div class=\"\">\n    \n</div>", "Coding"),
        CodeSnippet("c7", "JSON Stringify", "JSON.stringify()", "Coding"),
        CodeSnippet("c8", "Return Statement", "return;", "Coding"),
        CodeSnippet("c9", "Git Commit", "git commit -m \"\"", "Coding"),
        CodeSnippet("c10", "Python Def", "def main():\n    pass", "Coding"),

        // Gaming presets
        CodeSnippet("g1", "Good Game", "GG", "Gaming"),
        CodeSnippet("g2", "Good Luck", "glhf!", "Gaming"),
        CodeSnippet("g3", "Need Help", "Need backup at my position!", "Gaming"),
        CodeSnippet("g4", "AFK", "AFK for 2 mins", "Gaming"),
        CodeSnippet("g5", "Well Played", "Well played everyone!", "Gaming"),
        CodeSnippet("g6", "Rush Point", "Rush point now!", "Gaming"),
        CodeSnippet("g7", "Enemy Spotted", "Enemy spotted ahead!", "Gaming"),
        CodeSnippet("g8", "On My Way", "On my way! Hold on.", "Gaming")
    )

    fun getAllSnippets(): List<CodeSnippet> {
        val custom = getCustomSnippets()
        return defaultSnippets + custom
    }

    fun getSnippetsByCategory(category: String): List<CodeSnippet> {
        return getAllSnippets().filter { it.category.equals(category, ignoreCase = true) }
    }

    fun addCustomSnippet(title: String, code: String, category: String = "Coding"): Boolean {
        if (title.isBlank() || code.isBlank()) return false
        val current = getCustomSnippets().toMutableList()
        val newSnippet = CodeSnippet(
            id = "custom_" + System.currentTimeMillis(),
            title = title.trim(),
            code = code,
            category = category,
            isCustom = true
        )
        current.add(0, newSnippet)
        saveCustomSnippets(current)
        return true
    }

    fun deleteCustomSnippet(id: String) {
        val current = getCustomSnippets().filterNot { it.id == id }
        saveCustomSnippets(current)
    }

    private fun getCustomSnippets(): List<CodeSnippet> {
        val raw = prefs.getString(KEY_CUSTOM_SNIPPETS, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<CodeSnippet>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CodeSnippet(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        code = obj.getString("code"),
                        category = obj.optString("category", "Coding"),
                        isCustom = true
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveCustomSnippets(list: List<CodeSnippet>) {
        val array = JSONArray()
        list.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("title", s.title)
            obj.put("code", s.code)
            obj.put("category", s.category)
            array.put(obj)
        }
        prefs.edit().putString(KEY_CUSTOM_SNIPPETS, array.toString()).apply()
    }
}
