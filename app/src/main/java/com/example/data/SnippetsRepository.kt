package com.example.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class CodeSnippet(
    val id: String,
    val title: String,
    val code: String,
    val category: String, // "Shortcuts", "Coding", "Gaming", "General"
    val isCustom: Boolean = false,
    val shortcut: String = "" // e.g. "!email", "!addr", "!upi", "!meet", "!omw"
)

class SnippetsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("keynova_snippets", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CUSTOM_SNIPPETS = "custom_snippets_json"
    }

    private val defaultSnippets = listOf(
        // Super Shortcuts / Text Expander
        CodeSnippet("s1", "My Email", "myemail@example.com", "Shortcuts", shortcut = "!email"),
        CodeSnippet("s2", "Home Address", "123 Main Street, Apt 4B, City", "Shortcuts", shortcut = "!addr"),
        CodeSnippet("s3", "UPI ID", "username@okhdfcbank", "Shortcuts", shortcut = "!upi"),
        CodeSnippet("s4", "Google Meet Link", "https://meet.google.com/new", "Shortcuts", shortcut = "!meet"),
        CodeSnippet("s5", "On My Way", "On my way! Will be there in 10 minutes.", "Shortcuts", shortcut = "!omw"),
        CodeSnippet("s6", "Be Right Back", "Be right back in a few minutes!", "Shortcuts", shortcut = "!brb"),
        CodeSnippet("s7", "Phone Number", "+91 98765 43210", "Shortcuts", shortcut = "!ph"),
        CodeSnippet("s8", "Shrug Kaomoji", "¯\\_(ツ)_/¯", "Shortcuts", shortcut = "!shrug"),
        CodeSnippet("s9", "Thank You Note", "Thank you so much! Really appreciate your help 🙏", "Shortcuts", shortcut = "!ty"),

        // Coding presets
        CodeSnippet("c1", "Console Log", "console.log()", "Coding", shortcut = "!log"),
        CodeSnippet("c2", "Main Function", "fun main() {\n    \n}", "Coding", shortcut = "!main"),
        CodeSnippet("c3", "If Statement", "if () {\n    \n}", "Coding", shortcut = "!if"),
        CodeSnippet("c4", "Arrow Function", "() => {\n    \n}", "Coding", shortcut = "!fn"),
        CodeSnippet("c5", "Try-Catch", "try {\n    \n} catch (e) {\n    \n}", "Coding", shortcut = "!try"),
        CodeSnippet("c6", "HTML Div", "<div class=\"\">\n    \n</div>", "Coding", shortcut = "!div"),
        CodeSnippet("c7", "JSON Stringify", "JSON.stringify()", "Coding", shortcut = "!json"),
        CodeSnippet("c8", "Return Statement", "return;", "Coding", shortcut = "!ret"),
        CodeSnippet("c9", "Git Commit", "git commit -m \"\"", "Coding", shortcut = "!gc"),
        CodeSnippet("c10", "Python Def", "def main():\n    pass", "Coding", shortcut = "!py"),

        // Gaming presets
        CodeSnippet("g1", "Good Game", "GG", "Gaming", shortcut = "!gg"),
        CodeSnippet("g2", "Good Luck", "glhf!", "Gaming", shortcut = "!gl"),
        CodeSnippet("g3", "Need Help", "Need backup at my position!", "Gaming", shortcut = "!help"),
        CodeSnippet("g4", "AFK", "AFK for 2 mins", "Gaming", shortcut = "!afk"),
        CodeSnippet("g5", "Well Played", "Well played everyone!", "Gaming", shortcut = "!wp"),
        CodeSnippet("g6", "Rush Point", "Rush point now!", "Gaming", shortcut = "!rush"),
        CodeSnippet("g7", "Enemy Spotted", "Enemy spotted ahead!", "Gaming", shortcut = "!enemy"),
        CodeSnippet("g8", "On My Way", "On my way! Hold on.", "Gaming", shortcut = "!hold")
    )

    fun getAllSnippets(): List<CodeSnippet> {
        val custom = getCustomSnippets()
        return defaultSnippets + custom
    }

    fun getSnippetsByCategory(category: String): List<CodeSnippet> {
        return getAllSnippets().filter { it.category.equals(category, ignoreCase = true) }
    }

    /**
     * Finds snippets matching a shortcut query (e.g. user typed "!em", "!addr", or "addr")
     */
    fun getShortcutMatches(query: String): List<CodeSnippet> {
        if (query.isBlank()) return emptyList()
        val raw = query.trim().lowercase()
        val clean = raw.removePrefix("!")
        if (clean.length < 2 && !raw.startsWith("!")) return emptyList()
        return getAllSnippets().filter { snippet ->
            val scRaw = snippet.shortcut.lowercase()
            val scClean = scRaw.removePrefix("!")
            scClean.isNotBlank() && (
                scRaw.startsWith(raw) ||
                scClean.startsWith(clean) ||
                (clean.length >= 3 && scClean == clean)
            )
        }
    }

    fun addCustomSnippet(title: String, code: String, category: String = "Shortcuts", shortcut: String = ""): Boolean {
        if (title.isBlank() || code.isBlank()) return false
        val current = getCustomSnippets().toMutableList()
        val cleanShortcut = if (shortcut.isNotBlank() && !shortcut.startsWith("!")) "!$shortcut" else shortcut.trim()
        val newSnippet = CodeSnippet(
            id = "custom_" + System.currentTimeMillis(),
            title = title.trim(),
            code = code,
            category = category,
            isCustom = true,
            shortcut = cleanShortcut
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
                        category = obj.optString("category", "Shortcuts"),
                        isCustom = true,
                        shortcut = obj.optString("shortcut", "")
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
            obj.put("shortcut", s.shortcut)
            array.put(obj)
        }
        prefs.edit().putString(KEY_CUSTOM_SNIPPETS, array.toString()).apply()
    }
}
